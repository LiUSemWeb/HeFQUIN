package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.atlas.json.JsonBoolean;
import org.apache.jena.atlas.json.JsonNull;
import org.apache.jena.atlas.json.JsonNumber;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.SPARQL2GraphQLTranslator;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl.GraphQLQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.GraphCycleDetector;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.SGPNode;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.URI2GraphQLHelper;

public class SPARQL2GraphQLTranslatorImpl implements SPARQL2GraphQLTranslator {

    @Override
    public GraphQLQuery translateBGP(final BGP bgp, final GraphQL2RDFConfiguration config,
            final GraphQLEndpoint endpoint) {

        // Initialize necessary data structures
        final Map<Node, Set<TP>> subgraphPatterns = new HashMap<>();
        final Map<Integer, Node> connectors = new HashMap<>();

        // Partition BGP into SGPs and give unique integer id to TPs
        final Set<? extends TriplePattern> bgpSet = bgp.getTriplePatterns();
        int idCounter = 0;
        for(final TriplePattern t : bgpSet){
            final TP wrappedTriplePattern = new TP(idCounter,t);
            final Node subject = t.asJenaTriple().getSubject();
            if(subgraphPatterns.containsKey(subject)){
                subgraphPatterns.get(subject).add(wrappedTriplePattern);
            }
            else{
                final Set<TP> sgp = new HashSet<>();
                sgp.add(wrappedTriplePattern);
                subgraphPatterns.put(subject, sgp);
            }
            ++idCounter;
        }

        // Check for dependencies between the SGPs and create connectors
        final Map<Node,SGPNode> sgpNodes = new HashMap<>();
        for(final Node subject : subgraphPatterns.keySet()){
            final Set<TP> sgp = subgraphPatterns.get(subject);
            for(final TP tp : sgp){
                final Node object = tp.getTriplePattern().asJenaTriple().getObject();
                if(subgraphPatterns.containsKey(object) && ! object.equals(subject)){
                    if(!sgpNodes.containsKey(subject)){
                        sgpNodes.put(subject,new SGPNode());
                    }
                    if(!sgpNodes.containsKey(object)){
                        sgpNodes.put(object,new SGPNode());
                    }
                    final SGPNode subjectSgpNode = sgpNodes.get(subject);
                    final SGPNode objectSgpNode = sgpNodes.get(object);
                    subjectSgpNode.addAdjacentNode(tp.getId(),objectSgpNode);
                    connectors.put(tp.getId(), object);
                }
            }
        }
        // Remove all potential cyclic connector bindings
        final Set<Integer> connectorsToBeRemoved = GraphCycleDetector.determineCyclicConnectors(sgpNodes);
        for(final int i : connectorsToBeRemoved){
            connectors.remove(i);
        }

        // Creating necessary variables for the GraphQL query
        final TreeSet<String> fieldPaths = new TreeSet<>();
        final JsonObject argumentValues = new JsonObject();
        final Map<String, String> argumentDefinitions = new HashMap<>();

        // Get all SGP without a connector, if they have undeterminable GraphQL type then materializeAll
        final Map<Node,String> withoutConnector = new HashMap<>();
        boolean isDeterminable = true;
        for(final Node n : subgraphPatterns.keySet()){
            final String sgpType = determineSgpType(subgraphPatterns.get(n),config);
            if(!connectors.values().contains(n) && sgpType == null){
                materializeAll(fieldPaths,subgraphPatterns,connectors,config,endpoint);
                isDeterminable = false;
                break;
            }
            if(!connectors.values().contains(n)){
                withoutConnector.put(n,sgpType);
            }
        }

        // If the entire view doesn't need to be materialized
        if(isDeterminable){
            int entrypointCounter = 0;
            int variableCounter = 0;
            for(final Node n : withoutConnector.keySet()){
                final Map<String,LiteralLabel> sgpArguments = getSgpArguments(subgraphPatterns.get(n),config);
                final String sgpType = withoutConnector.get(n);
                final StringBuilder currentPath = new StringBuilder();
                GraphQLEntrypoint e;

                if(hasNecessaryArguments(sgpArguments.keySet(), 
                        endpoint.getEntrypoint(sgpType,GraphQLEntrypointType.SINGLE).getArgumentDefinitions().keySet(), 
                        true)){
                    // Get single object entrypoint
                    e = endpoint.getEntrypoint(sgpType,GraphQLEntrypointType.SINGLE);
                    final StringBuilder entrypointAlias = new StringBuilder("ep_single")
                    .append(entrypointCounter).append(":").append(e.getFieldName());
                    currentPath.append(entrypointAlias.toString());
                }
                else if(hasNecessaryArguments(sgpArguments.keySet(), 
                        endpoint.getEntrypoint(sgpType,GraphQLEntrypointType.FILTERED).getArgumentDefinitions().keySet(),
                        false)){
                    // Get filtered list entrypoint
                    e = endpoint.getEntrypoint(sgpType,GraphQLEntrypointType.FILTERED);
                    final StringBuilder entrypointAlias = new StringBuilder("ep_filtered")
                    .append(entrypointCounter).append(":").append(e.getFieldName());
                    currentPath.append(entrypointAlias.toString());
                }
                else{
                    // Get full list entrypoint (No argument values)
                    e = endpoint.getEntrypoint(sgpType,GraphQLEntrypointType.FULL);
                    final StringBuilder entrypointAlias = new StringBuilder("ep_full")
                    .append(entrypointCounter).append(":").append(e.getFieldName());
                    currentPath.append(entrypointAlias.toString());
                }
 
                // If entrypoint has arguments then add them to the currentPath, argumentValues and argumentDefinitions
                final Map<String,String> entrypointArgDefs = e.getArgumentDefinitions();
                if(entrypointArgDefs != null && !entrypointArgDefs.isEmpty()){
                    currentPath.append("(");
                    for(final String argName : new TreeSet<String>(entrypointArgDefs.keySet())){
                        final String variableName = "var"+variableCounter;
                        currentPath.append(argName).append(":$").append(variableName).append(",");
                        argumentDefinitions.put(variableName,entrypointArgDefs.get(argName));
                        if(sgpArguments.containsKey(argName)) {
                            argumentValues.put(variableName,literalToJsonValue(sgpArguments.get(argName)));
                        }
                        else{
                            argumentValues.put(variableName,JsonNull.instance);
                        }
                        ++variableCounter;
                    }
                    final int lastComma = currentPath.lastIndexOf(",");
                    currentPath.deleteCharAt(lastComma);
                    currentPath.append(")");
                }

                // Start adding fields for nested object(s) using recursion
                currentPath.append("/");
                addSgp(fieldPaths,subgraphPatterns,connectors,config,endpoint,
                    n,currentPath.toString(),sgpType);
                ++entrypointCounter;
            }
        }

        return new GraphQLQueryImpl(fieldPaths, argumentValues, argumentDefinitions);
    }


    /**
     * Recursive function used to add fields from the TPs in a given SGP
     */
    protected void addSgp(final TreeSet<String> fieldPaths, final Map<Node,Set<TP>> subgraphPatterns, 
            final Map<Integer,Node> connectors, final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint, 
            final Node subgraphNode, final String currentPath, final String sgpType){

        // Necessary id field present in all objects used to indentify the GraphQL object
        fieldPaths.add(new StringBuilder(currentPath).append("id_").append(sgpType).append(":id").toString());

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
                addEmptyObjectField(fieldPaths,subgraphPatterns,connectors,config,endpoint,
                    currentPath,sgpType,uri);
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
                        addEmptyObjectField(fieldPaths,subgraphPatterns,connectors,config,endpoint,
                            currentPath,sgpType,predicate.getURI());
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
    protected void addObjectField(final TreeSet<String> fieldPaths, final Map<Node,Set<TP>> subgraphPatterns, 
            final Map<Integer,Node> connectors, final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint, 
            final Node nestedSubgraphNode, final String currentPath, final String sgpType, final String predicateURI){

        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        final StringBuilder field = new StringBuilder("object_").append(alias).append(":").append(fieldName);
        final StringBuilder newPath = new StringBuilder(currentPath).append(field).append("/");
        final String nestedType = endpoint.getGraphQLFieldValueType(sgpType, fieldName);

        addSgp(fieldPaths,subgraphPatterns,connectors,config,endpoint,
            nestedSubgraphNode,newPath.toString(),nestedType);
    }

    /**
     * Helper function to add a field to the query that represents a nested object that only needs an id field (no more recursive calls)
     */
    protected void addEmptyObjectField(final TreeSet<String> fieldPaths, final Map<Node,Set<TP>> subgraphPatterns, 
            final Map<Integer,Node> connectors, final GraphQL2RDFConfiguration config,final GraphQLEndpoint endpoint, 
            final String currentPath, final String sgpType, final String predicateURI){
        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        final StringBuilder field = new StringBuilder("object_").append(alias).append(":").append(fieldName);
        final StringBuilder newPath = new StringBuilder(currentPath).append(field).append("/");
        final String nestedType = endpoint.getGraphQLFieldValueType(sgpType, fieldName);
        final StringBuilder nestedPath = new StringBuilder(newPath).append("id_").append(nestedType).append(":id");
        fieldPaths.add(nestedPath.toString());
    }

    /**
     * Helper function to add a field to the query that represents a scalar value
     */
    protected void addScalarField(final TreeSet<String> fieldPaths, final GraphQL2RDFConfiguration config, 
            final String currentPath, final String predicateURI){
        final String alias = config.removePropertyPrefix(predicateURI);
        final String fieldName = config.removePropertySuffix(alias);
        final StringBuilder field = new StringBuilder("scalar_").append(alias).append(":").append(fieldName);
        fieldPaths.add(new StringBuilder(currentPath).append(field.toString()).toString());
    }

    /**
     * When everything from the endpoint needs to be fetched
     */
    protected void materializeAll(final TreeSet<String> fieldPaths, final Map<Node,Set<TP>> subgraphPatterns, 
            final Map<Integer,Node> connectors,final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint){
        
        int entrypointCounter = 0;
        final Set<String> objectTypeNames = endpoint.getGraphQLObjectTypes();
        for(final String objectTypeName : objectTypeNames){
            // Get the full list entrypoint
            final GraphQLEntrypoint e = endpoint.getEntrypoint(objectTypeName,GraphQLEntrypointType.FULL);
            final StringBuilder currentPath = new StringBuilder("ep_full")
            .append(entrypointCounter).append(":").append(e.getFieldName()).append("/");
            fieldPaths.add(new StringBuilder(currentPath).append("id_").append(objectTypeName).append(":id").toString());

            final Set<String> allObjectURI = URI2GraphQLHelper.getPropertyURIs(objectTypeName,GraphQLFieldType.OBJECT,config, endpoint);
            final Set<String> allScalarURI = URI2GraphQLHelper.getPropertyURIs(objectTypeName,GraphQLFieldType.SCALAR,config, endpoint);

            // Add all fields that nests another object
            for(final String uri : allObjectURI){
                addEmptyObjectField(fieldPaths, subgraphPatterns, 
                    connectors, config, endpoint, currentPath.toString(), objectTypeName, uri);
            }
            // Add all fields that represent scalar values
            for(final String uri : allScalarURI){
                addScalarField(fieldPaths,config,currentPath.toString(),uri);
            }

            ++entrypointCounter;
        }
    }

    /**
     * Helper function which checks if any triple pattern predicate in @param sgp is a variable or blank node, returns true if so
     */
    protected boolean hasVariablePredicate(final Set<TP> sgp){
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
    protected String determineSgpType(final Set<TP> sgp, final GraphQL2RDFConfiguration config){
        for(final TP t : sgp){
            final Node predicate = t.getTriplePattern().asJenaTriple().getPredicate();
            final Node object = t.getTriplePattern().asJenaTriple().getObject();

            if(predicate.isURI() && predicate.getURI().startsWith(config.getPropertyPrefix())){
                return config.getClassFromPropertyURI(predicate.getURI());
            }
            else if(predicate.isURI() && predicate.getURI().equals(config.getClassMembershipURI())){
                if(object.isURI() && object.getURI().startsWith(config.getClassPrefix())){
                    final int splitIndex = config.getClassPrefix().length();
                    final String sgpType = object.getURI().substring(splitIndex);
                    return sgpType;
                }
            }
        }

        return null;
    }

    /**
     * @return a map consisting of what can be used as arguments from the given @param sgp
     * The predicate from a TP needs to be a property URI and the object needs to be a literal
     */
    protected final Map<String,LiteralLabel> getSgpArguments(final Set<TP> sgp, final GraphQL2RDFConfiguration config){
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
    protected boolean hasNecessaryArguments(final Set<String> sgpArguments, final Set<String> entrypointArguments, 
            final boolean checkAll){
        
        if(sgpArguments.isEmpty()){
            return false;
        }

        if(checkAll){
            return sgpArguments.containsAll(entrypointArguments);
        }
        else{
            for(final String argName : sgpArguments){
                if(entrypointArguments.contains(argName)){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Helper function used to convert a LiteralLabel value to a valid jsonvalue
     */
    protected JsonValue literalToJsonValue(final LiteralLabel literal){
        final Class<?> type = literal.getValue().getClass();

        if(type.equals(int.class)){
            return JsonNumber.value((int) literal.getValue());
        }
        else if(type.equals(boolean.class)){
            return new JsonBoolean((boolean) literal.getValue());
        }
        else if(type.equals(double.class) || type.equals(float.class)){
            return JsonNumber.value((double) literal.getValue());
        }
        else{
            return new JsonString((String) literal.getValue());
        }
    }

    /**
     * Wrapper class for a TriplePattern that includes an integer id
     */
    private class TP {
        private final int id;
        private final TriplePattern triplePattern;

        public TP(final int id, final TriplePattern triplePattern) {
            this.id = id;
            this.triplePattern = triplePattern;
        }

        public final int getId() {
            return id;
        }

        public final TriplePattern getTriplePattern(){
            return triplePattern;
        }
    }
}