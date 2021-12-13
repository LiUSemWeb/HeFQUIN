package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.Record2SolutionMappingTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherUtils;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherVarGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Record2SolutionMappingTranslatorImpl implements Record2SolutionMappingTranslator {

    @Override
    public List<SolutionMapping> translateRecord(final TableRecord record, final LPG2RDFConfiguration conf,
                                                 final CypherQuery query, final CypherVarGenerator generator) {
        final List<BindingBuilder> builders = new ArrayList<>();
        final BindingBuilder baseBuilder = Binding.builder();
        for (final RecordEntry entry : record.getRecordEntries()){
            final CypherVar var = entry.getName();
            final Value value = entry.getValue();
            final Var mappingVar = Var.alloc(generator.getReverseRetVar(var));
            if (value instanceof LPGNodeValue) {
                final Node nodeMapping  = conf.mapNode(((LPGNodeValue) value).getNode());
                if (builders.isEmpty())
                    baseBuilder.add(mappingVar, nodeMapping);
                else {
                    addToAllBuilders(builders, mappingVar, nodeMapping);
                }
            } else if (value instanceof LPGEdgeValue) {
                final Node edgeMapping = conf.mapEdgeLabel(((LPGEdgeValue) value).getEdge().getLabel());
                if (builders.isEmpty())
                    baseBuilder.add(mappingVar, edgeMapping);
                else {
                    addToAllBuilders(builders, mappingVar, edgeMapping);
                }
            } else if (value instanceof MapValue) {
                final Map<String, Object> mapValue = ((MapValue) value).getMap();
                final Node triple = NodeFactory.createTripleNode(
                        conf.mapNode((LPGNode) mapValue.get("source")),
                        conf.mapEdgeLabel(((LPGEdge) mapValue.get("edge")).getLabel()),
                        conf.mapNode((LPGNode) mapValue.get("target"))
                );
                if (builders.isEmpty())
                    baseBuilder.add(mappingVar, triple);
                else {
                    addToAllBuilders(builders, mappingVar, triple);
                }
            } else if (value instanceof ListValue) {
                final List<Object> values = ((ListValue) value).getList();
                final boolean isPropertyList = CypherUtils.isPropertyColumn(query, var);
                if (builders.isEmpty()) {
                    for (final Object val : values) {
                        Node node;
                        if (isPropertyList) {
                            node = conf.mapProperty(val.toString());
                        } else {
                            node = NodeFactory.createLiteral(val.toString());
                        }
                        final BindingBuilder builder = Binding.builder();
                        builder.addAll(baseBuilder.build());
                        builder.add(mappingVar, node);
                        builders.add(builder);
                    }
                } else {
                    for (final Object val : values) {
                        Node node;
                        if (isPropertyList) {
                            node = conf.mapProperty(val.toString());
                        } else {
                            node = NodeFactory.createLiteral(val.toString());
                        }
                        addToAllBuilders(builders, mappingVar, node);
                    }
                }
            } else if (value instanceof LiteralValue) {
                final String literal = ((LiteralValue) value).getValue().toString();
                Node node;
                if (literal.equals("label"))
                    node = conf.getLabel();
                else
                    node = NodeFactory.createLiteral(literal);
                if (builders.isEmpty()) {
                    baseBuilder.add(mappingVar, node);
                } else {
                    addToAllBuilders(builders, mappingVar, node);
                }
            }
        }
        if (builders.isEmpty()) {
            builders.add(baseBuilder);
        }
        final List<SolutionMapping> mappings = new ArrayList<>();
        for (final BindingBuilder builder : builders) {
            mappings.add(new SolutionMappingImpl(builder.build()));
        }
        return mappings;
    }

    @Override
    public List<SolutionMapping> translateRecords(final List<TableRecord> records,
                                                  final LPG2RDFConfiguration conf, final CypherQuery query,
                                                  final CypherVarGenerator generator) {
        List<SolutionMapping> results = new ArrayList<>();
        for (final TableRecord record : records) {
            results.addAll(translateRecord(record, conf, query, generator));
        }
        return results;
    }

    private void addToAllBuilders(final List<BindingBuilder> builders, final Var var, final Node node) {
        for (final BindingBuilder builder : builders)
            builder.add(var, node);
    }
}
