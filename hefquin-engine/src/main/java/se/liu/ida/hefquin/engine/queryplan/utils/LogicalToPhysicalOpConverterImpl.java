package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.List;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpRegistry;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;

/**
 * This class provides methods to convert logical operators into
 * physical operators by using the respective default type of
 * physical operator for each type of logical operator.
 */
public class LogicalToPhysicalOpConverterImpl implements LogicalToPhysicalOpConverter
{
	private final PhysicalOpRegistry registry;

	public LogicalToPhysicalOpConverterImpl( final List<PhysicalOpFactory> popFactories ) {
		registry = new PhysicalOpRegistry();
		for ( final PhysicalOpFactory f : popFactories ) {
			registry.register(f);
		}
	}

	@Override
	public NullaryPhysicalOp convert( final NullaryLogicalOp lop ) {
		return registry.create(lop);
	}

	@Override
	public UnaryPhysicalOp convert( final UnaryLogicalOp lop,
	                                final ExpectedVariables inputVars ) {
		return registry.create(lop, inputVars);
	}

	@Override
	public BinaryPhysicalOp convert( final BinaryLogicalOp lop,
	                                 final ExpectedVariables inputVars1,
	                                 final ExpectedVariables inputVars2 ) {
		return registry.create(lop, inputVars1, inputVars2);
	}

	@Override
	public NaryPhysicalOp convert( final NaryLogicalOp lop,
	                               final ExpectedVariables... inputVars ) {
		return registry.create(lop, inputVars);
	}
}
