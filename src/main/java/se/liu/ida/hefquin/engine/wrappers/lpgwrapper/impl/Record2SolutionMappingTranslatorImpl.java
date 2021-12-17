package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.Record2SolutionMappingTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Record2SolutionMappingTranslatorImpl implements Record2SolutionMappingTranslator {

    @Override
    public List<SolutionMapping> translateRecord(final TableRecord record, final LPG2RDFConfiguration conf,
                                                 final CypherQuery query, final Map<CypherVar, Node> varMap) {
        final List<Pair<CypherVar, ListValue>> lists = new ArrayList<>();
        final SolutionMapping base = translateRecordsPriv(record, conf, query, varMap, lists);
        return manageLists(base, lists, conf, query, varMap);
    }

    /**
     * This method is in charge to produce multiple solution mappings for records that contain lists of values.
     * All provided lists should have the same size, since each i-th element of each list will be part of the it-h
     * solution mapping of the result.
     */
    protected List<SolutionMapping> manageLists(final SolutionMapping base, final List<Pair<CypherVar, ListValue>> lists,
                                              final LPG2RDFConfiguration conf, final CypherQuery query,
                                              final Map<CypherVar, Node> varMap) {
        if (lists.isEmpty())
            return Collections.singletonList(base);
        final int N = lists.get(0).object2.getList().size();
        final List<BindingBuilder> builders = new ArrayList<>();
        for (final Pair<CypherVar, ListValue> pair : lists) {
            if (pair.object2.getList().size() != N)
                throw new IllegalStateException("Record List Values should be the same size");
        }
        for (int i = 0; i < N; i++) {
            if (builders.size() < (i + 1)) {
                final BindingBuilder builder = Binding.builder();
                builder.addAll(base.asJenaBinding());
                builders.add(builder);
            }
            for (Pair<CypherVar, ListValue> pair : lists) {
                final Node node;
                if (CypherUtils.isPropertyColumn(query, pair.object1)) {
                    node = conf.mapProperty(pair.object2.getList().get(i).toString());
                } else {
                    node = NodeFactory.createLiteral(pair.object2.getList().get(i).toString());
                }
                builders.get(i).add(Var.alloc(varMap.get(pair.object1)), node);
            }
        }
        final List<SolutionMapping> results = new ArrayList<>(builders.size());
        for (final BindingBuilder builder : builders) {
            results.add(new SolutionMappingImpl(builder.build()));
        }
        return results;
    }

    protected SolutionMapping translateRecordsPriv(final TableRecord record, final LPG2RDFConfiguration conf,
                                                 final CypherQuery query, final Map<CypherVar, Node> varMap,
                                                 final List<Pair<CypherVar, ListValue>> lists) {
        final BindingBuilder baseBuilder = Binding.builder();
        for (final RecordEntry entry : record.getRecordEntries()){
            final CypherVar var = entry.getName();
            final Value value = entry.getValue();
            final Var mappingVar = Var.alloc(varMap.get(var));
            if (value instanceof LPGNodeValue) {
                final Node nodeMapping  = conf.mapNode(((LPGNodeValue) value).getNode());
                baseBuilder.add(mappingVar, nodeMapping);
            } else if (value instanceof LPGEdgeValue) {
                final Node edgeMapping = conf.mapEdgeLabel(((LPGEdgeValue) value).getEdge().getLabel());
                baseBuilder.add(mappingVar, edgeMapping);
            } else if (value instanceof MapValue) {
                final Map<String, Object> mapValue = ((MapValue) value).getMap();
                final Node triple = NodeFactory.createTripleNode(
                        conf.mapNode((LPGNode) mapValue.get("source")),
                        conf.mapEdgeLabel(mapValue.get("edge").toString()),
                        conf.mapNode((LPGNode) mapValue.get("target"))
                );
                baseBuilder.add(mappingVar, triple);
            } else if (value instanceof ListValue) {
                lists.add(new Pair<>(var, (ListValue) value));
            } else if (value instanceof LiteralValue) {
                final String literal = ((LiteralValue) value).getValue().toString();
                Node node;
                if (literal.equals("label"))
                    node = conf.getLabel();
                else if (CypherUtils.isLabelColumn(query, var)) {
                    node = conf.mapNodeLabel(literal);
                } else if (CypherUtils.isRelationshipTypeColumn(query, var)) {
                    node = conf.mapEdgeLabel(literal);
                } else {
                    node = NodeFactory.createLiteral(literal);
                }
                baseBuilder.add(mappingVar, node);
            } else {
                throw new IllegalArgumentException("Unsupported implementation of value (" + value.getClass().getName() + ")");
            }
        }
        return new SolutionMappingImpl(baseBuilder.build());
    }

    @Override
    public List<SolutionMapping> translateRecords(final List<TableRecord> records,
                                                  final LPG2RDFConfiguration conf, final CypherQuery query,
                                                  final Map<CypherVar, Node> varMap) {
        final List<SolutionMapping> results = new ArrayList<>();
        for (final TableRecord record : records) {
            results.addAll(translateRecord(record, conf, query, varMap));
        }
        return results;
    }

}
