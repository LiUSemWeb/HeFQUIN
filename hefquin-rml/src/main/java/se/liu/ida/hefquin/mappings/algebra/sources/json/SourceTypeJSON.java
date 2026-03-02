package se.liu.ida.hefquin.mappings.algebra.sources.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;

import net.minidev.json.JSONArray;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceType;

public class SourceTypeJSON implements SourceType< JsonObject,
                                                   JsonObject,
                                                   JsonElement,
                                                   JsonPathQuery,
                                                   JsonPathQuery >
{
	public static final SourceTypeJSON instance = new SourceTypeJSON();

	protected static final List<JsonObject> empty1 = List.of();
	protected static final List<JsonElement> empty2 = List.of();

	protected static final Configuration conf = new Configuration
			.ConfigurationBuilder()
			.options(Option.ALWAYS_RETURN_LIST)
			.build();

	protected SourceTypeJSON() {}

	@Override
	public boolean isRelevantDataObject( final DataObject d ) {
		return d instanceof JsonObject;
	}

	@Override
	public List<JsonObject> eval( final JsonPathQuery query,
	                              final JsonObject input ) {
		final ReadContext ctx = input.getReadContext();
		final JsonPath jsonPath = query.getJsonPath();

		final Object queryResult;
		try {
			queryResult = ctx.read(jsonPath);
		}
		catch ( final PathNotFoundException e ) {
			return empty1;
		}

		if ( ! (queryResult instanceof List) ) {
			// We shouldn't end up here because 'conf'
			// is set up to always return a list.
			throw new IllegalArgumentException("Unexpected type of result: " + queryResult.getClass().getName() );
		}

		final List<?> resultList = (List<?>) queryResult;
		final List<JsonObject> output = new ArrayList<>( resultList.size() );
		for ( final Object elmt : resultList ) {
			final JsonElement jsonElmt = createJsonElement(elmt);

			if ( jsonElmt != null
			     && jsonElmt instanceof JsonObject obj ) {
				output.add(obj);
			}
		}

		return output;
	}

	@Override
	public List<JsonElement> eval( final JsonPathQuery query,
	                               final JsonObject input,
	                               final JsonObject cxtObj ) {
		final ReadContext ctx = cxtObj.getReadContext();
		final JsonPath jsonPath = query.getJsonPath();

		final Object queryResult;
		try {
			queryResult = ctx.read(jsonPath);
		}
		catch ( final PathNotFoundException e ) {
			return empty2;
		}

		if ( ! (queryResult instanceof List) ) {
			// We shouldn't end up here because 'conf'
			// is set up to always return a list.
			throw new IllegalArgumentException("Unexpected type of result: " + queryResult.getClass().getName() );
		}

		final List<?> resultList = (List<?>) queryResult;
		final List<JsonElement> output = new ArrayList<>( resultList.size() );
		for ( final Object elmt : resultList ) {
			final JsonElement jsonElmt = createJsonElement(elmt);

			// nulls are ignored for the result.
			if ( jsonElmt != null ) output.add(jsonElmt);
		}

		return output;
	}

	protected JsonElement createJsonElement( final Object elmt ) {
		if ( elmt instanceof Map map ) {
			final DocumentContext mapAsDocCtx = JsonPath.parse(map, conf);
			return new JsonObject(mapAsDocCtx);
		}

		if ( elmt instanceof JSONArray arr ) {
			return new JsonArray(arr);
		}

		// If it is neither a map nor a list, we assume it is a scalar value.
		if ( elmt == null ) return null;
		if ( elmt instanceof String s )  return new JsonScalarValue(s);
		if ( elmt instanceof Integer i ) return new JsonScalarValue(i);
		if ( elmt instanceof Double d )  return new JsonScalarValue(d);
		if ( elmt instanceof Boolean b ) return new JsonScalarValue(b);

		throw new IllegalArgumentException("The result of the JSONPath query has an unexpected type: " + elmt.getClass().getName() );
	}

	@Override
	public Node cast( final JsonElement elmt ) {
		if ( elmt instanceof JsonScalarValue sv ) {
			if ( sv.getValue() instanceof Integer i )
				return NodeFactory.createLiteralByValue( i, XSDDatatype.XSDinteger );

			return NodeFactory.createLiteralByValue( sv.getValue() );
		}

		if ( elmt instanceof JsonObject obj ) {
			final String lex = obj.getReadContext().jsonString();
			return NodeFactory.createLiteralDT( lex, CompositeDatatypeMap.type );
		}

		if ( elmt instanceof JsonArray arr ) {
			final String lex = arr.getArray().toJSONString();
			return NodeFactory.createLiteralDT( lex, CompositeDatatypeList.type );
		}

		throw new IllegalArgumentException("Unexpected type of JSON element: " + elmt.getClass().getName() );
	}

}
