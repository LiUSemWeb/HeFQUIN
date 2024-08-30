package se.liu.ida.hefquin.engine.wrappers.lpg.conf.impl;

import org.apache.jena.graph.NodeFactory;

public class DefaultLPG2RDFConfigurationImpl extends LPG2RDFConfigurationImpl
{
	protected static final String ELM_PREFIX = "http://example.org/relationship/";
	protected static final String PM_PREFIX  = "http://example.org/property/";
	protected static final String PREDICATE_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

	public DefaultLPG2RDFConfigurationImpl() {
		super( new NodeMappingImpl_AllToBNodes(),
		       new NodeLabelMappingImpl_AllToLiterals(),
		       new EdgeLabelMappingImpl_AllToURIs(ELM_PREFIX),
		       new PropertyNameMappingImpl_AllToURIs(PM_PREFIX),
		       NodeFactory.createURI(PREDICATE_LABEL) );
	}

}
