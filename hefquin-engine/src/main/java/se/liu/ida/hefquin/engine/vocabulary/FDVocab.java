package se.liu.ida.hefquin.engine.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class FDVocab
{
	public static final String uri = "http://www.example.org/se/liu/ida/hefquin/fd#";

	protected static final Resource resource( final String local ) {
		return ResourceFactory.createResource( uri + local );
	}

	protected static final Property property( final String local ) {
		return ResourceFactory.createProperty(uri, local);
	}

	public static final Resource FederationMember    = resource("FederationMember");
	public static final Resource SPARQLEndpoint      = resource("SPARQLEndpoint");
	public static final Resource TPFServer           = resource("TPFServer");
	public static final Resource brTPFServer         = resource("brTPFServer");

	public static final Resource SPARQLEndpointInterface    = resource("SPARQLEndpointInterface");
	public static final Resource TPFInterface               = resource("TPFInterface");
	public static final Resource brTPFInterface             = resource("brTPFInterface");
	public static final Resource GraphQLEndpointInterface   = resource("GraphQLEndpointInterface");
	public static final Resource BoltInterface              = resource("BoltInterface");

	public static final Property interface_               = property("interface");
	public static final Property endpointAddress          = property("endpointAddress");
	public static final Property exampleFragmentAddress   = property("exampleFragmentAddress");

	public static final Property vocabularyMappingsFile     = property("vocabularyMappingsFile");

}
