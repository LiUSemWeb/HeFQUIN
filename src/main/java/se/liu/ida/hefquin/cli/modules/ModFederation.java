package se.liu.ida.hefquin.cli.modules;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.cmd.ModBase;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFInterface;
import se.liu.ida.hefquin.engine.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.engine.federation.access.TPFInterface;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.BRTPFInterfaceUtils;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.SPARQLEndpointInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.TPFInterfaceUtils;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.federation.catalog.impl.FederationCatalogImpl;

public class ModFederation extends ModBase
{
	protected final ArgDecl fedDescrDecl   = new ArgDecl(ArgDecl.HasValue, "federationDescription", "fd");
	protected final ArgDecl sparqlEndpointDecl   = new ArgDecl(ArgDecl.HasValue, "considerSPARQLEndpoint");
	protected final ArgDecl tpfServerDecl        = new ArgDecl(ArgDecl.HasValue, "considerTPFServer");
	protected final ArgDecl brtpfServerDecl      = new ArgDecl(ArgDecl.HasValue, "considerBRTPFServer");

	protected final Map<String, FederationMember> membersByURI = new HashMap<>();

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
        cmdLine.getUsage().startCategory("Federation") ;
        cmdLine.add(sparqlEndpointDecl,  "--considerSPARQLEndpoint",  "URI of a SPARQL endpoint that is part of the federation (this argument can be used multiple times for multiple endpoints)");
        cmdLine.add(tpfServerDecl,       "--considerTPFServer",  "URI of a fragment provided by a TPF server that is part of the federation (this argument can be used multiple times for multiple TPF servers)");
        cmdLine.add(brtpfServerDecl,     "--considerBRTPFServer",  "URI of a fragment provided by a brTPF server that is part of the federation (this argument can be used multiple times for multiple brTPF servers)");
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
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

	protected void addSPARQLEndpoints( final List<String> sparqlEndpointValues ) {
		for ( final String v : sparqlEndpointValues )
			addSPARQLEndpoint(v);
	}

	protected void addTPFServers( final List<String> uris ) {
		for ( final String uri : uris )
			addTPFServer(uri);
	}

	protected void addBRTPFServers( final List<String> uris ) {
		for ( final String uri : uris )
			addBRTPFServer(uri);
	}

	protected void addSPARQLEndpoint( final String sparqlEndpointValue ) {
		verifyExpectedURI(sparqlEndpointValue);

		final SPARQLEndpointInterface iface = new SPARQLEndpointInterfaceImpl(sparqlEndpointValue);
		final SPARQLEndpoint fm = new SPARQLEndpoint() {
			@Override public SPARQLEndpointInterface getInterface() { return iface; }
			@Override public VocabularyMapping getVocabularyMapping() { return null; }
		};

		membersByURI.put(sparqlEndpointValue, fm);
	}

	protected void addTPFServer( final String uri ) {
		verifyExpectedURI(uri);

		final TPFInterface iface = TPFInterfaceUtils.createTPFInterface(uri);
		final TPFServer fm = new TPFServer() {
			@Override public VocabularyMapping getVocabularyMapping() { return null; }
			@Override public TPFInterface getInterface() { return iface;}
		};

		membersByURI.put(uri, fm);
	}

	protected void addBRTPFServer( final String uri ) {
		verifyExpectedURI(uri);

		final BRTPFInterface iface = BRTPFInterfaceUtils.createBRTPFInterface(uri);
		final BRTPFServer fm = new BRTPFServer() {
			@Override public VocabularyMapping getVocabularyMapping() { return null; }
			@Override public BRTPFInterface getInterface() { return iface;}
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

}
