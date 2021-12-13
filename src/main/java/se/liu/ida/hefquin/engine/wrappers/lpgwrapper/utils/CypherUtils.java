package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.liu.ida.hefquin.engine.federation.access.Neo4JException;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.TableRecordImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns.PropertyListReturnStatement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CypherUtils {

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
                    final JsonNode metaCol = metaIterator.next();
                    final RecordEntry entry = RecordEntryFactory.create(col, metaCol, names.get(counter));
                    entries.add(entry);
                    counter++;
                }
                records.add(new TableRecordImpl(entries));
            }
        }
        return records;
    }

    public static boolean isPropertyColumn(final CypherQuery query, final CypherVar colName) {
        if (query instanceof CypherMatchQuery) {
            return isPropertyColumnPriv((CypherMatchQuery) query, colName);
        }
        else if (query instanceof CypherUnionQuery) {
            for (final CypherMatchQuery q : ((CypherUnionQuery) query).getUnion()) {
                if (isPropertyColumnPriv(q, colName)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static boolean isPropertyColumnPriv( final CypherMatchQuery query, final CypherVar colName ) {
        final List<ReturnStatement> returns = query.getReturnExprs();
        for (final ReturnStatement r : returns) {
            if (colName.equals(r.getAlias()) && r instanceof PropertyListReturnStatement) {
                return true;
            }
        }
        return false;
    }

}
