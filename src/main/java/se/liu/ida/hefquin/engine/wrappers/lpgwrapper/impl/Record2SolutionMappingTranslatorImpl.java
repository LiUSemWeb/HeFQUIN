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
                    addToAllBuilders(builders, Var.alloc(var.getName()), nodeMapping);
                }
            } else if (value instanceof LPGEdgeValue) {
                final Node edgeMapping = conf.mapEdgeLabel(((LPGEdgeValue) value).getEdge().getLabel());
                if (builders.isEmpty())
                    baseBuilder.add(Var.alloc(var.getName()), edgeMapping);
                else {
                    addToAllBuilders(builders, Var.alloc(var.getName()), edgeMapping);
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
                    addToAllBuilders(builders, Var.alloc(var.getName()), triple);
                }
            } else if (value instanceof ListValue) {
                final List<Object> values = ((ListValue) value).getList();
                if (builders.isEmpty()) {
                    for (final Object val : values) {
                        Node node = getPropertyOrLiteral(val, conf);
                        final BindingBuilder builder = Binding.builder();
                        builder.addAll(baseBuilder.build());
                        //we still need to distinguish properties from values here
                        builder.add(Var.alloc(var.getName()), NodeFactory.createLiteral(val.toString()));
                        builders.add(builder);
                    }
                } else {
                    for (final Object val : values) {
                        addToAllBuilders(builders, Var.alloc(var.getName()), NodeFactory.createLiteral(val.toString()));
                    }
                }
            } else if (value instanceof LiteralValue) {
                final String literal = ((LiteralValue) value).getValue().toString();
                Node node = null;
                if (literal.equals("label"))
                    node = conf.getLabel();
                else
                    node = NodeFactory.createLiteral(literal);
                if (builders.isEmpty()) {
                    baseBuilder.add(Var.alloc(var.getName()), node);
                } else {
                    addToAllBuilders(builders, Var.alloc(var.getName()), node);
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

    private Node getPropertyOrLiteral(final Object value, final LPG2RDFConfiguration conf) {
        return null;
    }

    private void addToAllBuilders(final List<BindingBuilder> builders, final Var var, final Node node) {
        for (final BindingBuilder builder : builders)
            builder.add(var, node);
    }
}
