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
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
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
import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.exprs.MappingExpression;
import se.liu.ida.hefquin.mappings.algebra.exprs.MappingExpressionFactory;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpProject;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;
import se.liu.ida.hefquin.mappings.rml.RML2MappingAlgebra;
import se.liu.ida.hefquin.mappings.rml.RMLParserException;
import se.liu.ida.hefquin.vocabulary.FDVocab;
import se.liu.ida.hefquin.vocabulary.HydraVocab;

public class FederationDescriptionReader
{
	public static FederationCatalog readFromFile( final String filename ) {
		return instance.parseFedDescr(filename);
	}

	public static FederationCatalog readFromFiles( final List<String> filenames ) {
		return  instance.parseFedDescr(filenames);
	}

	public static FederationCatalog readFromModel( final Model fd ) {
		return instance.parseFedDescr(fd);
	}

	public static FederationCatalog readFromModels( final List<Model> fds ) {
		final Model mergedModel = ModelFactory.createDefaultModel();
		for ( final Model fd : fds ) {
			mergedModel.add(fd);
		}
		return instance.parseFedDescr(mergedModel);
	}

	public static FederationDescriptionReader instance = new FederationDescriptionReader();
	final Map<String, VocabularyMapping> vocabMappingByPath = new HashMap<>();

	protected FederationDescriptionReader() {}

	public FederationCatalog parseFedDescr( final String filename ) {
		final Model fd = RDFDataMgr.loadModel(filename);
		return parseFedDescr(fd);
	}

	public FederationCatalog parseFedDescr( final List<String> filenames ) {
		final Model mergedModel = ModelFactory.createDefaultModel();
		for ( final String filename : filenames ) {
			mergedModel.add( RDFDataMgr.loadModel(filename) );
		}
		return parseFedDescr(mergedModel);
	}

	public FederationCatalog parseFedDescr( final Model fd ) {
		final Map<String, FederationMember> membersByURI = new HashMap<>();

		final StmtIterator it = fd.listStatements(null, FDVocab.serviceURI, (RDFNode) null);
		while ( it.hasNext() ) {
			final Statement st = it.next();
			final Resource fedMember = st.getSubject();
			final String serviceURI = getAsURIString( st.getObject() );

			if ( serviceURI == null ) {
				throw new IllegalArgumentException( "Illegal serviceURI value: " + st.getObject().toString() );
			}

			final FederationMember fm = createFederationMember( fedMember,
			                                                    NodeFactory.createURI(serviceURI),
			                                                    fd );
			membersByURI.put(serviceURI, fm);
		}

		return new FederationCatalogImpl(membersByURI);
	}

	protected FederationMember createFederationMember( final Resource fedMember,
	                                                   final Node serviceURI,
	                                                   final Model fd ) {
		final VocabularyMapping vocabMap = parseVocabMapping(fedMember, fd);

		final Resource iface = fedMember.getProperty(FDVocab.interface_).getResource();
		final RDFNode ifaceType = fd.getRequiredProperty(iface, RDF.type).getObject();
		final Resource protocol = iface.getProperty(FDVocab.supportedProtocol).getResource();

		// Check the type of interface
		if ( ifaceType.equals(FDVocab.FixedEndpointInterface) )
		{
			return handleFixedEndpointInterface( iface, protocol, vocabMap, fedMember, fd, serviceURI );
		}
		else if ( ifaceType.equals(FDVocab.FragmentInterface) )
		{
			return handleFragmentInterface( iface, protocol, vocabMap, serviceURI );
		}
		else if ( ifaceType.equals(FDVocab.TemplateBasedInterface) )
		{
			return handleTemplateInterface( iface, protocol, vocabMap, fedMember, fd, serviceURI );
		}
		else {
			throw new IllegalArgumentException( ifaceType.toString() );
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

	/**
	 * Creates a federation member for a fixed endpoint interface (SPARQL, Bolt, GraphQL).
	 */
	protected FederationMember handleFixedEndpointInterface( final Resource iface,
	                                                         final Resource protocol,
	                                                         final VocabularyMapping vocabMap,
	                                                         final Resource fedMember,
	                                                         final Model fd,
	                                                         final Node serviceURI ) {
		if ( protocol.equals(FDVocab.SPARQLProtocol) ) {
			final String addrStr = getSingleURIProperty(
				iface,
				FDVocab.endpointAddress,
				"SPARQL endpointAddress is required!",
				"More than one SPARQL endpointAddress!" );

			return createSPARQLEndpoint(serviceURI, addrStr, vocabMap);
		}

		if ( protocol.equals(FDVocab.GenericWebAPIProtocol) ) {
			if ( vocabMap != null )
				throw new IllegalArgumentException("REST APIs cannot have a vocabulary mapping.");

			final String addrStr = getSingleURIProperty(
				iface,
				FDVocab.endpointAddress,
				"REST endpointAddress is required!",
				"More than one REST endpointAddress!" );

			final List<MappingExpression> trMaps = parseRMLMapping(fedMember, fd, serviceURI);

			if ( trMaps.isEmpty() )
				throw new IllegalArgumentException("The wrapped REST endpoint with service URI <" + serviceURI + "> does not have any RML triples maps.");

			return createWrappedRESTEndpoint(serviceURI, addrStr, null, trMaps);
		}

		if ( protocol.equals(FDVocab.BoltProtocol) ) {
			if ( vocabMap != null )
				throw new IllegalArgumentException("Neo4j endpoints cannot have a vocabulary mapping.");

			final String addrStr = getSingleURIProperty(
				iface,
				FDVocab.endpointAddress,
				"Bolt endpointAddress is required!",
				"More than one Bolt endpointAddress!" );

			return createNeo4jServer(serviceURI, addrStr);
		}

		if ( protocol.equals(FDVocab.GraphQLProtocol) ) {
			if ( vocabMap != null )
				throw new IllegalArgumentException("GraphQL endpoints cannot have a vocabulary mapping.");

			final String addrStr = getSingleURIProperty(
				iface,
				FDVocab.endpointAddress,
				"GraphQL endpointAddress is required!",
				"More than one GraphQL endpointAddress!" );

			return createGraphQLServer(serviceURI, addrStr);
		}

		throw new IllegalArgumentException( protocol.toString() );
	}

	/**
	 * Creates a federation member for a fragment interface (TPF, brTPF).
	 */
	protected FederationMember handleFragmentInterface( final Resource iface,
	                                                    final Resource protocol,
	                                                    final VocabularyMapping vocabMap,
	                                                    final Node serviceURI ) {
		if ( protocol.equals(FDVocab.TPFProtocol) ) {
			final String addrStr = getSingleURIProperty(
				iface,
				FDVocab.exampleFragmentAddress,
				"TPF exampleFragmentAddress is required!",
				"More than one TPF exampleFragmentAddress!" );

			return createTPFServer(serviceURI, addrStr, vocabMap);
		}
		else if ( protocol.equals(FDVocab.brTPFProtocol) ) {
			final String addrStr = getSingleURIProperty(
				iface,
				FDVocab.exampleFragmentAddress,
				"brTPF exampleFragmentAddress is required!",
				"More than one brTPF exampleFragmentAddress!" );

			return createBRTPFServer(serviceURI, addrStr, vocabMap);
		}
		else {
			throw new IllegalArgumentException( protocol.toString() );
		}
	}

	/**
	 * Creates a federation member for a template-based interface, including parameter
	 * and mapping handling.
	 */
	protected FederationMember handleTemplateInterface( final Resource iface,
	                                                    final Resource protocol,
	                                                    final VocabularyMapping vocabMap,
	                                                    final Resource fedMember,
	                                                    final Model fd,
	                                                    final Node serviceURI ) {
		if ( protocol.equals(FDVocab.GenericWebAPIProtocol) ) {
			if ( vocabMap != null )
				throw new IllegalArgumentException("REST APIs cannot have a vocabulary mapping.");

			final Resource uriTemplate = ModelUtils.getSingleMandatoryResourceProperty(iface, FDVocab.uriTemplate);

			final String uriTemplateString = ModelUtils.getSingleMandatoryProperty_XSDString(uriTemplate, HydraVocab.template);

			final StmtIterator paramIter = uriTemplate.listProperties(HydraVocab.mapping);

			final List<RESTEndpoint.Parameter> params = new ArrayList<>();
			while ( paramIter.hasNext() ) {
				final RDFNode x = paramIter.next().getObject();
				if ( !x.isResource() )
					throw new IllegalArgumentException("One of the query parameters of " + iface.toString()
							+ " is not a resource (but, probably, a literal instead).");

				final Resource p = x.asResource();
				final String name = ModelUtils.getSingleMandatoryProperty_XSDString(p, HydraVocab.variable);
				final String type = getAsURIString(ModelUtils.getSingleOptionalProperty(p, FDVocab.paramType));

				final Statement isRequiredStmt = p.getProperty(HydraVocab.required);
				final boolean isRequired = isRequiredStmt == null ? false : isRequiredStmt.getBoolean();

				final RDFDatatype dt;
				if ( type == null ) {
					dt = null;
				}
				else if ( XSDDatatype.XSDstring.getURI().equals(type) ) {
					dt = XSDDatatype.XSDstring;
				}
				else if ( XSDDatatype.XSDinteger.getURI().equals(type) ) {
					dt = XSDDatatype.XSDinteger;
				}
				else if ( XSDDatatype.XSDfloat.getURI().equals(type) ) {
					dt = XSDDatatype.XSDfloat;
				}
				else if ( XSDDatatype.XSDdouble.getURI().equals(type) ) {
					dt = XSDDatatype.XSDdouble;
				}
				else if ( XSDDatatype.XSDdecimal.getURI().equals(type) ) {
					dt = XSDDatatype.XSDdecimal;
				}
				else {
					throw new IllegalArgumentException("Unexpected data type for query parameter: " + type.toString());
				}

				final RESTEndpoint.Parameter param = new RESTEndpoint.Parameter() {
					@Override public String getName() { return name; }
					@Override public RDFDatatype getType() { return dt; }
					@Override public boolean isRequired() { return isRequired; }
				};

				params.add(param);
			}

			final List<MappingExpression> trMaps = parseRMLMapping(fedMember, fd, serviceURI);

			if ( trMaps.isEmpty() )
				throw new IllegalArgumentException("The wrapped REST endpoint with service URI <" + serviceURI + "> does not have any RML triples maps.");

			return createWrappedRESTEndpoint(serviceURI, uriTemplateString, params, trMaps);
		}
		else {
			throw new IllegalArgumentException( protocol.toString() );
		}
	}

	protected List<MappingExpression> parseRMLMapping( final Resource fedMember,
	                                                   final Model fd,
	                                                   final Node serviceURI ) {
		final Resource wrapper = ModelUtils.getSingleMandatoryResourceProperty( fedMember, FDVocab.wrapper );

		// TODO: Each of the TrMap-expressions created in the following
		// should be cached to avoid producing it again if another federation
		// member uses the exact same triples map.
		final Resource rmlTMsList = ModelUtils.getSingleMandatoryResourceProperty( wrapper, FDVocab.rmlTriplesMaps );
		if ( ! rmlTMsList.canAs(RDFList.class) )
			throw new IllegalArgumentException( FDVocab.rmlTriplesMaps.getLocalName() + " property of " + wrapper.toString() + " should be a list." );

		final Iterator<RDFNode> rmTMsIterator = rmlTMsList.as( RDFList.class ).iterator();
		final Node baseIRI = NodeFactory.createURI("http://example.org/FixedBaseIRI/HardcodedInFederationDescriptionReader/");
		final List<MappingExpression> trMaps = new ArrayList<>();
		while ( rmTMsIterator.hasNext() ) {
			final RDFNode tm = rmTMsIterator.next();
			if ( tm.isResource() ) {
				final MappingExpression trMap;
				try {
					trMap = RML2MappingAlgebra.convert( tm.asResource(),
					                                    fd,
					                                    baseIRI,
					                                    null );
				}
				catch ( final RMLParserException e ) {
					throw new IllegalArgumentException("There is a problem in the RML mapping for <" + serviceURI.toString() + ">: " +  e.getMessage(), e );
				}

				trMaps.add(trMap);
			}
		}

		return trMaps;
	}

	protected FederationMember createSPARQLEndpoint( final Node serviceURI,
	                                                 final String uri,
	                                                 final VocabularyMapping vm ) {
		verifyExpectedURI(uri);
		return new SPARQLEndpointImpl(serviceURI, uri, vm);
	}

	protected FederationMember createTPFServer( final Node serviceURI,
	                                            final String uri,
	                                            final VocabularyMapping vm ) {
		verifyExpectedURI(uri);
		return new TPFServerImpl(serviceURI, uri, vm);
	}

	protected FederationMember createBRTPFServer( final Node serviceURI,
	                                              final String uri,
	                                              final VocabularyMapping vm ) {
		verifyExpectedURI(uri);
		return new BRTPFServerImpl(serviceURI, uri, vm);
	}

	protected FederationMember createNeo4jServer( final Node serviceURI,
	                                              final String uri ) {
		verifyExpectedURI(uri);
		return new Neo4jServerImpl(serviceURI, uri);
	}

	protected FederationMember createGraphQLServer( final Node serviceURI,
	                                                final String uri ) {
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

		return new GraphQLEndpointImpl(serviceURI, uri, schema);
	}

	protected FederationMember createWrappedRESTEndpoint( final Node serviceURI,
	                                                      final String uri,
	                                                      final List<RESTEndpoint.Parameter> params,
	                                                      final List<MappingExpression> trMaps ) {
		assert ! trMaps.isEmpty();

		if ( trMaps.size() == 1 ) {
			final MappingExpression expr = trMaps.get(0);
			return new WrappedRESTEndpointImpl(serviceURI, uri, params, expr);
		}

		final MappingExpression[] exprs = new MappingExpression[ trMaps.size() ];
		final MappingOperator op = MappingOpProject.createWithSPOG();
		int i = 0;
		for ( final MappingExpression trMapExpr : trMaps ) {
			exprs[i++] = MappingExpressionFactory.create(op, trMapExpr);
		}

		final MappingExpression expr = MappingExpressionFactory.create(
				MappingOpUnion.getInstance(),
				exprs );
		return new WrappedRESTEndpointImpl(serviceURI, uri, params, expr);
	}

	/**
	 * Returns the single URI value of the given property from the resource.
	 * Throws an exception if the property is missing, occurs more than once
	 * or is not a valid URI.
	 */
	protected String getSingleURIProperty( final Resource iface,
	                                       final Property p,
	                                       final String errorMsg1,
	                                       final String errorMsg2 ) {
		final StmtIterator addressesIterator = iface.listProperties(p);
		if ( ! addressesIterator.hasNext() )
			throw new IllegalArgumentException(errorMsg1);

		final RDFNode addr = addressesIterator.next().getObject();

		if ( addressesIterator.hasNext() )
			throw new IllegalArgumentException(errorMsg2);

		final String addrStr = getAsURIString(addr);
		if ( addrStr == null ) {
			throw new IllegalArgumentException();
		}

		return addrStr;
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
			     || ! (uri.getScheme().equals("http") || uri.getScheme().equals("https")) ) {
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
	 * returned (including for the case that the given node is already
	 * {@code null}).
	 */
	protected String getAsURIString( final RDFNode n ) {
		if ( n == null )
			return null;

		if ( n.isURIResource() )
			return n.asResource().getURI();

		if ( n.isLiteral() ) {
			final Literal lit = n.asLiteral();
			final String anyURI = XSD.anyURI.getURI();
			if ( lit.getDatatypeURI().equals(anyURI) )
				return lit.getLexicalForm();
		}

		return null;
	}

}
