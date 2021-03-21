package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;

public abstract class ExecOpGenericIndexNestedLoopsJoinWithTPFRequests<MemberType extends FederationMember>
                   extends ExecOpGenericIndexNestedLoopsJoinWithTriplesRequests<TriplePattern,MemberType,TriplePatternRequest>
{
	public ExecOpGenericIndexNestedLoopsJoinWithTPFRequests( final TriplePattern query, final MemberType fm ) {
		super( query, fm );
	}

	@Override
	protected TriplePatternRequest createRequest( final SolutionMapping sm ) {
		final TriplePattern tp = JenaBasedQueryPatternUtils.applySolMapToTriplePattern(sm, query);
		return new TriplePatternRequestImpl(tp);
	}

	@Override
	protected Iterator<? extends SolutionMapping> convert( final Iterator<? extends Triple> itTriples, final TriplePatternRequest req ) {
		return TriplesToSolMapsConverter.convert( itTriples, req.getQueryPattern() );
	}

}
