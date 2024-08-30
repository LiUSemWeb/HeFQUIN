package se.liu.ida.hefquin.base.data.mappings.impl;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.base.data.mappings.TermMapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TermMappingImpl implements TermMapping
{
	protected final Node type;
	protected final Node globalTerm;
	protected final Set<Node> localTerms;

	public TermMappingImpl( final Node type, final Node globalTerm, final Set<Node> localTerms ) {
		assert type != null;
		assert globalTerm != null;
		assert localTerms != null;
		assert ! localTerms.isEmpty();

		this.type = type;
		this.globalTerm = globalTerm;
		this.localTerms = localTerms;
	}

	public TermMappingImpl( final Node type, final Node globalTerm, final Node ... localTerms ) {
		this( type, globalTerm, toSet(localTerms) );
	}

	protected static final Set<Node> toSet( final Node ... array ) {
		if ( array.length == 0 ) return Collections.emptySet();
		if ( array.length == 1 ) return Collections.singleton( array[0] );
		return new HashSet<>( Arrays.asList(array) );
	}

	@Override
	public Node getTypeOfRule() {
		return type;
	}

	@Override
	public Node getGlobalTerm() {
		return globalTerm;
	}

	@Override
	public Set<Node> getLocalTerms() {
		return localTerms;
	}

}
