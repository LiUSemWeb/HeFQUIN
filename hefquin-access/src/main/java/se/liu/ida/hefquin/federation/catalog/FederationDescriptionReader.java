package se.liu.ida.hefquin.federation.catalog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.mappings.impl.VocabularyMappingWrappingImpl;
import se.liu.ida.hefquin.engine.wrappers.graphql.GraphQLException;
import se.liu.ida.hefquin.engine.wrappers.graphql.GraphQLSchemaInitializer;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphql.impl.GraphQLSchemaInitializerImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.catalog.impl.FederationCatalogImpl;
import se.liu.ida.hefquin.federation.impl.BRTPFServerImpl;
import se.liu.ida.hefquin.federation.impl.GraphQLEndpointImpl;
import se.liu.ida.hefquin.federation.impl.Neo4jServerImpl;
import se.liu.ida.hefquin.federation.impl.SPARQLEndpointImpl;
import se.liu.ida.hefquin.federation.impl.TPFServerImpl;
import se.liu.ida.hefquin.vocabulary.FDVocab;

public class FederationDescriptionReader
{
	public static FederationCatalog readFromFile( final String filename ) {
		return instance.parseFedDescr(filename);
	}

	public static FederationCatalog readFromModel( final Model fd ) {
		return instance.parseFedDescr(fd);
	}

	public static FederationDescriptionReader instance = new FederationDescriptionReader();
	final Map<String, VocabularyMapping> vocabMappingByPath = new HashMap<>();

	protected FederationDescriptionReader() {}

	public FederationCatalog parseFedDescr( final String filename ) {
		final Model fd = RDFDataMgr.loadModel(filename);
		return parseFedDescr(fd);
	}

	public FederationCatalog parseFedDescr( final Model fd ) {
		final Map<String, FederationMember> membersByURI = new HashMap<>();

		// Iterate over all federation members mentioned in the description
		final ResIterator fedMembers = fd.listResourcesWithProperty(RDF.type, FDVocab.FederationMember);
		while ( fedMembers.hasNext() ) {
			final Resource fedMember = fedMembers.next();
			final VocabularyMapping vocabMap = parseVocabMapping(fedMember, fd);

			final Resource iface = fedMember.getProperty(FDVocab.interface_).getResource();
			final RDFNode ifaceType = fd.getRequiredProperty(iface, RDF.type).getObject();

			// Check the type of interface
			if ( ifaceType.equals(FDVocab.SPARQLEndpointInterface) )
			{
				final StmtIterator endpointAddressesIterator = iface.listProperties(FDVocab.endpointAddress);
				if ( ! endpointAddressesIterator.hasNext() )
					throw new IllegalArgumentException("SPARQL endpointAddress is required!");

				final RDFNode addr = endpointAddressesIterator.next().getObject();

				if ( endpointAddressesIterator.hasNext() )
					throw new IllegalArgumentException("More Than One SPARQL endpointAddress!");

				final String addrStr;
				if ( addr.isLiteral() ) {
					addrStr = addr.asLiteral().getLexicalForm();
				}
				else if ( addr.isURIResource() ) {
					addrStr = addr.asResource().getURI();
				}
				else {
					throw new IllegalArgumentException();
				}

				final FederationMember fm = createSPARQLEndpoint(addrStr, vocabMap);
				membersByURI.put(addrStr, fm);
			}
			else if ( ifaceType.equals(FDVocab.TPFInterface) )
			{
				final StmtIterator exampleFragmentAddressesIterator = iface.listProperties(FDVocab.exampleFragmentAddress);
				if ( ! exampleFragmentAddressesIterator.hasNext() )
					throw new IllegalArgumentException("TPF exampleFragmentAddress is required!");

				final RDFNode addr = exampleFragmentAddressesIterator.next().getObject();

				if ( exampleFragmentAddressesIterator.hasNext() )
					throw new IllegalArgumentException("More Than One TPF exampleFragmentAddress!");

				final String addrStr;
				if ( addr.isLiteral() ) {
					addrStr = addr.asLiteral().getLexicalForm();
				}
				else if ( addr.isURIResource() ) {
					addrStr = addr.asResource().getURI();
				}
				else {
					throw new IllegalArgumentException();
				}

				final FederationMember fm = createTPFServer(addrStr, vocabMap);
				membersByURI.put(addrStr, fm);
			}
			else if ( ifaceType.equals(FDVocab.brTPFInterface) )
			{
				final StmtIterator exampleFragmentAddressesIterator = iface.listProperties(FDVocab.exampleFragmentAddress);
				if ( ! exampleFragmentAddressesIterator.hasNext() )
					throw new IllegalArgumentException("brTPF exampleFragmentAddress is required!");

				final RDFNode addr = exampleFragmentAddressesIterator.next().getObject();

				if ( exampleFragmentAddressesIterator.hasNext() )
					throw new IllegalArgumentException("More Than One brTPF exampleFragmentAddress!");

				final String addrStr;
				if ( addr.isLiteral() ) {
					addrStr = addr.asLiteral().getLexicalForm();
				}
				else if ( addr.isURIResource() ) {
					addrStr = addr.asResource().getURI();
				}
				else {
					throw new IllegalArgumentException();
				}

				final FederationMember fm = createBRTPFServer(addrStr, vocabMap);
				membersByURI.put(addrStr, fm);
			}
			else if ( ifaceType.equals(FDVocab.BoltInterface) )
			{
				final StmtIterator endpointAddressesIterator = iface.listProperties(FDVocab.endpointAddress);
				if ( ! endpointAddressesIterator.hasNext() )
					throw new IllegalArgumentException("Bolt endpointAddress is required!");

				final RDFNode addr = endpointAddressesIterator.next().getObject();

				if ( endpointAddressesIterator.hasNext() )
					throw new IllegalArgumentException("More Than One Bolt endpointAddress!");

				final String addrStr;
				if ( addr.isLiteral() ) {
					addrStr = addr.asLiteral().getLexicalForm();
				}
				else if ( addr.isURIResource() ) {
					addrStr = addr.asResource().getURI();
				}
				else {
					throw new IllegalArgumentException();
				}

				final FederationMember fm = createNeo4jServer(addrStr, vocabMap);
				membersByURI.put(addrStr, fm);
			}
			else if ( ifaceType.equals(FDVocab.GraphQLEndpointInterface) )
			{
				final StmtIterator endpointAddressesIterator = iface.listProperties(FDVocab.endpointAddress);
				if ( ! endpointAddressesIterator.hasNext() )
					throw new IllegalArgumentException("GraphQL endpointAddress is required!");

				final RDFNode addr = endpointAddressesIterator.next().getObject();

				if ( endpointAddressesIterator.hasNext() )
					throw new IllegalArgumentException("More Than One GraphQL endpointAddress!");

				final String addrStr;
				if ( addr.isLiteral() ) {
					addrStr = addr.asLiteral().getLexicalForm();
				}
				else if ( addr.isURIResource() ) {
					addrStr = addr.asResource().getURI();
				}
				else {
					throw new IllegalArgumentException();
				}

				final FederationMember fm = createGraphQLServer(addrStr, vocabMap);
				membersByURI.put(addrStr, fm);
			}
			else {
				throw new IllegalArgumentException( ifaceType.toString() );
			}

		}

		return new FederationCatalogImpl(membersByURI);
	}

	/**
	 * Attempts to retrieve and parse the vocabulary mapping associated with the
	 * given RDF resource {@code fm}, representing a {@link FederationMember}, in
	 * the given federation description {@code fd}.
	 *
	 * The method attempts to load the vocabulary mapping from the specified path or
	 * URL and caches the result for reuse. If no vocabulary mappings file is
	 * present, the method returns {@code null}.
	 *
	 * @param fm RDF resource for the federation member
	 * @param fd RDF model of the federation description
	 * @return parsed {@link VocabularyMapping}, or {@code null} if not specified
	 * @throws IllegalArgumentException if the mapping file cannot be loaded or parsed
	 */
	protected VocabularyMapping parseVocabMapping( final Resource fm, final Model fd ) {
		if ( ! fd.contains( fm, FDVocab.vocabularyMappingsFile ) ) {
			return null;
		}

		final RDFNode pathToMappingFile = fd.getRequiredProperty( fm, FDVocab.vocabularyMappingsFile ).getObject();

		final String path = pathToMappingFile.toString();
		VocabularyMapping vm = vocabMappingByPath.get( path );
		if ( vm == null ) {
			final Graph g;
			try {
				g = RDFDataMgr.loadGraph( path );
			} catch ( Exception e ) {
				throw new IllegalArgumentException( e );
			}
			vm = new VocabularyMappingWrappingImpl( g );
			vocabMappingByPath.put( path, vm );
		}
		return vm;
	}

	protected FederationMember createSPARQLEndpoint( final String uri, final VocabularyMapping vm ) {
		verifyExpectedURI(uri);
		return new SPARQLEndpointImpl(uri, vm);
	}

	protected FederationMember createTPFServer( final String uri, final VocabularyMapping vm ) {
		verifyExpectedURI(uri);
		return new TPFServerImpl(uri, vm);
	}

	protected FederationMember createBRTPFServer( final String uri, final VocabularyMapping vm ) {
		verifyExpectedURI(uri);
		return new BRTPFServerImpl(uri, vm);
	}

	protected FederationMember createNeo4jServer( final String uri, final VocabularyMapping vm ) {
		verifyExpectedURI(uri);
		return new Neo4jServerImpl(uri, vm);
	}

	protected FederationMember createGraphQLServer( final String uri, final VocabularyMapping vm ) {
		verifyExpectedURI(uri);

		final GraphQLSchemaInitializer init = new GraphQLSchemaInitializerImpl();

		final int connTimeout = 5000;
		final int readTimeout = 5000;

		final GraphQLSchema schema;
		try {
			schema = init.initializeSchema(uri, connTimeout, readTimeout);
		}
		catch ( final GraphQLException | ParseException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}

		return new GraphQLEndpointImpl(uri, schema, vm);
	}

	/**
	 * Verifies that the given string represents an HTTP URI
	 * or an HTTPS URI and, if so, returns that URI.
	 */
	protected URI verifyExpectedURI( final String uriString ) {
		final URI uri;
		try {
			uri = new URI(uriString);
			if (    ! uri.isAbsolute()
			     || (uri.getScheme().equals("http") && uri.getScheme().equals("https")) ) {
				throw new IllegalArgumentException( "The following URI is of an unexpected type; it should be an HTTP URI or an HTTPS URI: " + uriString );
			}
		}
		catch ( final URISyntaxException ex ) {
			throw new IllegalArgumentException("URI parse exception (" + ex.getMessage() + ")");
		}

		return uri;
	}

}
