package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestBRTPF;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestSPARQL;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestTPFatBRTPFServer;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestTPFatTPFServer;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

import java.util.Objects;

/**
 * A physical operator that performs a request at a federation member
 * and, then, outputs the solution mappings obtained via this request.
 *
 * The actual algorithm of this operator is implemented in the following
 * classes, where each of them is specific to a different type of request
 * and federation member.
 * <ul>
 * <li>{@link ExecOpRequestTPFatTPFServer}</li>
 * <li>{@link ExecOpRequestTPFatBRTPFServer}</li>
 * <li>{@link ExecOpRequestBRTPF}</li>
 * <li>{@link ExecOpRequestSPARQL}</li>
 * </ul>
 */
public class PhysicalOpRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember> 
                       extends BaseForPhysicalOps implements NullaryPhysicalOpForLogicalOp
{
	protected final LogicalOpRequest<ReqType,MemberType> lop;

	public PhysicalOpRequest( final LogicalOpRequest<ReqType,MemberType> lop ) {
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
	                                         final ExpectedVariables ... inputVars ) {
		final ReqType req = lop.getRequest();
		final MemberType fm = lop.getFederationMember();
		if ( fm instanceof SPARQLEndpoint && req instanceof SPARQLRequest ) {
			return new ExecOpRequestSPARQL( (SPARQLRequest) req, (SPARQLEndpoint) fm, collectExceptions );
		}
		else if ( fm instanceof TPFServer && req instanceof TriplePatternRequest ) {
			return new ExecOpRequestTPFatTPFServer( (TriplePatternRequest) req, (TPFServer) fm, collectExceptions );
		}
		else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
			return new ExecOpRequestTPFatBRTPFServer( (TriplePatternRequest) req, (BRTPFServer) fm, collectExceptions );
		}
		else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
			return new ExecOpRequestBRTPF( (BindingsRestrictedTriplePatternRequest) req, (BRTPFServer) fm, collectExceptions );
		}
		else
			throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		return lop.getExpectedVariables(inputVars);
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return lop.toString();
	}

}
