package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.liu.ida.hefquin.engine.federation.access.Neo4JException;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.TableRecordImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.*;

import java.util.*;

public class CypherUtils {

    /**
     * This method parses a JSON response obtained from a Neo4j HTTP server into a list of POJOs
     * @throws JsonProcessingException if the received body is not valid JSON
     * @throws Neo4JException if the server responds with an error object
     */
    public static List<TableRecord> parse(final String body) throws JsonProcessingException, Neo4JException {
        final ObjectMapper mapper = new ObjectMapper();
        final List<TableRecord> records = new LinkedList<>();
        final JsonNode root = mapper.readTree(body);
        final JsonNode errors = root.get("errors");
        if (errors.isArray() && !errors.isEmpty()) {
            throw new Neo4JException(errors.textValue());
        }
        final JsonNode results = root.get("results");
        for (final JsonNode r : results) {
            final JsonNode columns = r.get("columns");
            final JsonNode data = r.get("data");
            final List<CypherVar> names = new ArrayList<>();
            for (final JsonNode c : columns) {
                names.add(new CypherVar(c.asText()));
            }
            for (final JsonNode e : data) {
                final JsonNode row = e.get("row");
                final JsonNode meta = e.get("meta");
                final Iterator<JsonNode> metaIterator = meta.iterator();
                final List<RecordEntry> entries = new LinkedList<>();
                int counter = 0;
                for (final JsonNode col : row) {
                    final RecordEntry entry = RecordEntryFactory.create(col, metaIterator, names.get(counter));
                    entries.add(entry);
                    counter++;
                }
                records.add(new TableRecordImpl(entries));
            }
        }
        return records;
    }

    public static Object replaceVariable(Map<CypherVar, CypherVar> equivalences, final CypherExpression ex) {
        if (Collections.disjoint(equivalences.keySet(), ex.getVars())) return ex;
        final VariableReplacementVisitor visitor = new VariableReplacementVisitor(equivalences);
        visitor.visit(ex);
        return visitor.getResult();
    }
}
