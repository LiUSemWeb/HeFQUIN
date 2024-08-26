package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import com.fasterxml.jackson.databind.JsonNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.*;

public class RecordEntryFactory
{
    public static RecordEntry create(final JsonNode col, final Iterator<JsonNode> metaRow, final CypherVar name) {
        final Value val;
        if (col.get("s") != null && col.get("e") != null && col.get("t") != null) {
            val = new MapValue(parseAsMap(col, metaRow));
        }
        else {
            final JsonNode metaCol = metaRow.next();
            final JsonNode type = metaCol.get("type");
            if (type != null) {
                if (type.asText().equals("node")) {
                    val = new LPGNodeValue(parseAsNode(col, metaCol.get("id")));
                }
                else if (type.asText().equals("relationship")) {
                    val = new LPGEdgeValue(parseAsEdge(col, metaCol.get("id")));
                }
                else {
                    throw new IllegalArgumentException( "Unexpected type of record entry: " + type.toString() );
                }
            }
            else {
                val = parseAsLiteralOrArrayValue(col);
            }
        }
        return new RecordEntryImpl(name, val);
    }

    private static Map<String, Object> parseAsMap(final JsonNode col, final Iterator<JsonNode> metaRow) {
        Map<String, Object> values = new HashMap<>();
        for (final Iterator<String> it = col.fieldNames(); it.hasNext(); ) {
            final String name = it.next();
            final JsonNode val = col.get(name);
            final JsonNode metaCol = metaRow.next();
            final Object mapObject;
            if (val.isObject()) {
                mapObject = parseAsNode(val, metaCol.get("id"));
            }
            else {
                mapObject = val.asText();
            }
            values.put(name, mapObject);
        }
        return values;
    }

    private static LPGEdge parseAsEdge(final JsonNode col, JsonNode id) {
        Map<String, Value> properties = parseProperties(col);
        return new LPGEdge(id.asText(), "", new PropertyMapImpl(properties));
    }

    private static LPGNode parseAsNode(final JsonNode col, JsonNode id) {
        Map<String, Value> properties = parseProperties(col);
        return new LPGNode(id.asText(), "", new PropertyMapImpl(properties));
    }

    private static Map<String, Value> parseProperties( final JsonNode col ) {
        final Map<String, Value> properties = new HashMap<>();
        final Iterator<String> it = col.fieldNames();
        while ( it.hasNext() ) {
            final String name = it.next();
            final JsonNode valNode = col.get(name);
            properties.put( name, parseAsLiteralOrArrayValue(valNode) );
        }
        return properties;
    }

    protected static Value parseAsLiteralOrArrayValue( final JsonNode valNode ) {
        if ( valNode.isArray() ) {
            final Iterator<JsonNode> it = valNode.elements();
            final List<LiteralValue> list = new ArrayList<>( valNode.size() );
            while ( it.hasNext() ) {
                final JsonNode elmt = it.next();
                final LiteralValue v = (LiteralValue) parseAsLiteralOrArrayValue(elmt);
                list.add(v);
            }

            return new ArrayValue(list);
        }

        final Object value;
        if ( valNode.isInt() )
            value = Integer.valueOf( valNode.asInt() );
        else if ( valNode.isDouble() )
            value = Double.valueOf( valNode.asDouble() );
        else if ( valNode.isBoolean() )
            value = Boolean.valueOf( valNode.asBoolean() );
        else if ( valNode.isLong() )
            value = Long.valueOf( valNode.asLong() );
        else if ( valNode.isTextual() )
            value = valNode.asText();
        else
            throw new IllegalArgumentException( "Unexpected type of value node in record entry: " + valNode.toString() );

        return new LiteralValue(value);
    }

}
