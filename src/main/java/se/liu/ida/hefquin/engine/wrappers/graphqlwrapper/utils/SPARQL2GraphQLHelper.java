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
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public class SPARQL2GraphQLHelper {
    /**
     * Recursive function used to add fields from the triple patterns in a given SGP
     */
    public static void addSgp(final Set<String> fieldPaths, final Map<Node,Set<TP>> subgraphPatterns, 
            final Map<Integer,Node> connectors, final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint, 
            final Node subgraphNode, final String currentPath, final String sgpType){

        // Necessary id field present in all objects used to indentify the GraphQL object
        fieldPaths.add(GraphQLFieldPathBuilder.addID(currentPath, sgpType));

        // Retrieve necessary information about current sgp
        final boolean addAllFields = hasVariablePredicate(subgraphPatterns.get(subgraphNode));
        final Set<TP> tpConnectors = new HashSet<>();
        for(final TP t : subgraphPatterns.get(subgraphNode)){
            if(connectors.containsKey(t.getId())){
                tpConnectors.add(t);
            }
        }

        // Add fields that has nested objects to other SGPs using recursion
        for(final TP currentTP : tpConnectors){
            final Triple t = currentTP.getTriplePattern().asJenaTriple();
            final Node nestedSubgraphNode = connectors.get(currentTP.getId());
            final Node predicate = t.getPredicate();

            if(predicate.isVariable()){
                // If the current TP predicate is a variable we need to add everything
                final Set<String> allObjectURI = URI2GraphQLHelper.getPropertyURIs(sgpType, GraphQLFieldType.OBJECT,config,endpoint);

                for(final String uri : allObjectURI){
                    addObjectField(fieldPaths,subgraphPatterns,connectors,config,endpoint,
                        nestedSubgraphNode,currentPath,sgpType,uri);
                }

            }
            else if(predicate.isURI() && URI2GraphQLHelper.containsPropertyURI(predicate.getURI(),config,endpoint)){
                // If the current TP predicate is a URI, add the GraphQL field it corresponds to
                addObjectField(fieldPaths,subgraphPatterns,connectors,config,endpoint,
                    nestedSubgraphNode,currentPath,sgpType,predicate.getURI());
            }
        }

        // Add fields that doesn't link to another SGP, 
        if(addAllFields){
            // If variable predicate exist in the current SGP, then query for everything in current object
            final Set<String> allObjectURI = URI2GraphQLHelper.getPropertyURIs(sgpType,GraphQLFieldType.OBJECT,config,endpoint);
            final Set<String> allScalarURI = URI2GraphQLHelper.getPropertyURIs(sgpType,GraphQLFieldType.SCALAR,config,endpoint);

            for(final String uri : allObjectURI){
                addEmptyObjectField(fieldPaths,config,endpoint,currentPath,sgpType,uri);
            }

            for(final String uri : allScalarURI){
                addScalarField(fieldPaths,config,currentPath,uri);
            }

        }
        else{
            // If no variable predicate exist, only the necessary fields from the SGP have to be added.
            for(final TP t : subgraphPatterns.get(subgraphNode)){
                final Node predicate = t.getTriplePattern().asJenaTriple().getPredicate();
                if(predicate.isURI() && URI2GraphQLHelper.containsPropertyURI(predicate.getURI(),config,endpoint)){
                    final String withoutPrefix = config.removePropertyPrefix(predicate.getURI());
                    final String propertyName = config.removePropertySuffix(withoutPrefix);
                    if(endpoint.getGraphQLFieldType(sgpType,propertyName) == GraphQLFieldType.OBJECT){
                        addEmptyObjectField(fieldPaths,config,endpoint,currentPath,sgpType,predicate.getURI());
                    }
                    else{
                        addScalarField(fieldPaths,config,currentPath,predicate.getURI());
                    }
                }
            }
        }
    }


    /**
     * Helper function to add a field to the query that represents a nested object. Fields in that nested 
     * object are added recursively through addSgp
     */
    public static void addObjectField(final Set<String> fieldPaths, final Map<Node,Set<TP>> subgraphPatterns, 
            final Map<Integer,Node> connectors, final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint, 
            final Node nestedSubgraphNode, final String currentPath, final String sgpType, final String predicateURI){

        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        final String newPath = GraphQLFieldPathBuilder.addObject(currentPath, alias, fieldName);
        final String nestedType = endpoint.getGraphQLFieldValueType(sgpType, fieldName);

        addSgp(fieldPaths,subgraphPatterns,connectors,config,endpoint,
            nestedSubgraphNode,newPath,nestedType);
    }

    /**
     * Helper function to add a field to the query that represents a nested object that only needs an id field (no more recursive calls)
     */
    public static void addEmptyObjectField(final Set<String> fieldPaths, final GraphQL2RDFConfiguration config,
            final GraphQLEndpoint endpoint, final String currentPath, final String sgpType, 
            final String predicateURI){
        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        final String newPath = GraphQLFieldPathBuilder.addObject(currentPath, alias, fieldName);
        final String nestedType = endpoint.getGraphQLFieldValueType(sgpType, fieldName);
        fieldPaths.add(GraphQLFieldPathBuilder.addID(newPath, nestedType));
    }

    /**
     * Helper function to add a field to the query that represents a scalar value
     */
    public static void addScalarField(final Set<String> fieldPaths, final GraphQL2RDFConfiguration config, 
            final String currentPath, final String predicateURI){
        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        fieldPaths.add(GraphQLFieldPathBuilder.addScalar(currentPath, alias, fieldName));
    }



    /**
     * Helper function which checks if any triple pattern predicate in @param sgp is a variable or blank node, returns true if so
     */
    public static boolean hasVariablePredicate(final Set<TP> sgp){
        for(final TP t : sgp){
            final Node predicate = t.getTriplePattern().asJenaTriple().getPredicate();
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
    public static String determineSgpType(final Set<TP> sgp, final GraphQL2RDFConfiguration config){
        for(final TP t : sgp){
            final Node predicate = t.getTriplePattern().asJenaTriple().getPredicate();
            final Node object = t.getTriplePattern().asJenaTriple().getObject();

            if(predicate.isURI() && predicate.getURI().startsWith(config.getPropertyPrefix())){
                return config.getClassFromPropertyURI(predicate.getURI());
            }
            else if(predicate.isURI() && predicate.getURI().equals(config.getClassMembershipURI())){
                if(object.isURI() && object.getURI().startsWith(config.getClassPrefix())){
                    final int splitIndex = config.getClassPrefix().length();
                    return object.getURI().substring(splitIndex);
                }
            }
        }

        return null;
    }

    /**
     * @return a map consisting of what can be used as arguments from the given @param sgp
     * The predicate from a TP needs to be a property URI and the object needs to be a literal
     */
    public static final Map<String,LiteralLabel> getSgpArguments(final Set<TP> sgp, final GraphQL2RDFConfiguration config){
        final Map<String,LiteralLabel> args = new HashMap<>();
        for(final TP t : sgp){
            final Node predicate = t.getTriplePattern().asJenaTriple().getPredicate();
            final Node object = t.getTriplePattern().asJenaTriple().getObject();

            if(predicate.isURI() && predicate.getURI().startsWith(config.getPropertyPrefix())){
                if(object.isLiteral() && object.getLiteral().isWellFormed()){
                    // Remove prefix and suffix before adding to map
                    final String s = config.removePropertyPrefix(predicate.toString());
                    final String argName = config.removePropertySuffix(s);
                    args.put(argName,object.getLiteral());
                }
            }
        }
        return args;
    }

    /**
     * Checks whether a set of SGP arguments, @param sgpArguments , meets the necessery arguments for
     * an endpoint @param entrypointArguments . If @param checkAll is true then all arguments from the entrypoint
     * must exist in the SGP. Otherwise atleast one matching argument is enough as long as @param sgpArguments
     * isn't an empty set.
     */

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
