package se.liu.ida.hefquin.engine.wrappers.graphql;

import org.apache.jena.atlas.json.JsonException;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;
import org.apache.jena.rdf.model.Model;

public interface JSON2SolutionGraphConverter
{
    /**
     * Translates a JSON object (obtained as a response to a GraphQL request)
     * into a solution graph (model) containing triples created by parsing
     * the JSON object.
     * @throws ParseException if the JSON structure for whatever reason isn't valid, thus making
     * it un-parsable.
     */
    Model translateJSON( final JsonObject jsonObj ) throws ParseException, JsonException;
}
