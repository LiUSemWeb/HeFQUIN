package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestBRTPF;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestOther;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestSPARQL;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestTPF;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

import java.util.Objects;

/**
 * A physical operator that performs a request at a federation member
 * and, then, outputs the solution mappings obtained via this request.
 *
 * The actual algorithm of this operator is implemented in the following
 * classes, where each of them is specific to a different type of request
 * and federation member.
 * <ul>
 * <li>{@link ExecOpRequestTPF}</li>
 * <li>{@link ExecOpRequestBRTPF}</li>
 * <li>{@link ExecOpRequestSPARQL}</li>
 * <li>{@link ExecOpRequestOther}</li>
 * </ul>
 */
public class PhysicalOpRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember> 
                       extends BaseForQueryPlanOperator
                       implements NullaryPhysicalOpForLogicalOp
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	protected final LogicalOpRequest<ReqType,MemberType> lop;

	protected PhysicalOpRequest( final LogicalOpRequest<ReqType,MemberType> lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpRequest<?,?> && ((PhysicalOpRequest<?,?>) o).lop.equals(lop);
	}

	@Override
	public int hashCode(){
		return lop.hashCode() ^ Objects.hash( this.getClass().getName() );
	}

	@Override
	public LogicalOpRequest<ReqType,MemberType> getLogicalOperator() {
		return lop;
	}

	@Override
	public NullaryExecutableOp createExecOp( final boolean collectExceptions,
	                                         final QueryPlanningInfo qpInfo,
		                                     final ExpectedVariables ... inputVars ) {
		final ReqType req = lop.getRequest();
		final MemberType fm = lop.getFederationMember();
		if ( fm instanceof SPARQLEndpoint sep && req instanceof SPARQLRequest sreq ) {
			return new ExecOpRequestSPARQL<>(sreq, sep, collectExceptions, qpInfo);
		}
		else if ( fm instanceof TPFServer tpf && req instanceof TriplePatternRequest tpreq ) {
			return new ExecOpRequestTPF<>(tpreq, tpf, collectExceptions, qpInfo);
		}
		else if ( fm instanceof BRTPFServer brtpf && req instanceof TriplePatternRequest tpreq ) {
			return new ExecOpRequestTPF<>(tpreq, brtpf, collectExceptions, qpInfo);
		}
		else if ( fm instanceof BRTPFServer brtpf && req instanceof BindingsRestrictedTriplePatternRequest brtpreq ) {
			return new ExecOpRequestBRTPF(brtpreq, brtpf, collectExceptions, qpInfo);
		}
		else if ( fm instanceof RESTEndpoint ep && req instanceof SPARQLRequest sreq ) {
			return new ExecOpRequestOther(sreq, ep, collectExceptions, qpInfo);
		}
		else
			throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return lop.toString();
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return ( lop instanceof LogicalOpRequest );
		}

		@Override
		public PhysicalOpRequest<?, ?> create( final NullaryLogicalOp lop ) {
			if ( lop instanceof  LogicalOpRequest<?,?> op ) {
				return new PhysicalOpRequest<>(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}
