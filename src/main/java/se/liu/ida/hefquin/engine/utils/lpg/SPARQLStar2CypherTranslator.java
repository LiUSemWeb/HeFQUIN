package se.liu.ida.hefquin.engine.utils.lpg;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.CypherQuery;
import se.liu.ida.hefquin.engine.query.impl.MatchCypherQuery;
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
            final String evar = MatchVariableGetter.getEdgeVariable(translation);
            if (configuration.mapsToProperty(p) && o.isLiteral()) {
                translation.addConditionConjunction(String.format(Translations.CONDITION_VALUE, evar,
                        configuration.unmapProperty(p), o.getLiteralValue()));
            } else if (configuration.mapsToProperty(p) && o.isVariable()) {
                translation.addConditionConjunction(String.format(Translations.CONDITION_EXISTS, evar,
                        configuration.unmapProperty(p)));
                translation.addReturnClause(String.format(Translations.RETURN_LITERAL_PATTERN, evar,
                        configuration.unmapProperty(p), o.getName()));
            } else if (p.isVariable() && o.isVariable()) {
                translation.addReturnClause(String.format(Translations.RETURN_KEYS, evar, p.getName()));
                translation.addReturnClause(String.format(Translations.RETURN_KEYS_VALUES, evar, evar, o.getName()));
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

        final static private String MATCH_EDGE              = "MATCH (%s)-[%s:%s]->(%s)";
        final static private String MATCH_EDGE_VAR          = "MATCH (%s)-[%s]->(%s)";
        final static private String MATCH_NODE              = "MATCH (%s)";
        final static private String CONDITION_EXISTS        = "EXISTS(%s.%s)";
        final static private String CONDITION_VALUE         = "%s.%s='%s'";
        final static private String CONDITION_CLASS         = "%s:%s" ;
        final static private String CONDITION_ID            = "ID(%s)=%s";
        final static private String RETURN_PATTERN          = "%s AS %s";
        final static private String RETURN_LITERAL_PATTERN  = "%s.%s AS %s";
        final static private String RETURN_NODE_MAPPING     = "nm(%s) AS %s";
        final static private String RETURN_EDGE_MAPPING     = "elm(%s) AS %s";
        final static private String RETURN_LABELS           = "labels(%s) AS %s";
        final private static String RETURN_KEYS             = "[k IN KEYS(%s) | pm(k)] AS %s";
        final private static String RETURN_KEYS_VALUES      = "[k IN KEYS(%s) | %s[k]] AS %s";
        final private static String RETURN_KEYS_WITH_VALUE  = "[k IN KEYS(%s) WHERE %s[k]='%s' | pm(k)] AS %s";

        public static CypherQuery getVarPropertyLiteral(final Node s, final Node p, final Node o,
                                                        final Configuration configuration, final CypherVarGenerator gen,
                                                        final Set<Node> certainNodes) {
            final String property = configuration.unmapProperty(p);
            final String literal = o.getLiteralValue().toString();
            final String svar = gen.getVarFor(s.getName());
            if (certainNodes.contains(s)) {
                return CypherQueryBuilder.newBuilder()
                        .match(String.format(MATCH_NODE, svar))
                        .condition(String.format(CONDITION_VALUE, svar, property, literal))
                        .returns(String.format(RETURN_NODE_MAPPING, svar, s.getName()))
                        .build();
            }
            final String xvar = gen.getAnonVar();
            final String evar = gen.getAnonVar();
            final String yvar = gen.getAnonVar();
            return new UnionCypherQuery(
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_NODE, svar))
                            .condition(String.format(CONDITION_VALUE, svar, property, literal))
                            .returns(String.format(RETURN_NODE_MAPPING, svar, "r1"))
                            .returns(String.format(RETURN_PATTERN, "''", "r2"))
                            .returns(String.format(RETURN_PATTERN, "''", "r3"))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_EDGE_VAR, xvar, evar, yvar))
                            .condition(String.format(CONDITION_VALUE, evar, property, literal))
                            .returns(String.format(RETURN_NODE_MAPPING, xvar, "r1"))
                            .returns(String.format(RETURN_EDGE_MAPPING, evar, "r2"))
                            .returns(String.format(RETURN_NODE_MAPPING, yvar, "r3"))
                            .build());
        }

        public static CypherQuery getVarLabelClass(final Node s, final Node p, final Node o,
                                              final Configuration configuration, final CypherVarGenerator gen) {
            final String clazz = configuration.unmapLabel(o);
            final String svar = gen.getVarFor(s.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_NODE, svar))
                    .condition(String.format(CONDITION_CLASS, svar, clazz))
                    .returns(String.format(RETURN_NODE_MAPPING, svar, s.getName()))
                    .build();
        }

        public static CypherQuery getVarRelationshipNode(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen) {
            final String relationship = configuration.unmapRelationship(p);
            final String nodeID = configuration.unmapNode(o);
            final String svar = gen.getVarFor(s.getName());
            final String evar = gen.getAnonVar();
            final String ovar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder().match(String.format(MATCH_EDGE, svar, evar, relationship, ovar))
                    .condition(String.format(CONDITION_ID, ovar, nodeID))
                    .returns(String.format(RETURN_NODE_MAPPING, svar, s.getName()))
                    .build();
        }

        public static CypherQuery getNodeLabelVar(final Node s, final Node p, final Node o,
                                             final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(o.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_NODE, svar))
                    .condition(String.format(CONDITION_ID, svar, configuration.unmapNode(s)))
                    .returns(String.format(RETURN_LABELS, svar, o.getName()))
                    .build();
        }

        public static CypherQuery getNodePropertyVar(final Node s, final Node p, final Node o,
                                                final Configuration configuration, final CypherVarGenerator gen) {
            final String property = configuration.unmapProperty(p);
            final String var = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_NODE, var))
                    .condition(String.format(CONDITION_ID, var, configuration.unmapNode(s)))
                    .condition(String.format(CONDITION_EXISTS, var, property))
                    .returns(String.format(RETURN_LITERAL_PATTERN, var, property, o.getName()))
                    .build();
        }

        public static CypherQuery getNodeRelationshipVar(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen) {
            final String xvar = gen.getAnonVar();
            final String evar = gen.getAnonVar();
            final String yvar = gen.getVarFor(o.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_EDGE, xvar, evar, configuration.unmapRelationship(p), yvar))
                    .condition(String.format(CONDITION_ID, xvar, configuration.unmapNode(s)))
                    .returns(String.format(RETURN_NODE_MAPPING, yvar, o.getName()))
                    .build();
        }

        public static CypherQuery getNodeVarNode(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final String xvar = gen.getAnonVar();
            final String evar = gen.getVarFor(p.getName());
            final String yvar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_EDGE_VAR, xvar, evar, yvar))
                    .condition(String.format(CONDITION_ID, xvar, configuration.unmapNode(s)))
                    .condition(String.format(CONDITION_ID, yvar, configuration.unmapNode(o)))
                    .returns(String.format(RETURN_EDGE_MAPPING, evar, p.getName()))
                    .build();
        }

        public static CypherQuery getNodeVarLiteral(final Node s, final Node p, final Node o,
                                               final Configuration configuration, final CypherVarGenerator gen) {
            final String xvar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_NODE, xvar))
                    .condition(String.format(CONDITION_ID, xvar, configuration.unmapNode(s)))
                    .returns(String.format(RETURN_KEYS_WITH_VALUE, xvar, xvar, o.getLiteralValue(), p.getName()))
                    .build();
        }

        public static CypherQuery getNodeVarLabel(final Node s, final Node p, final Node o,
                                             final Configuration configuration, final CypherVarGenerator gen) {
            return CypherQueryBuilder.newBuilder()
                    .returns(String.format(RETURN_PATTERN, configuration.getLabelIRI(), p.getName()))
                    .build();
        }

        public static CypherQuery getVarLabelVar(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_NODE, svar))
                    .returns(String.format(RETURN_NODE_MAPPING, svar, s.getName()))
                    .returns(String.format(RETURN_LABELS, svar, o.getName()))
                    .build();
        }

        public static CypherQuery getVarRelationshipVar(final Node s, final Node p, final Node o,
                                                   final Configuration configuration, final CypherVarGenerator gen) {
            final String relationship = configuration.unmapRelationship(p);
            final String svar = gen.getVarFor(s.getName());
            final String evar = gen.getAnonVar();
            final String ovar = gen.getVarFor(o.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_EDGE, svar, evar, relationship, ovar))
                    .returns(String.format(RETURN_NODE_MAPPING, svar, s.getName()))
                    .returns(String.format(RETURN_NODE_MAPPING, ovar, o.getName()))
                    .build();
        }

        public static CypherQuery getVarPropertyVar(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen,
                                                    final Set<Node> certainNodes) {
            final String property = configuration.unmapProperty(p);
            final String svar = gen.getVarFor(s.getName());
            if (certainNodes.contains(s)) {
                return CypherQueryBuilder.newBuilder()
                        .match(String.format(MATCH_NODE, svar))
                        .condition(String.format(CONDITION_EXISTS, svar, property))
                        .returns(String.format(RETURN_NODE_MAPPING, svar, s.getName()))
                        .returns(String.format(RETURN_LITERAL_PATTERN, svar, property, o.getName()))
                        .build();
            }
            final String evar = gen.getAnonVar();
            final String ovar = gen.getVarFor(o.getName());
            return new UnionCypherQuery(
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_EDGE_VAR, svar, evar, ovar))
                            .condition(String.format(CONDITION_EXISTS, evar, property))
                            .returns(String.format(RETURN_NODE_MAPPING, svar, "r1"))
                            .returns(String.format(RETURN_EDGE_MAPPING, evar, "r2"))
                            .returns(String.format(RETURN_NODE_MAPPING, ovar, "r3"))
                            .returns(String.format(RETURN_LITERAL_PATTERN, evar, property, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_NODE, svar))
                            .condition(String.format(CONDITION_EXISTS, svar, property))
                            .returns(String.format(RETURN_NODE_MAPPING, svar, "r1"))
                            .returns(String.format(RETURN_PATTERN, "''", "r2"))
                            .returns(String.format(RETURN_PATTERN, "''", "r3"))
                            .returns(String.format(RETURN_LITERAL_PATTERN, svar, property, o.getName()))
                            .build()
            );
        }

        public static CypherQuery getVarVarLabel(final Node s, final Node p, final Node o,
                                            final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_NODE, svar))
                    .condition(String.format(CONDITION_CLASS, svar, configuration.unmapLabel(o)))
                    .returns(String.format(RETURN_NODE_MAPPING, svar, s.getName()))
                    .returns(String.format(RETURN_PATTERN, configuration.getLabelIRI(), p.getName()))
                    .build();
        }

        public static CypherQuery getVarVarNode(final Node s, final Node p, final Node o,
                                           final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getVarFor(s.getName());
            final String pvar = gen.getVarFor(p.getName());
            final String yvar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_EDGE_VAR, svar, pvar, yvar))
                    .condition(String.format(CONDITION_ID, yvar, configuration.unmapNode(o)))
                    .returns(String.format(RETURN_NODE_MAPPING, svar, s.getName()))
                    .returns(String.format(RETURN_EDGE_MAPPING, pvar, p.getName()))
                    .build();
        }

        public static CypherQuery getVarVarLiteral(final Node s, final Node p, final Node o,
                                                   final Configuration configuration, final CypherVarGenerator gen,
                                                   final Set<Node> certainNodes) {
            final String svar = gen.getVarFor(s.getName());
            if (certainNodes.contains(s)) {
                return CypherQueryBuilder.newBuilder()
                        .match(String.format(MATCH_NODE, svar))
                        .returns(String.format(RETURN_NODE_MAPPING, svar, s.getName()))
                        .returns(String.format(RETURN_KEYS_WITH_VALUE, svar, svar, o.getLiteralValue(), p.getName()))
                        .build();
            }
            final String pvar = gen.getVarFor(p.getName());
            final String yvar = gen.getAnonVar();
            return new UnionCypherQuery(
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_EDGE_VAR, svar, pvar, yvar))
                            .returns(String.format(RETURN_NODE_MAPPING, svar, "r1"))
                            .returns(String.format(RETURN_EDGE_MAPPING, pvar, "r2"))
                            .returns(String.format(RETURN_NODE_MAPPING, yvar, "r3"))
                            .returns(String.format(RETURN_KEYS_WITH_VALUE, pvar, pvar, o.getLiteralValue(), p.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_NODE, svar))
                            .returns(String.format(RETURN_NODE_MAPPING, svar, "r1"))
                            .returns(String.format(RETURN_PATTERN, "''", "r2"))
                            .returns(String.format(RETURN_PATTERN, "''", "r3"))
                            .returns(String.format(RETURN_KEYS_WITH_VALUE, svar, svar, o.getLiteralValue(), p.getName()))
                            .build()
            );
        }

        public static CypherQuery getNodeVarVar(final Node s, final Node p, final Node o,
                                                final Configuration configuration, final CypherVarGenerator gen,
                                                final Set<Node> certainNodes) {
            final String nodeID = configuration.unmapNode(s);
            final String xvar = gen.getAnonVar();
            final String pvar = gen.getVarFor(p.getName());
            final String ovar = gen.getVarFor(o.getName());
            if (certainNodes.contains(o)){
                return CypherQueryBuilder.newBuilder()
                        .match(String.format(MATCH_EDGE_VAR, xvar, pvar, ovar))
                        .condition(String.format(CONDITION_ID, xvar, nodeID))
                        .returns(String.format(RETURN_EDGE_MAPPING, pvar, p.getName()))
                        .returns(String.format(RETURN_NODE_MAPPING, ovar, o.getName()))
                        .build();
            }
            return new UnionCypherQuery(
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_NODE, xvar))
                            .condition(String.format(CONDITION_ID, xvar, nodeID))
                            .returns(String.format(RETURN_PATTERN, configuration.getLabelIRI(), p.getName()))
                            .returns(String.format(RETURN_LABELS, xvar, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_NODE, xvar))
                            .condition(String.format(CONDITION_ID, xvar, nodeID))
                            .returns(String.format(RETURN_KEYS, xvar, p.getName()))
                            .returns(String.format(RETURN_KEYS_VALUES, xvar, xvar, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_EDGE_VAR, xvar, pvar, ovar))
                            .condition(String.format(CONDITION_ID, xvar, nodeID))
                            .returns(String.format(RETURN_EDGE_MAPPING, pvar, p.getName()))
                            .returns(String.format(RETURN_NODE_MAPPING, ovar, o.getName()))
                            .build()
            );
        }

        public static CypherQuery getVarVarVar(final Node s, final Node p, final Node o,
                                               final Configuration configuration, final CypherVarGenerator gen,
                                               final Set<Node> certainNodes) {
            final String svar = gen.getVarFor(s.getName());
            final String pvar = gen.getVarFor(p.getName());
            final String ovar = gen.getVarFor(o.getName());
            if (certainNodes.contains(s) && certainNodes.contains(o)) {
                return CypherQueryBuilder.newBuilder()
                        .match(String.format(MATCH_EDGE_VAR, svar, pvar, ovar))
                        .returns(String.format(RETURN_NODE_MAPPING, svar, s.getName()))
                        .returns(String.format(RETURN_EDGE_MAPPING, pvar, p.getName()))
                        .returns(String.format(RETURN_NODE_MAPPING, ovar, o.getName()))
                        .build();
            }
            return new UnionCypherQuery(
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_EDGE_VAR, svar, pvar, ovar))
                            .returns(String.format(RETURN_NODE_MAPPING, svar, "r1"))
                            .returns(String.format(RETURN_PATTERN, "''", "r2"))
                            .returns(String.format(RETURN_PATTERN, "''", "r3"))
                            .returns(String.format(RETURN_EDGE_MAPPING, pvar, p.getName()))
                            .returns(String.format(RETURN_NODE_MAPPING, ovar, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_NODE, svar))
                            .returns(String.format(RETURN_NODE_MAPPING, svar, "r1"))
                            .returns(String.format(RETURN_PATTERN, "''", "r2"))
                            .returns(String.format(RETURN_PATTERN, "''", "r3"))
                            .returns(String.format(RETURN_PATTERN, configuration.getLabelIRI(), p.getName()))
                            .returns(String.format(RETURN_LABELS, svar, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_EDGE_VAR, svar, pvar, ovar))
                            .returns(String.format(RETURN_NODE_MAPPING, svar, "r1"))
                            .returns(String.format(RETURN_EDGE_MAPPING, pvar, "r2"))
                            .returns(String.format(RETURN_NODE_MAPPING, ovar, "r3"))
                            .returns(String.format(RETURN_KEYS, pvar, p.getName()))
                            .returns(String.format(RETURN_KEYS_VALUES, pvar, pvar, o.getName()))
                            .build(),
                    CypherQueryBuilder.newBuilder()
                            .match(String.format(MATCH_NODE, svar))
                            .returns(String.format(RETURN_NODE_MAPPING, svar, "r1"))
                            .returns(String.format(RETURN_PATTERN, "''", "r2"))
                            .returns(String.format(RETURN_PATTERN, "''", "r3"))
                            .returns(String.format(RETURN_KEYS, svar, p.getName()))
                            .returns(String.format(RETURN_KEYS_VALUES, svar, svar, o.getName()))
                            .build()
            );
        }

        public static CypherQuery getNodePropertyLiteral(final Node s, final Node p, final Node o,
                                                         final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_NODE, svar))
                    .condition(String.format(CONDITION_ID, svar, configuration.unmapNode(s)))
                    .condition(String.format(CONDITION_VALUE, svar, configuration.unmapProperty(p), o.getLiteralValue()))
                    .build();
        }

        public static CypherQuery getNodeLabelLabel(final Node s, final Node p, final Node o,
                                                    final Configuration configuration, final CypherVarGenerator gen) {
            final String svar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_NODE, svar))
                    .condition(String.format(CONDITION_ID, svar, configuration.unmapNode(s)))
                    .condition(String.format(CONDITION_CLASS, svar,  configuration.unmapLabel(o)))
                    .build();
        }

        public static CypherQuery getNodeRelationshipNode(final Node s, final Node p, final Node o,
                                                          final Configuration configuration, final CypherVarGenerator gen) {
            final String xvar = gen.getAnonVar();
            final String evar = gen.getAnonVar();
            final String yvar = gen.getAnonVar();
            return CypherQueryBuilder.newBuilder()
                    .match(String.format(MATCH_EDGE, xvar, evar, configuration.unmapRelationship(p), yvar))
                    .condition(String.format(CONDITION_ID, xvar, configuration.unmapNode(s)))
                    .condition(String.format(CONDITION_ID, yvar, configuration.unmapNode(o)))
                    .build();
        }
    }

    protected static class CypherVarGenerator {

        protected final Map<String, String> varmap = new HashMap<>();
        protected final String VAR_PATTERN = "cpvar%d";
        protected int current = 1;

        public String getVarFor( final String var ) {
            if (varmap.containsKey(var))
                return varmap.get(var);
            varmap.put(var, String.format(VAR_PATTERN, current));
            current++;
            return varmap.get(var);
        }

        public String getAnonVar() {
            String var = String.format(VAR_PATTERN, current);
            current++;
            return var;
        }
    }
}
