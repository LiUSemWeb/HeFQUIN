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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
            throw new Neo4JException(errors.get(0).textValue());
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

    /**
     * This method checks if a given cypher query has a given column name, and if said column is from type
     * PropertyListReturnStatement. If the query is a {@link CypherUnionQuery}, the method just checks if
     * any of the {@link CypherMatchQuery} of the union satisfies the condition.
     * This class will be deprecated.
     */
    public static boolean isPropertyColumn(final CypherQuery query, final CypherVar colName) {
        if (query instanceof CypherMatchQuery) {
            return isPropertyColumn( (CypherMatchQuery) query, colName );
        }
        else if (query instanceof CypherUnionQuery) {
            for (final CypherMatchQuery q : ((CypherUnionQuery) query).getSubqueries()) {
                if (isPropertyColumn(q, colName)) {
                    return true;
                }
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported implementation of Cypher Query (" + query.getClass().getName() +")");
        }
        return false;
    }

    public static boolean isPropertyColumn( final CypherMatchQuery query, final CypherVar colName ) {
        final List<AliasedExpression> returns = query.getReturnExprs();
        for (final AliasedExpression r : returns) {
            if (colName.equals(r.getAlias()) && (r.getExpression() instanceof PropertyAccessExpression
                                             || r.getExpression() instanceof PropertyAccessWithVarExpression)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLabelColumn(final CypherQuery query, final CypherVar colName) {
        if (query instanceof CypherMatchQuery) {
            return isLabelColumn( (CypherMatchQuery) query, colName );
        }
        else if (query instanceof CypherUnionQuery) {
            for (final CypherMatchQuery q : ((CypherUnionQuery) query).getSubqueries()) {
                if (isLabelColumn(q, colName)) {
                    return true;
                }
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported implementation of Cypher Query (" + query.getClass().getName() +")");
        }
        return false;
    }

    public static boolean isLabelColumn(final CypherMatchQuery query, final CypherVar colName) {
        final List<AliasedExpression> returns = query.getReturnExprs();
        for (final AliasedExpression r : returns) {
            if (colName.equals(r.getAlias()) && r.getExpression() instanceof LabelsExpression) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRelationshipTypeColumn(final CypherQuery query, final CypherVar colName) {
        if (query instanceof CypherMatchQuery) {
            return isRelationshipTypeColumn( (CypherMatchQuery) query, colName );
        }
        else if (query instanceof CypherUnionQuery) {
            for (final CypherMatchQuery q : ((CypherUnionQuery) query).getSubqueries()) {
                if (isRelationshipTypeColumn(q, colName)) {
                    return true;
                }
            }
        }
        else {
            throw new IllegalArgumentException("Unsupported implementation of Cypher Query (" + query.getClass().getName() +")");
        }
        return false;
    }

    public static boolean isRelationshipTypeColumn(final CypherMatchQuery query, final CypherVar colName) {
        final List<AliasedExpression> returns = query.getReturnExprs();
        for (final AliasedExpression r : returns) {
            if (colName.equals(r.getAlias()) && r.getExpression() instanceof TypeExpression) {
                return true;
            }
        }
        return false;
    }
}
