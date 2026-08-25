package se.liu.ida.hefquin.federation.members.impl;

import java.util.Objects;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.federation.members.RDFBasedFederationMember;

public abstract class BaseForRDFBasedFederationMember
		extends BaseForFederationMember
		implements RDFBasedFederationMember
{
	private final VocabularyMapping vm;

	protected BaseForRDFBasedFederationMember( final Node serviceURI ) {
		this(serviceURI, null);
	}

	protected BaseForRDFBasedFederationMember( final Node serviceURI,
	                                           final VocabularyMapping vm ) {
		super(serviceURI);
		this.vm = vm;
	}

	@Override
	public VocabularyMapping getVocabularyMapping() {
		return vm;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( super.equals(o) == false )
			return false;

		return    o instanceof BaseForRDFBasedFederationMember fm
		       && Objects.equals( fm.getVocabularyMapping(), vm );
	}

}
