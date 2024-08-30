package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.json.JsonException;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;
import org.apache.jena.atlas.lib.PairOfSameType;
import org.apache.jena.datatypes.xsd.impl.XSDPlainType;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.JSON2SolutionGraphConverter;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLSchema;

/**
 * An implementation of the JSON2SolutionMappings approach
 */
public class JSON2SolutionGraphConverterImpl implements JSON2SolutionGraphConverter {

    final protected GraphQL2RDFConfiguration config;
    final protected GraphQLSchema schema;

    public JSON2SolutionGraphConverterImpl( final GraphQL2RDFConfiguration config, 
                                            final GraphQLSchema schema ) {
        this.config = config;
        this.schema = schema;
    }

    @Override
    public Model translateJSON( final JsonObject jsonObj ) throws ParseException, JsonException {
        final Model solutionGraph = ModelFactory.createDefaultModel();
        
        // Maps a GraphQL <ID,Type> to a blank node representing the GraphQL object
        final Map<PairOfSameType<String>,Resource> blankNodes = new HashMap<>();

        // Verify that the JSONResponse contains correct data throw error
        if ( ! jsonObj.hasKey("data") ) {
            throw new ParseException("The json response did not contain a data key.");
        }

        final JsonObject json = jsonObj.getObj("data");

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

        return solutionGraph;
    }

    /**
     * Parses through a JsonObject recursively and expands @param blankNodes and @param solutionGraph.
     * @return a Resource in order to create a triple with the parent and the child resource.
     * @throws ParseException if the json object does not contain a valid "id" key.
     */
    protected Resource parseJSON(final JsonObject root, 
                                 final Map<PairOfSameType<String>,Resource> blankNodes, 
                                 final Model solutionGraph) throws ParseException, JsonException{

        // Get the key that represent the current json objects id
        final String idKey = root.keySet()
            .stream()
            .filter(s -> s.startsWith(config.getJsonIDKeyPrefix()))
            .findAny()
            .orElse(null);

        if(idKey == null){
            throw new ParseException("JsonObject root did not provide a valid id key.");
        }

        // Initialize the necessary data needed for parsing the current json object
        final String graphqlID = root.getString(idKey);
        final String graphqlType = config.removeJsonKeyPrefix(idKey);
        final PairOfSameType<String> currentNodeID = new PairOfSameType<String>(graphqlID, graphqlType);
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
            final String graphqlFieldName = config.removeJsonKeyPrefix(key);

            if(value.isNull()){
                continue;
            }

            if(value.isArray() && key.startsWith(config.getJsonObjectKeyPrefix())){
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
            else if(value.isObject() && key.startsWith(config.getJsonObjectKeyPrefix())){
                // Value is a single object
                currentBlankNode.addProperty(
                    solutionGraph.createProperty(config.mapFieldToProperty(graphqlType,graphqlFieldName)),
                    parseJSON(value.getAsObject(),blankNodes,solutionGraph));
            }
            else if(value.isArray() && key.startsWith(config.getJsonScalarKeyPrefix())){
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
            else if(value.isPrimitive() && key.startsWith(config.getJsonScalarKeyPrefix())){
                // Value is a single scalar value
                currentBlankNode.addLiteral(
                    solutionGraph.createProperty(config.mapFieldToProperty(graphqlType,graphqlFieldName)), 
                    jsonPrimitiveToLiteral(value, graphqlType, graphqlFieldName, solutionGraph));
            }
        }

        return currentBlankNode;
    }

    /**
     * Utility function used to create a Literal from a json value @param primitive
     * usable by @param model. @param graphqlTypeName and @param graphqlFieldName are 
     * used to fetch value type data from the GraphQL endpoint to ensure that the correct 
     * XML datatype is used when creating the literal.
     */
    protected Literal jsonPrimitiveToLiteral(final JsonValue primitive, final String graphqlTypeName, 
            final String graphqlFieldName, final Model model){

        assert primitive.isPrimitive();

        final String valueType = schema.getGraphQLFieldValueType(graphqlTypeName, graphqlFieldName);

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
}
