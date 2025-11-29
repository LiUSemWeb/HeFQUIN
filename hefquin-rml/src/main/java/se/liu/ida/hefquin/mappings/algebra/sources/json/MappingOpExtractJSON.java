package se.liu.ida.hefquin.mappings.algebra.sources.json;

import java.util.Map;

import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtract;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

/**
 * This class is a version of {@link MappingOpExtract} specifically for
 * data objects of {@link SourceTypeJSON}.
 */
public class MappingOpExtractJSON extends MappingOpExtract< JsonObject,
                                                            JsonObject,
                                                            JsonScalarValue,
                                                            JsonPathQuery,
                                                            JsonPathQuery >
{
	public MappingOpExtractJSON( final SourceReference sr,
	                             final JsonPathQuery query,
	                             final Map<String, JsonPathQuery> P ) {
		super(sr, SourceTypeJSON.instance, query, P);
	}
}
