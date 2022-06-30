package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import java.util.List;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;

public interface JSON2SolutionMappings {
    
    /**
     * Translates a JSON response to valid solution mappings where @param originalQuery is the
     * SPARQL query originally used create the GraphQL query and receive @param jsonResponse.
     * @throws ParseException if the json structure for whatever reason isn't valid, thus making
     * it un-parsable.
     */
    public List<SolutionMapping> translateJSON(final JSONResponse jsonResponse, final SPARQLQuery originalQuery) throws ParseException;
}
