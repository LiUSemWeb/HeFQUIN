package se.liu.ida.hefquin.base.utils;

import java.io.PrintStream;
import java.util.List;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonBoolean;
import org.apache.jena.atlas.json.JsonNumber;
import org.apache.jena.atlas.json.JsonNull;

/**
 * A utility class for converting and printing {@link Stats} objects in JSON format.
 */
public class StatsPrinterJSON
{
	/**
	 * Converts and prints the given {@link Stats} object as JSON to the specified {@link PrintStream}.
	 *
	 * @param s         the {@link Stats} object to print
	 * @param out       the output stream to print to
	 * @param recursive whether nested {@link Stats} entries should be printed recursively
	 */
	public static void print( final Stats s, final PrintStream out, final boolean recursive ) {
		final JsonObject stats = statsAsJson( s, recursive );
		out.print( stats.toString() );
	}

	/**
	 * Converts the given {@link Stats} object as JSON. Nested {@link Stats} entries will be converted recursively.
	 *
	 * @param s the {@link Stats} object to convert
	 */
	public static JsonObject statsAsJson( final Stats s ) {
		return statsAsJson( s, true );
	}

	/**
	 * Converts the given {@link Stats} object to a JSON object.
	 *
	 * @param s         the {@link Stats} object to convert
	 * @param recursive whether nested {@link Stats} entries should be converted recursively
	 * @return a JSON object representation of the {@link Stats}
	 */
	public static JsonObject statsAsJson( final Stats s, final boolean recursive ) {
		final JsonObject stats = new JsonObject();
		if ( s == null || s.isEmpty() ) {
			return stats;
		}
		for ( final String entryName : s.getEntryNames() ) {
			final Object entry = s.getEntry( entryName );
			stats.put( entryName, asJson( entry, recursive ) );
		}

		return stats;
	}

	/**
	 * Converts the given {@link Stats} object to a JSON object.
	 *
	 * @param s         the {@link Stats} object to convert
	 * @param recursive whether nested {@link Stats} entries should be converted recursively
	 * @return a JSON object representation of the {@link Stats}
	 */
	private static JsonValue asJson( final Object entry, final boolean recursive ) {
		if ( entry == null ) {
			return JsonNull.instance;
		}

		if ( entry instanceof Stats stats ) {
			if ( recursive ) {
				return statsAsJson( stats, recursive );
			}
			return new JsonString( "..." );
		}

		// List of objects
		if ( entry instanceof List<?> list ) {
			final JsonArray jsonArray = new JsonArray();
			for ( final Object o : list ) {
				jsonArray.add( asJson( o, recursive ) );
			}
			return jsonArray;
		}

		// Array of objects
		if ( entry instanceof Object[] array ) {
			final JsonArray arr = new JsonArray();
			for ( Object o : array ) {
				arr.add( asJson( o, recursive ) );
			}
			return arr;
		}

		// Array of primitives
		if ( entry instanceof int[] a ) {
			return asJson( ArrayUtils.toObject( a ), recursive );
		}
		if ( entry instanceof long[] a ) {
			return asJson( ArrayUtils.toObject( a ), recursive );
		}
		if ( entry instanceof double[] a ) {
			return asJson( ArrayUtils.toObject( a ), recursive );
		}
		if ( entry instanceof float[] a ) {
			return asJson( ArrayUtils.toObject( a ), recursive );
		}
		if ( entry instanceof boolean[] a ) {
			return asJson( ArrayUtils.toObject( a ), recursive );
		}

		// JSON primitives
		if ( entry instanceof Boolean b ) {
			return new JsonBoolean( b );
		}

		if ( entry instanceof Number num ) {
			if ( num instanceof Double d && (Double.isNaN( d ) || Double.isInfinite( d )) ) {
				return new JsonString( d.toString() );
			}
			if ( num instanceof Float f && (Float.isNaN( f ) || Float.isInfinite( f )) ) {
				return new JsonString( f.toString() );
			}
			return JsonNumber.value( num.doubleValue() );
		}

		// default to string
		return new JsonString( entry.toString() );
	}
}
