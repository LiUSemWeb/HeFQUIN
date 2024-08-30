package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

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

import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointPath;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLIDPath;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLObjectPath;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLScalarPath;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl.GraphQLQueryImpl;

/**
 * Provides helper functions for creating the necessary data in a GraphQL query.
 */
public class SPARQL2GraphQLHelper
{
    protected final GraphQL2RDFConfiguration config;
    protected final GraphQLSchema schema;
    protected final Map<Node, StarPattern> indexedStarPatterns;
    protected final Map<TriplePattern,StarPattern> connectors;

    public SPARQL2GraphQLHelper( final GraphQL2RDFConfiguration config,
                                 final GraphQLSchema schema,
                                 final Map<Node, StarPattern> indexedStarPatterns,
                                 final Map<TriplePattern, StarPattern> connectors ) {
        this.config                = config;
        this.schema                = schema;
        this.indexedStarPatterns   = indexedStarPatterns;
        this.connectors            = connectors;
    }

    /**
     * Generates a GraphQL query that fetches everything from the GraphQL endpoint.
     */
    public GraphQLQuery materializeAll() {
        int entrypointCounter = 0;
        final Set<String> finishedFieldPaths = new HashSet<>();
        final Set<String> objectTypeNames = schema.getGraphQLObjectTypes();
        for(final String objectTypeName : objectTypeNames){
            // Get the full list entrypoint
            
            final GraphQLEntrypoint e = schema.getEntrypoint(objectTypeName,GraphQLEntrypointType.FULL);
            if(e == null){
                continue;
            }
            final String currentPath = new GraphQLEntrypointPath(e, entrypointCounter).toString();
            finishedFieldPaths.add(currentPath + new GraphQLIDPath(objectTypeName,config.getJsonIDKeyPrefix()));

            final Set<String> allObjectURI = URI2GraphQLHelper.getPropertyURIs(objectTypeName,GraphQLFieldType.OBJECT, config, schema );
            final Set<String> allScalarURI = URI2GraphQLHelper.getPropertyURIs(objectTypeName,GraphQLFieldType.SCALAR, config, schema );

            // Add all fields that nests another object
            for(final String uri : allObjectURI){
                finishedFieldPaths.add( addEmptyObjectField(currentPath,objectTypeName,uri) );
            }
            // Add all fields that represent scalar values
            for(final String uri : allScalarURI){
                finishedFieldPaths.add( addScalarField(currentPath,uri) );
            }

            ++entrypointCounter;
        }

        return new GraphQLQueryImpl(finishedFieldPaths,new HashSet<>());
    }

    /**
     * Recursive function used to add fields from the triple patterns in a given star pattern
     */
    public Set<String> addSgp( final StarPattern sp, final String currentPath, final String sgpType ){
        final Set<String> finishedFieldPaths = new HashSet<>();

        // Necessary id field present in all objects used to indentify the GraphQL object
        finishedFieldPaths.add(currentPath + new GraphQLIDPath(sgpType,config.getJsonIDKeyPrefix()));

        // Retrieve necessary information about current star pattern
        final boolean addAllFields = hasVariablePredicate(sp);
        final Set<TriplePattern> tpConnectors = new HashSet<>();
        for ( final TriplePattern tp : sp.getTriplePatterns() ) {
            if ( connectors.containsKey(tp) ) {
                tpConnectors.add(tp);
            }
        }

        // Add fields that has nested objects to other star patterns using recursion
        for(final TriplePattern currentTP : tpConnectors){
            final Triple t = currentTP.asJenaTriple();
            final StarPattern connectedSP = connectors.get(currentTP);
            final Node predicate = t.getPredicate();

            if(predicate.isVariable()){
                // If the current TP predicate is a variable we need to add everything
                final Set<String> allObjectURI = URI2GraphQLHelper.getPropertyURIs(sgpType, GraphQLFieldType.OBJECT,config,schema);

                for(final String uri : allObjectURI){
                    finishedFieldPaths.addAll(addObjectField(connectedSP,currentPath,sgpType,uri));
                }

            }
            else if(predicate.isURI() && URI2GraphQLHelper.containsPropertyURI(predicate.getURI(),config,schema)
                    && sgpType.equals(config.mapPropertyToType(predicate.getURI()))){
                // If the current TP predicate is a URI, add the GraphQL field it corresponds to
                finishedFieldPaths.addAll(addObjectField(connectedSP,currentPath,sgpType,predicate.getURI()));
            }
        }

        // Add fields that do not link to another star pattern
        if(addAllFields){
            // If variable predicate exist in the current SGP, then query for everything in current object
            final Set<String> allObjectURI = URI2GraphQLHelper.getPropertyURIs(sgpType,GraphQLFieldType.OBJECT,config,schema);
            final Set<String> allScalarURI = URI2GraphQLHelper.getPropertyURIs(sgpType,GraphQLFieldType.SCALAR,config,schema);

            for(final String uri : allObjectURI){
                finishedFieldPaths.add( addEmptyObjectField(currentPath,sgpType,uri) );
            }

            for(final String uri : allScalarURI){
                finishedFieldPaths.add( addScalarField(currentPath,uri) );
            }

        }
        else{
            // If no variable predicate exists, only the necessary fields from the star pattern have to be added.
            for ( final TriplePattern tp : sp.getTriplePatterns() ) {
                final Node predicate = tp.asJenaTriple().getPredicate();
                if(predicate.isURI() && URI2GraphQLHelper.containsPropertyURI(predicate.getURI(),config,schema) 
                        && sgpType.equals(config.mapPropertyToType(predicate.getURI()))){
                    final String fieldName = config.mapPropertyToField(predicate.getURI());
                    if(schema.getGraphQLFieldType(sgpType,fieldName) == GraphQLFieldType.OBJECT){
                        finishedFieldPaths.add( addEmptyObjectField(currentPath,sgpType,predicate.getURI()) );
                    }
                    else{
                        finishedFieldPaths.add( addScalarField(currentPath,predicate.getURI()) );
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
    protected Set<String> addObjectField( final StarPattern sp,
                                          final String currentPath,
                                          final String sgpType,
                                          final String predicateURI ) {
        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        final String newPath = currentPath + new GraphQLObjectPath(fieldName,config.getJsonObjectKeyPrefix());
        final String nestedType = schema.getGraphQLFieldValueType(sgpType, fieldName);

        return addSgp(sp, newPath, nestedType);
    }

    /**
     * Helper function to add a field to the query that represents a nested object that only needs an id field (no more recursive calls)
     */
    protected String addEmptyObjectField( final String currentPath,
                                       final String sgpType,
                                       final String predicateURI ) {
        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        final String newPath = currentPath + new GraphQLObjectPath(fieldName,config.getJsonObjectKeyPrefix());
        final String nestedType = schema.getGraphQLFieldValueType(sgpType, fieldName);
        return newPath + new GraphQLIDPath(nestedType,config.getJsonIDKeyPrefix());
    }

    /**
     * Helper function to add a field to the query that represents a scalar value
     */
    protected String addScalarField( final String currentPath, final String predicateURI ) {
        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        return currentPath + new GraphQLScalarPath(fieldName,config.getJsonScalarKeyPrefix());
    }

    /**
     * Helper function which checks if any triple pattern predicate in @param sp is a variable or blank node, returns true if so
     */
    protected static boolean hasVariablePredicate( final StarPattern sp ){
        for ( final TriplePattern tp : sp.getTriplePatterns() ) {
            if ( tp.asJenaTriple().getPredicate().isVariable() ) {
                return true;
            }
        }
        return false;
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
