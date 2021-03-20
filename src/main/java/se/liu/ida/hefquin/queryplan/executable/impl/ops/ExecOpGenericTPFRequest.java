package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;

public abstract class ExecOpGenericTPFRequest<MemberType extends FederationMember>
                extends ExecOpGenericTriplesRequest<TriplePatternRequest, MemberType>
{
	public ExecOpGenericTPFRequest( final TriplePatternRequest req, final MemberType fm ) {
		super( req, fm );
	}

	@Override
	protected Iterator<? extends SolutionMapping> convert( final Iterator<? extends Triple> itTriples ) {
		return TriplesToSolMapsConverter.convert( itTriples, req.getQueryPattern() );
	}

}
