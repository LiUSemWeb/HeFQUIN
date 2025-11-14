package se.liu.ida.hefquin.federation.catalog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.mappings.impl.VocabularyMappingWrappingImpl;
import se.liu.ida.hefquin.engine.wrappers.graphql.GraphQLException;
import se.liu.ida.hefquin.engine.wrappers.graphql.GraphQLSchemaInitializer;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphql.impl.GraphQLSchemaInitializerImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.catalog.impl.FederationCatalogImpl;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;
import se.liu.ida.hefquin.federation.members.impl.BRTPFServerImpl;
import se.liu.ida.hefquin.federation.members.impl.GraphQLEndpointImpl;
import se.liu.ida.hefquin.federation.members.impl.Neo4jServerImpl;
import se.liu.ida.hefquin.federation.members.impl.SPARQLEndpointImpl;
import se.liu.ida.hefquin.federation.members.impl.TPFServerImpl;
import se.liu.ida.hefquin.federation.members.impl.WrappedRESTEndpointImpl;
import se.liu.ida.hefquin.jenaext.ModelUtils;
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
		addFederationMembers( fd.listResourcesWithProperty(RDF.type, FDVocab.FederationMember),
		                      fd, membersByURI );
		addFederationMembers( fd.listResourcesWithProperty(RDF.type, FDVocab.WrappedFederationMember),
		                      fd, membersByURI );

		return new FederationCatalogImpl(membersByURI);
	}

	protected void addFederationMembers( final ResIterator fedMembers,
	                                     final Model fd,
	                                     final Map<String, FederationMember> membersByURI ) {
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

				final String addrStr = getAsURIString(addr);
				if ( addrStr == null ) {
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

				final String addrStr = getAsURIString(addr);
				if ( addrStr == null ) {
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

				final String addrStr = getAsURIString(addr);
				if ( addrStr == null ) {
					throw new IllegalArgumentException();
				}

				final FederationMember fm = createBRTPFServer(addrStr, vocabMap);
				membersByURI.put(addrStr, fm);
			}
			else if ( ifaceType.equals(FDVocab.BoltInterface) )
			{
				if ( vocabMap != null )
					throw new IllegalArgumentException("Neo4j endpoints cannot have a vocabulary mapping.");

				final StmtIterator endpointAddressesIterator = iface.listProperties(FDVocab.endpointAddress);
				if ( ! endpointAddressesIterator.hasNext() )
					throw new IllegalArgumentException("Bolt endpointAddress is required!");

				final RDFNode addr = endpointAddressesIterator.next().getObject();

				if ( endpointAddressesIterator.hasNext() )
					throw new IllegalArgumentException("More Than One Bolt endpointAddress!");

				final String addrStr = getAsURIString(addr);
				if ( addrStr == null ) {
					throw new IllegalArgumentException();
				}

				final FederationMember fm = createNeo4jServer(addrStr);
				membersByURI.put(addrStr, fm);
			}
			else if ( ifaceType.equals(FDVocab.GraphQLEndpointInterface) )
			{
				if ( vocabMap != null )
					throw new IllegalArgumentException("GraphQL endpoints cannot have a vocabulary mapping.");

				final StmtIterator endpointAddressesIterator = iface.listProperties(FDVocab.endpointAddress);
				if ( ! endpointAddressesIterator.hasNext() )
					throw new IllegalArgumentException("GraphQL endpointAddress is required!");

				final RDFNode addr = endpointAddressesIterator.next().getObject();

				if ( endpointAddressesIterator.hasNext() )
					throw new IllegalArgumentException("More Than One GraphQL endpointAddress!");

				final String addrStr = getAsURIString(addr);
				if ( addrStr == null ) {
					throw new IllegalArgumentException();
				}

				final FederationMember fm = createGraphQLServer(addrStr);
				membersByURI.put(addrStr, fm);
			}
			else if ( ifaceType.equals(FDVocab.RESTInterface) )
			{
				if ( vocabMap != null )
					throw new IllegalArgumentException("REST APIs cannot have a vocabulary mapping.");

				final RDFNode addr = ModelUtils.getSingleMandatoryProperty( iface, FDVocab.endpointAddress );

				final String addrStr = getAsURIString(addr);
				if ( addrStr == null ) {
					throw new IllegalArgumentException();
				}

				final Resource queryParamsList = ModelUtils.getSingleMandatoryResourceProperty( iface, FDVocab.queryParameters );
				if ( ! queryParamsList.canAs(RDFList.class) )
					throw new IllegalArgumentException( FDVocab.queryParameters.getLocalName() + " property of " + iface.toString() + " should be a list." );

				final Iterator<RDFNode> queryParamsIterator = queryParamsList.as( RDFList.class ).iterator();
				final List<RESTEndpoint.Parameter> params = new ArrayList<>();
				while ( queryParamsIterator.hasNext() ) {
					final RDFNode x = queryParamsIterator.next();
					if ( ! x.isResource() )
						throw new IllegalArgumentException( "One of the query parameters of " + iface.toString() + " is not a resource (but, probably, a literal instead)." );

					final Resource p = x.asResource();
					final String name = ModelUtils.getSingleMandatoryProperty_XSDString(p, FDVocab.paramName);
					final String type = getAsURIString( ModelUtils.getSingleMandatoryProperty(p, FDVocab.paramType) );
					if ( type == null )
						throw new IllegalArgumentException();

					final RDFDatatype dt;
					if ( XSDDatatype.XSDstring.getURI().equals(type) ) {
						dt = XSDDatatype.XSDstring;
					}
					else if ( XSDDatatype.XSDinteger.getURI().equals(type) ) {
						dt = XSDDatatype.XSDinteger;
					}
					else if ( XSDDatatype.XSDfloat.getURI().equals(type) ) {
						dt = XSDDatatype.XSDfloat;
					}
					else {
						throw new IllegalArgumentException("Unexpected data type for query parameter: " +  type.toString() );
					}

					final RESTEndpoint.Parameter param = new RESTEndpoint.Parameter() {
						@Override public String getName() { return name; }
						@Override public RDFDatatype getType() { return dt; }
					};

					params.add(param);
				}

				final Resource wrapper = ModelUtils.getSingleMandatoryResourceProperty( fedMember, FDVocab.wrapper );

				final FederationMember fm = createWrappedRESTEndpoint(addrStr, params);
				membersByURI.put(addrStr, fm);
			}
			else {
				throw new IllegalArgumentException( ifaceType.toString() );
			}
		}
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

	protected FederationMember createNeo4jServer( final String uri ) {
		verifyExpectedURI(uri);
		return new Neo4jServerImpl(uri);
	}

	protected FederationMember createGraphQLServer( final String uri ) {
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

		return new GraphQLEndpointImpl(uri, schema);
	}

	protected FederationMember createWrappedRESTEndpoint( final String uri,
	                                               final List<RESTEndpoint.Parameter> params ) {
		verifyExpectedURI(uri);
		return new WrappedRESTEndpointImpl(uri, params);
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

	/**
	 * Returns a string that represents a URI obtained from the given RDF node.
	 * In particular, if the node is a URI, then that URI is returned (as a
	 * string); if the node is an xsd:anyURI literal with a valid URI as its
	 * lexical form, then that URI is returned; otherwise, {@code null} is
	 * returned.
	 */
	protected String getAsURIString( final RDFNode n ) {
		if ( n.isLiteral() && n.asLiteral().getDatatypeURI().equals(XSD.anyURI.getURI()) ) {
			return n.asLiteral().getLexicalForm();
		}
		else if ( n.isURIResource() ) {
			return n.asResource().getURI();
		}
		else {
			return null;
		}
	}

}
