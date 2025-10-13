package se.liu.ida.hefquin.engine.wrappers.lpg.impl;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import se.liu.ida.hefquin.engine.wrappers.lpg.SPARQL2CypherTranslationResult;
import se.liu.ida.hefquin.engine.wrappers.lpg.SPARQLStar2CypherTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpg.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.MatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.UnwindIterator;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.*;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.match.PathMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.*;

import java.util.*;
import java.util.stream.Collectors;

public class SPARQLStar2CypherTranslatorImpl implements SPARQLStar2CypherTranslator {

    @Override
    public SPARQL2CypherTranslationResult translateBGP(final Set<Triple> bgp, final LPG2RDFConfiguration conf,
                                                               final boolean naive) {
        final Set<Node> certainNodes = new HashSet<>();
        final Set<Node> certainEdgeLabels = new HashSet<>();
        final Set<Node> certainNodeLabels = new HashSet<>();
        final Set<Node> certainPropertyNames = new HashSet<>();
        final Set<Node> certainPropertyValues = new HashSet<>();
        if (!naive) {
            for ( final Triple tp : bgp ) {
                final Node s = tp.getSubject();
                final Node p = tp.getPredicate();
                final Node o = tp.getObject();
                if (s.isVariable()) {
                    if (conf.getLabelPredicate().equals(p) || conf.isIRIForEdgeLabel(p) || conf.isRDFTermForLPGNode(o) || conf.isRDFTermForNodeLabel(o)) {
                        certainNodes.add(s);
                    }
                }
                if (o.isVariable()) {
                    if (conf.isIRIForEdgeLabel(p)) {
                        certainNodes.add(o);
                    }
                    if (conf.getLabelPredicate().equals(p)) {
                        certainNodeLabels.add(o);
                    }
                    if (conf.isIRIForPropertyName(p) || s.isTripleTerm()) {
                        certainPropertyValues.add(o);
                    }
                }
                if (p.isVariable()) {
                    if (conf.isRDFTermForLPGNode(o)) {
                        certainEdgeLabels.add(p);
                    }
                    if (o.isLiteral() || s.isTripleTerm()) {
                        certainPropertyNames.add(p);
                    }
                }
            }
        }
        CypherQuery result = null;
        final CypherVarGenerator gen = new CypherVarGenerator();
        for ( final Triple tp : bgp ) {
            final SPARQL2CypherTranslationResult translationResult = translateTriplePattern(tp, conf, gen, certainNodes,
                    certainEdgeLabels, certainNodeLabels, certainPropertyNames, certainPropertyValues);
            final CypherQuery cypherForTP = translationResult.getCypherQuery();
            if (result == null){
                result = cypherForTP;
            } else {
                result = CypherQueryCombinator.combine(result, cypherForTP, gen);
            }
        }

        return new SPARQL2CypherTranslationResultImpl( result, gen.getReverseMap() );
    }

    @Override
    public SPARQL2CypherTranslationResult translateTriplePattern(Triple tp, LPG2RDFConfiguration conf) {
        return translateTriplePattern(tp, conf, new CypherVarGenerator(), new HashSet<>(), new HashSet<>(), new HashSet<>(),
                new HashSet<>(), new HashSet<>());
    }

    @Override
    public SPARQL2CypherTranslationResult translateTriplePattern( final Triple tp,
                                                                  final LPG2RDFConfiguration conf,
                                                                  final CypherVarGenerator generator,
                                                                  final Set<Node> certainNodes,
                                                                  final Set<Node> certainEdgeLabels,
                                                                  final Set<Node> certainNodeLabels,
                                                                  final Set<Node> certainPropertyNames,
                                                                  final Set<Node> certainPropertyValues) {
        return new SPARQL2CypherTranslationResultImpl(handleTriplePattern(tp, conf, generator, certainNodes, certainEdgeLabels,
                certainNodeLabels, certainPropertyNames, certainPropertyValues), generator.getReverseMap());
    }

    /**
     * This method handles the translation of a given triple pattern, distinguishing the cases where the
     * triple pattern is nested or non-nested. If the triple pattern is non-nested, this method returns
     * the direct translation of it. If the triple pattern is nested, it first translates the embedded
     * triple pattern, and then adds conditions, iterators or return statements depending on the case.
     */
    protected static CypherQuery handleTriplePattern(final Triple tp,
                                                     final LPG2RDFConfiguration configuration,
                                                     final CypherVarGenerator gen,
                                                     final Set<Node> certainNodes,
                                                     final Set<Node> certainEdgeLabels,
                                                     final Set<Node> certainNodeLabels,
                                                     final Set<Node> certainPropertyNames,
                                                     final Set<Node> certainPropertyValues) {
        final Node s = tp.getSubject();
        if ( ! s.isTripleTerm() ) {
            return translateTriplePattern(tp, configuration, gen, certainNodes, certainEdgeLabels,
                    certainNodeLabels, certainPropertyNames, certainPropertyValues, false);
        }
        final CypherQuery translation = translateTriplePattern(s.getTriple(),
                configuration, gen, certainNodes, certainEdgeLabels, certainNodeLabels, certainPropertyNames,
                certainPropertyValues, true);
        if (!(translation instanceof CypherMatchQuery)){
            return null; //TODO throw exception
        }
        final CypherMatchQuery innerTPTranslation = (CypherMatchQuery) translation;
        final Node p = tp.getPredicate();
        final Node o = tp.getObject();

        final CypherVar edgeVar = ((EdgeMatchClause) innerTPTranslation.getMatches().get(0)).getEdge();

        final CypherQueryBuilder builder = new CypherQueryBuilder().addAll(innerTPTranslation);
        final CypherVar k = new CypherVar("k");
        if (configuration.isIRIForPropertyName(p) && o.isLiteral()) {
            builder.add(new EqualityExpression(new PropertyAccessExpression(edgeVar, configuration.getPropertyNameForIRI(p)),
                    new LiteralExpression(o.getLiteralValue().toString())));
        } else if (p.isVariable() && o.isLiteral()) {
            final CypherVar iterVar = gen.getAnonVar();
            builder.add(new UnwindIteratorImpl(k, new KeysExpression(edgeVar),
                    List.of(new EqualityExpression(new PropertyAccessWithVarExpression(edgeVar, k),
                                new LiteralExpression(o.getLiteralValue().toString()))),
                    List.of(k), iterVar))
                    .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)));
        } else if (configuration.isIRIForPropertyName(p) && o.isVariable()) {
            builder.add(new EXISTSExpression(new PropertyAccessExpression(edgeVar, configuration.getPropertyNameForIRI(p))));
            builder.add(new AliasedExpression(
                    new PropertyAccessExpression(edgeVar, configuration.getPropertyNameForIRI(p)), gen.getRetVar(o)));
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
    protected static CypherQuery translateTriplePattern(final Triple tp,
                                                      final LPG2RDFConfiguration configuration,
                                                      final CypherVarGenerator gen,
                                                      final Set<Node> certainNodes,
                                                      final Set<Node> certainEdgeLabels,
                                                      final Set<Node> certainNodeLabels,
                                                      final Set<Node> certainPropertyNames,
                                                      final Set<Node> certainPropertyValues,
                                                      final boolean isEdgeCompatible) {
        final Node s = tp.getSubject();
        final Node p = tp.getPredicate();
        final Node o = tp.getObject();
        final int nbOfVars = getNumberOfVarOccurrences(tp);
        if (nbOfVars == 0) {
            if (configuration.isRDFTermForLPGNode(s) && configuration.getLabelPredicate().equals(p) && configuration.isRDFTermForNodeLabel(o)){
                return getNodeLabelLabel(s, p, o, configuration, gen);
            }
            else if (configuration.isRDFTermForLPGNode(s) && configuration.isIRIForPropertyName(p) && o.isLiteral()) {
                return getNodePropertyLiteral(s, p, o, configuration, gen);
            }
            else if (configuration.isRDFTermForLPGNode(s) && configuration.isIRIForEdgeLabel(p) && configuration.isRDFTermForLPGNode(o)){
                return getNodeRelationshipNode(s, p, o, configuration, gen);
            }
            else {
                return null;
            }
        }
        else if (nbOfVars == 1){
            if (s.isVariable()) {
                if (configuration.isIRIForPropertyName(p) && o.isLiteral()) {
                    return getVarPropertyLiteral(s, p, o, configuration, gen, certainNodes);
                } else if (configuration.getLabelPredicate().equals(p) && configuration.isRDFTermForNodeLabel(o)) {
                    return getVarLabelClass(s, p, o, configuration, gen);
                } else if (configuration.isIRIForEdgeLabel(p) && configuration.isRDFTermForLPGNode(o)){
                    return getVarRelationshipNode(s, p, o, configuration, gen);
                } else {
                    return null;
                }
            }
            else if (o.isVariable()) {
                if (configuration.getLabelPredicate().equals(p) && configuration.isRDFTermForLPGNode(s)) {
                    return getNodeLabelVar(s, p, o, configuration, gen);
                } else if (configuration.isIRIForPropertyName(p) && configuration.isRDFTermForLPGNode(s)) {
                    return getNodePropertyVar(s, p, o, configuration, gen);
                } else if (configuration.isIRIForEdgeLabel(p) && configuration.isRDFTermForLPGNode(s)) {
                    return getNodeRelationshipVar(s, p, o, configuration, gen);
                } else {
                    return null;
                }
            }
            else if (p.isVariable()) {
                if (configuration.isRDFTermForLPGNode(s) && configuration.isRDFTermForLPGNode(o)) {
                    return getNodeVarNode(s, p, o, configuration, gen);
                } else if (configuration.isRDFTermForLPGNode(s) && o.isLiteral()) {
                    return getNodeVarLiteral(s, p, o, configuration, gen);
                } else if (configuration.isRDFTermForLPGNode(s) && configuration.isRDFTermForNodeLabel(o)) {
                    return getNodeVarLabel(s, p, o, configuration, gen);
                } else {
                    return null;
                }
            }
        } else if (nbOfVars == 2) {
            if (s.isVariable() && o.isVariable()) {
                if (configuration.getLabelPredicate().equals(p)) {
                    return getVarLabelVar(s, p, o, configuration, gen);
                } else if (configuration.isIRIForEdgeLabel(p)) {
                    return getVarRelationshipVar(s, p, o, configuration, gen);
                } else if (configuration.isIRIForPropertyName(p)) {
                    return getVarPropertyVar(s, p, o, configuration, gen, certainNodes);
                } else {
                    return null;
                }
            }
            else if (s.isVariable() && p.isVariable()) {
                if (configuration.isRDFTermForNodeLabel(o)) {
                    return getVarVarLabel(s, p, o, configuration, gen);
                } else if (configuration.isRDFTermForLPGNode(o)) {
                    return getVarVarNode(s, p, o, configuration, gen);
                } else if (o.isLiteral()) {
                    return getVarVarLiteral(s, p, o, configuration, gen, certainNodes);
                }
            }
            else if(p.isVariable() && o.isVariable()) {
                if (configuration.isRDFTermForLPGNode(s)) {
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
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(s).getId(), XSDDatatype.XSDinteger)))
                .add(new VariableLabelExpression(nodeVar, configuration.getNodeLabelForRDFTerm(o)))
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
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(s).getId(), XSDDatatype.XSDinteger)))
                .add(new EqualityExpression(new PropertyAccessExpression(nodeVar, configuration.getPropertyNameForIRI(p)),
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
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(s).getId(), XSDDatatype.XSDinteger)))
                .add(new VariableLabelExpression(edgeVar, configuration.getEdgeLabelForIRI(p)))
                .add(new EqualityExpression(new VariableIDExpression(tgtVar),
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(o).getId(), XSDDatatype.XSDinteger)))
                .add(new AliasedExpression(new CountLargerThanZeroExpression(), gen.getAnonVar()))
                .build();
    }

    protected static CypherQuery getVarPropertyLiteral(final Node s, final Node p, final Node o,
                                                    final LPG2RDFConfiguration configuration,
                                                    final CypherVarGenerator gen,
                                                    final Set<Node> certainNodes) {
        final String property = configuration.getPropertyNameForIRI(p);
        final String literal = o.getLiteralValue().toString();
        final CypherVar a1 = gen.getAnonVar();
        if (certainNodes.contains(s)) {
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(a1))
                    .add(new EqualityExpression(new PropertyAccessExpression(a1, property),
                            new LiteralExpression(literal)))
                    .add(new AliasedExpression(a1, gen.getRetVar(s)))
                    .build();
        }
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar a3 = gen.getAnonVar();
        final CypherVar a4 = gen.getAnonVar();
        final CypherVar marker = gen.getMarkerVar();
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EqualityExpression(new PropertyAccessExpression(a1, property),
                                new LiteralExpression(literal)))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(a1, gen.getRetVar(s)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a2, a3, a4))
                        .add(new EqualityExpression(new PropertyAccessExpression(a3, property),
                                new LiteralExpression(literal)))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(new TripleMapExpression(a2, a3, a4),
                                gen.getRetVar(s)))
                        .build());
    }

    protected static CypherQuery getVarLabelClass(final Node s, final Node p, final Node o,
                                               final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final String label = configuration.getNodeLabelForRDFTerm(o);
        final CypherVar a1 = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(a1))
                .add(new VariableLabelExpression(a1, label))
                .add(new AliasedExpression(a1, gen.getRetVar(s)))
                .build();
    }

    protected static CypherQuery getVarRelationshipNode(final Node s, final Node p, final Node o,
                                                     final LPG2RDFConfiguration configuration,
                                                     final CypherVarGenerator gen) {
        final String relationship = configuration.getEdgeLabelForIRI(p);
        final LPGNode node = configuration.getLPGNodeForRDFTerm(o);
        final CypherVar a1 = gen.getAnonVar();
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar a3 = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, a2, a3))
                .add(new EqualityExpression(new VariableIDExpression(a3),
                        new LiteralExpression(node.getId(), XSDDatatype.XSDinteger)))
                .add(new VariableLabelExpression(a2, relationship))
                .add(new AliasedExpression(a1, gen.getRetVar(s)))
                .build();
    }

    protected static CypherQuery getNodeLabelVar(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar a1 = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(a1))
                .add(new EqualityExpression(new VariableIDExpression(a1),
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(s).getId(), XSDDatatype.XSDinteger)))
                .add(new AliasedExpression(new FirstLabelExpression(a1), gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodePropertyVar(final Node s, final Node p, final Node o,
                                                 final LPG2RDFConfiguration configuration,
                                                 final CypherVarGenerator gen) {
        final String property = configuration.getPropertyNameForIRI(p);
        final CypherVar var = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(var))
                .add(new EqualityExpression(new VariableIDExpression(var),
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(s).getId(), XSDDatatype.XSDinteger)))
                .add(new EXISTSExpression(new PropertyAccessExpression(var, property)))
                .add(new AliasedExpression(new PropertyAccessExpression(var, property), gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodeRelationshipVar(final Node s, final Node p, final Node o,
                                                     final LPG2RDFConfiguration configuration,
                                                     final CypherVarGenerator gen) {
        final CypherVar a1 = gen.getAnonVar();
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar a3 = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, a2, a3))
                .add(new EqualityExpression(new VariableIDExpression(a1),
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(s).getId(), XSDDatatype.XSDinteger)))
                .add(new VariableLabelExpression(a2, configuration.getEdgeLabelForIRI(p)))
                .add(new AliasedExpression(a3, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getNodeVarNode(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar a1 = gen.getAnonVar();
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar a3 = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, a2, a3))
                .add(new EqualityExpression(new VariableIDExpression(a1),
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(s).getId(), XSDDatatype.XSDinteger)))
                .add(new EqualityExpression(new VariableIDExpression(a3),
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(o).getId(), XSDDatatype.XSDinteger)))
                .add(new AliasedExpression(new TypeExpression(a2), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getNodeVarLiteral(final Node s, final Node p, final Node o,
                                                final LPG2RDFConfiguration configuration,
                                                final CypherVarGenerator gen) {
        final CypherVar a1 = gen.getAnonVar();
        final String literal = o.getLiteralValue().toString();
        final CypherVar iterVar = gen.getAnonVar();
        final CypherVar innerVar = new CypherVar("k");
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(a1))
                .add(new EqualityExpression(new VariableIDExpression(a1),
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(s).getId(), XSDDatatype.XSDinteger)))
                .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a1),
                        List.of(new EqualityExpression(
                                new PropertyAccessWithVarExpression(a1, innerVar),
                                new LiteralExpression(literal))),
                        List.of(innerVar), iterVar))
                .add(new AliasedExpression(new GetItemExpression(iterVar, 0), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getNodeVarLabel(final Node s, final Node p, final Node o,
                                              final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar a1 = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(a1))
                .add(new EqualityExpression(new VariableIDExpression(a1),
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(s).getId(), XSDDatatype.XSDinteger)))
                .add(new VariableLabelExpression(a1, configuration.getNodeLabelForRDFTerm(o)))
                .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getVarLabelVar(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar a1 = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(a1))
                .add(new AliasedExpression(a1, gen.getRetVar(s)))
                .add(new AliasedExpression(new FirstLabelExpression(a1), gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getVarRelationshipVar(final Node s, final Node p, final Node o,
                                                    final LPG2RDFConfiguration configuration,
                                                    final CypherVarGenerator gen) {
        final String relationship = configuration.getEdgeLabelForIRI(p);
        final CypherVar a1 = gen.getAnonVar();
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar a3 = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, a2, a3))
                .add(new VariableLabelExpression(a2, relationship))
                .add(new AliasedExpression(a1, gen.getRetVar(s)))
                .add(new AliasedExpression(a3, gen.getRetVar(o)))
                .build();
    }

    protected static CypherQuery getVarPropertyVar(final Node s, final Node p, final Node o,
                                                final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                                final Set<Node> certainNodes) {
        final String property = configuration.getPropertyNameForIRI(p);
        final CypherVar a1 = gen.getAnonVar();
        if (certainNodes.contains(s)) {
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(a1))
                    .add(new EXISTSExpression(new PropertyAccessExpression(a1, property)))
                    .add(new AliasedExpression(a1, gen.getRetVar(s)))
                    .add(new AliasedExpression(new PropertyAccessExpression(a1, property),
                            gen.getRetVar(o)))
                    .build();
        }
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar a3 = gen.getAnonVar();
        final CypherVar a4 = gen.getAnonVar();
        final CypherVar marker = gen.getMarkerVar();
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a2, a3, a4))
                        .add(new EXISTSExpression(new PropertyAccessExpression(a3, property)))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(new TripleMapExpression(a2, a3, a4), gen.getRetVar(s)))
                        .add(new AliasedExpression(new PropertyAccessExpression(a3, property), gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new EXISTSExpression(new PropertyAccessExpression(a1, property)))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(a1, gen.getRetVar(s)))
                        .add(new AliasedExpression(new PropertyAccessExpression(a1, property),
                                gen.getRetVar(o)))
                        .build()
        );
    }

    protected static CypherQuery getVarVarLabel(final Node s, final Node p, final Node o,
                                             final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar a1 = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new NodeMatchClause(a1))
                .add(new VariableLabelExpression(a1, configuration.getNodeLabelForRDFTerm(o)))
                .add(new AliasedExpression(a1, gen.getRetVar(s)))
                .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getVarVarNode(final Node s, final Node p, final Node o,
                                            final LPG2RDFConfiguration configuration, final CypherVarGenerator gen) {
        final CypherVar a1 = gen.getAnonVar();
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar a3 = gen.getAnonVar();
        return new CypherQueryBuilder()
                .add(new EdgeMatchClause(a1, a2, a3))
                .add(new EqualityExpression(new VariableIDExpression(a3),
                        new LiteralExpression(configuration.getLPGNodeForRDFTerm(o).getId(), XSDDatatype.XSDinteger)))
                .add(new AliasedExpression(a1, gen.getRetVar(s)))
                .add(new AliasedExpression(new TypeExpression(a2), gen.getRetVar(p)))
                .build();
    }

    protected static CypherQuery getVarVarLiteral(final Node s, final Node p, final Node o,
                                               final LPG2RDFConfiguration configuration, final CypherVarGenerator gen,
                                               final Set<Node> certainNodes) {
        final CypherVar a1 = gen.getAnonVar();
        final CypherVar innerVar = new CypherVar("k");
        final CypherVar a2 = gen.getAnonVar();
        final String literal = o.getLiteralValue().toString();
        if (certainNodes.contains(s)) {
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(a1))
                    .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a1),
                            List.of(new EqualityExpression(
                                    new PropertyAccessWithVarExpression(a1, innerVar),
                                    new LiteralExpression(literal))),
                            List.of(innerVar), a2))
                    .add(new AliasedExpression(a1, gen.getRetVar(s)))
                    .add(new AliasedExpression(new GetItemExpression(a2, 0), gen.getRetVar(p)))
                    .build();
        }
        final CypherVar a3 = gen.getAnonVar();
        final CypherVar a4 = gen.getAnonVar();
        final CypherVar a5 = gen.getAnonVar();
        final CypherVar a6 = gen.getAnonVar();
        final CypherVar marker = gen.getMarkerVar();
        return new CypherUnionQueryImpl(
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a3, a4, a5))
                        .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a4),
                                List.of(new EqualityExpression(
                                        new PropertyAccessWithVarExpression(a4, innerVar),
                                        new LiteralExpression(literal))), List.of(innerVar), a6))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(new TripleMapExpression(a3, a4, a5), gen.getRetVar(s)))
                        .add(new AliasedExpression(new GetItemExpression(a6 , 0), gen.getRetVar(p)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a1))
                        .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a1),
                                List.of(new EqualityExpression(
                                        new PropertyAccessWithVarExpression(a1, innerVar),
                                        new LiteralExpression(literal))),
                                List.of(innerVar), a2))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(a1, gen.getRetVar(s)))
                        .add(new AliasedExpression(new GetItemExpression(a2, 0), gen.getRetVar(p)))
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
        final LPGNode node = configuration.getLPGNodeForRDFTerm(s);
        final CypherVar a1 = gen.getAnonVar();
        if (certainNodeLabels.contains(o)){
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(a1))
                    .add(new EqualityExpression(new VariableIDExpression(a1),
                            new LiteralExpression(node.getId(), XSDDatatype.XSDinteger)))
                    .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                    .add(new AliasedExpression(new FirstLabelExpression(a1), gen.getRetVar(o)))
                    .build();
        }
        final CypherVar a2 = gen.getAnonVar();
        final CypherVar innerVar = new CypherVar("k");
        if (certainPropertyNames.contains(p) || certainPropertyValues.contains(o)){
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(a1))
                    .add(new EqualityExpression(new VariableIDExpression(a1),
                            new LiteralExpression(node.getId(), XSDDatatype.XSDinteger)))
                    .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a1), null,
                            List.of(innerVar, new PropertyAccessWithVarExpression(a1, innerVar)), a2))
                    .add(new AliasedExpression(new GetItemExpression(a2, 0), gen.getRetVar(p)))
                    .add(new AliasedExpression(new GetItemExpression(a2, 1), gen.getRetVar(o)))
                    .build();
        }
        final CypherVar a3 = gen.getAnonVar();
        //In general, we shouldn't reuse parts of queries, because it messes with the order of anonymous variables
        if (certainNodes.contains(o) || isEdgeCompatible){
            return new CypherQueryBuilder()
                    .add(new EdgeMatchClause(a1, a2, a3))
                    .add(new EqualityExpression(new VariableIDExpression(a1),
                            new LiteralExpression(node.getId(), XSDDatatype.XSDinteger)))
                    .add(new AliasedExpression(new TypeExpression(a2), gen.getRetVar(p)))
                    .add(new AliasedExpression(a3, gen.getRetVar(o)))
                    .build();
        }
        if (certainEdgeLabels.contains(p)){
            return new CypherQueryBuilder()
                    .add(new EdgeMatchClause(a1, a2, a3))
                    .add(new EqualityExpression(new VariableIDExpression(a1),
                            new LiteralExpression(node.getId(), XSDDatatype.XSDinteger)))
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
                                new LiteralExpression(node.getId(), XSDDatatype.XSDinteger)))
                        .add(new MarkerExpression(0, marker))
                        .add(new AliasedExpression(new LiteralExpression("label"), gen.getRetVar(p)))
                        .add(new AliasedExpression(new FirstLabelExpression(a1), gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new NodeMatchClause(a2))
                        .add(new EqualityExpression(new VariableIDExpression(a2),
                                new LiteralExpression(node.getId(), XSDDatatype.XSDinteger)))
                        .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a2), null,
                                List.of(innerVar, new PropertyAccessWithVarExpression(a2, innerVar)), a3))
                        .add(new MarkerExpression(1, marker))
                        .add(new AliasedExpression(new GetItemExpression(a3, 0), gen.getRetVar(p)))
                        .add(new AliasedExpression(new GetItemExpression(a3, 1), gen.getRetVar(o)))
                        .build(),
                new CypherQueryBuilder()
                        .add(new EdgeMatchClause(a4, a5, a6))
                        .add(new EqualityExpression(new VariableIDExpression(a4),
                                new LiteralExpression(node.getId(), XSDDatatype.XSDinteger)))
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
        final CypherVar innerVar = new CypherVar("k");
        if (certainNodes.contains(s) && (certainPropertyNames.contains(p) || certainPropertyValues.contains(o))) {
            return new CypherQueryBuilder()
                    .add(new NodeMatchClause(a1))
                    .add(new UnwindIteratorImpl(innerVar, new KeysExpression(a1), List.of(),
                            List.of(innerVar, new PropertyAccessWithVarExpression(a1, innerVar)), a2))
                    .add(new AliasedExpression(a1, gen.getRetVar(s)))
                    .add(new AliasedExpression(new GetItemExpression(a2, 0), gen.getRetVar(p)))
                    .add(new AliasedExpression(new GetItemExpression(a2, 1), gen.getRetVar(o)))
                    .build();
        }
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
                    .add(new AliasedExpression(new FirstLabelExpression(a4), gen.getRetVar(o)))
                    .build();
        }

        final CypherVar a5 = gen.getAnonVar();
        final CypherVar a6 = gen.getAnonVar();
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
                            .add(new AliasedExpression(new FirstLabelExpression(a4), gen.getRetVar(o)))
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
                        .add(new AliasedExpression(new FirstLabelExpression(a4), gen.getRetVar(o)))
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

    @Override
    public CypherMatchQuery rewriteJoins(final CypherMatchQuery query) {
        final CypherQueryBuilder builder = new CypherQueryBuilder();
        final List<BooleanCypherExpression> variableJoins = query.getConditions().stream()
                .filter(x -> x instanceof EqualityExpression
                        && ((EqualityExpression) x).getLeftExpression() instanceof CypherVar
                        && ((EqualityExpression) x).getRightExpression() instanceof CypherVar
                        && query.getMatchVars().contains((CypherVar) ((EqualityExpression) x).getLeftExpression())
                        && query.getMatchVars().contains((CypherVar) ((EqualityExpression) x).getRightExpression()))
                .collect(Collectors.toList());
        if (variableJoins.isEmpty()) return query;
        final Map<CypherVar, CypherVar> equivalences = getEquivalenceMap(variableJoins);
        //first rewrite the MATCH clauses by replacing the equivalent variables
        final List<MatchClause> matches = new ArrayList<>();
        for (final MatchClause m : query.getMatches()){
            final MatchClause newM = (MatchClause) CypherUtils.replaceVariable(equivalences, m);
            if (!matches.contains(newM))
                matches.add(newM);
        }
        final List<NodeMatchClause> nodes = matches.stream().filter(x->x instanceof NodeMatchClause)
                .map(x->(NodeMatchClause)x).collect(Collectors.toList());
        //then remove any redundant node patterns
        if (!nodes.isEmpty()) {
            final List<MatchClause> toRemove = new ArrayList<>();
            for (final MatchClause m : matches) {
                if (m instanceof EdgeMatchClause) {
                    for (final NodeMatchClause node : nodes) {
                        if (m.isRedundantWith(node))
                            toRemove.add(node);
                    }
                }
            }
            matches.removeAll(toRemove);
        }
        for (final MatchClause m : matches)
            builder.add(m);

        //now replace the variables in the rest of the elements of the query
        for (final BooleanCypherExpression c : query.getConditions()) {
            if (variableJoins.contains(c)) continue;
            builder.add(CypherUtils.replaceVariable(equivalences, c));
        }
        for (final UnwindIterator i : query.getIterators()) {
            builder.add(CypherUtils.replaceVariable(equivalences, i));
        }
        for (final AliasedExpression r : query.getReturnExprs()) {
            builder.add(CypherUtils.replaceVariable(equivalences, r));
        }
        return builder.build();
    }

    @Override
    public List<MatchClause> mergePaths(final List<MatchClause> matchClauses) {
        final LabeledGraphBuilder builder = new LabeledGraphBuilder();
        for (final MatchClause m : matchClauses) {
            if (m instanceof NodeMatchClause)
                builder.addNode(((NodeMatchClause) m).getNode());
            else if (m instanceof  EdgeMatchClause) {
                final EdgeMatchClause e = (EdgeMatchClause) m;
                builder.addEdge(e.getSourceNode(), e.getEdge(), e.getTargetNode());
            } else {
                throw new IllegalArgumentException("Unsupported type of Match clause given to pattern merging: " + m.getClass().getName() );
            }
        }
        final LabeledGraph graph = builder.build();
        final List<MatchClause> mergedPatterns = new ArrayList<>();
        while (!graph.isEmpty()) {
            final LabeledGraph.Path longest = graph.getLongestPath();
            if (longest.size()==0)
                mergedPatterns.add(new NodeMatchClause(longest.getStart()));
            else
                mergedPatterns.add(new PathMatchClause(longest));
            graph.removePath(longest);
        }
        return mergedPatterns;
    }

    private Map<CypherVar, CypherVar> getEquivalenceMap(final List<BooleanCypherExpression> variableJoins) {
        final Set<Set<CypherVar>> equivalenceClasses = new HashSet<>();
        for (final BooleanCypherExpression join : variableJoins) {
            final EqualityExpression eq = (EqualityExpression) join;
            final CypherVar v1 = (CypherVar) eq.getLeftExpression();
            final CypherVar v2 = (CypherVar) eq.getRightExpression();
            boolean found = false;
            for (final Set<CypherVar> eqClass : equivalenceClasses) {
                if (eqClass.contains(v1) || eqClass.contains(v2)) {
                    eqClass.add(v1); eqClass.add(v2);
                    found = true;
                    break;
                }
            }
            if (!found) {
                final Set<CypherVar> eqClass = new HashSet<>();
                eqClass.add(v1); eqClass.add(v2);
                equivalenceClasses.add(eqClass);
            }
        }
        final Map<CypherVar, CypherVar> equivalenceMap = new HashMap<>();
        for (final Set<CypherVar> eqClass : equivalenceClasses) {
            CypherVar id = null;
            boolean first = true;
            for (final CypherVar v : eqClass) {
                if (first) {
                    id = v;
                    first = false;
                    continue;
                }
                equivalenceMap.put(v, id);
            }
        }
        return equivalenceMap;
    }

    /**
     * Returns the number of elements of the given triple pattern that are variables.
     */
    public static int getNumberOfVarOccurrences( final Triple tp ) {
        int n = 0;
        if ( tp.getSubject().isVariable() )   { n += 1; }
        if ( tp.getPredicate().isVariable() ) { n += 1; }
        if ( tp.getObject().isVariable() )    { n += 1; }
        return n;
    }

}
