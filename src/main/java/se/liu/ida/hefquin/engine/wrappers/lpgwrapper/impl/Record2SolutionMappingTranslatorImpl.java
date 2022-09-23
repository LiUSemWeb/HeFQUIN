package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.Binding0;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.Record2SolutionMappingTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Record2SolutionMappingTranslatorImpl implements Record2SolutionMappingTranslator {

    @Override
    public SolutionMapping translateRecord(final TableRecord record, final LPG2RDFConfiguration conf,
                                           final CypherQuery query, final Map<CypherVar, Var> varMap) {
        final BindingBuilder builder = Binding.builder();
        final int n = record.size();
        final List<AliasedExpression> returnExpressions = getRelevantReturnExpressions(query);

        for (int i = 0; i < n; i++) {
            final RecordEntry current = record.getEntry(i);
            final AliasedExpression aExp = returnExpressions.get(i);
            final CypherVar alias = aExp.getAlias();
            final Var var = varMap.get(alias);
            final CypherExpression expression = aExp.getExpression();
            if (expression instanceof CountLargerThanZeroExpression) {
                if (current.getValue().toString().equals("true")) continue;
                else return new SolutionMappingImpl(BindingFactory.binding());
            }
            if (expression instanceof CypherVar) {
                builder.add(var, conf.mapNode(((LPGNodeValue) current.getValue()).getNode()));
            } else if (expression instanceof TripleMapExpression) {
                final Map<String, Object> map = ((MapValue) current.getValue()).getMap();
                builder.add(var, NodeFactory.createTripleNode(
                        conf.mapNode((LPGNode) map.get("s")),
                        conf.mapEdgeLabel(map.get("e").toString()),
                        conf.mapNode((LPGNode) map.get("t"))));
            } else if (expression instanceof TypeExpression) {
                builder.add(var, conf.mapEdgeLabel(current.getValue().toString()));
            } else if (expression instanceof GetItemExpression) {
                final int index = ((GetItemExpression) expression).getIndex();
                if (index == 0) {
                    builder.add(var, conf.mapProperty(current.getValue().toString()));
                } else if (index == 1) {
                    builder.add(var, NodeFactory.createLiteral(current.getValue().toString()));
                } else {
                    throw new IllegalArgumentException("Invalid Return Statement");
                }
            } else if (expression instanceof LabelsExpression) {
                builder.add(var, conf.mapNodeLabel(current.getValue().toString()));
            } else if (expression instanceof PropertyAccessExpression) {
                builder.add(var, NodeFactory.createLiteral(current.getValue().toString()));
            } else if (expression instanceof LiteralExpression) {
                builder.add(var, conf.getLabel());
            }
        }

        return new SolutionMappingImpl(builder.build());
    }

    private List<AliasedExpression> getRelevantReturnExpressions(final CypherQuery query) {
        if (query instanceof CypherMatchQuery)
            return ((CypherMatchQuery) query).getReturnExprs();
        return null;
    }

    @Override
    public List<SolutionMapping> translateRecords(final List<TableRecord> records,
                                                  final LPG2RDFConfiguration conf, final CypherQuery query,
                                                  final Map<CypherVar, Var> varMap) {
        final List<SolutionMapping> results = new ArrayList<>();
        for (final TableRecord record : records) {
            results.add(translateRecord(record, conf, query, varMap));
        }
        return results;
    }

}
