package se.liu.ida.hefquin.engine.federation.access.utils;

import com.fasterxml.jackson.databind.JsonNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;

import java.util.*;

public class RecordEntryFactory {
    public static RecordEntry create(final JsonNode col, final JsonNode metaCol, final CypherVar name) {
        Value val;
        final JsonNode type = metaCol.get("type");
        if (type != null) {
            if (type.asText().equals("node")) {
                val = new LPGNodeValue(parseAsNode(col, metaCol.get("id")));
            } else if (type.asText().equals("relationship")) {
                val = new LPGEdgeValue(parseAsEdge(col, metaCol.get("id")));
            } else {
                val = null;
            }
        } else {
            if (col.isArray()) {
                val = new ListValue(parseAsList(col));
            } else if (col.isObject()) {
                val = new MapValue(parseAsMap(col));
            } else {
                val = new LiteralValue(col.asText());
            }
        }
        return new RecordEntryImpl(name, val);
    }

    private static Map<String, Object> parseAsMap(final JsonNode col) {
        Map<String, Object> values = new HashMap<>();
        for (final Iterator<String> it = col.fieldNames(); it.hasNext(); ) {
            final String name = it.next();
            values.put(name, col.get(name).asText());
        }
        return values;
    }

    private static List<Object> parseAsList(final JsonNode col) {
        List<Object> values = new LinkedList<>();
        for (final Iterator<JsonNode> it = col.elements(); it.hasNext(); ) {
            final JsonNode item = it.next();
            values.add(item.asText());
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

    private static Map<String, Value> parseProperties(JsonNode col) {
        final Map<String, Value> properties = new HashMap<>();
        for (final Iterator<String> it = col.fieldNames(); it.hasNext(); ) {
            final String name = it.next();
            final JsonNode val = col.get(name);
            properties.put(name, new LiteralValue(val.asText()));
        }
        return properties;
    }
}
