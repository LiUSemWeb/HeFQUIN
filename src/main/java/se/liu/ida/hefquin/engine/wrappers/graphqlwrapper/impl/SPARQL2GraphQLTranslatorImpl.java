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

import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.SPARQL2GraphQLTranslator;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl.GraphQLQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.CyclicFinder;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.SGPNode;

public class SPARQL2GraphQLTranslatorImpl implements SPARQL2GraphQLTranslator {

    @Override
    public GraphQLQuery translateBGP(final BGP bgp, final GraphQL2RDFConfiguration config) {

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
        final Set<Integer> connectorsToBeRemoved = CyclicFinder.determineCyclicConnectors(sgpNodes);
        for(final int i : connectorsToBeRemoved){
            connectors.remove(i);
        }

        // Creating necessary variables for the GraphQL query
        final TreeSet<String> fieldPaths = new TreeSet<>();
        final JsonObject parameterValues = new JsonObject();
        final Map<String, String> parameterDefinitions = new HashMap<>();

        // Get all SGP without a connector
        final Map<Node,String> withoutConnector = new HashMap<>();
        boolean isDeterminable = true;
        for(final Node n : subgraphPatterns.keySet()){
            final String sgpType = determineSgpType(subgraphPatterns.get(n),config);
            if(!connectors.values().contains(n) && sgpType == null){
                materializeAll(fieldPaths,subgraphPatterns,connectors,config);
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
                final Map<String,LiteralLabel> sgpParameters = getSgpParameters(subgraphPatterns.get(n),config);
                final String sgpType = withoutConnector.get(n);
                String currentPath = "";

                if(hasNecessaryParameters(sgpParameters.keySet(), 
                        config.getEntrypoint(sgpType,GraphQLEntrypointType.SINGLE).getParameterDefinitions().keySet(), 
                        true)){
                    // Get single object entrypoint
                    final GraphQLEntrypoint e = config.getEntrypoint(sgpType,GraphQLEntrypointType.SINGLE);
                    final String entrypointAlias = "ep_single" + entrypointCounter + ":" + e.getFieldName();
                    currentPath += entrypointAlias + "(";
                    final Map<String,String> entrypointParamDefs = e.getParameterDefinitions();

                    for(final String paramName : new TreeSet<String>(entrypointParamDefs.keySet())){
                        final String variableName = "var"+variableCounter;
                        currentPath += paramName + ":$" + variableName + ",";
                        parameterDefinitions.put(variableName,entrypointParamDefs.get(paramName));
                        if(sgpParameters.containsKey(paramName)) {
                            parameterValues.put(variableName,literalToJsonValue(sgpParameters.get(paramName)));
                        }
                        else{
                            parameterValues.put(variableName,JsonNull.instance);
                        }
                        ++variableCounter;
                    }
                    final int lastComma = currentPath.lastIndexOf(',');
                    currentPath = currentPath.substring(0, lastComma);
                    currentPath += ")/";
                }
                else if(hasNecessaryParameters(sgpParameters.keySet(), 
                        config.getEntrypoint(sgpType,GraphQLEntrypointType.FILTERED).getParameterDefinitions().keySet(),false)){
                    // Get filtered list entrypoint
                    final GraphQLEntrypoint e = config.getEntrypoint(sgpType,GraphQLEntrypointType.FILTERED);
                    final String entrypointAlias = "ep_filtered" + entrypointCounter + ":" + e.getFieldName();
                    currentPath += entrypointAlias + "(";
                    final Map<String,String> entrypointParamDefs = e.getParameterDefinitions();

                    for(final String paramName : new TreeSet<String>(entrypointParamDefs.keySet())){
                        final String variableName = "var"+variableCounter;
                        currentPath += paramName + ":$" + variableName + ",";
                        parameterDefinitions.put(variableName,entrypointParamDefs.get(paramName));

                        if(sgpParameters.containsKey(paramName)) {
                            parameterValues.put(variableName,literalToJsonValue(sgpParameters.get(paramName)));
                        }
                        else{
                            parameterValues.put(variableName,JsonNull.instance);
                        }
                        ++variableCounter;
                    }
                    final int lastComma = currentPath.lastIndexOf(',');
                    currentPath = currentPath.substring(0, lastComma);
                    currentPath += ")/";
                }
                else{
                    // Get full list entrypoint
                    final GraphQLEntrypoint e = config.getEntrypoint(sgpType,GraphQLEntrypointType.FULL);
                    final String entrypointAlias = "ep_full" + entrypointCounter + ":" + e.getFieldName();
                    currentPath += entrypointAlias + "/";
                }
                addSgp(fieldPaths,subgraphPatterns,connectors,config,
                    n,currentPath,sgpType);
                ++entrypointCounter;
            }
        }

        return new GraphQLQueryImpl(fieldPaths, parameterValues, parameterDefinitions);
    }


    /**
     * Recursive function used to add fields from the TPs in a given SGP
     */
    protected void addSgp(final TreeSet<String> fieldPaths, final Map<Node,Set<TP>> subgraphPatterns, final Map<Integer,Node> connectors,
            final GraphQL2RDFConfiguration config, final Node subgraphNode, final String currentPath, final String sgpType){

        // Necessary id field present in all objects used to indentify the GraphQL object
        fieldPaths.add(currentPath + "id_" + sgpType + ":id");

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
                final Set<String> allObjectURI = config.getPropertyURIs(sgpType, GraphQLFieldType.OBJECT);

                for(final String uri : allObjectURI){
                    addNodeField(fieldPaths,subgraphPatterns,connectors,config,nestedSubgraphNode,currentPath,sgpType,uri);
                }

            }
            else if(predicate.isURI() && config.containsPropertyURI(predicate.getURI())){
                // If the current TP predicate is a URI, add field onl
                addNodeField(fieldPaths,subgraphPatterns,connectors,config,nestedSubgraphNode,
                    currentPath,sgpType,predicate.getURI());
            }
        }

        // Add fields that doesn't link to another SGP, 
        if(addAllFields){
            // If variable predicate exist in the current SGP, then query for everything
            final Set<String> allObjectURI = config.getPropertyURIs(sgpType,GraphQLFieldType.OBJECT);
            final Set<String> allScalarURI = config.getPropertyURIs(sgpType,GraphQLFieldType.SCALAR);

            for(final String uri : allObjectURI){
                addEmptyNodeField(fieldPaths,subgraphPatterns,connectors,config,currentPath,sgpType,uri);
            }

            for(final String uri : allScalarURI){
                addScalarField(fieldPaths,config,currentPath,uri);
            }

        }
        else{
            // If no variable predicate exist, only the necessary fields from the SGP have to be added.
            for(final TP t : subgraphPatterns.get(subgraphNode)){
                final Node predicate = t.getTriplePattern().asJenaTriple().getPredicate();
                if(predicate.isURI() && config.containsPropertyURI(predicate.getURI())){
                    final String withoutPrefix = config.removePropertyPrefix(predicate.getURI());
                    final String propertyName = config.removePropertySuffix(withoutPrefix);
                    if(config.getPropertyFieldType(sgpType,propertyName) == GraphQLFieldType.OBJECT){
                        addEmptyNodeField(fieldPaths,subgraphPatterns,connectors,config,
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
    protected void addNodeField(final TreeSet<String> fieldPaths, final Map<Node,Set<TP>> subgraphPatterns, final Map<Integer,Node> connectors,
            final GraphQL2RDFConfiguration config, final Node nestedSubgraphNode, final String currentPath, final String sgpType, 
            final String predicateURI){

        final String alias = config.removePropertyPrefix(predicateURI);
        final String propertyName = config.removePropertySuffix(alias);
        final String fieldName = "node_" + alias + ":" + propertyName;
        final String newPath = currentPath + fieldName + "/";
        final String nestedType = config.getPropertyValueType(sgpType, propertyName);

        addSgp(fieldPaths,subgraphPatterns,connectors,config,
            nestedSubgraphNode,newPath,nestedType);
    }

    /**
     * Helper function to add a field to the query that represents a nested object that only needs an id field
     */
    protected void addEmptyNodeField(final TreeSet<String> fieldPaths, final Map<Node,Set<TP>> subgraphPatterns, final Map<Integer,Node> connectors,
            final GraphQL2RDFConfiguration config, final String currentPath, final String sgpType, 
            final String predicateURI){
        final String alias = config.removePropertyPrefix(predicateURI);
        final String propertyName = config.removePropertySuffix(alias);
        final String fieldName = "node_" + alias + ":" + propertyName;
        final String newPath = currentPath + fieldName + "/";
        final String nestedType = config.getPropertyValueType(sgpType, propertyName);
        fieldPaths.add(newPath + "id_" + nestedType + ":id");
    }

    /**
     * Helper function to add a field to the query that represents a scalar value
     */
    protected void addScalarField(final TreeSet<String> fieldPaths, final GraphQL2RDFConfiguration config, 
            final String currentPath, final String predicateURI){
        final String alias = config.removePropertyPrefix(predicateURI);
        final String propertyName = config.removePropertySuffix(alias);
        final String fieldName = "scalar_" + alias + ":" + propertyName;
        fieldPaths.add(currentPath + fieldName);
    }

    /**
     * Materialize the entire view
     */
    protected void materializeAll(final TreeSet<String> fieldPaths, final Map<Node,Set<TP>> subgraphPatterns, 
            final Map<Integer,Node> connectors,final GraphQL2RDFConfiguration config){
        
        int entrypointCounter = 0;
        final Set<String> classNames = config.getClasses();
        for(final String className : classNames){
            // Get the full list entrypoint
            final GraphQLEntrypoint e = config.getEntrypoint(className,GraphQLEntrypointType.FULL);
            final String entrypointAlias = "ep_full" + entrypointCounter + ":" + e.getFieldName();
            final String currentPath = entrypointAlias + "/";
            fieldPaths.add(currentPath + "id_" + className + ":id");

            final Set<String> allObjectURI = config.getPropertyURIs(className,GraphQLFieldType.OBJECT);
            final Set<String> allScalarURI = config.getPropertyURIs(className,GraphQLFieldType.SCALAR);

            for(final String uri : allObjectURI){
                // Add all fields that nests another object
                addEmptyNodeField(fieldPaths, subgraphPatterns, 
                    connectors, config, currentPath, className, uri);
            }
            for(final String uri : allScalarURI){
                // Add all fields that represent scalar values
                addScalarField(fieldPaths,config,currentPath,uri);
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
     * Returns the rdfs class @param sgp correponds to. If the type is undeterminable @return null
     */
    protected String determineSgpType(final Set<TP> sgp, final GraphQL2RDFConfiguration config){
        for(final TP t : sgp){
            final Node predicate = t.getTriplePattern().asJenaTriple().getPredicate();
            final Node object = t.getTriplePattern().asJenaTriple().getObject();

            if(predicate.isURI() && predicate.getURI().startsWith(config.getPropertyPrefix())){
                return config.getClassFromPropertyURI(predicate.getURI());
            }
            else if(predicate.isURI() && predicate.getURI().equals(config.getRDFPrefix() + "type")){
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
     * @return a map consisting of what can be used as parameters from the given @param sgp
     * The predicate from a TP needs to be a property URI and the object needs to be a literal
     */
    protected final Map<String,LiteralLabel> getSgpParameters(final Set<TP> sgp, final GraphQL2RDFConfiguration config){
        final Map<String,LiteralLabel> parameters = new HashMap<>();
        for(final TP t : sgp){
            final Node predicate = t.getTriplePattern().asJenaTriple().getPredicate();
            final Node object = t.getTriplePattern().asJenaTriple().getObject();

            if(predicate.isURI() && predicate.getURI().startsWith(config.getPropertyPrefix())){
                if(object.isLiteral() && object.getLiteral().isWellFormed()){
                    // Remove prefix and suffix before adding to map
                    final String s = config.removePropertyPrefix(predicate.toString());
                    final String parameterName = config.removePropertySuffix(s);
                    parameters.put(parameterName,object.getLiteral());
                }
            }
        }
        return parameters;
    }

    /**
     * If @param checkAll is true then every element in @param entrypointParameter must exist in 
     * @param sgpParameters . Otherwise a single match is enough. If @param sgpParameters is empty return false.
     */
    protected boolean hasNecessaryParameters(final Set<String> sgpParameters, final Set<String> entrypointParameters, 
            final boolean checkAll){
        
        if(sgpParameters.isEmpty()){
            return false;
        }

        if(checkAll){
            return sgpParameters.containsAll(entrypointParameters);
        }
        else{
            for(final String paramName : sgpParameters){
                if(entrypointParameters.contains(paramName)){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Utility function used to convert a literal value to a valid jsonvalue
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
