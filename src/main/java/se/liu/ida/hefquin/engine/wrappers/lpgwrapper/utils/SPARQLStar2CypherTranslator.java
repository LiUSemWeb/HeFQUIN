package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns.*;

import java.util.*;

public class SPARQLStar2CypherTranslator {

    public static CypherQuery translateTriplePattern(final TriplePatternImpl tp, final LPG2RDFConfiguration conf) {
        return translateTriplePattern(tp, conf, new CypherVarGenerator(), new HashSet<>());
    }

    protected static CypherQuery translateTriplePattern(final TriplePattern pattern,
                                                        final LPG2RDFConfiguration configuration,
                                                        final CypherVarGenerator gen,
                                                        final Set<Node> certainNodes) {
        final Triple b = pattern.asJenaTriple();
        final Node s = b.getSubject();
        final Node p = b.getPredicate();
        final Node o = b.getObject();
        if (pattern.numberOfVars() == 1){
            if (s.isVariable()) {
                if (configuration.mapsToProperty(p) && o.isLiteral()) {
                    return getVarPropertyLiteral(s, p, o, configuration, gen, certainNodes);
                } else if (configuration.isLabelIRI(p) && configuration.mapsToLabel(o)) {
                    return getVarLabelClass(s, p, o, configuration, gen);
                } else if (configuration.mapsToRelationship(p) && configuration.mapsToNode(o)){
                    return getVarRelationshipNode(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Illegal values for predicate and object");
                }
            }
            else if (o.isVariable()) {
                if (configuration.isLabelIRI(p) && configuration.mapsToNode(s)) {
                    return getNodeLabelVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToProperty(p) && configuration.mapsToNode(s)) {
                    return getNodePropertyVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToRelationship(p) && configuration.mapsToNode(s)) {
                    return getNodeRelationshipVar(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Illegal values for subject and predicate");
                }
            }
            else if (p.isVariable()) {
                if (configuration.mapsToNode(s) && configuration.mapsToNode(o)) {
                    return getNodeVarNode(s, p, o, configuration, gen);
                } else if (configuration.mapsToNode(s) && o.isLiteral()) {
                    return getNodeVarLiteral(s, p, o, configuration, gen);
                } else if (configuration.mapsToNode(s) && configuration.mapsToLabel(o)) {
                    return getNodeVarLabel(s, p, o, configuration, gen);
                } else {
                    throw new IllegalArgumentException("Illegal values for subject and object");
                }
            }
        } else if (pattern.numberOfVars() == 2) {
            if (s.isVariable() && o.isVariable()) {
                if (configuration.isLabelIRI(p)) {
                    return getVarLabelVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToRelationship(p)) {
                    return getVarRelationshipVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToProperty(p)) {
                    return getVarPropertyVar(s, p, o, configuration, gen, certainNodes);
                } else {
                    throw new IllegalArgumentException("Predicate must be a mapping of a property or a relationship " +
                            "or the label URI");
                }
            } else if (s.isVariable() && p.isVariable()) {
                if (configuration.mapsToLabel(o)) {
                    return getVarVarLabel(s, p, o, configuration, gen);
                } else if (configuration.mapsToNode(o)) {
                    return getVarVarNode(s, p, o, configuration, gen);
                } else if (o.isLiteral()) {
                    return getVarVarLiteral(s, p, o, configuration, gen, certainNodes);
                }
            } else if(p.isVariable() && o.isVariable()) {
                if (configuration.mapsToNode(s)) {
                    return getNodeVarVar(s, p, o, configuration, gen, certainNodes);
                }
            }
        } else {
            return getVarVarVar(s, p, o, configuration, gen, certainNodes);
        }
        throw new IllegalArgumentException("Malformed query pattern");
    }

    protected static CypherQuery getVarPropertyLiteral(final Node s, final Node p, final Node o,
                                                    final LPG2RDFConfiguration configuration,
                                                    final CypherVarGenerator gen,
                                                    final Set<Node> certainNodes) {
        final String property = configuration.unmapProperty(p.getURI());
        final String literal = o.getLiteralValue().toString();
        final CypherVar svar = gen.getVarFor(s);
        if (certainNodes.contains(s)) {
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(svar))
                    .condition(new PropertyValueCondition(svar, property, literal))
                    .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                    .build();
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        return new CypherUnionQueryImpl(
                CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(svar))
                        .condition(new PropertyValueCondition(svar, property, literal))
                        .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                        .build(),
                CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .condition(new PropertyValueCondition(sedge.get(1), property, literal))
                        .returns(new TripleMapReturnStatement(sedge.get(0), sedge.get(1), sedge.get(2), gen.getRetVar(s)))
                        .build());
    }

    protected static CypherQuery getVarLabelClass(final Node s, final Node p, final Node o,
                                               final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final String clazz = configuration.unmapNodeLabel(o.getURI());
        final CypherVar svar = gen.getVarFor(s);
        return CypherQueryBuilder.newBuilder()
                .match(new NodeMatchClause(svar))
                .condition(new NodeLabelCondition(svar, clazz))
                .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .build();
    }

    protected static CypherQuery getVarRelationshipNode(final Node s, final Node p, final Node o,
                                                     final LPG2RDFConfiguration configuration,
                                                     final CypherVarGenerator gen) {
        final String relationship = configuration.unmapEdgeLabel(p.getURI());
        final LPGNode node = configuration.unmapNode(o.getURI());
        final CypherVar svar = gen.getVarFor(s);
        final CypherVar evar = gen.getAnonVar();
        final CypherVar ovar = gen.getAnonVar();
        return CypherQueryBuilder.newBuilder()
                .match(new EdgeMatchClause(svar, evar, ovar))
                .condition(new NodeIDCondition(ovar, node.getId()))
                .condition(new EdgeLabelCondition(evar, relationship))
                .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .build();
    }

    protected static CypherQuery getNodeLabelVar(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(o);
        return CypherQueryBuilder.newBuilder()
                .match(new NodeMatchClause(svar))
                .condition(new NodeIDCondition(svar, configuration.unmapNode(s.getURI()).getId()))
                .returns(new LabelsReturnStatement(svar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodePropertyVar(final Node s, final Node p, final Node o,
                                                 final LPG2RDFConfiguration configuration,
                                                 final CypherVarGenerator gen) {
        final String property = configuration.unmapProperty(p.getURI());
        final CypherVar var = gen.getAnonVar();
        return CypherQueryBuilder.newBuilder()
                .match(new NodeMatchClause(var))
                .condition(new NodeIDCondition(var, configuration.unmapNode(s.getURI()).getId()))
                .condition(new PropertyEXISTSCondition(var, property))
                .returns(new PropertyValueReturnStatement(var, property, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodeRelationshipVar(final Node s, final Node p, final Node o,
                                                     final LPG2RDFConfiguration configuration,
                                                     final CypherVarGenerator gen) {
        final CypherVar xvar = gen.getAnonVar();
        final CypherVar evar = gen.getAnonVar();
        final CypherVar yvar = gen.getVarFor(o);
        return CypherQueryBuilder.newBuilder()
                .match(new EdgeMatchClause(xvar, evar, yvar))
                .condition(new NodeIDCondition(xvar, configuration.unmapNode(s.getURI()).getId()))
                .condition(new EdgeLabelCondition(evar, configuration.unmapEdgeLabel(p.getURI())))
                .returns(new VariableReturnStatement(yvar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodeVarNode(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar xvar = gen.getAnonVar();
        final CypherVar evar = gen.getVarFor(p);
        final CypherVar yvar = gen.getAnonVar();
        return CypherQueryBuilder.newBuilder()
                .match(new EdgeMatchClause(xvar, evar, yvar))
                .condition(new NodeIDCondition(xvar, configuration.unmapNode(s.getURI()).getId()))
                .condition(new NodeIDCondition(yvar, configuration.unmapNode(o.getURI()).getId()))
                .returns(new VariableReturnStatement(evar, gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getNodeVarLiteral(final Node s, final Node p, final Node o,
                                                final LPG2RDFConfiguration configuration,
                                                final CypherVarGenerator gen) {
        final CypherVar xvar = gen.getAnonVar();
        return CypherQueryBuilder.newBuilder()
                .match(new NodeMatchClause(xvar))
                .condition(new NodeIDCondition(xvar, configuration.unmapNode(s.getURI()).getId()))
                .returns(new FilteredPropertiesReturnStatement(xvar, o.getLiteralValue().toString(), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getNodeVarLabel(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar node = gen.getAnonVar();
        return CypherQueryBuilder.newBuilder()
                .match(new NodeMatchClause(node))
                .condition(new NodeIDCondition(node, configuration.unmapNode(s.getURI()).getId()))
                .condition(new NodeLabelCondition(node, configuration.unmapNodeLabel(o.getURI())))
                .returns(new LiteralValueReturnStatement("label", gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getVarLabelVar(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(s);
        return CypherQueryBuilder.newBuilder()
                .match(new NodeMatchClause(svar))
                .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .returns(new LabelsReturnStatement(svar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getVarRelationshipVar(final Node s, final Node p, final Node o,
                                                    final LPG2RDFConfiguration configuration,
                                                    final CypherVarGenerator gen) {
        final String relationship = configuration.unmapEdgeLabel(p.getURI());
        final CypherVar svar = gen.getVarFor(s);
        final CypherVar evar = gen.getAnonVar();
        final CypherVar ovar = gen.getVarFor(o);
        return CypherQueryBuilder.newBuilder()
                .match(new EdgeMatchClause(svar, evar, ovar))
                .condition(new EdgeLabelCondition(evar, relationship))
                .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .returns(new VariableReturnStatement(ovar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getVarPropertyVar(final Node s, final Node p, final Node o,
                                                final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                                final Set<Node> certainNodes) {
        final String property = configuration.unmapProperty(p.getURI());
        final CypherVar svar = gen.getVarFor(s);
        if (certainNodes.contains(s)) {
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(svar))
                    .condition(new PropertyEXISTSCondition(svar, property))
                    .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                    .returns(new PropertyValueReturnStatement(svar, property, gen.getRetVar(o)))
                    .build();
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        return new CypherUnionQueryImpl(
                CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .condition(new PropertyEXISTSCondition(sedge.get(1), property))
                        .returns(new TripleMapReturnStatement(sedge.get(0), sedge.get(1), sedge.get(2), gen.getRetVar(s)))
                        .returns(new PropertyValueReturnStatement(sedge.get(1), property, gen.getRetVar(o)))
                        .build(),
                CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(svar))
                        .condition(new PropertyEXISTSCondition(svar, property))
                        .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                        .returns(new PropertyValueReturnStatement(svar, property, gen.getRetVar(o)))
                        .build()
        );
    }

    protected static CypherQuery getVarVarLabel(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(s);
        return CypherQueryBuilder.newBuilder()
                .match(new NodeMatchClause(svar))
                .condition(new NodeLabelCondition(svar, configuration.unmapNodeLabel(o.getURI())))
                .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .build();
    }

    protected static CypherQuery getVarVarNode(final Node s, final Node p, final Node o,
                                            final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(s);
        final CypherVar pvar = gen.getVarFor(p);
        final CypherVar yvar = gen.getAnonVar();
        return CypherQueryBuilder.newBuilder()
                .match(new EdgeMatchClause(svar, pvar, yvar))
                .condition(new NodeIDCondition(yvar, configuration.unmapNode(o.getURI()).getId()))
                .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .returns(new VariableReturnStatement(pvar, gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getVarVarLiteral(final Node s, final Node p, final Node o,
                                               final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                               final Set<Node> certainNodes) {
        final CypherVar svar = gen.getVarFor(s);
        if (certainNodes.contains(s)) {
            return CypherQueryBuilder.newBuilder()
                    .match(new NodeMatchClause(svar))
                    .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                    .returns(new FilteredPropertiesReturnStatement(svar, o.getLiteralValue().toString(), gen.getRetVar(p)))
                    .build();
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        return new CypherUnionQueryImpl(
                CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .returns(new TripleMapReturnStatement(sedge.get(0), sedge.get(1), sedge.get(2), gen.getRetVar(s)))
                        .returns(new FilteredPropertiesReturnStatement(sedge.get(1), o.getLiteralValue().toString(), gen.getRetVar(p)))
                        .build(),
                CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(svar))
                        .returns(new VariableReturnStatement(svar, gen.getRetVar(s)))
                        .returns(new FilteredPropertiesReturnStatement(svar, o.getLiteralValue().toString(), gen.getRetVar(p)))
                        .build()
        );
    }

    protected static CypherQuery getNodeVarVar(final Node s, final Node p, final Node o,
                                            final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                            final Set<Node> certainNodes) {
        final LPGNode node = configuration.unmapNode(s.getURI());
        final CypherVar a1 = gen.getAnonVar();
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar a3 = gen.getAnonVar();
        final CypherVar a4 = gen.getAnonVar();
        final CypherVar a5 = gen.getAnonVar();
        final CypherVar pvar = gen.getVarFor(p);
        final CypherVar ovar = gen.getVarFor(o);
        if (certainNodes.contains(o)){
            return CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(a1, pvar, ovar))
                    .condition(new NodeIDCondition(a1, node.getId()))
                    .returns(new VariableReturnStatement(pvar, gen.getRetVar(p)))
                    .returns(new VariableReturnStatement(ovar, gen.getRetVar(o)))
                    .build();
        }
        return new CypherUnionQueryImpl(
                CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(a1))
                        .condition(new NodeIDCondition(a1, node.getId()))
                        .returns(new LiteralValueReturnStatement("label", gen.getRetVar(p)))
                        .returns(new LabelsReturnStatement(a1, gen.getRetVar(o)))
                        .build(),
                CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(a2))
                        .condition(new NodeIDCondition(a2, node.getId()))
                        .returns(new PropertyListReturnStatement(a2, gen.getRetVar(p)))
                        .returns(new AllPropertyValuesReturnStatement(a2, gen.getRetVar(o)))
                        .build(),
                CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(a3, a4, a5))
                        .condition(new NodeIDCondition(a3, node.getId()))
                        .returns(new VariableReturnStatement(a4, gen.getRetVar(p)))
                        .returns(new VariableReturnStatement(a5, gen.getRetVar(o)))
                        .build()
        );
    }

    protected static CypherQuery getVarVarVar(final Node s, final Node p, final Node o,
                                           final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                           final Set<Node> certainNodes) {
        final CypherVar a1 = new CypherVar("a1");
        final CypherVar a2 = new CypherVar("a2");
        final CypherVar a3 = new CypherVar("a3");
        if (certainNodes.contains(s) && certainNodes.contains(o)) {
            return CypherQueryBuilder.newBuilder()
                    .match(new EdgeMatchClause(a1, a2, a3))
                    .returns(new VariableReturnStatement(a1, gen.getRetVar(s)))
                    .returns(new VariableReturnStatement(a2, gen.getRetVar(p)))
                    .returns(new VariableReturnStatement(a3, gen.getRetVar(o)))
                    .build();
        }

        final CypherVar a4 = new CypherVar("a4");
        final CypherVar a5 = new CypherVar("a5");
        final CypherVar a6 = new CypherVar("a6");
        final CypherVar a7 = new CypherVar("a7");
        final CypherVar a8 = new CypherVar("a8");
        return new CypherUnionQueryImpl(
                CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(a1))
                        .returns(new VariableReturnStatement(a1, gen.getRetVar(s)))
                        .returns(new LiteralValueReturnStatement("label", gen.getRetVar(p)))
                        .returns(new LabelsReturnStatement(a1, gen.getRetVar(o)))
                        .build(),
                CypherQueryBuilder.newBuilder()
                        .match(new NodeMatchClause(a2))
                        .returns(new VariableReturnStatement(a2, gen.getRetVar(s)))
                        .returns(new PropertyListReturnStatement(a2, gen.getRetVar(p)))
                        .returns(new AllPropertyValuesReturnStatement(a2, gen.getRetVar(o)))
                        .build(),
                CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(a3, a4, a5))
                        .returns(new TripleMapReturnStatement(a3, a4, a5, gen.getRetVar(s)))
                        .returns(new PropertyListReturnStatement(a4, gen.getRetVar(p)))
                        .returns(new AllPropertyValuesReturnStatement(a4, gen.getRetVar(o)))
                        .build(),
                CypherQueryBuilder.newBuilder()
                        .match(new EdgeMatchClause(a6, a7, a8))
                        .returns(new VariableReturnStatement(a6, gen.getRetVar(s)))
                        .returns(new VariableReturnStatement(a7, gen.getRetVar(p)))
                        .returns(new VariableReturnStatement(a8, gen.getRetVar(o)))
                        .build()
        );
    }

    protected static class CypherVarGenerator {

        private final Map<String, CypherVar> innerVars = new HashMap<>();
        private final Map<String, CypherVar> retVars = new HashMap<>();
        private final Map<String, List<CypherVar>> edgeVars = new HashMap<>();

        private int varCount = 0;
        private int edgeCount = 0;
        private int anonCount = 0;
        private int retVarCount = 0;

        private final String varPrefix = "cpvar";
        private final String retPrefix = "ret";
        private final String anonPrefix = "a";

        public CypherVar getVarFor(final Node n) {
            if (innerVars.containsKey(n.getName()))
                return innerVars.get(n.getName());
            varCount++;
            final CypherVar var = new CypherVar(varPrefix + varCount);
            innerVars.put(n.getName(), var);
            return var;
        }

        public List<CypherVar> getEdgeVars(final Node n) {
            if (edgeVars.containsKey(n.getName()))
                return edgeVars.get(n.getName());
            edgeCount++;
            final CypherVar source = new CypherVar("src" + edgeCount);
            final CypherVar edge = new CypherVar("edge" + edgeCount);
            final CypherVar target = new CypherVar("tgt" + edgeCount);
            final List<CypherVar> vars = new ArrayList<>();
            vars.add(source);
            vars.add(edge);
            vars.add(target);
            return vars;
        }

        public CypherVar getAnonVar() {
            anonCount++;
            return new CypherVar(anonPrefix + anonCount);
        }

        public CypherVar getRetVar(final Node n) {
            if (retVars.containsKey(n.getName()))
                return retVars.get(n.getName());
            retVarCount++;
            final CypherVar var = new CypherVar(retPrefix + retVarCount);
            retVars.put(n.getName(), var);
            return var;
        }
    }
}
