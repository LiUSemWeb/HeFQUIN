package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.Collection;
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
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.StarPattern;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.URI2GraphQLHelper;

public class SPARQL2GraphQLTranslatorImpl implements SPARQL2GraphQLTranslator {

    @Override
    public GraphQLQuery translateBGP(final BGP bgp, final GraphQL2RDFConfiguration config,
            final GraphQLEndpoint endpoint) {

        // Initialize necessary data structures
        // - collection of subject-based star patterns of the given
        //   BGP, indexed by their respective subject nodes
        final Map<Node, StarPattern> indexedStarPatterns = decomposeIntoStarPatterns(bgp);
        // - mapping from triple patterns to star patterns such that
        //   each triple pattern whose object is the subject of a star
        //   pattern maps to that star pattern
        final Map<TriplePattern, StarPattern> connectors = createConnectors(indexedStarPatterns);

        // Determine all star patterns that do not have an incoming connector,
        // if they have undeterminable GraphQL type then materializeAll
        final Set<StarPattern> withoutConnectorTmp = determineSPsWithoutIncomingConnector( indexedStarPatterns.values(), connectors );
        final Map<StarPattern,String> withoutConnector = new HashMap<>();
        for ( final StarPattern sp : withoutConnectorTmp ) {
            final String sgpType = SPARQL2GraphQLHelper.determineSgpType(sp, config);
            if ( sgpType == null ) {
                return materializeAll(config, endpoint);
            }
            withoutConnector.put(sp, sgpType);
        }

        return generateQueryData(config, endpoint, indexedStarPatterns, connectors, withoutConnector);
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
     * Decomposes the given BGP into its subject-based star patterns and returns
     * the resulting star patterns indexed by their respective subject nodes.
     */
    protected Map<Node, StarPattern> decomposeIntoStarPatterns(final BGP bgp){
        final Map<Node, StarPattern> result = new HashMap<>();
        for ( final TriplePattern tp : bgp.getTriplePatterns() ){
            final Node subject = tp.asJenaTriple().getSubject();

            StarPattern starPattern = result.get(subject);
            if ( starPattern == null ) {
                starPattern = new StarPattern();
                result.put(subject, starPattern);
            }

            starPattern.addTriplePattern(tp);
        }

        return result;
    }

    /**
     * Creates a connector map using @param indexedStarPatterns
     */
    protected Map<TriplePattern,StarPattern> createConnectors( final Map<Node, StarPattern> indexedStarPatterns ) {
        final Map<TriplePattern,StarPattern> connectors = new HashMap<>();
        final Map<Node,SGPNode> sgpNodes = new HashMap<>();

        for ( final StarPattern sp : indexedStarPatterns.values() ) {
            final Node subject = sp.getSubject();
            for ( final TriplePattern tp : sp.getTriplePatterns() ) {
                final Node object = tp.asJenaTriple().getObject();
                final StarPattern spForObject = indexedStarPatterns.get(object); // may be null!

                if ( spForObject != null && ! object.equals(subject) ) {
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
                    connectors.put(tp, spForObject);
                }

            }
        }

        // Remove all potential cyclic connector bindings
        final Set<TriplePattern> connectorsToBeRemoved = GraphCycleDetector.determineCyclicConnectors(sgpNodes.values());
        connectors.keySet().removeAll(connectorsToBeRemoved);
        return connectors;
    } 

    /**
     * Returns a subset of the given collection of star patterns by filtering
     * out every star pattern that has an incoming connector. In other words,
     * the returned set contains only the star patterns that do not have an
     * incoming connector.
     */
    protected Set<StarPattern> determineSPsWithoutIncomingConnector( final Collection<StarPattern> sps,
                                                                     final Map<TriplePattern, StarPattern> connectors ) {
        final Set<StarPattern> result = new HashSet<>();
        for ( final StarPattern sp : sps ) {
            final boolean hasConnectors = connectors.containsValue(sp);
            if ( ! hasConnectors ) {
                result.add(sp);
            }
        }
        return result;
    }

    /**
     * Generates a GraphQL query from provided @param indexedStarPatterns,connectors,withoutConnnectors
     */
    protected GraphQLQuery generateQueryData(final GraphQL2RDFConfiguration config, final GraphQLEndpoint endpoint,
            final Map<Node, StarPattern> indexedStarPatterns, final Map<TriplePattern,StarPattern> connectors, 
            final Map<StarPattern,String> withoutConnector){

        final Set<String> fieldPaths = new HashSet<>();
        final Set<GraphQLArgument> queryArgs = new HashSet<>();
        
        // Counters for creating unique variable and entrypoint names
        int entrypointCounter = 0;
        int variableCounter = 0;

        // Create an entrypoint for each star pattern without an incomming connector binding
        for ( final java.util.Map.Entry<StarPattern, String>  entry : withoutConnector.entrySet() ) {
            final StarPattern sp = entry.getKey();
            final Map<String, LiteralLabel> sgpArguments = SPARQL2GraphQLHelper.getArguments(sp,config);
            final String sgpType = entry.getValue();
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

            fieldPaths.addAll(SPARQL2GraphQLHelper.addSgp(sp, indexedStarPatterns, connectors, config, endpoint,
                currentPath, sgpType));
            ++entrypointCounter;
        }

        return new GraphQLQueryImpl(fieldPaths, queryArgs);
    }
}
