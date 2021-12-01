package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl;

import com.github.jsonldjava.core.RDFDataset;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.Record2SolutionMappingTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Record2SolutionMappingTranslatorImpl implements Record2SolutionMappingTranslator {
    @Override
    public List<SolutionMapping> translateRecord(final TableRecord record, final LPG2RDFConfiguration conf) {
        final List<BindingBuilder> builders = new ArrayList<>();
        final BindingBuilder baseBuilder = Binding.builder();
        for (final RecordEntry entry : record.getRecordEntries()){
            final CypherVar var = entry.getName();
            final Value value = entry.getValue();
            if (value instanceof LPGNodeValue) {
                final Node nodeMapping  = conf.mapNode(((LPGNodeValue) value).getNode());
                if (builders.isEmpty())
                    baseBuilder.add(Var.alloc(var.getName()), nodeMapping);
                else {
                    for (final BindingBuilder builder : builders)
                        builder.add(Var.alloc(var.getName()), nodeMapping);
                }
            } else if (value instanceof LPGEdgeValue) {
                final Node edgeMapping = conf.mapEdgeLabel(((LPGEdgeValue) value).getEdge().getLabel());
                if (builders.isEmpty())
                    baseBuilder.add(Var.alloc(var.getName()), edgeMapping);
                else {
                    for (final BindingBuilder builder : builders)
                        builder.add(Var.alloc(var.getName()), edgeMapping);
                }
            } else if (value instanceof MapValue) {
                final Map<String, Object> mapValue = ((MapValue) value).getMap();
                final Node triple = NodeFactory.createTripleNode(
                        conf.mapNode((LPGNode) mapValue.get("source")),
                        conf.mapEdgeLabel(((LPGEdge)mapValue.get("edge")).getLabel()),
                        conf.mapNode((LPGNode) mapValue.get("target"))
                );
                if (builders.isEmpty())
                    baseBuilder.add(Var.alloc(var.getName()), triple);
                else {
                    for (final BindingBuilder builder : builders)
                        builder.add(Var.alloc(var.getName()), triple);
                }
            } else if (value instanceof ListValue) {
                final List<Object> values = ((ListValue) value).getList();
                if (builders.isEmpty()) {
                    for (final Object val : values) {
                        final BindingBuilder builder = Binding.builder();
                        builder.addAll(baseBuilder.build());
                        //we still need to distinguish properties from values here
                        builder.add(Var.alloc(var.getName()), NodeFactory.createLiteral(val.toString()));
                        builders.add(builder);
                    }
                } else {
                    for (int i=0 ; i<values.size(); i++) {
                        builders.get(i).add(Var.alloc(var.getName()),
                                NodeFactory.createLiteral(values.get(i).toString()));
                    }
                }
            } else if (value instanceof LiteralValue) {
                if (builders.isEmpty()) {
                    //we still need to distinguish general literals from the "label" special value
                    baseBuilder.add(Var.alloc(var.getName()),
                            NodeFactory.createLiteral(((LiteralValue) value).getValue().toString()));
                } else {
                    for (final BindingBuilder builder : builders) {
                        builder.add(Var.alloc(var.getName()),
                                NodeFactory.createLiteral(((LiteralValue) value).getValue().toString()));
                    }
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
}
