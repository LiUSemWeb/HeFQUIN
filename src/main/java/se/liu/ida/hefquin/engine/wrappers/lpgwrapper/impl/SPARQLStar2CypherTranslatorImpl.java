package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.SPARQLStar2CypherTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.UnwindIteratorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherQueryBuilder;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherVarGenerator;

import java.util.*;

public class SPARQLStar2CypherTranslatorImpl implements SPARQLStar2CypherTranslator {

    @Override
    public Pair<CypherQuery, Map<CypherVar, Node>> translateTriplePattern(TriplePattern tp, LPG2RDFConfiguration conf) {
        return translateTriplePattern(tp, conf, new HashSet<>(), new HashSet<>(), new HashSet<>(),
                new HashSet<>(), new HashSet<>());
    }

    @Override
    public Pair<CypherQuery, Map<CypherVar, Node>> translateTriplePattern(final TriplePattern tp,
                                                                          final LPG2RDFConfiguration conf,
                                                                          final Set<Node> certainNodes,
                                                                          final Set<Node> certainEdgeLabels,
                                                                          final Set<Node> certainNodeLabels,
                                                                          final Set<Node> certainPropertyNames,
                                                                          final Set<Node> certainPropertyValues) {
        final CypherVarGenerator generator = new CypherVarGenerator();
        return new Pair<>(handleTriplePattern(tp, conf, generator, certainNodes, certainEdgeLabels,
                certainNodeLabels, certainPropertyNames, certainPropertyValues), generator.getReverseMap());
    }

    /**
     * This method handles the translation of a given triple pattern, distinguishing the cases where the
     * triple pattern is nested or non-nested. If the triple pattern is non-nested, this method returns
     * the direct translation of it. If the triple pattern is nested, it first translates the embedded
     * triple pattern, and then adds conditions, iterators or return statements depending on the case.
     */
    protected static CypherQuery handleTriplePattern(final TriplePattern pattern,
                                                     final LPG2RDFConfiguration configuration,
                                                     final CypherVarGenerator gen,
                                                     final Set<Node> certainNodes,
                                                     final Set<Node> certainEdgeLabels,
                                                     final Set<Node> certainNodeLabels,
                                                     final Set<Node> certainPropertyNames,
                                                     final Set<Node> certainPropertyValues) {
        final Triple b = pattern.asJenaTriple();
        final Node s = b.getSubject();
        if (!s.isNodeTriple()) {
            return translateTriplePattern(pattern, configuration, gen, certainNodes, certainEdgeLabels,
                    certainNodeLabels, certainPropertyNames, certainPropertyValues, false);
        }
        final CypherQuery translation = translateTriplePattern(new TriplePatternImpl(s.getTriple()),
                configuration, gen, certainNodes, certainEdgeLabels, certainNodeLabels, certainPropertyNames,
                certainPropertyValues, true);
        if (!(translation instanceof CypherMatchQuery)){
            return null; //TODO throw exception
        }
        final CypherMatchQuery innerTPTranslation = (CypherMatchQuery) translation;
        final Node p = b.getPredicate();
        final Node o = b.getObject();

        final CypherVar edgeVar = ((EdgeMatchClause) innerTPTranslation.getMatches().get(0)).getEdge();

        final CypherQueryBuilder builder = new CypherQueryBuilder().addAll(innerTPTranslation);
        final CypherVar k = new CypherVar("k");
        if (configuration.mapsToProperty(p) && o.isLiteral()) {
            builder.add(new PropertyValueCondition(edgeVar, configuration.unmapProperty(p),
                    o.getLiteralValue().toString()));
        } else if (p.isVariable() && o.isLiteral()) {
            final CypherVar iterVar = gen.getAnonVar();
            builder.add(new UnwindIteratorImpl(k, "KEYS("+edgeVar+")",
                    List.of(new PropertyValueConditionWithVar(edgeVar, k, o.getLiteralValue().toString())),
                    List.of("k"), iterVar))
                    .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)));
        } else if (configuration.mapsToProperty(p) && o.isVariable()) {
            builder.add(new PropertyEXISTSCondition(edgeVar, configuration.unmapProperty(p)));
            builder.add(new AliasedExpression(
                    new PropertyAccessExpression(edgeVar, configuration.unmapProperty(p)), gen.getRetVar(o)));
        } else if (p.isVariable() && o.isVariable()) {
            final CypherVar iterVar = gen.getAnonVar();
            builder.add(new UnwindIteratorImpl(k, "KEYS("+edgeVar+")", null,
                    List.of("k", edgeVar+"[k]"), iterVar))
                    .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)))
                    .add(new AliasedExpression(new GetItemExpression(iterVar, 1), gen.getRetVar(o)));
        } else {
            return null;
        }
        return builder.build();
    }

    /**
     * This method translates non-nested triple patterns into Cypher, leveraging knowledge of properties that
     * the variables in the triple pattern may hold like boundedness to given LPG elements or edge-compatibility.
     */
    protected static CypherQuery translateTriplePattern(final TriplePattern pattern,
                                                      final LPG2RDFConfiguration configuration,
                                                      final CypherVarGenerator gen,
                                                      final Set<Node> certainNodes,
                                                      final Set<Node> certainEdgeLabels,
                                                      final Set<Node> certainNodeLabels,
                                                      final Set<Node> certainPropertyNames,
                                                      final Set<Node> certainPropertyValues,
                                                      final boolean isEdgeCompatible) {
        final Triple b = pattern.asJenaTriple();
        final Node s = b.getSubject();
        final Node p = b.getPredicate();
        final Node o = b.getObject();
        final int nbOfVars = QueryPatternUtils.getNumberOfVarOccurrences(pattern);
        if (nbOfVars == 0) {
            if (configuration.mapsToNode(s) && configuration.isLabelIRI(p) && configuration.mapsToLabel(o)){
                return getNodeLabelLabel(s, p, o, configuration, gen);
            }
            else if (configuration.mapsToNode(s) && configuration.mapsToProperty(p) && o.isLiteral()) {
                return getNodePropertyLiteral(s, p, o, configuration, gen);
            }
            else if (configuration.mapsToNode(s) && configuration.mapsToEdgeLabel(p) && configuration.mapsToNode(o)){
                return getNodeRelationshipNode(s, p, o, configuration, gen);
            }
            else {
                return null;
            }
        }
        else if (nbOfVars == 1){
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
            }
            else if (s.isVariable() && p.isVariable()) {
                if (configuration.mapsToLabel(o)) {
                    return getVarVarLabel(s, p, o, configuration, gen);
                } else if (configuration.mapsToNode(o)) {
                    return getVarVarNode(s, p, o, configuration, gen);
                } else if (o.isLiteral()) {
                    return getVarVarLiteral(s, p, o, configuration, gen, certainNodes);
                }
            }
            else if(p.isVariable() && o.isVariable()) {
                if (configuration.mapsToNode(s)) {
                    return getNodeVarVar(s, p, o, configuration, gen, certainNodes, certainNodeLabels,
                            certainPropertyNames, certainPropertyValues, certainEdgeLabels, isEdgeCompatible);
                }
            }
        } else {
            return getVarVarVar(s, p, o, configuration, gen, certainNodes, certainNodeLabels,
                    certainPropertyNames, certainPropertyValues, certainEdgeLabels, isEdgeCompatible);
        }
        return null;
    }

    private static CypherQuery getNodeLabelLabel(final Node s, final Node p, final Node o,
                                                 final LPG2RDFConfiguration configuration,
                                                 final CypherVarGenerator gen) {
        final CypherVar nodeVar = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(nodeVar))
                .add(new NodeIDCondition(nodeVar, configuration.unmapNode(s).getId()))
                .add(new NodeLabelCondition(nodeVar, configuration.unmapNodeLabel(o)))
                .add(new AliasedExpression(new CountLargerThanZeroExpression(), gen.getAnonVar()))
                .build();
    }

    private static CypherQuery getNodePropertyLiteral(final Node s, final Node p, final Node o,
                                                      final LPG2RDFConfiguration configuration,
                                                      final CypherVarGenerator gen) {
        final CypherVar nodeVar = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(nodeVar))
                .add(new NodeIDCondition(nodeVar, configuration.unmapNode(s).getId()))
                .add(new PropertyValueCondition(nodeVar, configuration.unmapProperty(p), o.getLiteralValue().toString()))
                .add(new AliasedExpression(new CountLargerThanZeroExpression(), gen.getAnonVar()))
                .build();
    }

    private static CypherQuery getNodeRelationshipNode(final Node s, final Node p, final Node o,
                                                       final LPG2RDFConfiguration configuration,
                                                       final CypherVarGenerator gen) {
        final CypherVar srcVar = gen.getAnonVar();
        final CypherVar edgeVar = gen.getAnonVar();
        final CypherVar tgtVar = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(srcVar, edgeVar, tgtVar))
                .add(new NodeIDCondition(srcVar, configuration.unmapNode(s).getId()))
                .add(new EdgeLabelCondition(edgeVar, configuration.unmapEdgeLabel(p)))
                .add(new NodeIDCondition(tgtVar, configuration.unmapNode(o).getId()))
                .add(new AliasedExpression(new CountLargerThanZeroExpression(), gen.getAnonVar()))
                .build();
    }

    protected static CypherQuery getVarPropertyLiteral(final Node s, final Node p, final Node o,
                                                    final LPG2RDFConfiguration configuration,
                                                    final CypherVarGenerator gen,
                                                    final Set<Node> certainNodes) {
        final String property = configuration.unmapProperty(p);
        final String literal = o.getLiteralValue().toString();
        final CypherVar svar = gen.getVarFor(s);
        final CypherMatchQuery q = new CypherQueryBuilder()
                                    .add(new NodeMatchClause(svar))
                                    .add(new PropertyValueCondition(svar, property, literal))
                                    .add(new AliasedExpression(svar, gen.getRetVar(s)))
                                    .build();
        if (certainNodes.contains(s)) {
            return q;
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        return new CypherUnionQueryImpl(
                q,
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .add(new PropertyValueCondition(sedge.get(1), property, literal))
                        .add(new AliasedExpression(new TripleMapExpression(sedge.get(0), sedge.get(1), sedge.get(2)),
                                gen.getRetVar(s)))
                        .build());
    }

    protected static CypherQuery getVarLabelClass(final Node s, final Node p, final Node o,
                                               final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final String label = configuration.unmapNodeLabel(o);
        final CypherVar svar = gen.getVarFor(s);
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(svar))
                .add(new NodeLabelCondition(svar, label))
                .add(new AliasedExpression(svar, gen.getRetVar(s)))
                .build();
    }

    protected static CypherQuery getVarRelationshipNode(final Node s, final Node p, final Node o,
                                                     final LPG2RDFConfiguration configuration,
                                                     final CypherVarGenerator gen) {
        final String relationship = configuration.unmapEdgeLabel(p);
        final LPGNode node = configuration.unmapNode(o);
        final CypherVar svar = gen.getVarFor(s);
        final CypherVar evar = gen.getAnonVar();
        final CypherVar ovar = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(svar, evar, ovar))
                .add(new NodeIDCondition(ovar, node.getId()))
                .add(new EdgeLabelCondition(evar, relationship))
                .add(new AliasedExpression(svar, gen.getRetVar(s)))
                .build();
    }

    protected static CypherQuery getNodeLabelVar(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(o);
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(svar))
                .add(new NodeIDCondition(svar, configuration.unmapNode(s).getId()))
                .add(new AliasedExpression(new LabelsExpression(svar), gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodePropertyVar(final Node s, final Node p, final Node o,
                                                 final LPG2RDFConfiguration configuration,
                                                 final CypherVarGenerator gen) {
        final String property = configuration.unmapProperty(p);
        final CypherVar var = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(var))
                .add(new NodeIDCondition(var, configuration.unmapNode(s).getId()))
                .add(new PropertyEXISTSCondition(var, property))
                .add(new AliasedExpression(new PropertyAccessExpression(var, property), gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodeRelationshipVar(final Node s, final Node p, final Node o,
                                                     final LPG2RDFConfiguration configuration,
                                                     final CypherVarGenerator gen) {
        final CypherVar xvar = gen.getAnonVar();
        final CypherVar evar = gen.getAnonVar();
        final CypherVar yvar = gen.getVarFor(o);
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(xvar, evar, yvar))
                .add(new NodeIDCondition(xvar, configuration.unmapNode(s).getId()))
                .add(new EdgeLabelCondition(evar, configuration.unmapEdgeLabel(p)))
                .add(new AliasedExpression(yvar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodeVarNode(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar xvar = gen.getAnonVar();
        final CypherVar evar = gen.getVarFor(p);
        final CypherVar yvar = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(xvar, evar, yvar))
                .add(new NodeIDCondition(xvar, configuration.unmapNode(s).getId()))
                .add(new NodeIDCondition(yvar, configuration.unmapNode(o).getId()))
                .add(new AliasedExpression(new TypeExpression(evar), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getNodeVarLiteral(final Node s, final Node p, final Node o,
                                                final LPG2RDFConfiguration configuration,
                                                final CypherVarGenerator gen) {
        final CypherVar xvar = gen.getAnonVar();
        final String literal = o.getLiteralValue().toString();
        final CypherVar iterVar = gen.getAnonVar();
        final CypherVar innerVar = new CypherVar("k");
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(xvar))
                .add(new NodeIDCondition(xvar, configuration.unmapNode(s).getId()))
                .add(new UnwindIteratorImpl(innerVar, "KEYS("+xvar+")",
                        Collections.singletonList(new PropertyValueConditionWithVar(xvar, innerVar, literal)),
                        Collections.singletonList("k"), iterVar))
                .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getNodeVarLabel(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar node = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(node))
                .add(new NodeIDCondition(node, configuration.unmapNode(s).getId()))
                .add(new NodeLabelCondition(node, configuration.unmapNodeLabel(o)))
                .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getVarLabelVar(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(s);
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(svar))
                .add(new AliasedExpression(svar, gen.getRetVar(s)))
                .add(new AliasedExpression(new LabelsExpression(svar), gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getVarRelationshipVar(final Node s, final Node p, final Node o,
                                                    final LPG2RDFConfiguration configuration,
                                                    final CypherVarGenerator gen) {
        final String relationship = configuration.unmapEdgeLabel(p);
        final CypherVar svar = gen.getVarFor(s);
        final CypherVar evar = gen.getAnonVar();
        final CypherVar ovar = gen.getVarFor(o);
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(svar, evar, ovar))
                .add(new EdgeLabelCondition(evar, relationship))
                .add(new AliasedExpression(svar, gen.getRetVar(s)))
                .add(new AliasedExpression(ovar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getVarPropertyVar(final Node s, final Node p, final Node o,
                                                final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                                final Set<Node> certainNodes) {
        final String property = configuration.unmapProperty(p);
        final CypherVar svar = gen.getVarFor(s);
        final CypherMatchQuery q = new CypherQueryBuilder()
                                        .add(new NodeMatchClause(svar))
                                        .add(new PropertyEXISTSCondition(svar, property))
                                        .add(new AliasedExpression(svar, gen.getRetVar(s)))
                                        .add(new AliasedExpression(new PropertyAccessExpression(svar, property),
                                                gen.getRetVar(o)))
                                        .build();
        if (certainNodes.contains(s)) {
            return q;
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .add(new PropertyEXISTSCondition(sedge.get(1), property))
                        .add(new AliasedExpression(new TripleMapExpression(sedge.get(0), sedge.get(1), sedge.get(2)), gen.getRetVar(s)))
                        .add(new AliasedExpression(new PropertyAccessExpression(sedge.get(1), property), gen.getRetVar(o)))
                        .build(),
                q
        );
    }

    protected static CypherQuery getVarVarLabel(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(s);
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(svar))
                .add(new NodeLabelCondition(svar, configuration.unmapNodeLabel(o)))
                .add(new AliasedExpression(svar, gen.getRetVar(s)))
                .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getVarVarNode(final Node s, final Node p, final Node o,
                                            final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(s);
        final CypherVar pvar = gen.getVarFor(p);
        final CypherVar yvar = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(svar, pvar, yvar))
                .add(new NodeIDCondition(yvar, configuration.unmapNode(o).getId()))
                .add(new AliasedExpression(svar, gen.getRetVar(s)))
                .add(new AliasedExpression(new TypeExpression(pvar), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getVarVarLiteral(final Node s, final Node p, final Node o,
                                               final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                               final Set<Node> certainNodes) {
        final CypherVar svar = gen.getVarFor(s);
        final CypherVar innerVar = new CypherVar("k");
        final CypherVar iterVar = gen.getAnonVar();
        final CypherVar iterVar2 = gen.getAnonVar();
        final String literal = o.getLiteralValue().toString();
        final CypherMatchQuery q = new CypherQueryBuilder()
                                        .add(new NodeMatchClause(svar))
                                        .add(new UnwindIteratorImpl(innerVar, "KEYS("+svar+")",
                                                List.of(new PropertyValueConditionWithVar(svar, innerVar, literal)),
                                                Collections.singletonList("k"), iterVar))
                                        .add(new AliasedExpression(svar, gen.getRetVar(s)))
                                        .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)))
                                        .build();
        if (certainNodes.contains(s)) {
            return q;
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .add(new UnwindIteratorImpl(innerVar, "KEYS("+sedge.get(1)+")",
                                List.of(new PropertyValueConditionWithVar(sedge.get(1), innerVar, literal)),
                                Collections.singletonList("k"), iterVar2))
                        .add(new AliasedExpression(new TripleMapExpression(sedge.get(0), sedge.get(1), sedge.get(2)),
                                gen.getRetVar(s)))
                        .add(new AliasedExpression(new GetItemExpression(iterVar2 , 0), gen.getRetVar(p)))
                        .build(),
                q
        );
    }

    protected static CypherQuery getNodeVarVar(final Node s, final Node p, final Node o,
                                               final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                               final Set<Node> certainNodes,
                                               final Set<Node> certainNodeLabels,
                                               final Set<Node> certainPropertyNames,
                                               final Set<Node> certainPropertyValues,
                                               final Set<Node> certainEdgeLabels,
                                               final boolean isEdgeCompatible) {
        final LPGNode node = configuration.unmapNode(s);
        final CypherVar pvar = gen.getVarFor(p);
        final CypherVar ovar = gen.getVarFor(o);
        final CypherVar a1 = gen.getAnonVar();
        if (certainNodes.contains(o) || isEdgeCompatible){
            return new CypherQueryBuilder()
                    //this rule in the paper uses pvar and ovar, not anonymous vars so this query can't be reused
                    .add(new EdgeMatchClause(a1, pvar, ovar))
                    .add(new NodeIDCondition(a1, node.getId()))
                    .add(new AliasedExpression(new TypeExpression(pvar), gen.getRetVar(p)))
                    .add(new AliasedExpression(ovar, gen.getRetVar(o)))
                    .build();
        }

        final CypherVar a2 = gen.getAnonVar();
        final CypherMatchQuery qLabels = new CypherQueryBuilder()
                .add(new NodeMatchClause(a1))
                .add(new NodeIDCondition(a1, node.getId()))
                .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                .add(new AliasedExpression(new LabelsExpression(a1), gen.getRetVar(o)))
                .build();
        if (certainNodeLabels.contains(o)){
            return qLabels;
        }

        final CypherVar iterVar = gen.getAnonVar();
        final CypherVar a4 = gen.getAnonVar();
        final CypherVar a5 = gen.getAnonVar();
        final CypherVar a6 = gen.getAnonVar();
        final CypherMatchQuery qEdges = new CypherQueryBuilder()
                .add(new EdgeMatchClause(a4, a5, a6))
                .add(new NodeIDCondition(a4, node.getId()))
                .add(new AliasedExpression(new TypeExpression(a5), gen.getRetVar(p)))
                .add(new AliasedExpression(a6, gen.getRetVar(o)))
                .build();
        if (certainEdgeLabels.contains(p)){
            return qEdges;
        }

        final CypherVar innerVar = new CypherVar("k");
        final CypherMatchQuery qProperties = new CypherQueryBuilder()
                .add(new NodeMatchClause(a2))
                .add(new NodeIDCondition(a2, node.getId()))
                .add(new UnwindIteratorImpl(innerVar, "KEYS("+a2+")", null,
                        List.of("k", a2+"[k]"), iterVar))
                .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)))
                .add(new AliasedExpression(new GetItemExpression(iterVar, 1), gen.getRetVar(o)))
                .build();
        if (certainPropertyNames.contains(p) || certainPropertyValues.contains(o)){
            return qProperties;
        }

        return new CypherUnionQueryImpl(qLabels, qProperties, qEdges);
    }

    protected static CypherQuery getVarVarVar(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration,
                                              final CypherVarGenerator gen,
                                              final Set<Node> certainNodes,
                                              final Set<Node> certainNodeLabels,
                                              final Set<Node> certainPropertyNames,
                                              final Set<Node> certainPropertyValues,
                                              final Set<Node> certainEdgeLabels,
                                              final boolean isEdgeCompatible) {
        final CypherVar a1 = gen.getAnonVar();
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar a3 = gen.getAnonVar();
        final CypherMatchQuery qEdges = new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, a2, a3))
                .add(new AliasedExpression(a1, gen.getRetVar(s)))
                .add(new AliasedExpression(new TypeExpression(a2), gen.getRetVar(p)))
                .add(new AliasedExpression(a3, gen.getRetVar(o)))
                .build();
        if ((certainNodes.contains(s) && certainNodes.contains(o)) || certainEdgeLabels.contains(p)
        || isEdgeCompatible) {
            return qEdges;
        }

        final CypherVar a4 = gen.getAnonVar();
        final CypherMatchQuery qNodeLabels = new CypherQueryBuilder()
                .add(new NodeMatchClause(a4))
                .add(new AliasedExpression(a4, gen.getRetVar(s)))
                .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                .add(new AliasedExpression(new LabelsExpression(a4), gen.getRetVar(o)))
                .build();
        if (certainNodeLabels.contains(o)) {
            return qNodeLabels;
        }

        final CypherVar a5 = gen.getAnonVar();
        final CypherVar iterVar = gen.getAnonVar();
        final CypherVar a7 = gen.getAnonVar();
        final CypherVar a8 = gen.getAnonVar();
        final CypherVar a9 = gen.getAnonVar();
        final CypherVar iterVar2 = gen.getAnonVar();
        final CypherVar innerVar = new CypherVar("k");
        final CypherMatchQuery qNodeProperties = new CypherQueryBuilder()
                .add(new NodeMatchClause(a5))
                .add(new UnwindIteratorImpl(innerVar, "KEYS("+a5+")", null,
                        List.of("k", a5+"[k]"), iterVar))
                .add(new AliasedExpression(a5, gen.getRetVar(s)))
                .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)))
                .add(new AliasedExpression(new GetItemExpression(iterVar, 1), gen.getRetVar(o)))
                .build();
        final CypherMatchQuery qEdgeProperties = new CypherQueryBuilder()
                .add(new EdgeMatchClause(a7, a8, a9))
                .add(new UnwindIteratorImpl(innerVar, "KEYS("+a8+")", null,
                        List.of("k", a8+"[k]"), iterVar2))
                .add(new AliasedExpression(new TripleMapExpression(a7, a8, a9), gen.getRetVar(s)))
                .add(new AliasedExpression(new GetItemExpression(iterVar2, 0), gen.getRetVar(p)))
                .add(new AliasedExpression(new GetItemExpression(iterVar2, 1), gen.getRetVar(o)))
                .build();
        if (certainNodes.contains(s)){
            return new CypherUnionQueryImpl(qEdges, qNodeLabels, qNodeProperties);
        }
        if (certainPropertyNames.contains(p) || certainPropertyValues.contains(o)) {
            return new CypherUnionQueryImpl(qNodeProperties, qEdgeProperties);
        }
        return new CypherUnionQueryImpl(qEdges, qNodeLabels, qNodeProperties, qEdgeProperties);
    }
}
