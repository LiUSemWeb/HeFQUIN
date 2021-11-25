package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.SPARQLStar2CypherTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherQueryBuilder;

import java.util.*;

public class SPARQLStar2CypherTranslatorImpl implements SPARQLStar2CypherTranslator {

    public CypherQuery translateTriplePattern(final TriplePattern tp, final LPG2RDFConfiguration conf) {
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
        final int nbOfVars = getNumberOfVars(s, p, o);
        if (nbOfVars == 1){
            if (s.isVariable()) {
                if (configuration.mapsToProperty(p) && o.isLiteral()) {
                    return getVarPropertyLiteral(s, p, o, configuration, gen, certainNodes);
                } else if (configuration.isLabelIRI(p) && configuration.mapsToLabel(o)) {
                    return getVarLabelClass(s, p, o, configuration, gen);
                } else if (configuration.mapsToEdgeLabel(p) && configuration.mapsToNode(o)){
                    return getVarRelationshipNode(s, p, o, configuration, gen);
                } else {
                    return null;
                }
            }
            else if (o.isVariable()) {
                if (configuration.isLabelIRI(p) && configuration.mapsToNode(s)) {
                    return getNodeLabelVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToProperty(p) && configuration.mapsToNode(s)) {
                    return getNodePropertyVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToEdgeLabel(p) && configuration.mapsToNode(s)) {
                    return getNodeRelationshipVar(s, p, o, configuration, gen);
                } else {
                    return null;
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
                    return null;
                }
            }
        } else if (nbOfVars == 2) {
            if (s.isVariable() && o.isVariable()) {
                if (configuration.isLabelIRI(p)) {
                    return getVarLabelVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToEdgeLabel(p)) {
                    return getVarRelationshipVar(s, p, o, configuration, gen);
                } else if (configuration.mapsToProperty(p)) {
                    return getVarPropertyVar(s, p, o, configuration, gen, certainNodes);
                } else {
                    return null;
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
        return null;
    }

    private static int getNumberOfVars(final Node s, final Node p, final Node o) {
        int count = 0;
        if (s.isVariable())
            count++;
        if (p.isVariable())
            count++;
        if (o.isVariable())
            count++;
        return count;
    }

    protected static CypherQuery getVarPropertyLiteral(final Node s, final Node p, final Node o,
                                                    final LPG2RDFConfiguration configuration,
                                                    final CypherVarGenerator gen,
                                                    final Set<Node> certainNodes) {
        final String property = configuration.unmapProperty(p.getURI());
        final String literal = o.getLiteralValue().toString();
        final CypherVar svar = gen.getVarFor(s);
        final CypherMatchQuery q = new CypherQueryBuilder()
                                    .addMatch(new NodeMatchClause(svar))
                                    .addCondition(new PropertyValueCondition(svar, property, literal))
                                    .addReturn(new VariableReturnStatement(svar, gen.getRetVar(s)))
                                    .build();
        if (certainNodes.contains(s)) {
            return q;
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        return new CypherUnionQueryImpl(
                q,
                new CypherQueryBuilder()
                        .addMatch(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .addCondition(new PropertyValueCondition(sedge.get(1), property, literal))
                        .addReturn(new TripleMapReturnStatement(sedge.get(0), sedge.get(1), sedge.get(2), gen.getRetVar(s)))
                        .build());
    }

    protected static CypherQuery getVarLabelClass(final Node s, final Node p, final Node o,
                                               final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final String label = configuration.unmapNodeLabel(o);
        final CypherVar svar = gen.getVarFor(s);
        return new CypherQueryBuilder()
                .addMatch(new NodeMatchClause(svar))
                .addCondition(new NodeLabelCondition(svar, label))
                .addReturn(new VariableReturnStatement(svar, gen.getRetVar(s)))
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
        return new CypherQueryBuilder()
                .addMatch(new EdgeMatchClause(svar, evar, ovar))
                .addCondition(new NodeIDCondition(ovar, node.getId()))
                .addCondition(new EdgeLabelCondition(evar, relationship))
                .addReturn(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .build();
    }

    protected static CypherQuery getNodeLabelVar(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(o);
        return new CypherQueryBuilder()
                .addMatch(new NodeMatchClause(svar))
                .addCondition(new NodeIDCondition(svar, configuration.unmapNode(s.getURI()).getId()))
                .addReturn(new LabelsReturnStatement(svar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodePropertyVar(final Node s, final Node p, final Node o,
                                                 final LPG2RDFConfiguration configuration,
                                                 final CypherVarGenerator gen) {
        final String property = configuration.unmapProperty(p.getURI());
        final CypherVar var = gen.getAnonVar();
        return new CypherQueryBuilder()
                .addMatch(new NodeMatchClause(var))
                .addCondition(new NodeIDCondition(var, configuration.unmapNode(s.getURI()).getId()))
                .addCondition(new PropertyEXISTSCondition(var, property))
                .addReturn(new PropertyValueReturnStatement(var, property, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodeRelationshipVar(final Node s, final Node p, final Node o,
                                                     final LPG2RDFConfiguration configuration,
                                                     final CypherVarGenerator gen) {
        final CypherVar xvar = gen.getAnonVar();
        final CypherVar evar = gen.getAnonVar();
        final CypherVar yvar = gen.getVarFor(o);
        return new CypherQueryBuilder()
                .addMatch(new EdgeMatchClause(xvar, evar, yvar))
                .addCondition(new NodeIDCondition(xvar, configuration.unmapNode(s.getURI()).getId()))
                .addCondition(new EdgeLabelCondition(evar, configuration.unmapEdgeLabel(p.getURI())))
                .addReturn(new VariableReturnStatement(yvar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodeVarNode(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar xvar = gen.getAnonVar();
        final CypherVar evar = gen.getVarFor(p);
        final CypherVar yvar = gen.getAnonVar();
        return new CypherQueryBuilder()
                .addMatch(new EdgeMatchClause(xvar, evar, yvar))
                .addCondition(new NodeIDCondition(xvar, configuration.unmapNode(s.getURI()).getId()))
                .addCondition(new NodeIDCondition(yvar, configuration.unmapNode(o.getURI()).getId()))
                .addReturn(new VariableReturnStatement(evar, gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getNodeVarLiteral(final Node s, final Node p, final Node o,
                                                final LPG2RDFConfiguration configuration,
                                                final CypherVarGenerator gen) {
        final CypherVar xvar = gen.getAnonVar();
        return new CypherQueryBuilder()
                .addMatch(new NodeMatchClause(xvar))
                .addCondition(new NodeIDCondition(xvar, configuration.unmapNode(s.getURI()).getId()))
                .addReturn(new FilteredPropertiesReturnStatement(xvar, o.getLiteralValue().toString(), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getNodeVarLabel(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar node = gen.getAnonVar();
        return new CypherQueryBuilder()
                .addMatch(new NodeMatchClause(node))
                .addCondition(new NodeIDCondition(node, configuration.unmapNode(s.getURI()).getId()))
                .addCondition(new NodeLabelCondition(node, configuration.unmapNodeLabel(o)))
                .addReturn(new LiteralValueReturnStatement("label", gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getVarLabelVar(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(s);
        return new CypherQueryBuilder()
                .addMatch(new NodeMatchClause(svar))
                .addReturn(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .addReturn(new LabelsReturnStatement(svar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getVarRelationshipVar(final Node s, final Node p, final Node o,
                                                    final LPG2RDFConfiguration configuration,
                                                    final CypherVarGenerator gen) {
        final String relationship = configuration.unmapEdgeLabel(p.getURI());
        final CypherVar svar = gen.getVarFor(s);
        final CypherVar evar = gen.getAnonVar();
        final CypherVar ovar = gen.getVarFor(o);
        return new CypherQueryBuilder()
                .addMatch(new EdgeMatchClause(svar, evar, ovar))
                .addCondition(new EdgeLabelCondition(evar, relationship))
                .addReturn(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .addReturn(new VariableReturnStatement(ovar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getVarPropertyVar(final Node s, final Node p, final Node o,
                                                final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                                final Set<Node> certainNodes) {
        final String property = configuration.unmapProperty(p.getURI());
        final CypherVar svar = gen.getVarFor(s);
        final CypherMatchQuery q = new CypherQueryBuilder()
                                        .addMatch(new NodeMatchClause(svar))
                                        .addCondition(new PropertyEXISTSCondition(svar, property))
                                        .addReturn(new VariableReturnStatement(svar, gen.getRetVar(s)))
                                        .addReturn(new PropertyValueReturnStatement(svar, property, gen.getRetVar(o)))
                                        .build();
        if (certainNodes.contains(s)) {
            return q;
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .addMatch(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .addCondition(new PropertyEXISTSCondition(sedge.get(1), property))
                        .addReturn(new TripleMapReturnStatement(sedge.get(0), sedge.get(1), sedge.get(2), gen.getRetVar(s)))
                        .addReturn(new PropertyValueReturnStatement(sedge.get(1), property, gen.getRetVar(o)))
                        .build(),
                q
        );
    }

    protected static CypherQuery getVarVarLabel(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(s);
        return new CypherQueryBuilder()
                .addMatch(new NodeMatchClause(svar))
                .addCondition(new NodeLabelCondition(svar, configuration.unmapNodeLabel(o)))
                .addReturn(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .build();
    }

    protected static CypherQuery getVarVarNode(final Node s, final Node p, final Node o,
                                            final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(s);
        final CypherVar pvar = gen.getVarFor(p);
        final CypherVar yvar = gen.getAnonVar();
        return new CypherQueryBuilder()
                .addMatch(new EdgeMatchClause(svar, pvar, yvar))
                .addCondition(new NodeIDCondition(yvar, configuration.unmapNode(o.getURI()).getId()))
                .addReturn(new VariableReturnStatement(svar, gen.getRetVar(s)))
                .addReturn(new VariableReturnStatement(pvar, gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getVarVarLiteral(final Node s, final Node p, final Node o,
                                               final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                               final Set<Node> certainNodes) {
        final CypherVar svar = gen.getVarFor(s);
        final CypherMatchQuery q = new CypherQueryBuilder()
                                        .addMatch(new NodeMatchClause(svar))
                                        .addReturn(new VariableReturnStatement(svar, gen.getRetVar(s)))
                                        .addReturn(new FilteredPropertiesReturnStatement(svar, o.getLiteralValue().toString(), gen.getRetVar(p)))
                                        .build();
        if (certainNodes.contains(s)) {
            return q;
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .addMatch(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .addReturn(new TripleMapReturnStatement(sedge.get(0), sedge.get(1), sedge.get(2), gen.getRetVar(s)))
                        .addReturn(new FilteredPropertiesReturnStatement(sedge.get(1), o.getLiteralValue().toString(), gen.getRetVar(p)))
                        .build(),
                q
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
            return new CypherQueryBuilder()
                    //this rule in the paper uses pvar and ovar, not anonymous vars so this query can't be reused
                    .addMatch(new EdgeMatchClause(a1, pvar, ovar))
                    .addCondition(new NodeIDCondition(a1, node.getId()))
                    .addReturn(new VariableReturnStatement(pvar, gen.getRetVar(p)))
                    .addReturn(new VariableReturnStatement(ovar, gen.getRetVar(o)))
                    .build();
        }
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .addMatch(new NodeMatchClause(a1))
                        .addCondition(new NodeIDCondition(a1, node.getId()))
                        .addReturn(new LiteralValueReturnStatement("label", gen.getRetVar(p)))
                        .addReturn(new LabelsReturnStatement(a1, gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .addMatch(new NodeMatchClause(a2))
                        .addCondition(new NodeIDCondition(a2, node.getId()))
                        .addReturn(new PropertyListReturnStatement(a2, gen.getRetVar(p)))
                        .addReturn(new AllPropertyValuesReturnStatement(a2, gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .addMatch(new EdgeMatchClause(a3, a4, a5))
                        .addCondition(new NodeIDCondition(a3, node.getId()))
                        .addReturn(new VariableReturnStatement(a4, gen.getRetVar(p)))
                        .addReturn(new VariableReturnStatement(a5, gen.getRetVar(o)))
                        .build()
        );
    }

    protected static CypherQuery getVarVarVar(final Node s, final Node p, final Node o,
                                           final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                           final Set<Node> certainNodes) {
        final CypherVar a1 = gen.getAnonVar();
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar a3 = gen.getAnonVar();
        final CypherMatchQuery q = new CypherQueryBuilder()
                .addMatch(new EdgeMatchClause(a1, a2, a3))
                .addReturn(new VariableReturnStatement(a1, gen.getRetVar(s)))
                .addReturn(new VariableReturnStatement(a2, gen.getRetVar(p)))
                .addReturn(new VariableReturnStatement(a3, gen.getRetVar(o)))
                .build();
        if (certainNodes.contains(s) && certainNodes.contains(o)) {
            return q;
        }

        final CypherVar a4 = gen.getAnonVar();
        final CypherVar a5 = gen.getAnonVar();
        final CypherVar a6 = gen.getAnonVar();
        final CypherVar a7 = gen.getAnonVar();
        final CypherVar a8 = gen.getAnonVar();
        return new CypherUnionQueryImpl(
                q,
                new CypherQueryBuilder()
                        .addMatch(new NodeMatchClause(a4))
                        .addReturn(new VariableReturnStatement(a4, gen.getRetVar(s)))
                        .addReturn(new LiteralValueReturnStatement("label", gen.getRetVar(p)))
                        .addReturn(new LabelsReturnStatement(a4, gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .addMatch(new NodeMatchClause(a5))
                        .addReturn(new VariableReturnStatement(a5, gen.getRetVar(s)))
                        .addReturn(new PropertyListReturnStatement(a5, gen.getRetVar(p)))
                        .addReturn(new AllPropertyValuesReturnStatement(a5, gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .addMatch(new EdgeMatchClause(a6, a7, a8))
                        .addReturn(new TripleMapReturnStatement(a6, a7, a8, gen.getRetVar(s)))
                        .addReturn(new PropertyListReturnStatement(a7, gen.getRetVar(p)))
                        .addReturn(new AllPropertyValuesReturnStatement(a7, gen.getRetVar(o)))
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
            CypherVar var = innerVars.get(n.getName());
            if (var != null)
                return var;
            varCount++;
            var = new CypherVar(varPrefix + varCount);
            innerVars.put(n.getName(), var);
            return var;
        }

        public List<CypherVar> getEdgeVars(final Node n) {
            List<CypherVar> var = edgeVars.get(n.getName());
            if (var != null)
                return var;
            edgeCount++;
            final CypherVar source = new CypherVar("src" + edgeCount);
            final CypherVar edge = new CypherVar("edge" + edgeCount);
            final CypherVar target = new CypherVar("tgt" + edgeCount);
            final List<CypherVar> vars = new ArrayList<>(3);
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
            CypherVar var = retVars.get(n.getName());
            if (var != null)
                return var;
            retVarCount++;
            var = new CypherVar(retPrefix + retVarCount);
            retVars.put(n.getName(), var);
            return var;
        }
    }
}
