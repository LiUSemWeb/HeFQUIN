package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.atlas.json.JsonException;

import se.liu.ida.hefquin.engine.federation.access.JSONResponse;

public interface JSON2SolutionGraphConverter {
    
    /**
     * Translates a JSON response @param jsonResponse to a solution graph (model) containing
     * triples created by parsing the json.
     * @throws ParseException if the json structure for whatever reason isn't valid, thus making
     * it un-parsable.
     */
    public Model translateJSON(final JSONResponse jsonResponse) throws ParseException, JsonException;
}
