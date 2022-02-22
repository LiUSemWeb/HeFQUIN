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
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.SPARQLEndpointInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.federation.catalog.impl.FederationCatalogImpl;

public class ModFederation extends ModBase
{
	protected final ArgDecl fedDescrDecl   = new ArgDecl(ArgDecl.HasValue, "federationDescription", "fd");
	protected final ArgDecl sparqlEndpointDecl   = new ArgDecl(ArgDecl.HasValue, "considerSPARQLEndpoint");

	protected final Map<String, FederationMember> membersByURI = new HashMap<>();

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
        cmdLine.getUsage().startCategory("Federation specification") ;
        cmdLine.add(sparqlEndpointDecl,   "--considerSPARQLEndpoint",  "URI of a SPARQL endpoint that is part of the federation");
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
	}


	public FederationCatalog getFederationCatalog() {
		return new FederationCatalogImpl(membersByURI);
	}

	protected void addSPARQLEndpoints( final List<String> sparqlEndpointValues ) {
		for ( final String v : sparqlEndpointValues )
			addSPARQLEndpoint(v);
	}

	protected void addSPARQLEndpoint( final String sparqlEndpointValue ) {
		verifyExpectedURI(sparqlEndpointValue);

		final SPARQLEndpointInterface iface = new SPARQLEndpointInterfaceImpl(sparqlEndpointValue);
		final SPARQLEndpoint fm = new SPARQLEndpoint() {
			@Override public SPARQLEndpointInterface getInterface() { return iface; }

			@Override
			public VocabularyMapping getVocabularyMapping() {
				return null;
			}
		};
		membersByURI.put(sparqlEndpointValue, fm);
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
