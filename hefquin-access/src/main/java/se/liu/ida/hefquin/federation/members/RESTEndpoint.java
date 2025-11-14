package se.liu.ida.hefquin.federation.members;

import org.apache.jena.datatypes.RDFDatatype;

import se.liu.ida.hefquin.federation.FederationMember;

public interface RESTEndpoint extends FederationMember
{
	/** Returns the URL of this REST endpoint. */
	String getURL();

	int getNumberOfParameters();

	Iterable<Parameter> getParameters();

	public interface Parameter {
		String getName();
		RDFDatatype getType();
	}

}
