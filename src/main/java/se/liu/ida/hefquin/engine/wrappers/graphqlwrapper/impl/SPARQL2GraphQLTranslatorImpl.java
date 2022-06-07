package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.atlas.json.JsonNull;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.SPARQL2GraphQLTranslator;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLArgument;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLArgumentImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointPath;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLIDPath;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl.GraphQLQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.GraphCycleDetector;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.SGPNode;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.SPARQL2GraphQLHelper;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.URI2GraphQLHelper;

public class SPARQL2GraphQLTranslatorImpl implements SPARQL2GraphQLTranslator {

    @Override
    public GraphQLQuery translateBGP(final BGP bgp, final GraphQL2RDFConfiguration config,
            final GraphQLEndpoint endpoint) {

        // Initialize necessary data structures
        final Map<Node, Set<TriplePattern>> subgraphPatterns = createSubGraphPatterns(bgp);
        final Map<TriplePattern, Node> connectors = createConnectors(subgraphPatterns);

        // Get all SGPs without a connector, if they have undeterminable GraphQL type then materializeAll
        final Map<Node,String> withoutConnector = new HashMap<>();
        for(final Node n : subgraphPatterns.keySet()){
            final String sgpType = SPARQL2GraphQLHelper.determineSgpType(subgraphPatterns.get(n),config);
            if(!connectors.containsValue(n) && sgpType == null){
                return materializeAll(config,endpoint);
            }
            if(!connectors.containsValue(n)){
                withoutConnector.put(n,sgpType);
            }
        }

        return generateQueryData(config, endpoint, subgraphPatterns, connectors, withoutConnector);
    }

    /**
     * Genereates a GraphQL query that fetches everyhing from @param endpoint
     */
    protected GraphQLQuery materializeAll(final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint){

        int entrypointCounter = 0;
        final Set<String> finishedFieldPaths = new HashSet<>();
        final Set<String> objectTypeNames = endpoint.getGraphQLObjectTypes();
        for(final String objectTypeName : objectTypeNames){
            // Get the full list entrypoint
            final GraphQLEntrypoint e = endpoint.getEntrypoint(objectTypeName,GraphQLEntrypointType.FULL);
            final String currentPath = new GraphQLEntrypointPath(e, entrypointCounter).toString();
            finishedFieldPaths.add(currentPath + new GraphQLIDPath(objectTypeName));

            final Set<String> allObjectURI = URI2GraphQLHelper.getPropertyURIs(objectTypeName,GraphQLFieldType.OBJECT,config, endpoint);
            final Set<String> allScalarURI = URI2GraphQLHelper.getPropertyURIs(objectTypeName,GraphQLFieldType.SCALAR,config, endpoint);

            // Add all fields that nests another object
            for(final String uri : allObjectURI){
                finishedFieldPaths.add(SPARQL2GraphQLHelper.addEmptyObjectField(config,endpoint,currentPath,objectTypeName,uri));
            }
            // Add all fields that represent scalar values
            for(final String uri : allScalarURI){
                finishedFieldPaths.add(SPARQL2GraphQLHelper.addScalarField(config,currentPath,uri));
            }

            ++entrypointCounter;
        }

        return new GraphQLQueryImpl(finishedFieldPaths,new HashSet<>());
    }

    /**
     * Creates a subgraphpatterns map using @param bgp
     */
    protected Map<Node, Set<TriplePattern>> createSubGraphPatterns(final BGP bgp){
        final Map<Node, Set<TriplePattern>> subgraphPatterns = new HashMap<>();

        // Partition BGP into SGPs and give unique integer id to TPs
        final Set<? extends TriplePattern> bgpSet = bgp.getTriplePatterns();
        for(final TriplePattern t : bgpSet){
            final Node subject = t.asJenaTriple().getSubject();
            if(subgraphPatterns.containsKey(subject)){
                subgraphPatterns.get(subject).add(t);
            }
            else{
                final Set<TriplePattern> sgp = new HashSet<>();
                sgp.add(t);
                subgraphPatterns.put(subject, sgp);
            }
        }

        return subgraphPatterns;
    }

    /**
     * Creates a connector map using @param subgraphPatterns
     */
    protected Map<TriplePattern,Node> createConnectors(final Map<Node, Set<TriplePattern>> subgraphPatterns){
        final Map<TriplePattern,Node> connectors = new HashMap<>();
        final Map<Node,SGPNode> sgpNodes = new HashMap<>();

        for ( final Node subject : subgraphPatterns.keySet() ) {
            for ( final TriplePattern tp : subgraphPatterns.get(subject) ) {
                final Node object = tp.asJenaTriple().getObject();

                if(subgraphPatterns.containsKey(object) && ! object.equals(subject)){
                    SGPNode subjectSgpNode = sgpNodes.get(subject);
                    SGPNode objectSgpNode = sgpNodes.get(object);

                    if ( subjectSgpNode == null ) {
                        subjectSgpNode = new SGPNode();
                        sgpNodes.put(subject, subjectSgpNode);
                    }

                    if ( objectSgpNode == null ) {
                        objectSgpNode = new SGPNode();
                        sgpNodes.put(object, objectSgpNode);
                    }

                    subjectSgpNode.addAdjacentNode(tp, objectSgpNode);
                    connectors.put(tp, object);
                }

            }
        }

        // Remove all potential cyclic connector bindings
        final Set<TriplePattern> connectorsToBeRemoved = GraphCycleDetector.determineCyclicConnectors(sgpNodes.values());
        connectors.keySet().removeAll(connectorsToBeRemoved);
        return connectors;
    } 

    /**
     * Generates a GraphQL query from provided @param subgraphPatterns,connnectors,withoutConnnectors
     */
    protected GraphQLQuery generateQueryData(final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint,
            final Map<Node,Set<TriplePattern>> subgraphPatterns, final Map<TriplePattern,Node> connectors, 
            final Map<Node,String> withoutConnector){

        final Set<String> fieldPaths = new HashSet<>();
        final Set<GraphQLArgument> queryArgs = new HashSet<>();
        
        // Counters for creating unique variable and entrypoint names
        int entrypointCounter = 0;
        int variableCounter = 0;

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

            // Create GraphQLArguments for the current path
            final Set<GraphQLArgument> pathArguments = new LinkedHashSet<>();
            final Map<String, String> entrypointArgDefs = e.getArgumentDefinitions();

            if (entrypointArgDefs != null && !entrypointArgDefs.isEmpty()) {
                for (final String argName : new TreeSet<String>(entrypointArgDefs.keySet())) {
                    final String variableName = "var" + variableCounter;

                    final String currArgDefinition = entrypointArgDefs.get(argName);
                    final JsonValue currArgValue;

                    if (sgpArguments.containsKey(argName)) {
                        currArgValue = SPARQL2GraphQLHelper.literalToJsonValue(sgpArguments.get(argName));
                    } else {
                        currArgValue = JsonNull.instance;
                    }

                    final GraphQLArgument currArg = new GraphQLArgumentImpl(variableName, argName, currArgValue, currArgDefinition);
                    queryArgs.add(currArg);
                    pathArguments.add(currArg);
                    ++variableCounter;
                }
            }

            final String currentPath = new GraphQLEntrypointPath(e,entrypointCounter,pathArguments).toString();

            fieldPaths.addAll(SPARQL2GraphQLHelper.addSgp(subgraphPatterns, connectors, config, endpoint,
                n, currentPath, sgpType));
            ++entrypointCounter;
        }

        return new GraphQLQueryImpl(fieldPaths, queryArgs);
    }
}
