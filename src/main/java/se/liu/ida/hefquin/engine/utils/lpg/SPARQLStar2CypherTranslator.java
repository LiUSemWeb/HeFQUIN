package se.liu.ida.hefquin.engine.utils.lpg;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.CypherQuery;
import se.liu.ida.hefquin.engine.query.cypher.*;
import se.liu.ida.hefquin.engine.query.utils.CypherQueryBuilder;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.query.impl.UnionCypherQuery;
import se.liu.ida.hefquin.engine.query.utils.MatchVariableGetter;

import java.util.*;

public class SPARQLStar2CypherTranslator {

    public static CypherQuery translate(final BGP bgp ) {
        return translate(bgp, new DefaultConfiguration(), new CypherVarGenerator());
    }

    public static CypherQuery translate( final BGP bgp, final Configuration configuration) {
        return translate(bgp, configuration, new CypherVarGenerator());
    }

    public static CypherQuery translate(final BGP bgp, final Configuration configuration,
                                        final CypherVarGenerator gen) {
        final Set<? extends TriplePattern> triples = bgp.getTriplePatterns();
        if (triples.size() == 1) {
            return translateTriple((TriplePattern) triples.toArray()[0], configuration, gen, new HashSet<>());
        }
        return translateBGP(bgp, configuration, gen);
    }

    private static CypherQuery translateBGP(final BGP bgp, final Configuration configuration,
                                            final CypherVarGenerator gen) {
        CypherQuery result = null;
        final Set<Node> certainNodes = new HashSet<>();
        findCertainNodes(bgp.getTriplePatterns(), certainNodes, configuration);
        for (final TriplePattern tp : bgp.getTriplePatterns()){
            final CypherQuery translation = translateTriple(tp, configuration, gen, certainNodes);
            if (result == null)
                result = translation;
            else
                result = result.combineWith(translation);
        }
        return result;
    }

    private static void findCertainNodes(final Set<? extends TriplePattern> triplePatterns,
                                           final Set<Node> certainNodes, final Configuration configuration) {
        for (final TriplePattern tp : triplePatterns) {
            final Triple b = tp.asJenaTriple();
            final Node s = b.getSubject();
            final Node p = b.getPredicate();
            final Node o = b.getObject();
            if (s.isVariable()) {
                if (configuration.isLabelIRI(p) || configuration.mapsToLabel(o) || configuration.mapsToRelationship(p)
                        || configuration.mapsToNode(o))
                    certainNodes.add(s);
            }
            if (o.isVariable() && configuration.mapsToRelationship(p)) certainNodes.add(o);
        }
    }

    private static CypherQuery translateTriple(final TriplePattern pattern, final Configuration configuration,
                                               final CypherVarGenerator gen, final Set<Node> certainNodes) {
        final Triple b = pattern.asJenaTriple();
        final Node s = b.getSubject();
        final Node p = b.getPredicate();
        final Node o = b.getObject();
        if (s.isNodeTriple()) {
            final TriplePattern tp = new TriplePatternImpl(s.getTriple());
            final CypherQuery translation = translateTriple(tp, configuration, gen, certainNodes);
            final CypherVar evar = MatchVariableGetter.getEdgeVariable(translation);
            if (configuration.mapsToProperty(p) && o.isLiteral()) {
                translation.addConditionConjunction(
                        new ValueWhereCondition(evar, configuration.unmapProperty(p), o.getLiteralValue().toString()));
            } else if (configuration.mapsToProperty(p) && o.isVariable()) {
                translation.addConditionConjunction(
                        new EXISTSWhereCondition(evar, configuration.unmapProperty(p)));
                translation.addReturnClause(
                        new LiteralReturnStatement(evar, configuration.unmapProperty(p), o.getName()));
            } else if (p.isVariable() && o.isVariable()) {
                translation.addReturnClause(
                        new PropertyListReturnStatement(evar, p.getName()));
                translation.addReturnClause(
                        new PropertyValuesReturnStatement(evar, o.getName()));
            } else {
                throw new IllegalArgumentException("If the subject is a triple, the predicate and object can only" +
                        " match with properties");
            }
            return translation;
        }
        if (pattern.numberOfVars() == 0) {
            //These translations should only be used for nested triples.
            if (configuration.mapsToNode(s)) {
                if (configuration.mapsToProperty(p) && o.isLiteral()) {
                    return Translations.getNodePropertyLiteral(s, p, o, configuration, gen);
                } else if (configuration.isLabelIRI(p) && configuration.mapsToLabel(o)) {
                    return Translations.getNodeLabelLabel(s, p, o, configuration, gen);
                } else if (configuration.mapsToRelationship(p) && configuration.mapsToNode(o)) {
                    return Translations.getNodeRelationshipNode(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Predicate must be a property, relationship or the label IRI. " +
                            "Object must be a literal, label or node respectively.");
                }
            }
            else {
                throw new IllegalArgumentException("IRIs in the subject position must map to a node");
            }
        }
        if (pattern.numberOfVars() == 1){
            if (s.isVariable()) {
                if (configuration.mapsToProperty(p) && o.isLiteral()) {
                    return Translations.getVarPropertyLiteral(s, p, o, configuration, gen, certainNodes);
                } else if (configuration.isLabelIRI(p) && configuration.mapsToLabel(o)) {
                    return Translations.getVarLabelClass(s, p, o, configuration, gen);
                } else if (configuration.mapsToRelationship(p) && configuration.mapsToNode(o)){
                    return Translations.getVarRelationshipNode(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Illegal values for predicate and object");
                }
            }
            else if (o.isVariable()) {
                if (configuration.isLabelIRI(p) && configuration.mapsToNode(s)) {
                    return Translations.getNodeLabelVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToProperty(p) && configuration.mapsToNode(s)) {
                    return Translations.getNodePropertyVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToRelationship(p) && configuration.mapsToNode(s)) {
                    return Translations.getNodeRelationshipVar(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Illegal values for subject and predicate");
                }
            }
            else if (p.isVariable()) {
                if (configuration.mapsToNode(s) && configuration.mapsToNode(o)) {
                    return Translations.getNodeVarNode(s, p, o, configuration, gen);
                } else if (configuration.mapsToNode(s) && o.isLiteral()) {
                    return Translations.getNodeVarLiteral(s, p, o, configuration, gen);
                } else if (configuration.mapsToNode(s) && configuration.mapsToLabel(o)) {
                    return Translations.getNodeVarLabel(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Illegal values for subject and object");
                }
            }
        } else if (pattern.numberOfVars() == 2) {
            if (s.isVariable() && o.isVariable()) {
                if (configuration.isLabelIRI(p)) {
                    return Translations.getVarLabelVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToRelationship(p)) {
                    return Translations.getVarRelationshipVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToProperty(p)) {
                    return Translations.getVarPropertyVar(s, p, o, configuration, gen, certainNodes);
                } else {
                    throw new IllegalArgumentException("Predicate must be a mapping of a property or a relationship " +
                            "or the label URI");
                }
            } else if (s.isVariable() && p.isVariable()) {
                if (configuration.mapsToLabel(o)) {
                    return Translations.getVarVarLabel(s, p, o, configuration, gen);
                } else if (configuration.mapsToNode(o)) {
                    return Translations.getVarVarNode(s, p, o, configuration, gen);
                } else if (o.isLiteral()) {
                    return Translations.getVarVarLiteral(s, p, o, configuration, gen, certainNodes);
                }
            } else if(p.isVariable() && o.isVariable()) {
                if (configuration.mapsToNode(s)) {
                    return Translations.getNodeVarVar(s, p, o, configuration, gen, certainNodes);
                }
            }
        } else {
            return Translations.getVarVarVar(s, p, o, configuration, gen, certainNodes);
        }
        throw new IllegalArgumentException("Malformed query pattern");
    }

    private static class Translations {

        public static CypherQuery getVarPropertyLiteral(final Node s, final Node p, final Node o,
                                                        final Configuration configuration, final CypherVarGenerator gen,
                                                        final Set<Node> certainNodes) {
            final String property = configuration.unmapProperty(p);
            final String literal = o.getLiteralValue().toString();
            final CypherVar svar = gen.getVarFor(s.getName());
            if (certainNodes.contains(s)) {
                return CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(svar))
                        .condition(new ValueWhereCondition(svar, property, literal))
                        .returns(new NodeMappingReturnStatement(svar, s.getName()))
                        .build();
            }
            final CypherVar xvar = gen.getAnonVar();
            final CypherVar evar = gen.getAnonVar();
            final CypherVar yvar = gen.getAnonVar();
            return new UnionCypherQuery(
                    CypherQueryBuilder.newBuilder()
                            .match(new NodeMatchClause(svar))
                            .condition(new ValueWhereCondition(svar, property, literal))
                            .returns(new NodeMappingReturnStatement(svar, "r1"))
                            .returns(new EmptyReturnStatement("r2"))
                            .returns(new EmptyReturnStatement("r3"))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(new EdgeMatchClause(xvar, yvar, evar, null))
                            .condition(new ValueWhereCondition(evar, property, literal))
                            .returns(new NodeMappingReturnStatement(xvar, "r1"))
                            .returns(new EdgeMappingReturnStatement(evar, "r2"))
                            .returns(new NodeMappingReturnStatement(yvar, "r3"))
                            .build());
        }

        public static CypherQuery getVarLabelClass(final Node s, final Node p, final Node o,
                                              final Configuration configuration, final CypherVarGenerator gen) {
            final String clazz = configuration.unmapLabel(o);
            final CypherVar svar = gen.getVarFor(s.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(svar))
                    .condition(new ClassWhereCondition(svar, clazz))
                    .returns(new NodeMappingReturnStatement(svar, s.getName()))
                    .build();
        }

        public static CypherQuery getVarRelationshipNode(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen) {
            final String relationship = configuration.unmapRelationship(p);
            final String nodeID = configuration.unmapNode(o);
            final CypherVar svar = gen.getVarFor(s.getName());
            final CypherVar evar = gen.getAnonVar();
            final CypherVar ovar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(svar, ovar, evar, relationship))
                    .condition(new IDWhereCondition(ovar, nodeID))
                    .returns(new NodeMappingReturnStatement(svar, s.getName()))
                    .build();
        }

        public static CypherQuery getNodeLabelVar(final Node s, final Node p, final Node o,
                                             final Configuration configuration, final CypherVarGenerator gen) {
            final CypherVar svar = gen.getVarFor(o.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(svar))
                    .condition(new IDWhereCondition(svar, configuration.unmapNode(s)))
                    .returns(new LabelsReturnStatement(svar, o.getName()))
                    .build();
        }

        public static CypherQuery getNodePropertyVar(final Node s, final Node p, final Node o,
                                                final Configuration configuration, final CypherVarGenerator gen) {
            final String property = configuration.unmapProperty(p);
            final CypherVar var = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(var))
                    .condition(new IDWhereCondition(var, configuration.unmapNode(s)))
                    .condition(new EXISTSWhereCondition(var, property))
                    .returns(new LiteralReturnStatement(var, property, o.getName()))
                    .build();
        }

        public static CypherQuery getNodeRelationshipVar(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen) {
            final CypherVar xvar = gen.getAnonVar();
            final CypherVar evar = gen.getAnonVar();
            final CypherVar yvar = gen.getVarFor(o.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(xvar, yvar, evar, configuration.unmapRelationship(p)))
                    .condition(new IDWhereCondition(xvar, configuration.unmapNode(s)))
                    .returns(new NodeMappingReturnStatement(yvar, o.getName()))
                    .build();
        }

        public static CypherQuery getNodeVarNode(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final CypherVar xvar = gen.getAnonVar();
            final CypherVar evar = gen.getVarFor(p.getName());
            final CypherVar yvar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(xvar, yvar, evar, null))
                    .condition(new IDWhereCondition(xvar, configuration.unmapNode(s)))
                    .condition(new IDWhereCondition(yvar, configuration.unmapNode(o)))
                    .returns(new EdgeMappingReturnStatement(evar, p.getName()))
                    .build();
        }

        public static CypherQuery getNodeVarLiteral(final Node s, final Node p, final Node o,
                                               final Configuration configuration, final CypherVarGenerator gen) {
            final CypherVar xvar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(xvar))
                    .condition(new IDWhereCondition(xvar, configuration.unmapNode(s)))
                    .returns(new FilteredPropertiesReturnStatement(xvar, p.getName(), o.getLiteralValue().toString()))
                    .build();
        }

        public static CypherQuery getNodeVarLabel(final Node s, final Node p, final Node o,
                                             final Configuration configuration, final CypherVarGenerator gen) {
            return CypherQueryBuilder.newBuilder()
                    .returns(new ValueReturnStatement(configuration.getLabelIRI(), p.getName()))
                    .build();
        }

        public static CypherQuery getVarLabelVar(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final CypherVar svar = gen.getVarFor(s.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(svar))
                    .returns(new NodeMappingReturnStatement(svar, s.getName()))
                    .returns(new LabelsReturnStatement(svar, o.getName()))
                    .build();
        }

        public static CypherQuery getVarRelationshipVar(final Node s, final Node p, final Node o,
                                                   final Configuration configuration, final CypherVarGenerator gen) {
            final String relationship = configuration.unmapRelationship(p);
            final CypherVar svar = gen.getVarFor(s.getName());
            final CypherVar evar = gen.getAnonVar();
            final CypherVar ovar = gen.getVarFor(o.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(svar, ovar, evar, relationship))
                    .returns(new NodeMappingReturnStatement(svar, s.getName()))
                    .returns(new NodeMappingReturnStatement(ovar, o.getName()))
                    .build();
        }

        public static CypherQuery getVarPropertyVar(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen,
                                                    final Set<Node> certainNodes) {
            final String property = configuration.unmapProperty(p);
            final CypherVar svar = gen.getVarFor(s.getName());
            if (certainNodes.contains(s)) {
                return CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(svar))
                        .condition(new EXISTSWhereCondition(svar, property))
                        .returns(new NodeMappingReturnStatement(svar, s.getName()))
                        .returns(new LiteralReturnStatement(svar, property, o.getName()))
                        .build();
            }
            final CypherVar evar = gen.getAnonVar();
            final CypherVar ovar = gen.getVarFor(o.getName());
            return new UnionCypherQuery(
                    CypherQueryBuilder.newBuilder()
                            .match(new EdgeMatchClause(svar, ovar, evar, null))
                            .condition(new EXISTSWhereCondition(evar, property))
                            .returns(new NodeMappingReturnStatement(svar, "r1"))
                            .returns(new EdgeMappingReturnStatement(evar, "r2"))
                            .returns(new NodeMappingReturnStatement(ovar, "r3"))
                            .returns(new LiteralReturnStatement(evar, property, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(new NodeMatchClause(svar))
                            .condition(new EXISTSWhereCondition(svar, property))
                            .returns(new NodeMappingReturnStatement(svar, "r1"))
                            .returns(new EmptyReturnStatement("r2"))
                            .returns(new EmptyReturnStatement("r3"))
                            .returns(new LiteralReturnStatement(svar, property, o.getName()))
                            .build()
            );
        }

        public static CypherQuery getVarVarLabel(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final CypherVar svar = gen.getVarFor(s.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(svar))
                    .condition(new ClassWhereCondition(svar, configuration.unmapLabel(o)))
                    .returns(new NodeMappingReturnStatement(svar, s.getName()))
                    .returns(new ValueReturnStatement(configuration.getLabelIRI(), p.getName()))
                    .build();
        }

        public static CypherQuery getVarVarNode(final Node s, final Node p, final Node o,
                                           final Configuration configuration, final CypherVarGenerator gen) {
            final CypherVar svar = gen.getVarFor(s.getName());
            final CypherVar pvar = gen.getVarFor(p.getName());
            final CypherVar yvar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(svar, yvar, pvar, null))
                    .condition(new IDWhereCondition(yvar, configuration.unmapNode(o)))
                    .returns(new NodeMappingReturnStatement(svar, s.getName()))
                    .returns(new EdgeMappingReturnStatement(pvar, p.getName()))
                    .build();
        }

        public static CypherQuery getVarVarLiteral(final Node s, final Node p, final Node o,
                                                   final Configuration configuration, final CypherVarGenerator gen,
                                                   final Set<Node> certainNodes) {
            final CypherVar svar = gen.getVarFor(s.getName());
            if (certainNodes.contains(s)) {
                return CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(svar))
                        .returns(new NodeMappingReturnStatement(svar, s.getName()))
                        .returns(new PropertyValuesReturnStatement(svar, o.getLiteralValue().toString(), p.getName()))
                        .build();
            }
            final CypherVar pvar = gen.getVarFor(p.getName());
            final CypherVar yvar = gen.getAnonVar();
            return new UnionCypherQuery(
                    CypherQueryBuilder.newBuilder()
                            .match(new EdgeMatchClause(svar, yvar, pvar, null))
                            .returns(new NodeMappingReturnStatement(svar, "r1"))
                            .returns(new EdgeMappingReturnStatement(pvar, "r2"))
                            .returns(new NodeMappingReturnStatement(yvar, "r3"))
                            .returns(new FilteredPropertiesReturnStatement(pvar, p.getName(), o.getLiteralValue().toString()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(new NodeMatchClause(svar))
                            .returns(new NodeMappingReturnStatement(svar, "r1"))
                            .returns(new EmptyReturnStatement("r2"))
                            .returns(new EmptyReturnStatement("r3"))
                            .returns(new FilteredPropertiesReturnStatement(svar, p.getName(), o.getLiteralValue().toString()))
                            .build()
            );
        }

        public static CypherQuery getNodeVarVar(final Node s, final Node p, final Node o,
                                                final Configuration configuration, final CypherVarGenerator gen,
                                                final Set<Node> certainNodes) {
            final String nodeID = configuration.unmapNode(s);
            final CypherVar xvar = gen.getAnonVar();
            final CypherVar pvar = gen.getVarFor(p.getName());
            final CypherVar ovar = gen.getVarFor(o.getName());
            if (certainNodes.contains(o)){
                return CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(xvar, ovar, pvar, null))
                        .condition(new IDWhereCondition(xvar, nodeID))
                        .returns(new EdgeMappingReturnStatement(pvar, p.getName()))
                        .returns(new NodeMappingReturnStatement(ovar, o.getName()))
                        .build();
            }
            return new UnionCypherQuery(
                    CypherQueryBuilder.newBuilder()
                            .match(new NodeMatchClause(xvar))
                            .condition(new IDWhereCondition(xvar, nodeID))
                            .returns(new ValueReturnStatement(configuration.getLabelIRI(), p.getName()))
                            .returns(new LabelsReturnStatement(xvar, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(new NodeMatchClause(xvar))
                            .condition(new IDWhereCondition(xvar, nodeID))
                            .returns(new PropertyListReturnStatement(xvar, p.getName()))
                            .returns(new PropertyValuesReturnStatement(xvar, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(new EdgeMatchClause(xvar, ovar, pvar, null))
                            .condition(new IDWhereCondition(xvar, nodeID))
                            .returns(new EdgeMappingReturnStatement(pvar, p.getName()))
                            .returns(new NodeMappingReturnStatement(ovar, o.getName()))
                            .build()
            );
        }

        public static CypherQuery getVarVarVar(final Node s, final Node p, final Node o,
                                               final Configuration configuration, final CypherVarGenerator gen,
                                               final Set<Node> certainNodes) {
            final CypherVar svar = gen.getVarFor(s.getName());
            final CypherVar pvar = gen.getVarFor(p.getName());
            final CypherVar ovar = gen.getVarFor(o.getName());
            if (certainNodes.contains(s) && certainNodes.contains(o)) {
                return CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(svar, ovar, pvar, null))
                        .returns(new NodeMappingReturnStatement(svar, s.getName()))
                        .returns(new EdgeMappingReturnStatement(pvar, p.getName()))
                        .returns(new NodeMappingReturnStatement(ovar, o.getName()))
                        .build();
            }
            return new UnionCypherQuery(
                    CypherQueryBuilder.newBuilder()
                            .match(new EdgeMatchClause(svar, ovar, pvar, null))
                            .returns(new NodeMappingReturnStatement(svar, "r1"))
                            .returns(new EmptyReturnStatement("r2"))
                            .returns(new EmptyReturnStatement("r3"))
                            .returns(new EdgeMappingReturnStatement(pvar, p.getName()))
                            .returns(new NodeMappingReturnStatement(ovar, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(new NodeMatchClause(svar))
                            .returns(new NodeMappingReturnStatement(svar, "r1"))
                            .returns(new EmptyReturnStatement("r2"))
                            .returns(new EmptyReturnStatement("r3"))
                            .returns(new ValueReturnStatement(configuration.getLabelIRI(), p.getName()))
                            .returns(new LabelsReturnStatement(svar, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(new EdgeMatchClause(svar, ovar, pvar, null))
                            .returns(new NodeMappingReturnStatement(svar, "r1"))
                            .returns(new EdgeMappingReturnStatement(pvar, "r2"))
                            .returns(new NodeMappingReturnStatement(ovar, "r3"))
                            .returns(new PropertyListReturnStatement(pvar, p.getName()))
                            .returns(new PropertyValuesReturnStatement(pvar, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(new NodeMatchClause(svar))
                            .returns(new NodeMappingReturnStatement(svar, "r1"))
                            .returns(new EmptyReturnStatement("r2"))
                            .returns(new EmptyReturnStatement("r3"))
                            .returns(new PropertyListReturnStatement(svar, p.getName()))
                            .returns(new PropertyValuesReturnStatement(svar, o.getName()))
                            .build()
            );
        }

        public static CypherQuery getNodePropertyLiteral(final Node s, final Node p, final Node o,
                                                         final Configuration configuration, final CypherVarGenerator gen) {
            final CypherVar svar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(svar))
                    .condition(new IDWhereCondition(svar, configuration.unmapNode(s)))
                    .condition(new ValueWhereCondition(svar, configuration.unmapProperty(p), o.getLiteralValue().toString()))
                    .build();
        }

        public static CypherQuery getNodeLabelLabel(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen) {
            final CypherVar svar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(svar))
                    .condition(new IDWhereCondition(svar, configuration.unmapNode(s)))
                    .condition(new ClassWhereCondition(svar,  configuration.unmapLabel(o)))
                    .build();
        }

        public static CypherQuery getNodeRelationshipNode(final Node s, final Node p, final Node o,
                                                          final Configuration configuration, final CypherVarGenerator gen) {
            final CypherVar xvar = gen.getAnonVar();
            final CypherVar evar = gen.getAnonVar();
            final CypherVar yvar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(xvar, yvar, evar, configuration.unmapRelationship(p)))
                    .condition(new IDWhereCondition(xvar, configuration.unmapNode(s)))
                    .condition(new IDWhereCondition(yvar, configuration.unmapNode(o)))
                    .build();
        }
    }

    protected static class CypherVarGenerator {

        protected final Map<String, String> varmap = new HashMap<>();
        protected final String VAR_PATTERN = "cpvar%d";
        protected int current = 1;

        public CypherVar getVarFor( final String var ) {
            if (varmap.containsKey(var))
                return new CypherVar(varmap.get(var));
            varmap.put(var, String.format(VAR_PATTERN, current));
            current++;
            return new CypherVar(varmap.get(var));
        }

        public CypherVar getAnonVar() {
            String var = String.format(VAR_PATTERN, current);
            current++;
            return new CypherVar(var);
        }
    }
}
