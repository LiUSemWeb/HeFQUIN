package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpParallelMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;

/**
 * A physical operator that implements a left-outer join for multiple optional
 * parts; as a unary operator, the operator has a single input, which provides
 * the solutions of the non-optional part.
 *
 * The idea of the algorithm associated with this operator is to perform bind
 * joins for all the optional parts in parallel and then merge the results.
 *
 * TODO: describe the algorithm in more detail.
 *
 * The actual algorithm of this operator is implemented
 * in the {@link ExecOpParallelMultiwayLeftJoin} class.
 */
public class PhysicalOpParallelMultiLeftJoin extends BaseForPhysicalOps implements UnaryPhysicalOp
{
	/**
	 * Checks whether a {@link LogicalOpMultiwayLeftJoin} with the given list
	 * of physical plans can be implemented by the parallel multi-left-join
	 * (as captured by this physical operator). If yes, this method returns
	 * the optional parts of that multi-left-join. If not, this method returns
	 * <code>null</code>.
	 */
	public static List<LogicalOpRequest<?,?>> checkApplicability( final List<PhysicalPlan> children )
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


	protected final List<LogicalOpRequest<?,?>> optionalParts;

	public PhysicalOpParallelMultiLeftJoin( final List<LogicalOpRequest<?,?>> optionalParts ) {
		assert ! optionalParts.isEmpty();
		this.optionalParts = optionalParts;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final Set<Var> certainVars = inputVars[0].getCertainVariables();
		final Set<Var> possibleVars = inputVars[0].getPossibleVariables();

		for ( final LogicalOpRequest<?,?> req : optionalParts ) {
			final ExpectedVariables ev = req.getRequest().getExpectedVariables();
			possibleVars.addAll( ev.getCertainVariables() );
			possibleVars.addAll( ev.getPossibleVariables() );
		}

		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		return new ExecOpParallelMultiwayLeftJoin( collectExceptions, inputVars[0], optionalParts );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpParallelMultiLeftJoin
				&& ((PhysicalOpParallelMultiLeftJoin) o).optionalParts.equals(optionalParts);
	}

	@Override
	public int hashCode(){
		return optionalParts.hashCode();
	}

	@Override
	public String toString(){
		return "> parallelMultiLeftJoin with " + optionalParts.size() + " optional parts";
	}

}
