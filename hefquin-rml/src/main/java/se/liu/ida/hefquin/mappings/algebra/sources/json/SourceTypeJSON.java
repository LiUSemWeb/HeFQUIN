package se.liu.ida.hefquin.mappings.algebra.sources.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceType;

public class SourceTypeJSON implements SourceType< JsonObject,
                                                   JsonObject,
                                                   JsonScalarValue,
                                                   JsonPathQuery,
                                                   JsonPathQuery >
{
	public static final SourceTypeJSON instance = new SourceTypeJSON();

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

		final Object queryResult = ctx.read(jsonPath);

		if ( queryResult instanceof List list ) {
			final List<JsonObject> output = new ArrayList<>( list.size() );
			for ( final Object elmt : list ) {
				final DocumentContext elmtAsDocCtx = JsonPath.parse(elmt);
				output.add( new JsonObject(elmtAsDocCtx) );
			}

			return output;
		}

		if ( queryResult instanceof Map map ) {
			final DocumentContext mapAsDocCtx = JsonPath.parse(map);
			return List.of( new JsonObject(mapAsDocCtx) );
		}

		throw new IllegalArgumentException("The result of the JSONPath query has an unexpected type: " + queryResult.getClass().getName() );
	}

	@Override
	public List<JsonScalarValue> eval( final JsonPathQuery query,
	                                   final JsonObject input,
	                                   final JsonObject cxtObj ) {
		final ReadContext ctx = cxtObj.getReadContext();
		final JsonPath jsonPath = query.getJsonPath();

		final Object queryResult = ctx.read(jsonPath);

		if ( queryResult instanceof List list ) {
			final List<JsonScalarValue> output = new ArrayList<>( list.size() );
			for ( final Object elmt : list ) {
				try {
					final JsonScalarValue v = createJsonScalarValue(elmt);
					output.add(v);
				}
				catch ( final Exception e ) {
					// ignore, elements that are not scalar values are ignored for the result
				}
			}

			return output;
		}

		final JsonScalarValue v;
		try {
			v = createJsonScalarValue(queryResult);
		}
		catch ( final Exception e ) {
			return List.of();
		}

		return List.of(v);
	}

	protected JsonScalarValue createJsonScalarValue( final Object obj ) {
		if ( obj == null ) return null;
		if ( obj instanceof String s )  return new JsonScalarValue(s);
		if ( obj instanceof Integer i ) return new JsonScalarValue(i);
		if ( obj instanceof Double d )  return new JsonScalarValue(d);
		if ( obj instanceof Boolean b ) return new JsonScalarValue(b);

		throw new IllegalArgumentException("The result of the JSONPath query has an unexpected type: " + obj.getClass().getName() );
	}

	@Override
	public Node cast( final JsonScalarValue d ) {
		return NodeFactory.createLiteralByValue( d.getValue() );
	}

}
