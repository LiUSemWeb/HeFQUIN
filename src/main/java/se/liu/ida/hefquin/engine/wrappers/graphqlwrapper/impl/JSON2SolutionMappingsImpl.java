package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;
import org.apache.jena.datatypes.xsd.impl.XSDPlainType;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.JSON2SolutionMappings;

/**
 * An implementation of the JSON2SolutionMappings approach
 */
public class JSON2SolutionMappingsImpl implements JSON2SolutionMappings {

    final protected GraphQL2RDFConfiguration config;
    final protected GraphQLEndpoint endpoint;

    public JSON2SolutionMappingsImpl(final GraphQL2RDFConfiguration config, 
                                     final GraphQLEndpoint endpoint){
        this.config = config;
        this.endpoint = endpoint;
    }

    @Override
    public List<SolutionMapping> translateJSON( final JSONResponse jsonResponse, 
                                                final SPARQLQuery originalQuery ) throws ParseException {

        final JsonObject outerJson = jsonResponse.getJsonObject();
        final Query jenaQuery = originalQuery.asJenaQuery();
        final Model solutionGraph = ModelFactory.createDefaultModel();
        final List<SolutionMapping> solutionMappings = new ArrayList<>();
        
        // Maps a GraphQL type then its ID to a blank node representing the GraphQL object
        final Map<BlankNodeID,Resource> blankNodes = new HashMap<>();

        // Verify that the JSONResponse contains correct data throw error
        if(!outerJson.hasKey("data")){
            throw new ParseException("The json response did not contain a data key.");
        }

        final JsonObject json = outerJson.getObj("data");

        // Parse through each individual entrypoint in the JSON 
        // (the return value from parseJSON isn't necessary on the original call)
        for(final String entrypoint : json.keySet()){

            final JsonValue e = json.get(entrypoint);

            if(e.isArray()){
                for(final JsonValue v : e.getAsArray()){
                    if(v.isObject()){
                        parseJSON(v.getAsObject(), blankNodes, solutionGraph);
                    } 
                }
            }
            else if(e.isObject()){
                parseJSON(e.getAsObject(), blankNodes, solutionGraph);
            }
        }

        // Get the solution mappings
        try (final QueryExecution qexec = QueryExecutionFactory.create(jenaQuery, solutionGraph)) {
            final ResultSet results = qexec.execSelect();
            for (; results.hasNext();) {
                solutionMappings.add(new SolutionMappingImpl(results.nextBinding()));
            }
        }

        return solutionMappings;
    }

    /**
     * Parses through a JsonObject recursively and expands @param blankNodes and @param solutionGraph.
     * @return a Resource in order to create a triple with the parent and the child resource.
     * @throws ParseException if the json object does not contain a valid "id" key.
     */
    protected Resource parseJSON(final JsonObject root, 
                                 final Map<BlankNodeID,Resource> blankNodes, 
                                 final Model solutionGraph) throws ParseException {

        // Get the key that represent the current json objects id
        final String idKey = root.keySet().stream().filter(s -> s.startsWith("id")).findAny().orElse(null);

        if(idKey == null){
            throw new ParseException("JsonObject root did not provide a valid id key.");
        }

        // Initialize the necessary data needed for parsing the current json object
        final String graphqlID = root.getString(idKey);
        root.keySet().remove(idKey);
        final String graphqlType = removeJsonKeyPrefix(idKey);
        final BlankNodeID currentNodeID = new BlankNodeID(graphqlID, graphqlType);
        final Resource currentBlankNode;

        // Create the blank node associated with the GraphQL type and id (if none exist yet)
        if(!blankNodes.containsKey(currentNodeID)){
            currentBlankNode = solutionGraph.createResource();
            blankNodes.put(currentNodeID, currentBlankNode);
            currentBlankNode.addProperty(solutionGraph.createProperty(config.getClassMembershipURI()),
                solutionGraph.createResource(config.mapTypeToClass(graphqlType)));
        }
        else{
            currentBlankNode = blankNodes.get(currentNodeID);
        }

        /*
            Parse through each key-value pair in the json object, If the value is another object or
            an array of objects, do a recursive call to parseJSON for each of them.
            If the value is a scalar or an array of scalars, create triple(s) using the current blank node
            and the scalar value(s).
        */
        for(final String key : root.keySet()){
            final JsonValue value = root.get(key);
            final String graphqlFieldName = removeJsonKeyPrefix(key);

            if(value.isNull()){
                continue;
            }

            if(value.isArray() && key.startsWith("object")){
                // Value is array of objects
                for(final JsonValue arrayObject : value.getAsArray()){
                    if(arrayObject.isNull()){
                        continue;
                    }
                    currentBlankNode.addProperty(
                        solutionGraph.createProperty(config.mapFieldToProperty(graphqlType,graphqlFieldName)),
                        parseJSON(arrayObject.getAsObject(),blankNodes,solutionGraph));
                }
            }
            else if(value.isObject()){
                // Value is a single object
                currentBlankNode.addProperty(
                    solutionGraph.createProperty(config.mapFieldToProperty(graphqlType,graphqlFieldName)),
                    parseJSON(value.getAsObject(),blankNodes,solutionGraph));
            }
            else if(value.isArray() && key.startsWith("scalar")){
                // Value is array of scalar values
                for(final JsonValue arrayScalar : value.getAsArray()){
                    if(arrayScalar.isNull()){
                        continue;
                    }
                    currentBlankNode.addLiteral(
                        solutionGraph.createProperty(config.mapFieldToProperty(graphqlType,graphqlFieldName)), 
                        jsonPrimitiveToLiteral(arrayScalar, graphqlType, graphqlFieldName, solutionGraph));
                    
                }
            }
            else if(value.isPrimitive()){
                // Value is a single scalar value
                currentBlankNode.addLiteral(
                    solutionGraph.createProperty(config.mapFieldToProperty(graphqlType,graphqlFieldName)), 
                    jsonPrimitiveToLiteral(value, graphqlType, graphqlFieldName, solutionGraph));
            }
        }

        return currentBlankNode;
    }

    /**
     * Removes the prefix from the json key @param key
     */
    protected String removeJsonKeyPrefix(final String key){
        final int splitIndex = key.indexOf("_") + 1;
        return key.substring(splitIndex);
    }

    /**
     * Utility function used to create a Literal from a json value @param primitive
     * usable by @param model. @param graphqlTypeName and @param graphqlFieldName are 
     * used to fetch value type data from the GraphQLEndpoint to ensure that the correct 
     * XML datatype is used when creating the literal.
     */
    protected Literal jsonPrimitiveToLiteral(final JsonValue primitive, final String graphqlTypeName, 
            final String graphqlFieldName, final Model model){

        assert primitive.isPrimitive();

        final String valueType = endpoint.getGraphQLFieldValueType(graphqlTypeName, graphqlFieldName);

        // The scalar value types from GraphQL ( GraphQL String, ID and custom scalar types etc. are assumed as XSD Strings)
        switch(valueType){
            case "Int":
            case "Int!":
                return model.createTypedLiteral(primitive.getAsNumber().value(), XSDPlainType.XSDint);
            case "Boolean":
            case "Boolean!":
                return model.createTypedLiteral(primitive.getAsBoolean().value(), XSDPlainType.XSDboolean);
            case "Float":
            case "Float!":
                return model.createTypedLiteral(primitive.getAsNumber().value(), XSDPlainType.XSDdouble);
            default:
                return model.createTypedLiteral(primitive.getAsString().value(), XSDPlainType.XSDstring);
        }
    }

    /**
     * Utility class used to represent a double valued key for Maps etc.
     */
    protected class BlankNodeID {
        final String graphqlID;
        final String graphqlType;

        public BlankNodeID(final String graphqlID, final String graphqlType){
            this.graphqlID = graphqlID;
            this.graphqlType = graphqlType;
        }

        @Override
        public boolean equals(final Object o){
            if(this == o){
                return true;
            }

            if(!(o instanceof BlankNodeID)){
                return false;
            }

            final BlankNodeID that = (BlankNodeID) o;

            return this.graphqlID.equals(that.graphqlID) && this.graphqlType.equals(that.graphqlType);
        }

        @Override
        public int hashCode(){
            return Objects.hash(graphqlID,graphqlType);
        }
    }
}
