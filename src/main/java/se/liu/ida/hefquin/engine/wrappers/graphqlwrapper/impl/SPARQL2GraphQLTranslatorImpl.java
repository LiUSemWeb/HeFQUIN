package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.jena.atlas.json.JsonNull;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.SPARQL2GraphQLTranslator;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLArgument;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl.GraphQLQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.GraphCycleDetector;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.GraphQLFieldPathBuilder;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.SGPNode;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.SPARQL2GraphQLHelper;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.TP;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.URI2GraphQLHelper;

public class SPARQL2GraphQLTranslatorImpl implements SPARQL2GraphQLTranslator {

    @Override
    public GraphQLQuery translateBGP(final BGP bgp, final GraphQL2RDFConfiguration config,
            final GraphQLEndpoint endpoint) {

        // Initialize necessary data structures
        final Map<Node, Set<TP>> subgraphPatterns = createSubGraphPatterns(bgp);
        final Map<Integer, Node> connectors = createConnectors(subgraphPatterns);

        // Creating necessary variables for the GraphQL query
        final Set<String> fieldPaths = new HashSet<>();
        final Set<GraphQLArgument> queryArgs = new HashSet<>();

        // Get all SGPs without a connector, if they have undeterminable GraphQL type then materializeAll
        final Map<Node,String> withoutConnector = new HashMap<>();
        boolean isDeterminable = true;
        for(final Node n : subgraphPatterns.keySet()){
            final String sgpType = SPARQL2GraphQLHelper.determineSgpType(subgraphPatterns.get(n),config);
            if(!connectors.containsValue(n) && sgpType == null){
                materializeAll(fieldPaths,config,endpoint);
                isDeterminable = false;
                break;
            }
            if(!connectors.containsValue(n)){
                withoutConnector.put(n,sgpType);
            }
        }

        // If materializeAll isn't called, generate query data normally
        if(isDeterminable){
            generateQueryData(config, endpoint, subgraphPatterns, connectors, fieldPaths, 
                queryArgs, withoutConnector);
        }

        return new GraphQLQueryImpl(fieldPaths, queryArgs);
    }

    /**
     * When everything from the endpoint needs to be fetched
     */
    protected void materializeAll(final Set<String> fieldPaths,final GraphQL2RDFConfiguration config, 
            final GraphQLEndpoint endpoint){
        
        int entrypointCounter = 0;
        final Set<String> objectTypeNames = endpoint.getGraphQLObjectTypes();
        for(final String objectTypeName : objectTypeNames){
            // Get the full list entrypoint
            final GraphQLEntrypoint e = endpoint.getEntrypoint(objectTypeName,GraphQLEntrypointType.FULL);
            final String currentPath = GraphQLFieldPathBuilder.addNoArgEntrypoint(e, entrypointCounter);
            fieldPaths.add(GraphQLFieldPathBuilder.addID(currentPath, objectTypeName));

            final Set<String> allObjectURI = URI2GraphQLHelper.getPropertyURIs(objectTypeName,GraphQLFieldType.OBJECT,config, endpoint);
            final Set<String> allScalarURI = URI2GraphQLHelper.getPropertyURIs(objectTypeName,GraphQLFieldType.SCALAR,config, endpoint);

            // Add all fields that nests another object
            for(final String uri : allObjectURI){
                SPARQL2GraphQLHelper.addEmptyObjectField(fieldPaths,config,endpoint,currentPath,objectTypeName,uri);
            }
            // Add all fields that represent scalar values
            for(final String uri : allScalarURI){
                SPARQL2GraphQLHelper.addScalarField(fieldPaths,config,currentPath,uri);
            }

            ++entrypointCounter;
        }
    }

    /**
     * Creates a subgraphpatterns map using @param bgp
     */
    protected Map<Node, Set<TP>> createSubGraphPatterns(final BGP bgp){
        final Map<Node, Set<TP>> subgraphPatterns = new HashMap<>();

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

        return subgraphPatterns;
    }

    /**
     * Creates a connector map using @param subgraphPatterns
     */
    protected Map<Integer,Node> createConnectors(final Map<Node, Set<TP>> subgraphPatterns){
        final Map<Integer,Node> connectors = new HashMap<>();
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
        connectors.keySet().removeAll(connectorsToBeRemoved);
        return connectors;
    } 

    /**
     * Generates query data for @param fieldPaths, @param queryArgs
     */
    protected void generateQueryData(final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint,
            final Map<Node,Set<TP>> subgraphPatterns, final Map<Integer,Node> connectors, final Set<String> fieldPaths,
            final Set<GraphQLArgument> queryArgs, final Map<Node,String> withoutConnector){

        // Counters for creating unique variable and entrypoint names
        int entrypointCounter = 0;
        final MutableInt variableCounter = new MutableInt(0);

        // Create an entrypoint for each SGP without an incomming connector binding
        for (final Node n : withoutConnector.keySet()) {
            final Map<String, LiteralLabel> sgpArguments = SPARQL2GraphQLHelper.getSgpArguments(subgraphPatterns.get(n),config);
            final String sgpType = withoutConnector.get(n);
            final GraphQLEntrypoint e;

            // Select entrypoint with regards to if the SGP has the required arguments
            if (SPARQL2GraphQLHelper.hasAllNecessaryArguments(sgpArguments.keySet(),
                    endpoint.getEntrypoint(sgpType, GraphQLEntrypointType.SINGLE).getArgumentDefinitions().keySet())) {
                // Get single object entrypoint
                e = endpoint.getEntrypoint(sgpType, GraphQLEntrypointType.SINGLE);
            } else if (SPARQL2GraphQLHelper.hasNecessaryArguments(sgpArguments.keySet(),
                    endpoint.getEntrypoint(sgpType, GraphQLEntrypointType.FILTERED).getArgumentDefinitions().keySet())) {
                // Get filtered list entrypoint
                e = endpoint.getEntrypoint(sgpType, GraphQLEntrypointType.FILTERED);
            } else {
                // Get full list entrypoint (No argument values)
                e = endpoint.getEntrypoint(sgpType, GraphQLEntrypointType.FULL);
            }
            
            final String currentPath = GraphQLFieldPathBuilder.addEntrypoint(e, queryArgs, 
                sgpArguments, variableCounter, entrypointCounter);

            SPARQL2GraphQLHelper.addSgp(fieldPaths, subgraphPatterns, connectors, config, endpoint,
                    n, currentPath, sgpType);
            ++entrypointCounter;
        }
    }
}
