package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestBRTPF;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestSPARQL;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestTPFatBRTPFServer;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestTPFatTPFServer;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOpForLogicalOp;

public class PhysicalOpRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
                       implements NullaryPhysicalOpForLogicalOp
{
	protected final LogicalOpRequest<ReqType,MemberType> lop;

	public PhysicalOpRequest( final LogicalOpRequest<ReqType,MemberType> lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public LogicalOpRequest<ReqType,MemberType> getLogicalOperator() {
		return lop;
	}

	@Override
	public NullaryExecutableOp createExecOp( final ExpectedVariables ... inputVars ) {
		final ReqType req = lop.getRequest();
		final MemberType fm = lop.getFederationMember();
		if ( fm instanceof SPARQLEndpoint && req instanceof SPARQLRequest ) {
			return new ExecOpRequestSPARQL( (SPARQLRequest) req, (SPARQLEndpoint) fm );
		}
		else if ( fm instanceof TPFServer && req instanceof TriplePatternRequest ) {
			return new ExecOpRequestTPFatTPFServer( (TriplePatternRequest) req, (TPFServer) fm );
		}
		else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
			return new ExecOpRequestTPFatBRTPFServer( (TriplePatternRequest) req, (BRTPFServer) fm );
		}
		else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
			return new ExecOpRequestBRTPF( (BindingsRestrictedTriplePatternRequest) req, (BRTPFServer) fm );
		}
		else
			throw new IllegalArgumentException("Unsupported combination of federation member (type: " + fm.getClass().getName() + ") and request type (" + req.getClass().getName() + ")");
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 0;

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return lop.getRequest().getExpectedVariables(); }
			@Override public Set<Var> getPossibleVariables() { return new HashSet<>(); }
		};
	}

}
