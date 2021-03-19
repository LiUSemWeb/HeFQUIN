package se.liu.ida.hefquin.queryplan.physical.impl;

import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.ExecOpSPARQLRequest;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.queryplan.logical.LogicalOpRequest;
import se.liu.ida.hefquin.queryplan.physical.NullaryPhysicalOpForLogicalOp;

public class PhysicalOpSPARQLRequest extends NullaryPhysicalOpImpl implements NullaryPhysicalOpForLogicalOp
{
	protected final LogicalOpRequest<SPARQLRequest,SPARQLEndpoint> lop;

	public PhysicalOpSPARQLRequest( final LogicalOpRequest<SPARQLRequest,SPARQLEndpoint> lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public LogicalOpRequest<SPARQLRequest,SPARQLEndpoint> getLogicalOperator() {
		return lop;
	}

	@Override
	public NullaryExecutableOp createExecOp() {
		return new ExecOpSPARQLRequest( lop.getRequest(), lop.getFederationMember() );
	}

}
