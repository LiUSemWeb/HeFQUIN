package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.SPARQLStar2CypherTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherQueryBuilder;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherQueryCombinator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherVarGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SPARQLStar2CypherTranslatorImpl implements SPARQLStar2CypherTranslator {

    @Override
    public Pair<CypherQuery, Map<CypherVar, Node>> translateBGP(final BGP bgp, final LPG2RDFConfiguration conf) {
        final Set<Node> certainNodes = new HashSet<>();
        final Set<Node> certainEdgeLabels = new HashSet<>();
        final Set<Node> certainNodeLabels = new HashSet<>();
        final Set<Node> certainPropertyNames = new HashSet<>();
        final Set<Node> certainPropertyValues = new HashSet<>();
        for (final TriplePattern tp : bgp.getTriplePatterns()) {
            final Node s = tp.asJenaTriple().getSubject();
            final Node p = tp.asJenaTriple().getPredicate();
            final Node o = tp.asJenaTriple().getObject();
            if (s.isVariable()) {
                if (conf.isLabelIRI(p) || conf.mapsToEdgeLabel(p) || conf.mapsToNode(o) || conf.mapsToLabel(o)) {
                    certainNodes.add(s);
                }
            }
            if (o.isVariable()) {
                if (conf.mapsToEdgeLabel(p)){
                    certainNodes.add(o);
                }
                if (conf.isLabelIRI(p)) {
                    certainNodeLabels.add(o);
                }
                if (conf.mapsToProperty(p) || s.isNodeTriple()) {
                    certainPropertyValues.add(o);
                }
            }
            if (p.isVariable()) {
                if (conf.mapsToNode(o)) {
                    certainEdgeLabels.add(p);
                }
                if (o.isLiteral() || s.isNodeTriple()) {
                    certainPropertyNames.add(p);
                }
            }
        }

        CypherQuery result = null;
        final CypherVarGenerator gen = new CypherVarGenerator();
        for (final TriplePattern tp : bgp.getTriplePatterns()) {
            CypherQuery tpTranslation = translateTriplePattern(tp, conf, gen, certainNodes,
                    certainEdgeLabels, certainNodeLabels, certainPropertyNames, certainPropertyValues).object1;
            if (result == null){
                result = tpTranslation;
            } else {
                result = CypherQueryCombinator.combine(result, tpTranslation, gen);
            }
        }
        return new Pair<>(result, gen.getReverseMap());
    }

    @Override
    public Pair<CypherQuery, Map<CypherVar, Node>> translateTriplePattern(TriplePattern tp, LPG2RDFConfiguration conf) {
        return translateTriplePattern(tp, conf, new CypherVarGenerator(), new HashSet<>(), new HashSet<>(), new HashSet<>(),
                new HashSet<>(), new HashSet<>());
    }

    @Override
    public Pair<CypherQuery, Map<CypherVar, Node>> translateTriplePattern(final TriplePattern tp,
                                                                          final LPG2RDFConfiguration conf,
                                                                          final CypherVarGenerator generator,
                                                                          final Set<Node> certainNodes,
                                                                          final Set<Node> certainEdgeLabels,
                                                                          final Set<Node> certainNodeLabels,
                                                                          final Set<Node> certainPropertyNames,
                                                                          final Set<Node> certainPropertyValues) {
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
            builder.add(new EqualityExpression(new PropertyAccessExpression(edgeVar, configuration.unmapProperty(p)),
                    new LiteralExpression(o.getLiteralValue().toString())));
        } else if (p.isVariable() && o.isLiteral()) {
            final CypherVar iterVar = gen.getAnonVar();
            builder.add(new UnwindIteratorImpl(k, new KeysExpression(edgeVar),
                    List.of(new EqualityExpression(new PropertyAccessWithVarExpression(edgeVar, k),
                                new LiteralExpression(o.getLiteralValue().toString()))),
                    List.of(k), iterVar))
                    .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)));
        } else if (configuration.mapsToProperty(p) && o.isVariable()) {
            builder.add(new EXISTSExpression(new PropertyAccessExpression(edgeVar, configuration.unmapProperty(p))));
            builder.add(new AliasedExpression(
                    new PropertyAccessExpression(edgeVar, configuration.unmapProperty(p)), gen.getRetVar(o)));
        } else if (p.isVariable() && o.isVariable()) {
            final CypherVar iterVar = gen.getAnonVar();
            builder.add(new UnwindIteratorImpl(k, new KeysExpression(edgeVar), null,
                    List.of(k, new PropertyAccessWithVarExpression(edgeVar, k)), iterVar))
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
                .add(new EqualityExpression(new VariableIDExpression(nodeVar),
                        new LiteralExpression(configuration.unmapNode(s).getId())))
                .add(new VariableLabelExpression(nodeVar, configuration.unmapNodeLabel(o)))
                .add(new AliasedExpression(new CountLargerThanZeroExpression(), gen.getAnonVar()))
                .build();
    }

    private static CypherQuery getNodePropertyLiteral(final Node s, final Node p, final Node o,
                                                      final LPG2RDFConfiguration configuration,
                                                      final CypherVarGenerator gen) {
        final CypherVar nodeVar = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(nodeVar))
                .add(new EqualityExpression(new VariableIDExpression(nodeVar),
                        new LiteralExpression(configuration.unmapNode(s).getId())))
                .add(new EqualityExpression(new PropertyAccessExpression(nodeVar, configuration.unmapProperty(p)),
                        new LiteralExpression(o.getLiteralValue().toString())))
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
                .add(new EqualityExpression(new VariableIDExpression(srcVar),
                        new LiteralExpression(configuration.unmapNode(s).getId())))
                .add(new VariableLabelExpression(edgeVar, configuration.unmapEdgeLabel(p)))
                .add(new EqualityExpression(new VariableIDExpression(tgtVar),
                        new LiteralExpression(configuration.unmapNode(o).getId())))
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
        if (certainNodes.contains(s)) {
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(svar))
                    .add(new EqualityExpression(new PropertyAccessExpression(svar, property),
                            new LiteralExpression(literal)))
                    .add(new AliasedExpression(svar, gen.getRetVar(s)))
                    .build();
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        final CypherVar marker = gen.getMarkerVar();
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(svar))
                        .add(new EqualityExpression(new PropertyAccessExpression(svar, property),
                                new LiteralExpression(literal)))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(svar, gen.getRetVar(s)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .add(new EqualityExpression(new PropertyAccessExpression(sedge.get(1), property),
                                new LiteralExpression(literal)))
                        .add(new MarkerExpression(1, marker))
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
                .add(new VariableLabelExpression(svar, label))
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
                .add(new EqualityExpression(new VariableIDExpression(ovar),
                        new LiteralExpression(node.getId())))
                .add(new VariableLabelExpression(evar, relationship))
                .add(new AliasedExpression(svar, gen.getRetVar(s)))
                .build();
    }

    protected static CypherQuery getNodeLabelVar(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(o);
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(svar))
                .add(new EqualityExpression(new VariableIDExpression(svar),
                        new LiteralExpression(configuration.unmapNode(s).getId())))
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
                .add(new EqualityExpression(new VariableIDExpression(var),
                        new LiteralExpression(configuration.unmapNode(s).getId())))
                .add(new EXISTSExpression(new PropertyAccessExpression(var, property)))
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
                .add(new EqualityExpression(new VariableIDExpression(xvar),
                        new LiteralExpression(configuration.unmapNode(s).getId())))
                .add(new VariableLabelExpression(evar, configuration.unmapEdgeLabel(p)))
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
                .add(new EqualityExpression(new VariableIDExpression(xvar),
                        new LiteralExpression(configuration.unmapNode(s).getId())))
                .add(new EqualityExpression(new VariableIDExpression(yvar),
                        new LiteralExpression(configuration.unmapNode(o).getId())))
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
                .add(new EqualityExpression(new VariableIDExpression(xvar),
                        new LiteralExpression(configuration.unmapNode(s).getId())))
                .add(new UnwindIteratorImpl(innerVar, new KeysExpression(xvar),
                        List.of(new EqualityExpression(
                                new PropertyAccessWithVarExpression(xvar, innerVar),
                                new LiteralExpression(literal))),
                        List.of(innerVar), iterVar))
                .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getNodeVarLabel(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar node = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(node))
                .add(new EqualityExpression(new VariableIDExpression(node),
                        new LiteralExpression(configuration.unmapNode(s).getId())))
                .add(new VariableLabelExpression(node, configuration.unmapNodeLabel(o)))
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
                .add(new VariableLabelExpression(evar, relationship))
                .add(new AliasedExpression(svar, gen.getRetVar(s)))
                .add(new AliasedExpression(ovar, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getVarPropertyVar(final Node s, final Node p, final Node o,
                                                final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                                final Set<Node> certainNodes) {
        final String property = configuration.unmapProperty(p);
        final CypherVar svar = gen.getVarFor(s);
        if (certainNodes.contains(s)) {
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(svar))
                    .add(new EXISTSExpression(new PropertyAccessExpression(svar, property)))
                    .add(new AliasedExpression(svar, gen.getRetVar(s)))
                    .add(new AliasedExpression(new PropertyAccessExpression(svar, property),
                            gen.getRetVar(o)))
                    .build();
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        final CypherVar marker = gen.getMarkerVar();
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .add(new EXISTSExpression(new PropertyAccessExpression(sedge.get(1), property)))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(new TripleMapExpression(sedge.get(0), sedge.get(1), sedge.get(2)), gen.getRetVar(s)))
                        .add(new AliasedExpression(new PropertyAccessExpression(sedge.get(1), property), gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(svar))
                        .add(new EXISTSExpression(new PropertyAccessExpression(svar, property)))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(svar, gen.getRetVar(s)))
                        .add(new AliasedExpression(new PropertyAccessExpression(svar, property),
                                gen.getRetVar(o)))
                        .build()
        );
    }

    protected static CypherQuery getVarVarLabel(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar svar = gen.getVarFor(s);
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(svar))
                .add(new VariableLabelExpression(svar, configuration.unmapNodeLabel(o)))
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
                .add(new EqualityExpression(new VariableIDExpression(yvar),
                        new LiteralExpression(configuration.unmapNode(o).getId())))
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
        if (certainNodes.contains(s)) {
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(svar))
                    .add(new UnwindIteratorImpl(innerVar, new KeysExpression(svar),
                            List.of(new EqualityExpression(
                                    new PropertyAccessWithVarExpression(svar, innerVar),
                                    new LiteralExpression(literal))),
                            List.of(innerVar), iterVar))
                    .add(new AliasedExpression(svar, gen.getRetVar(s)))
                    .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)))
                    .build();
        }
        final List<CypherVar> sedge = gen.getEdgeVars(s);
        final CypherVar marker = gen.getMarkerVar();
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(sedge.get(0), sedge.get(1), sedge.get(2)))
                        .add(new UnwindIteratorImpl(innerVar, new KeysExpression(sedge.get(1)),
                                List.of(new EqualityExpression(
                                        new PropertyAccessWithVarExpression(sedge.get(1), innerVar),
                                        new LiteralExpression(literal))), List.of(innerVar), iterVar2))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(new TripleMapExpression(sedge.get(0), sedge.get(1), sedge.get(2)),
                                gen.getRetVar(s)))
                        .add(new AliasedExpression(new GetItemExpression(iterVar2 , 0), gen.getRetVar(p)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(svar))
                        .add(new UnwindIteratorImpl(innerVar, new KeysExpression(svar),
                                List.of(new EqualityExpression(
                                        new PropertyAccessWithVarExpression(svar, innerVar),
                                        new LiteralExpression(literal))),
                                List.of(innerVar), iterVar))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(svar, gen.getRetVar(s)))
                        .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)))
                        .build()
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
        //In general, we shouldn't reuse parts of queries, because it messes with the order of anonymous variables
        if (certainNodes.contains(o) || isEdgeCompatible){
            return new CypherQueryBuilder()
                    .add(new EdgeMatchClause(a1, pvar, ovar))
                    .add(new EqualityExpression(new VariableIDExpression(a1),
                            new LiteralExpression(node.getId())))
                    .add(new AliasedExpression(new TypeExpression(pvar), gen.getRetVar(p)))
                    .add(new AliasedExpression(ovar, gen.getRetVar(o)))
                    .build();
        }
        if (certainNodeLabels.contains(o)){
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(a1))
                    .add(new EqualityExpression(new VariableIDExpression(a1),
                            new LiteralExpression(node.getId())))
                    .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                    .add(new AliasedExpression(new LabelsExpression(a1), gen.getRetVar(o)))
                    .build();
        }
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar innerVar = new CypherVar("k");
        if (certainPropertyNames.contains(p) || certainPropertyValues.contains(o)){
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(a1))
                    .add(new EqualityExpression(new VariableIDExpression(a1),
                            new LiteralExpression(node.getId())))
                    .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a1), null,
                            List.of(innerVar, new PropertyAccessWithVarExpression(a1, innerVar)), a2))
                    .add(new AliasedExpression(new GetItemExpression(a2, 0), gen.getRetVar(p)))
                    .add(new AliasedExpression(new GetItemExpression(a2, 1), gen.getRetVar(o)))
                    .build();
        }
        final CypherVar a3 = gen.getAnonVar();
        if (certainEdgeLabels.contains(p)){
            return new CypherQueryBuilder()
                    .add(new EdgeMatchClause(a1, a2, a3))
                    .add(new EqualityExpression(new VariableIDExpression(a1),
                            new LiteralExpression(node.getId())))
                    .add(new AliasedExpression(new TypeExpression(a2), gen.getRetVar(p)))
                    .add(new AliasedExpression(a3, gen.getRetVar(o)))
                    .build();
        }
        if (certainEdgeLabels.contains(p)){
            return new CypherQueryBuilder()
                    .add(new EdgeMatchClause(a1, a2, a3))
                    .add(new EqualityExpression(new VariableIDExpression(a1),
                            new LiteralExpression(node.getId())))
                    .add(new AliasedExpression(new TypeExpression(a2), gen.getRetVar(p)))
                    .add(new AliasedExpression(a3, gen.getRetVar(o)))
                    .build();
        }
        final CypherVar a4 = gen.getAnonVar();
        final CypherVar a5 = gen.getAnonVar();
        final CypherVar a6 = gen.getAnonVar();
        final CypherVar marker = gen.getMarkerVar();
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new VariableIDExpression(a1),
                                new LiteralExpression(node.getId())))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                        .add(new AliasedExpression(new LabelsExpression(a1), gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a2))
                        .add(new EqualityExpression(new VariableIDExpression(a2),
                                new LiteralExpression(node.getId())))
                        .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a2), null,
                                List.of(innerVar, new PropertyAccessWithVarExpression(a2, innerVar)), a3))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(new GetItemExpression(a3, 0), gen.getRetVar(p)))
                        .add(new AliasedExpression(new GetItemExpression(a3, 1), gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a4, a5, a6))
                        .add(new EqualityExpression(new VariableIDExpression(a4),
                                new LiteralExpression(node.getId())))
                        .add(new MarkerExpression(2, marker))
                        .add(new AliasedExpression(new TypeExpression(a5), gen.getRetVar(p)))
                        .add(new AliasedExpression(a6, gen.getRetVar(o)))
                        .build());
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
        if ((certainNodes.contains(s) && certainNodes.contains(o)) || certainEdgeLabels.contains(p)
        || isEdgeCompatible) {
            return new CypherQueryBuilder()
                    .add(new EdgeMatchClause(a1, a2, a3))
                    .add(new AliasedExpression(a1, gen.getRetVar(s)))
                    .add(new AliasedExpression(new TypeExpression(a2), gen.getRetVar(p)))
                    .add(new AliasedExpression(a3, gen.getRetVar(o)))
                    .build();
        }

        final CypherVar a4 = gen.getAnonVar();
        if (certainNodeLabels.contains(o)) {
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(a4))
                    .add(new AliasedExpression(a4, gen.getRetVar(s)))
                    .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                    .add(new AliasedExpression(new LabelsExpression(a4), gen.getRetVar(o)))
                    .build();
        }

        final CypherVar a5 = gen.getAnonVar();
        final CypherVar a6 = gen.getAnonVar();
        final CypherVar innerVar = new CypherVar("k");
        final CypherVar marker = gen.getMarkerVar();
        if (certainNodes.contains(s)){
            return new CypherUnionQueryImpl(
                    new CypherQueryBuilder()
                            .add(new EdgeMatchClause(a1, a2, a3))
                            .add(new MarkerExpression(0, marker))
                            .add(new AliasedExpression(a1, gen.getRetVar(s)))
                            .add(new AliasedExpression(new TypeExpression(a2), gen.getRetVar(p)))
                            .add(new AliasedExpression(a3, gen.getRetVar(o)))
                            .build(),
                    new CypherQueryBuilder()
                            .add(new NodeMatchClause(a4))
                            .add(new MarkerExpression(1, marker))
                            .add(new AliasedExpression(a4, gen.getRetVar(s)))
                            .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                            .add(new AliasedExpression(new LabelsExpression(a4), gen.getRetVar(o)))
                            .build(),
                    new CypherQueryBuilder()
                            .add(new NodeMatchClause(a5))
                            .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a5), null,
                                    List.of(innerVar, new PropertyAccessWithVarExpression(a5, innerVar)), a6))
                            .add(new MarkerExpression(2, marker))
                            .add(new AliasedExpression(a5, gen.getRetVar(s)))
                            .add(new AliasedExpression(new GetItemExpression(a6, 0), gen.getRetVar(p)))
                            .add(new AliasedExpression(new GetItemExpression(a6, 1), gen.getRetVar(o)))
                            .build());
        }
        if (certainPropertyNames.contains(p) || certainPropertyValues.contains(o)) {
            return new CypherUnionQueryImpl(
                    new CypherQueryBuilder()
                            .add(new NodeMatchClause(a1))
                            .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a1), null,
                                    List.of(innerVar, new PropertyAccessWithVarExpression(a1, innerVar)), a2))
                            .add(new MarkerExpression(0, marker))
                            .add(new AliasedExpression(a1, gen.getRetVar(s)))
                            .add(new AliasedExpression(new GetItemExpression(a2, 0), gen.getRetVar(p)))
                            .add(new AliasedExpression(new GetItemExpression(a2, 1), gen.getRetVar(o)))
                            .build(),
                    new CypherQueryBuilder()
                            .add(new EdgeMatchClause(a3, a4, a5))
                            .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a4), null,
                                    List.of(innerVar, new PropertyAccessWithVarExpression(a4, innerVar)), a6))
                            .add(new MarkerExpression(1, marker))
                            .add(new AliasedExpression(new TripleMapExpression(a3, a4, a5), gen.getRetVar(s)))
                            .add(new AliasedExpression(new GetItemExpression(a6, 0), gen.getRetVar(p)))
                            .add(new AliasedExpression(new GetItemExpression(a6, 1), gen.getRetVar(o)))
                            .build());
        }

        final CypherVar a7 = gen.getAnonVar();
        final CypherVar a8 = gen.getAnonVar();
        final CypherVar a9 = gen.getAnonVar();
        final CypherVar a10 = gen.getAnonVar();
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a1, a2, a3))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(a1, gen.getRetVar(s)))
                        .add(new AliasedExpression(new TypeExpression(a2), gen.getRetVar(p)))
                        .add(new AliasedExpression(a3, gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a4))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(a4, gen.getRetVar(s)))
                        .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                        .add(new AliasedExpression(new LabelsExpression(a4), gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a5))
                        .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a5), null,
                                List.of(innerVar, new PropertyAccessWithVarExpression(a5, innerVar)), a6))
                        .add(new MarkerExpression(2, marker))
                        .add(new AliasedExpression(a5, gen.getRetVar(s)))
                        .add(new AliasedExpression(new GetItemExpression(a6, 0), gen.getRetVar(p)))
                        .add(new AliasedExpression(new GetItemExpression(a6, 1), gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a7, a8, a9))
                        .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a8), null,
                                List.of(innerVar, new PropertyAccessWithVarExpression(a8, innerVar)), a10))
                        .add(new MarkerExpression(3, marker))
                        .add(new AliasedExpression(new TripleMapExpression(a7, a8, a9), gen.getRetVar(s)))
                        .add(new AliasedExpression(new GetItemExpression(a10, 0), gen.getRetVar(p)))
                        .add(new AliasedExpression(new GetItemExpression(a10, 1), gen.getRetVar(o)))
                        .build());
    }
}
