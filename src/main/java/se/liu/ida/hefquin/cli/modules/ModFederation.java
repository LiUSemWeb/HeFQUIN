package se.liu.ida.hefquin.cli.modules;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.impl.VocabularyMappingImpl;
import se.liu.ida.hefquin.engine.federation.*;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.*;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.federation.catalog.impl.FederationCatalogImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQLEndpointInitializer;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.GraphQLEndpointInitializerImpl;
import se.liu.ida.hefquin.vocabulary.FD;

public class ModFederation extends ModBase
{
	protected final ArgDecl fedDescrDecl   = new ArgDecl(ArgDecl.HasValue, "federationDescription", "fd");
	protected final ArgDecl sparqlEndpointDecl   = new ArgDecl(ArgDecl.HasValue, "considerSPARQLEndpoint");
	protected final ArgDecl tpfServerDecl        = new ArgDecl(ArgDecl.HasValue, "considerTPFServer");
	protected final ArgDecl brtpfServerDecl      = new ArgDecl(ArgDecl.HasValue, "considerBRTPFServer");

	protected final Map<String, FederationMember> membersByURI = new HashMap<>();

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Federation");
		cmdLine.add(fedDescrDecl,  "--federationDescription",  "file with an RDF description of the federation");
		cmdLine.add(sparqlEndpointDecl,  "--considerSPARQLEndpoint",  "URI of a SPARQL endpoint that is part of the federation (this argument can be used multiple times for multiple endpoints)");
		cmdLine.add(tpfServerDecl,       "--considerTPFServer",  "URI of a fragment provided by a TPF server that is part of the federation (this argument can be used multiple times for multiple TPF servers)");
		cmdLine.add(brtpfServerDecl,     "--considerBRTPFServer",  "URI of a fragment provided by a brTPF server that is part of the federation (this argument can be used multiple times for multiple brTPF servers)");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		if ( cmdLine.contains(fedDescrDecl) ) {
			final String fedDescrFilename = cmdLine.getValue(fedDescrDecl);
			parseFedDescr(fedDescrFilename);
		}

		if ( cmdLine.contains(sparqlEndpointDecl) ) {
			try {
				addSPARQLEndpoints( cmdLine.getValues(sparqlEndpointDecl) );
			}
			catch ( final IllegalArgumentException ex ) {
				cmdLine.cmdError( ex.getMessage() );
			}
		}

		if ( cmdLine.contains(tpfServerDecl) ) {
			try {
				addTPFServers( cmdLine.getValues(tpfServerDecl) );
			}
			catch ( final IllegalArgumentException ex ) {
				cmdLine.cmdError( ex.getMessage() );
			}
		}

		if ( cmdLine.contains(brtpfServerDecl) ) {
			try {
				addBRTPFServers( cmdLine.getValues(brtpfServerDecl) );
			}
			catch ( final IllegalArgumentException ex ) {
				cmdLine.cmdError( ex.getMessage() );
			}
		}
	}


	public FederationCatalog getFederationCatalog() {
		return new FederationCatalogImpl(membersByURI);
	}

	protected void parseFedDescr( final String filename ) {
		final Model fd = RDFDataMgr.loadModel(filename);

		final ResIterator fedMembers = fd.listResourcesWithProperty(RDF.type, FD.FederationMember);
		// Iterate over all federation members
		while ( fedMembers.hasNext() ) {
			final Resource fedMember = fedMembers.next();
			VocabularyMapping vocabMap = null;
			if( fd.contains(fedMember, FD.vocabularyMappingsFile) ){
				final RDFNode pathToMappingFile = fd.getRequiredProperty(fedMember, FD.vocabularyMappingsFile).getObject();

				final String path = pathToMappingFile.toString();
				if ( verifyValidVocabMappingFile( path ) ) {
					vocabMap = new VocabularyMappingImpl(path);
				}
			}

			final Resource iface = fedMember.getProperty(FD.interface_).getResource();
			final RDFNode ifaceType = fd.getRequiredProperty(iface, RDF.type).getObject();
			// Check the type of interface
			if ( ifaceType.equals(FD.SPARQLEndpointInterface) ){
				final RDFNode addr = fd.getRequiredProperty(iface, FD.endpointAddress).getObject();

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
				addSPARQLEndpoint(addrStr, vocabMap);
			}
			else if ( ifaceType.equals(FD.TPFInterface) ){
				final RDFNode addr = fd.getRequiredProperty(iface, FD.exampleFragmentAddress).getObject();

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

				addTPFServer(addrStr, vocabMap);
			}
			else if ( ifaceType.equals(FD.brTPFInterface) ){
				final RDFNode addr = fd.getRequiredProperty(iface, FD.exampleFragmentAddress).getObject();

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

				addBRTPFServer(addrStr, vocabMap);
			}
			else if ( ifaceType.equals(FD.BoltInterface) ){
				final RDFNode addr = fd.getRequiredProperty(iface, FD.endpointAddress).getObject();

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

				addNeo4jServer(addrStr, vocabMap);
			}
			else if ( ifaceType.equals(FD.GraphQLEndpointInterface) ){
				final RDFNode addr = fd.getRequiredProperty(iface, FD.endpointAddress).getObject();

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

				addGraphQLServer(addrStr);
			}

		}
	}

	protected void addSPARQLEndpoints( final List<String> sparqlEndpointValues ) {
		for ( final String v : sparqlEndpointValues )
			addSPARQLEndpoint(v, null);
	}

	protected void addTPFServers( final List<String> uris ) {
		for ( final String uri : uris )
			addTPFServer(uri, null);
	}

	protected void addBRTPFServers( final List<String> uris ) {
		for ( final String uri : uris )
			addBRTPFServer(uri, null);
	}

	protected void addSPARQLEndpoint( final String sparqlEndpointValue, final VocabularyMapping vocabMappings ) {
		verifyExpectedURI(sparqlEndpointValue);

		final SPARQLEndpointInterface iface = new SPARQLEndpointInterfaceImpl(sparqlEndpointValue);
		final SPARQLEndpoint fm = new SPARQLEndpoint() {
			@Override public SPARQLEndpointInterface getInterface() { return iface; }
			@Override public VocabularyMapping getVocabularyMapping() { return vocabMappings; }
		};

		membersByURI.put(sparqlEndpointValue, fm);
	}

	protected void addTPFServer( final String uri, final VocabularyMapping vocabMappings ) {
		verifyExpectedURI(uri);

		final TPFInterface iface = TPFInterfaceUtils.createTPFInterface(uri);
		final TPFServer fm = new TPFServer() {
			@Override public VocabularyMapping getVocabularyMapping() { return vocabMappings; }
			@Override public TPFInterface getInterface() { return iface; }
		};

		membersByURI.put(uri, fm);
	}

	protected void addBRTPFServer( final String uri, final VocabularyMapping vocabMappings ) {
		verifyExpectedURI(uri);

		final BRTPFInterface iface = BRTPFInterfaceUtils.createBRTPFInterface(uri);
		final BRTPFServer fm = new BRTPFServer() {
			@Override public VocabularyMapping getVocabularyMapping() { return vocabMappings; }
			@Override public BRTPFInterface getInterface() { return iface; }
		};

		membersByURI.put(uri, fm);
	}

	protected void addGraphQLServer( final String uri ) {
		verifyExpectedURI(uri);

		final GraphQLEndpointInitializer init = new GraphQLEndpointInitializerImpl();

		final int connTimeout = 5000;
        final int readTimeout = 5000;

		final GraphQLEndpoint fm;
		try {
			fm = init.initializeEndpoint(uri, connTimeout, readTimeout);
			membersByURI.put(uri, fm);
		}
		catch ( final FederationAccessException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( final ParseException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void addNeo4jServer( final String uri, final VocabularyMapping vocabMappings ) {
		verifyExpectedURI(uri);

		final Neo4jInterface iface = new Neo4jInterfaceImpl(uri);
		final Neo4jServer fm = new Neo4jServer() {
			@Override public VocabularyMapping getVocabularyMapping() { return vocabMappings; }
			@Override public Neo4jInterface getInterface() { return iface; }
		};

		membersByURI.put(uri, fm);
	}

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

	protected boolean verifyValidVocabMappingFile(final String pathString ) {
		final File f = new File(pathString);
		if ( f.exists() && f.isFile() ){
			return true;
		}
		else
			throw new IllegalArgumentException( "The following path to vocab.mapping does not exist:" + pathString );
	}

}
