package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class LogicalToPhysicalPlanConverterImpl implements LogicalToPhysicalPlanConverter
{
	protected final boolean ignorePhysicalOpsForLogicalAddOps;
	protected final boolean ignoreParallelMultiLeftJoin;

	public LogicalToPhysicalPlanConverterImpl( final boolean ignorePhysicalOpsForLogicalAddOps,
	                                           final boolean ignoreParallelMultiLeftJoin ) {
		this.ignorePhysicalOpsForLogicalAddOps = ignorePhysicalOpsForLogicalAddOps;
		this.ignoreParallelMultiLeftJoin = ignoreParallelMultiLeftJoin;
	}

	@Override
	public PhysicalPlan convert( final LogicalPlan lp, final boolean keepMultiwayJoins ) {
		return new Worker().convert(lp, keepMultiwayJoins);
	}

	// makes sure that sub-plans that are contained multiple times in the
	// given logical plan are converted only once; hence, the corresponding
	// physical sub-plan will then also contained multiple times within the
	// overall physical plan that is produced
	protected class Worker
	{
		final protected Map<LogicalPlan, PhysicalPlan> convertedSubPlans = new HashMap<>();

		public PhysicalPlan convert( final LogicalPlan lp, final boolean keepMultiwayJoins ) {
			final PhysicalPlan alreadyConverted = convertedSubPlans.get(lp);
			if ( alreadyConverted != null ) {
				return alreadyConverted;
			}

			final List<PhysicalPlan> children = convertChildren(lp, keepMultiwayJoins);
			final PhysicalPlan pp = createPhysicalPlan( lp.getRootOperator(), children, keepMultiwayJoins );
			convertedSubPlans.put(lp, pp);
			return pp;
		}

		/**
		 * Converts the sub-plans of the given logical plan (if any) and returns
		 * a list of the resulting physical plans. The order of the physical plans
		 * in the returned list is such that the i-th physical plan in the list
		 * is the physical plan that has been created for the i-th sub-plan of
		 * the given logical plan. For logical plans that do not contain any
		 * sub-plans, an empty list is returned.
		 */
		protected List<PhysicalPlan> convertChildren( final LogicalPlan lp,
		                                              final boolean keepMultiwayJoins ) {
			final List<PhysicalPlan> children = new ArrayList<PhysicalPlan>();
			final int numChildren = lp.numberOfSubPlans();
			if ( numChildren > 0 ) {
				for ( int i = 0; i < numChildren; ++i ) {
					children.add( convert(lp.getSubPlan(i), keepMultiwayJoins) );
				}
			}
			return children;
		}

		protected PhysicalPlan createPhysicalPlan( final LogicalOperator lop,
		                                           final List<PhysicalPlan> children,
		                                           final boolean keepMultiwayJoins )
		{
			if ( lop instanceof NullaryLogicalOp ) {
				if ( children.size() != 0 )
					throw new IllegalArgumentException( "unexpected number of sub-plans: " + children.size() );

				return createPhysicalPlanWithNullaryRoot( (NullaryLogicalOp) lop );
			}
			else if ( lop instanceof UnaryLogicalOp ) {
				if ( children.size() != 1 )
					throw new IllegalArgumentException( "unexpected number of sub-plans: " + children.size() );

				return createPhysicalPlanWithUnaryRoot( (UnaryLogicalOp) lop, children.get(0) );
			}
			else if ( lop instanceof BinaryLogicalOp ) {
				if ( children.size() != 2 )
					throw new IllegalArgumentException( "unexpected number of sub-plans: " + children.size() );

				return createPhysicalPlanWithBinaryRoot( (BinaryLogicalOp) lop, children.get(0), children.get(1) );
			}
			else if ( lop instanceof NaryLogicalOp ) {
				if ( children.size() < 1 )
					throw new IllegalArgumentException( "unexpected number of sub-plans: " + children.size() );

				return createPhysicalPlanWithNaryRoot( (NaryLogicalOp) lop, children, keepMultiwayJoins );
			}
			else {
				throw new IllegalArgumentException( "unknown logical operator: " + lop.getClass().getName() );
			}
		}

		protected PhysicalPlan createPhysicalPlanWithNullaryRoot( final NullaryLogicalOp lop ) {
			return PhysicalPlanFactory.createPlan(lop);
		}

		protected PhysicalPlan createPhysicalPlanWithUnaryRoot( final UnaryLogicalOp lop, final PhysicalPlan child ) {
			return PhysicalPlanFactory.createPlan(lop, child);
		}

		protected PhysicalPlan createPhysicalPlanWithBinaryRoot( final BinaryLogicalOp lop, final PhysicalPlan child1, final PhysicalPlan child2 ) {
			return PhysicalPlanFactory.createPlan(lop, child1, child2);
		}

		protected PhysicalPlan createPhysicalPlanWithNaryRoot( final NaryLogicalOp lop,
		                                                       final List<PhysicalPlan> children,
		                                                       final boolean keepMultiwayJoins )
		{
			if ( lop instanceof LogicalOpMultiwayJoin ) {
				return createPhysicalPlanForMultiwayJoin( (LogicalOpMultiwayJoin) lop, children, keepMultiwayJoins );
			}
			else if ( lop instanceof LogicalOpMultiwayLeftJoin ) {
				return createPhysicalPlanForMultiwayLeftJoin( (LogicalOpMultiwayLeftJoin) lop, children, keepMultiwayJoins );
			}
			else {
				return PhysicalPlanFactory.createPlan(lop, children);
			}
		}

		protected PhysicalPlan createPhysicalPlanForMultiwayJoin( final LogicalOpMultiwayJoin lop,
		                                                          final List<PhysicalPlan> children,
		                                                          final boolean keepMultiwayJoins )
		{
			if ( children.size() == 1 ) {
				return children.get(0);
			}

			if ( keepMultiwayJoins ) {
				final NaryPhysicalOp pop = new BaseForPhysicalOpMultiwayJoin(lop) {
					@Override public void visit(PhysicalPlanVisitor visitor) { throw new UnsupportedOperationException(); }
					@Override public NaryExecutableOp createExecOp(boolean collectExceptions, ExpectedVariables... inputVars) { throw new UnsupportedOperationException(); }
				};
				return PhysicalPlanFactory.createPlan(pop, children);
			}

			// Multiway joins are converted to a left-deep plan of joins, where
			// tpAdd and bgpAdd are used when possible; otherwise, binary joins
			// are used by default.
			PhysicalPlan currentSubPlan = children.get(0);
			for ( int i = 1; i < children.size(); ++i ) {
				final PhysicalPlan nextChild = children.get(i);
				if( ! ignorePhysicalOpsForLogicalAddOps ) {
					currentSubPlan = PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentSubPlan, nextChild);
				}
				else {
					currentSubPlan = createPhysicalPlanWithBinaryRoot(
							LogicalOpJoin.getInstance(),
							currentSubPlan,
							nextChild );
				}
			}

			return currentSubPlan;
		}

		protected PhysicalPlan createPhysicalPlanForMultiwayLeftJoin( final LogicalOpMultiwayLeftJoin lop,
		                                                              final List<PhysicalPlan> children,
		                                                              final boolean keepMultiwayJoins )
		{
			if ( children.size() == 1 ) {
				return children.get(0);
			}

			// Before going to the generic option that works for all cases,
			// check whether we have a case in which the parallel multi-left-
			// join can be used.
			if ( ! ignoreParallelMultiLeftJoin && children.size() > 2 ) {
				final List<LogicalOpRequest<?,?>> optionalParts = PhysicalOpParallelMultiLeftJoin.checkApplicability(children);
				if ( optionalParts != null ) {
					// If the parallel multi-left-join can indeed be used, do so.
					final UnaryPhysicalOp rootOp = new PhysicalOpParallelMultiLeftJoin(optionalParts);
					return PhysicalPlanFactory.createPlan( rootOp, children.get(0) );
				}
			}

			// Now comes the generic option that works for all cases.

			// Multiway left joins are converted to right-deep plans of right
			// outer joins, where tpOptAdd and bgpOptAdd are used when possible;
			// otherwise, binary outer joins are used by default.

			// The first child of the multiway left join is the non-optional part
			// and, thus, is used as the right input to the first right outer join
			// (second case below) or as the input to the tpOptAdd/bgpOptAdd (first
			// case below).
			PhysicalPlan currentSubPlan = children.get(0);
			for ( int i = 1; i < children.size(); ++i ) {
				final PhysicalPlan nextChild = children.get(i);
				final PhysicalOperator rootOpOfNextChild = nextChild.getRootOperator();
				if( ! ignorePhysicalOpsForLogicalAddOps && rootOpOfNextChild instanceof PhysicalOpRequest ){
					currentSubPlan = createPhysicalPlanWithUnaryRoot(
							LogicalOpUtils.createLogicalOptAddOpFromPhysicalReqOp(rootOpOfNextChild),
							currentSubPlan );
				}
				else {
					currentSubPlan = createPhysicalPlanWithBinaryRoot(
							LogicalOpRightJoin.getInstance(),
							nextChild,
							currentSubPlan );
				}
			}

			return currentSubPlan;
		}

		protected PhysicalPlan createPhysicalPlanForMultiwayUnion( final LogicalOpMultiwayUnion lop, final List<PhysicalPlan> children ) {
			if ( children.size() == 1 ) {
				return children.get(0);
			}

			// As long as we do not have an actual algorithm for multiway unions,
			// we simply convert this to a left-deep plan of binary unions.
			PhysicalPlan currentSubPlan = children.get(0);
			for ( int i = 1; i < children.size(); ++i ) {
				currentSubPlan = createPhysicalPlanWithBinaryRoot(
						LogicalOpUnion.getInstance(),
						currentSubPlan,
						children.get(i) );
			}

			return currentSubPlan;
		}

	} // end of helper class Worker

}
