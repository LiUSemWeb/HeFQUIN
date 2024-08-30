package se.liu.ida.hefquin.engine.wrappers.graphql.utils;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.BGPImpl;

/**
 * This class represents a subject-based star pattern, that is,
 * a set of triple patterns that all share the same subject.
 */
public class StarPattern extends BGPImpl
{
	private Node subject = null;

	public Node getSubject() {
		return subject;
	}

	@Override
	public void addTriplePattern( final TriplePattern tp ) {
		if ( subject == null ) {
			subject = tp.asJenaTriple().getSubject();
		}
		else {
			assert tp.asJenaTriple().getSubject().equals(subject);
		}

		super.addTriplePattern(tp);
	}
}
