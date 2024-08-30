package se.liu.ida.hefquin.engine.wrappers.lpg.impl;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;

import se.liu.ida.hefquin.engine.wrappers.lpg.Record2SolutionMappingTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpg.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.LPGNodeValue;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.impl.MapValue;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherUnionQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Record2SolutionMappingTranslatorImpl implements Record2SolutionMappingTranslator {

    private CypherExpression currentMarker =  null;
    private List<AliasedExpression> returnExpressions = null;

    @Override
    public Binding translateRecord( final TableRecord record,
                                    final LPG2RDFConfiguration conf,
                                    final CypherQuery query,
                                    final Map<CypherVar, Var> varMap ) {
        final BindingBuilder builder = Binding.builder();
        final int n = record.size();
        if (returnExpressions == null)
            returnExpressions = getRelevantReturnExpressions(0, query);

        for (int i = 0; i < n; i++) {
            final RecordEntry current = record.getEntry(i);
            final AliasedExpression aExp = returnExpressions.get(i);
            final CypherVar alias = aExp.getAlias();
            final Var var = varMap.get(alias);
            final CypherExpression expression = aExp.getExpression();
            if (aExp instanceof MarkerExpression) {
                final CypherExpression dataMarker = new LiteralExpression(current.getValue().toString());
                if (currentMarker == null) {
                    currentMarker = expression;
                }
                if (! currentMarker.equals(dataMarker)) {
                    currentMarker = dataMarker;
                    returnExpressions = getRelevantReturnExpressions(extractIndex(dataMarker), query);
                }
                continue;
            }
            if (expression instanceof CountLargerThanZeroExpression) {
                if (current.getValue().toString().equals("true")) continue;
                else return BindingFactory.binding();
            }
            if (expression instanceof CypherVar) {
                builder.add(var, conf.getRDFTermForLPGNode(((LPGNodeValue) current.getValue()).getNode()));
            } else if (expression instanceof TripleMapExpression) {
                final Map<String, Object> map = ((MapValue) current.getValue()).getMap();
                builder.add(var, NodeFactory.createTripleNode(
                        conf.getRDFTermForLPGNode((LPGNode) map.get("s")),
                        conf.getIRIForEdgeLabel(map.get("e").toString()),
                        conf.getRDFTermForLPGNode((LPGNode) map.get("t"))));
            } else if (expression instanceof TypeExpression) {
                builder.add(var, conf.getIRIForEdgeLabel(current.getValue().toString()));
            } else if (expression instanceof GetItemExpression) {
                final int index = ((GetItemExpression) expression).getIndex();
                if (index == 0) {
                    builder.add(var, conf.getIRIForPropertyName(current.getValue().toString()));
                } else if (index == 1) {
                    builder.add(var, NodeFactory.createLiteral(current.getValue().toString()));
                } else {
                    throw new IllegalArgumentException("Invalid Return Statement");
                }
            } else if (expression instanceof FirstLabelExpression) {
                builder.add(var, conf.getRDFTermForNodeLabel(current.getValue().toString()));
            } else if (expression instanceof PropertyAccessExpression) {
                builder.add(var, NodeFactory.createLiteral(current.getValue().toString()));
            } else if (expression instanceof LiteralExpression) {
                builder.add(var, conf.getLabelPredicate());
            } else {
                throw new IllegalArgumentException("Invalid Return Statement");
            }
        }

        return builder.build();
    }

    private int extractIndex(CypherExpression dataMarker) {
        final String literal = dataMarker.toString();
        return Integer.parseInt(literal.substring(2, literal.length()-1));
    }

    private List<AliasedExpression> getRelevantReturnExpressions(final int index, final CypherQuery query) {
        if (query instanceof CypherMatchQuery)
            return ((CypherMatchQuery) query).getReturnExprs();
        return ((CypherUnionQuery) query).getSubqueries().get(index).getReturnExprs();
    }

    @Override
    public List<Binding> translateRecords( final List<TableRecord> records,
                                           final LPG2RDFConfiguration conf,
                                           final CypherQuery query,
                                           final Map<CypherVar, Var> varMap ) {
        final List<Binding> results = new ArrayList<>();
        for ( final TableRecord record : records ) {
            results.add( translateRecord(record, conf, query, varMap) );
        }
        return results;
    }

}
