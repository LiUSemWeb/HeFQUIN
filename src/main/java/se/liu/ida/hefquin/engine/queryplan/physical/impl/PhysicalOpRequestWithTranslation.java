package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestTPFatTPFServerWithTranslation;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * This class represents a (physical) request operator that rewrites its
 * request based on the vocabulary mapping associated with the given
 * federation member, and then it also rewrites the solutions mappings
 * by applying the inverse of the vocabulary mapping.
 */
public class PhysicalOpRequestWithTranslation<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
                       implements NullaryPhysicalOpForLogicalOp
{
	protected final LogicalOpRequest<ReqType,MemberType> lop;

	public PhysicalOpRequestWithTranslation( final LogicalOpRequest<ReqType,MemberType> lop ) {
		assert lop != null;
		assert lop.getFederationMember().getVocabularyMapping() != null;

		this.lop = lop;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpRequestWithTranslation<?,?> && ((PhysicalOpRequestWithTranslation<?,?>) o).lop.equals(lop);
	}

	@Override
	public LogicalOpRequest<ReqType,MemberType> getLogicalOperator() {
		return lop;
	}

	@Override
	public NullaryExecutableOp createExecOp( final ExpectedVariables ... inputVars ) {
		final ReqType req = lop.getRequest();
		final MemberType fm = lop.getFederationMember();
		if ( fm instanceof TPFServer && req instanceof TriplePatternRequest ) {
			return new ExecOpRequestTPFatTPFServerWithTranslation( (TriplePatternRequest) req, (TPFServer) fm );
		}
// TODO: we need executable operators for the following cases
//		else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
//			return new ExecOpRequestTPFatBRTPFServer( (TriplePatternRequest) req, (BRTPFServer) fm );
//		}
//		else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
//			return new ExecOpRequestBRTPF( (BindingsRestrictedTriplePatternRequest) req, (BRTPFServer) fm );
//		}
//		else if ( fm instanceof SPARQLEndpoint && req instanceof SPARQLRequest ) {
//			return new ExecOpRequestSPARQL( (SPARQLRequest) req, (SPARQLEndpoint) fm );
//		}
		else
			throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 0;

		return lop.getRequest().getExpectedVariables();
	}

	@Override
	public void visit(final PhysicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return lop.toString();
	}

}
