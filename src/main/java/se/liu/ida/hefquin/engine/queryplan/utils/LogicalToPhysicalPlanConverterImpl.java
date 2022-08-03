package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRightJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.BasePhysicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.BasePhysicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpParallelMultiLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequestWithTranslation;

public class LogicalToPhysicalPlanConverterImpl implements LogicalToPhysicalPlanConverter
{
	@Override
	public PhysicalPlan convert( final LogicalPlan lp, final boolean keepMultiwayJoins ) {
		final List<PhysicalPlan> children = convertChildren(lp, keepMultiwayJoins);
		return createPhysicalPlan( lp.getRootOperator(), children, keepMultiwayJoins );
	}

	/**
	 * Converts the sub-plans of the given logical plan (if any) and returns
	 * a list of the resulting physical plans. The order of the physical plans
	 * in the returned list is such that the i-th physical plan in the list
	 * is the physical plan that has been created for the i-th sub-plan of
	 * the given logical plan. For logical plans that do not contain any
	 * sub-plans, an empty list is returned.
	 */
	protected List<PhysicalPlan> convertChildren( final LogicalPlan lp, final boolean keepMultiwayJoins ) {
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
		else if ( lop instanceof LogicalOpMultiwayUnion ) {
			// TODO: remove this else-if branch, including the method that it
			// calls, once we have a physical operator for multiway union 
			return createPhysicalPlanForMultiwayUnion( (LogicalOpMultiwayUnion) lop, children );
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
			final NaryPhysicalOp pop = new BasePhysicalOpMultiwayJoin(lop) {
				@Override public void visit(PhysicalPlanVisitor visitor) { throw new UnsupportedOperationException(); }
				@Override public NaryExecutableOp createExecOp(ExpectedVariables... inputVars) { throw new UnsupportedOperationException(); }
			};
			return PhysicalPlanFactory.createPlan(pop, children);
		}

//		Multiway joins are converted to left-deep plan of joins:
//		For join operators, use tpAdd and bgpAdd when possible; otherwise, binary joins are used by default.
		PhysicalPlan currentSubPlan = children.get(0);
		for ( int i = 1; i < children.size(); ++i ) {
			final PhysicalPlan nextChild = children.get(i);
			final PhysicalOperator rootOpOfNextChild = nextChild.getRootOperator();
			if( rootOpOfNextChild instanceof PhysicalOpRequest ){
				currentSubPlan = createPhysicalPlanWithUnaryRoot(
						LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(rootOpOfNextChild),
						currentSubPlan );
			}
			else if ( currentSubPlan.getRootOperator() instanceof PhysicalOpRequest ){
				currentSubPlan = createPhysicalPlanWithUnaryRoot(
						LogicalOpUtils.createLogicalAddOpFromPhysicalReqOp(currentSubPlan.getRootOperator()),
						nextChild );
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
		final List<LogicalOpRequest<?,?>> optionalParts = getOptionalPartsForParallelMultiLeftJoin(children);
		if ( optionalParts != null ) {
			// If the parallel multi-left-join can indeed be used, do so.
			final UnaryPhysicalOp rootOp = new PhysicalOpParallelMultiLeftJoin(optionalParts);
			return PhysicalPlanFactory.createPlan( rootOp, children.get(0) );
		}

		// Now comes the generic option that works for all cases.

		if ( keepMultiwayJoins ) {
			final NaryPhysicalOp pop = new BasePhysicalOpMultiwayLeftJoin(lop) {
				@Override public void visit(PhysicalPlanVisitor visitor) { throw new UnsupportedOperationException(); }
				@Override public NaryExecutableOp createExecOp(ExpectedVariables... inputVars) { throw new UnsupportedOperationException(); }
			};
			return PhysicalPlanFactory.createPlan(pop, children);
		}

//		Multiway left joins are converted to right-deep plan of right outer joins:
//		For join operators, use tpOptAdd and bgpOptAdd when possible; otherwise, binary joins are used by default.

		// The first child of the multiway left join is the non-optional part
		// and, thus, is used as the right input to the first right outer join
		// (second case below) or as the input to the tpOptAdd/bgpOptAdd (first
		// case below).
		PhysicalPlan currentSubPlan = children.get(0);
		for ( int i = 1; i < children.size(); ++i ) {
			final PhysicalPlan nextChild = children.get(i);
			final PhysicalOperator rootOpOfNextChild = nextChild.getRootOperator();
			if( rootOpOfNextChild instanceof PhysicalOpRequest ){
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

	/**
	 * Checks whether we have a case in which the parallel multi-left-join can
	 * be used. If so, this method returns the optional parts of that multi-left-
	 * join. If not, this method returns <code>null</code>.
	 */
	protected static List<LogicalOpRequest<?,?>> getOptionalPartsForParallelMultiLeftJoin( final List<PhysicalPlan> children )
	{
		final List<LogicalOpRequest<?,?>> optionalParts = new ArrayList<>( children.size()-1 );
		final List<ExpectedVariables> expVarsOfOptionalParts = new ArrayList<>( children.size()-1 );

		final Iterator<PhysicalPlan> it = children.iterator();
		final PhysicalPlan firstChildPlan = it.next(); // the non-optional part

		// condition 1: every non-optional part is just a request operator
		while ( it.hasNext() ) {
			final PhysicalOperator childRootOp = it.next().getRootOperator();
			final LogicalOpRequest<?,?> reqOp;
			if ( childRootOp instanceof PhysicalOpRequest<?,?> ) {
				reqOp = ((PhysicalOpRequest<?,?>) childRootOp).getLogicalOperator();
			}
			else if ( childRootOp instanceof PhysicalOpRequestWithTranslation<?,?>  ) {
				reqOp = ((PhysicalOpRequestWithTranslation<?,?>) childRootOp).getLogicalOperator();
			}
			else {
				return null;
			}

			optionalParts.add(reqOp);
			expVarsOfOptionalParts.add( reqOp.getRequest().getExpectedVariables() );
		}

		// condition 2: the join variable(s) between the non-optional part
		//              and an optional part must be the same for each of
		//              the optional parts
		final ExpectedVariables expVarsNonOptPart = firstChildPlan.getExpectedVariables();

		final Iterator<ExpectedVariables> it2 = expVarsOfOptionalParts.iterator();
		final ExpectedVariables expVarsFirstOptPart = it2.next();
		final Set<Var> joinVarsFirstOptPart = ExpectedVariablesUtils.intersectionOfAllVariables(expVarsNonOptPart, expVarsFirstOptPart);

		while ( it2.hasNext() ) {
			final ExpectedVariables expVarsNextOptPart = it2.next();
			final Set<Var> joinVarsNextOptPart = ExpectedVariablesUtils.intersectionOfAllVariables(expVarsNonOptPart, expVarsNextOptPart);
			if ( ! joinVarsNextOptPart.equals(joinVarsFirstOptPart) ) {
				return null;
			}
		}

		// condition 3: the only variables that different optional parts
		//              have in common are the join variable(s)
		// hence, we need to do a pairwise comparison
		for ( int i = 0; i < expVarsOfOptionalParts.size()-1; i++ ) {
			final ExpectedVariables iExpVarsOptPart = expVarsOfOptionalParts.get(i);
			for ( int j = i+1; j < expVarsOfOptionalParts.size(); j++ ) {
				final ExpectedVariables jExpVarsOptPart = expVarsOfOptionalParts.get(j);
				final Set<Var> test = ExpectedVariablesUtils.intersectionOfAllVariables(iExpVarsOptPart, jExpVarsOptPart);
				if ( ! test.equals(joinVarsFirstOptPart) ) {
					return null;
				}
			}
		}

		return optionalParts;
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

}
