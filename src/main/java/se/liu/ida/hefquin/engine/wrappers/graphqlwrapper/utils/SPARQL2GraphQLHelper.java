package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.json.JsonBoolean;
import org.apache.jena.atlas.json.JsonNumber;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLIDPath;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLObjectPath;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLScalarPath;

/**
 * Provides helper functions for creating the necessary data in a GraphQL query.
 */
public class SPARQL2GraphQLHelper {
    /**
     * Recursive function used to add fields from the triple patterns in a given SGP
     */
    public static Set<String> addSgp(final Map<Node,Set<TriplePattern>> subgraphPatterns, final Map<TriplePattern,Node> connectors, 
            final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint, final Node subgraphNode, 
            final String currentPath, final String sgpType){

        final Set<String> finishedFieldPaths = new HashSet<>();

        // Necessary id field present in all objects used to indentify the GraphQL object
        finishedFieldPaths.add(currentPath + new GraphQLIDPath(sgpType));

        // Retrieve necessary information about current sgp
        final boolean addAllFields = hasVariablePredicate(subgraphPatterns.get(subgraphNode));
        final Set<TriplePattern> tpConnectors = new HashSet<>();
        for(final TriplePattern t : subgraphPatterns.get(subgraphNode)){
            if(connectors.containsKey(t)){
                tpConnectors.add(t);
            }
        }

        // Add fields that has nested objects to other SGPs using recursion
        for(final TriplePattern currentTP : tpConnectors){
            final Triple t = currentTP.asJenaTriple();
            final Node nestedSubgraphNode = connectors.get(currentTP);
            final Node predicate = t.getPredicate();

            if(predicate.isVariable()){
                // If the current TP predicate is a variable we need to add everything
                final Set<String> allObjectURI = URI2GraphQLHelper.getPropertyURIs(sgpType, GraphQLFieldType.OBJECT,config,endpoint);

                for(final String uri : allObjectURI){
                    finishedFieldPaths.addAll(addObjectField(subgraphPatterns,connectors,config,endpoint,
                        nestedSubgraphNode,currentPath,sgpType,uri));
                }

            }
            else if(predicate.isURI() && URI2GraphQLHelper.containsPropertyURI(predicate.getURI(),config,endpoint)){
                // If the current TP predicate is a URI, add the GraphQL field it corresponds to
                finishedFieldPaths.addAll(addObjectField(subgraphPatterns,connectors,config,endpoint,
                    nestedSubgraphNode,currentPath,sgpType,predicate.getURI()));
            }
        }

        // Add fields that doesn't link to another SGP, 
        if(addAllFields){
            // If variable predicate exist in the current SGP, then query for everything in current object
            final Set<String> allObjectURI = URI2GraphQLHelper.getPropertyURIs(sgpType,GraphQLFieldType.OBJECT,config,endpoint);
            final Set<String> allScalarURI = URI2GraphQLHelper.getPropertyURIs(sgpType,GraphQLFieldType.SCALAR,config,endpoint);

            for(final String uri : allObjectURI){
                finishedFieldPaths.add(addEmptyObjectField(config,endpoint,currentPath,sgpType,uri));
            }

            for(final String uri : allScalarURI){
                finishedFieldPaths.add(addScalarField(config,currentPath,uri));
            }

        }
        else{
            // If no variable predicate exist, only the necessary fields from the SGP have to be added.
            for(final TriplePattern t : subgraphPatterns.get(subgraphNode)){
                final Node predicate = t.asJenaTriple().getPredicate();
                if(predicate.isURI() && URI2GraphQLHelper.containsPropertyURI(predicate.getURI(),config,endpoint)){
                    final String fieldName = config.mapPropertyToField(predicate.getURI());
                    if(endpoint.getGraphQLFieldType(sgpType,fieldName) == GraphQLFieldType.OBJECT){
                        finishedFieldPaths.add(addEmptyObjectField(config,endpoint,currentPath,sgpType,predicate.getURI()));
                    }
                    else{
                        finishedFieldPaths.add(addScalarField(config,currentPath,predicate.getURI()));
                    }
                }
            }
        }

        return finishedFieldPaths;
    }


    /**
     * Helper function to add a field to the query that represents a nested object. Fields in that nested 
     * object are added recursively through addSgp
     */
    public static Set<String> addObjectField(final Map<Node,Set<TriplePattern>> subgraphPatterns, final Map<TriplePattern,Node> connectors, 
            final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint, final Node nestedSubgraphNode, 
            final String currentPath, final String sgpType, final String predicateURI){

        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        final String newPath = currentPath + new GraphQLObjectPath(alias, fieldName);
        final String nestedType = endpoint.getGraphQLFieldValueType(sgpType, fieldName);

        return addSgp(subgraphPatterns,connectors,config,endpoint,
            nestedSubgraphNode,newPath,nestedType);
    }

    /**
     * Helper function to add a field to the query that represents a nested object that only needs an id field (no more recursive calls)
     */
    public static String addEmptyObjectField(final GraphQL2RDFConfiguration config,final GraphQLEndpoint endpoint, 
            final String currentPath, final String sgpType, final String predicateURI){
        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        final String newPath = currentPath + new GraphQLObjectPath(alias, fieldName);
        final String nestedType = endpoint.getGraphQLFieldValueType(sgpType, fieldName);
        return newPath + new GraphQLIDPath(nestedType);
    }

    /**
     * Helper function to add a field to the query that represents a scalar value
     */
    public static String addScalarField(final GraphQL2RDFConfiguration config, final String currentPath, 
            final String predicateURI){
        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        return currentPath + new GraphQLScalarPath(alias, fieldName);
    }

    /**
     * Helper function which checks if any triple pattern predicate in @param sgp is a variable or blank node, returns true if so
     */
    public static boolean hasVariablePredicate(final Set<TriplePattern> sgp){
        for(final TriplePattern t : sgp){
            final Node predicate = t.asJenaTriple().getPredicate();
            if(predicate.isVariable()){
                return true;
            }    
        }
        return false;
    }

    /**
     * Returns the GraphQL object type @param sgp correponds to. 
     * If the type is undeterminable @return null
     */
    public static String determineSgpType(final Set<TriplePattern> sgp, final GraphQL2RDFConfiguration config){
        for(final TriplePattern t : sgp){
            final Node predicate = t.asJenaTriple().getPredicate();
            final Node object = t.asJenaTriple().getObject();

            if(predicate.isURI() && config.isValidPropertyURI(predicate.getURI())){
                return config.mapPropertyToType(predicate.getURI());
            }
            else if(predicate.isURI() && config.isValidMembershipURI(predicate.getURI())){
                if(object.isURI() && config.isValidClassURI(object.getURI())){
                    return config.mapClassToType(object.getURI());
                }
            }
        }

        return null;
    }

    /**
     * @return a map consisting of what can be used as arguments from the given @param sgp
     * The predicate from a TP needs to be a property URI and the object needs to be a literal
     */
    public static Map<String,LiteralLabel> getSgpArguments(final Set<TriplePattern> sgp, final GraphQL2RDFConfiguration config){
        final Map<String,LiteralLabel> args = new HashMap<>();
        for(final TriplePattern t : sgp){
            final Node predicate = t.asJenaTriple().getPredicate();
            final Node object = t.asJenaTriple().getObject();

            if(predicate.isURI() && config.isValidPropertyURI(predicate.getURI())){
                if(object.isLiteral()){
                    args.put(config.mapPropertyToField(predicate.toString()),object.getLiteral());
                }
            }
        }
        return args;
    }

    /**
     * Check if @param sgpArgumentNames have atleast one match with @param entrypointArgumentNames
     */
    public static boolean hasNecessaryArguments(final Set<String> sgpArgumentNames, final Set<String> entrypointArgumentNames){
        
        for(final String argName : sgpArgumentNames){
            if(entrypointArgumentNames.contains(argName)){
                return true;
            }
        }

        return false;
    }

    /**
     * Check if @param sgpArgumentNames contains all argument name from @param entrypointArgumentNames.
     * If @param sgpArgumentNames is empty then returns false.
     */
    public static boolean hasAllNecessaryArguments(final Set<String> sgpArgumentNames, final Set<String> entrypointArgumentNames){
        if(sgpArgumentNames.isEmpty()){
            return false;
        }

        return sgpArgumentNames.containsAll(entrypointArgumentNames);
    }

    /**
     * Helper function used to convert a LiteralLabel value to a valid jsonvalue
     */
    public static JsonValue literalToJsonValue(final LiteralLabel literal){
        final Object value = literal.getValue();

        if(value instanceof Integer){
            return JsonNumber.value((int) literal.getValue());
        }
        else if(value instanceof Boolean){
            return new JsonBoolean((boolean) literal.getValue());
        }
        else if(value instanceof Double || value instanceof Float){
            return JsonNumber.value((double) literal.getValue());
        }
        else{
            return new JsonString((String) literal.getValue());
        }
    }
}
