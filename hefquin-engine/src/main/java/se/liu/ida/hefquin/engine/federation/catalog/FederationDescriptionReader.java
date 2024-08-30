package se.liu.ida.hefquin.engine.federation.catalog;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.mappings.impl.VocabularyMappingWrappingImpl;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFInterface;
import se.liu.ida.hefquin.engine.federation.access.GraphQLInterface;
import se.liu.ida.hefquin.engine.federation.access.Neo4jInterface;
import se.liu.ida.hefquin.engine.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.engine.federation.access.TPFInterface;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.BRTPFInterfaceUtils;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.GraphQLInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.Neo4jInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.SPARQLEndpointInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.TPFInterfaceUtils;
import se.liu.ida.hefquin.engine.federation.catalog.impl.FederationCatalogImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQLException;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQLSchemaInitializer;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.GraphQLSchemaInitializerImpl;
import se.liu.ida.hefquin.engine.vocabulary.FD;

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
		final ResIterator fedMembers = fd.listResourcesWithProperty(RDF.type, FD.FederationMember);
		while ( fedMembers.hasNext() ) {
			final Resource fedMember = fedMembers.next();
			final VocabularyMapping vocabMap = parseVocabMapping(fedMember, fd);

			final Resource iface = fedMember.getProperty(FD.interface_).getResource();
			final RDFNode ifaceType = fd.getRequiredProperty(iface, RDF.type).getObject();

			// Check the type of interface
			if ( ifaceType.equals(FD.SPARQLEndpointInterface) )
			{
				final StmtIterator endpointAddressesIterator = iface.listProperties(FD.endpointAddress);
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
			else if ( ifaceType.equals(FD.TPFInterface) )
			{
				final StmtIterator exampleFragmentAddressesIterator = iface.listProperties(FD.exampleFragmentAddress);
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
			else if ( ifaceType.equals(FD.brTPFInterface) )
			{
				final StmtIterator exampleFragmentAddressesIterator = iface.listProperties(FD.exampleFragmentAddress);
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
			else if ( ifaceType.equals(FD.BoltInterface) )
			{
				final StmtIterator endpointAddressesIterator = iface.listProperties(FD.endpointAddress);
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
			else if ( ifaceType.equals(FD.GraphQLEndpointInterface) )
			{
				final StmtIterator endpointAddressesIterator = iface.listProperties(FD.endpointAddress);
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
	 * Checks whether given RDF resource (fm) representing a federation
	 * member is associated with a vocabulary mapping in the given federation
	 * description (fd) and, if so, parses this vocabulary mapping and returns
	 * it. Otherwise, this function returns <code>null</code>.
	 */
	protected VocabularyMapping parseVocabMapping( final Resource fm, final Model fd ) {
		if( fd.contains(fm, FD.vocabularyMappingsFile) ){
			final RDFNode pathToMappingFile = fd.getRequiredProperty(fm, FD.vocabularyMappingsFile).getObject();

			final String path = pathToMappingFile.toString();
			if ( verifyValidVocabMappingFile(path) ) {
				VocabularyMapping vm = vocabMappingByPath.get( path );
				if ( vm == null ) {
					vm = new VocabularyMappingWrappingImpl(path);
					vocabMappingByPath.put( path, vm );
				}
				return vm;
			}
		}

		return null;
	}

	protected FederationMember createSPARQLEndpoint( final String uri, final VocabularyMapping vm ) {
		verifyExpectedURI(uri);

		final SPARQLEndpointInterface iface = new SPARQLEndpointInterfaceImpl(uri);
		final SPARQLEndpoint fm = new SPARQLEndpoint() {
			@Override public SPARQLEndpointInterface getInterface() { return iface; }
			@Override public VocabularyMapping getVocabularyMapping() { return vm; }
			@Override public String toString( ) { return "SPARQL endpoint (" + iface.toString() + ")"; }
		};

		return fm;
	}

	protected FederationMember createTPFServer( final String uri, final VocabularyMapping vm ) {
		verifyExpectedURI(uri);

		final TPFInterface iface = TPFInterfaceUtils.createTPFInterface(uri);
		final TPFServer fm = new TPFServer() {
			@Override public VocabularyMapping getVocabularyMapping() { return vm; }
			@Override public TPFInterface getInterface() { return iface; }
			@Override public String toString( ) { return "TPF server (" + iface.toString() + ")"; }
		};

		return fm;
	}

	protected FederationMember createBRTPFServer( final String uri, final VocabularyMapping vm ) {
		verifyExpectedURI(uri);

		final BRTPFInterface iface = BRTPFInterfaceUtils.createBRTPFInterface(uri);
		final BRTPFServer fm = new BRTPFServer() {
			@Override public VocabularyMapping getVocabularyMapping() { return vm; }
			@Override public BRTPFInterface getInterface() { return iface; }
			@Override public String toString( ) { return "brTPF server (" + iface.toString() + ")"; }
		};

		return fm;
	}

	protected FederationMember createNeo4jServer( final String uri, final VocabularyMapping vm ) {
		verifyExpectedURI(uri);

		final Neo4jInterface iface = new Neo4jInterfaceImpl(uri);
		final Neo4jServer fm = new Neo4jServer() {
			@Override public VocabularyMapping getVocabularyMapping() { return vm; }
			@Override public Neo4jInterface getInterface() { return iface; }
			@Override public String toString( ) { return "Neo4j server (" + iface.toString() + ")"; }
		};

		return fm;
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

		final GraphQLInterface iface = new GraphQLInterfaceImpl(uri);

		return new GraphQLEndpoint() {
			@Override public VocabularyMapping getVocabularyMapping() { return vm; }
			@Override public GraphQLInterface getInterface() { return iface; }
			@Override public GraphQLSchema getSchema() { return schema; }
			@Override public String toString( ) { return "GraphQL endpoint at " + uri; }
		};
	}

	/**
	 * Verifies that the file at the given path exists.
	 */
	protected boolean verifyValidVocabMappingFile( final String pathToMappingFile ) {
		final File f = new File(pathToMappingFile);
		if ( f.exists() && f.isFile() ){
			return true;
		}
		else
			throw new IllegalArgumentException( "The following path to vocab.mapping does not exist:" + pathToMappingFile );
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
