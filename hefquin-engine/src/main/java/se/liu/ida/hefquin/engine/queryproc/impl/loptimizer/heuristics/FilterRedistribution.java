package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;

import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

/**
 * Redistributes filter conditions across the branches of inner joins
 * and multiway inner joins in a given logical plan. Whether a particular
 * filter condition can be redistributed to another branch depends on
 * whether all variables mentioned in the condition are certainly available
 * in that branch.
 *
 * Filter conditions are collected from both logical filter operators
 * and SPARQL requests within each branch. A filter condition is copied
 * to every other branch that can evaluate it, provided that the branch
 * does not already contain the same filter condition.
 *
 * This heuristic can introduce additional filter operators in the logical
 * plan, allowing filter conditions to be evaluated independently in
 * multiple branches. Filter conditions already present in SPARQL requests
 * are detected but are not directly merged into other requests by this
 * heuristic.
 */
public class FilterRedistribution implements HeuristicForLogicalOptimization
{
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan,
	                          final QueryProcContext ctxt ) {
		return apply(inputPlan);
	}

	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		final LogicalOperator rootOp = inputPlan.getRootOperator();
		// If the root is an inner join, redistribute filters across its branches.
		if ( isInnerJoin(rootOp) ) {
			return redistributeFilters(inputPlan, numberOfSubPlans, rootOp.mayReduce());
		}
		else {
			// Otherwise, recursively apply the heuristic to each subplan.
			final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
			boolean noChanges = true; // set to false if the heuristic changes any of the subplans
			for ( int i = 0; i < numberOfSubPlans; i++ ) {
				final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
				newSubPlans[i] = apply(oldSubPlan);
				if ( ! newSubPlans[i].equals(oldSubPlan) ) {
					noChanges = false;
				}
			}

			if ( noChanges )
				return inputPlan;

			return LogicalPlanUtils.createPlanWithSubPlans(rootOp, null, newSubPlans);
		}
	}

	/**
	 * Redistributes filter conditions across the branches of a join plan.
	 *
	 * Collects the filter conditions from each branch and copies each
	 * condition to other branches where all referenced variables are
	 * certainly available, provided that the condition is not already
	 * present in the target branch.
	 *
	 * @param joinPlan the logical plan rooted at an inner join
	 * @param numberOfSubPlans the number of branches in the join
	 * @param mayReduce whether the join may reduce duplicate solution mappings
	 * @return a logical plan with the redistributed filter conditions
	 */
	protected LogicalPlan redistributeFilters( final LogicalPlan joinPlan, final int numberOfSubPlans, final boolean mayReduce ) {
		final List<List<ExprList>> branchFilters = new ArrayList<>();

		// Collect the filter conditions occurring in each join branch.
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan branch = joinPlan.getSubPlan(i);
			// traverse the whole subplan and
			// collect filters (logicalopfilter, filters inside requests)
			branchFilters.add(collectFilters(branch));
		}

		// Collect the updated branches as filter conditions are redistributed.
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];

		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			newSubPlans[i] = joinPlan.getSubPlan(i);
		}

		// Copy each filter condition to other branches where it can be evaluated.
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			for ( final ExprList filterExpressions : branchFilters.get(i) ) {
				for ( int j = 0; j < numberOfSubPlans; j++ ) {
					if ( i == j )
						continue;

					final LogicalPlan branch = newSubPlans[j];


					// Avoid duplicating filters and ensure that all required
					// variables are certainly available in the target branch.
					if ( ! containsFilter(branch, filterExpressions) && canEvaluate(filterExpressions, branch) ) {
						final LogicalOperator filterOp = new LogicalOpFilter(filterExpressions, mayReduce);

						newSubPlans[j] = LogicalPlanUtils.createPlanWithSubPlans(filterOp, null, branch);
					}
				}
			}
		}

		// Rebuild the join with the updated branches.
		final LogicalPlan newJoinPlan = LogicalPlanUtils.createPlanWithSubPlans(joinPlan.getRootOperator(), null, newSubPlans);

		return newJoinPlan;
	}

	/**
	 * Collects filter conditions from a logical plan.
	 *
	 * Traverses the plan recursively, collecting conditions from logical
	 * filter operators and supported SPARQL request patterns.
	 */
	protected List<ExprList> collectFilters( final LogicalPlan branch ) {
		final List<ExprList> filters = new ArrayList<>();
		final LogicalOperator rootOp = branch.getRootOperator();

		// Collect filter conditions from a logical filter operator.
		if ( rootOp instanceof LogicalOpFilter filterOp ) {
			filters.add(filterOp.getFilterExpressions());
		}

		// Collect filter conditions embedded in a SPARQL request.
		if ( rootOp instanceof LogicalOpRequest requestOp ) {
			if ( requestOp.getRequest() instanceof SPARQLRequest req && req.getQueryPattern() instanceof GenericSPARQLGraphPatternImpl1 pattern )
				filters.addAll(collectFiltersFromPattern(pattern.asJenaElement()));

			// Requests are leaves of the logical plan, so no further traversal is needed.
			return filters;
		}

		// Recursively collect filter conditions from child plans.
		for ( int i = 0; i < branch.numberOfSubPlans(); i++ ) {
			filters.addAll(collectFilters(branch.getSubPlan(i)));
		}

		return filters;
	}

	/**
	 * Collects filter conditions from a Jena SPARQL pattern element.
	 *
	 * Extracts conditions from {@link ElementFilter} elements and recursively
	 * examines the children of {@link ElementGroup} elements. Other element
	 * types are not currently traversed.
	 */
	protected List<ExprList> collectFiltersFromPattern( final Element element ) {
		final List<ExprList> filters = new ArrayList<>();

		// Extract the filter condition from a SPARQL FILTER element.
		if ( element instanceof ElementFilter filter ) {
			final ExprList expressions = new ExprList();
			expressions.add(filter.getExpr());
			filters.add(expressions);
		}
		// Recursively inspect the elements of a SPARQL group pattern.
		else if ( element instanceof ElementGroup group ) {
			for ( final Element subElement : group.getElements() ) {
				filters.addAll(collectFiltersFromPattern(subElement));
			}
		}
		// Additional SPARQL element types can be handled as needed.

		return filters;
	}

	/**
	 * Checks whether a branch already contains a filter with the given
	 * expressions.
	 *
	 * Searches logical filter operators and supported SPARQL request
	 * patterns throughout the branch.
	 */
	protected boolean containsFilter( final LogicalPlan branch, final ExprList filterExpressions ) {
		final LogicalOperator branchRootOp = branch.getRootOperator();

		// Check whether the root is a filter with the same expressions.
		if ( branchRootOp instanceof LogicalOpFilter filterOp && filterOp.getFilterExpressions().equals(filterExpressions) )
			return true;

		// Check whether a SPARQL request already contains the filter.
		if ( branchRootOp instanceof LogicalOpRequest requestOp ) {
			if ( requestOp.getRequest() instanceof SPARQLRequest req && req.getQueryPattern() instanceof GenericSPARQLGraphPatternImpl1 pattern ) {
				final List<ExprList> requestFilters = collectFiltersFromPattern(pattern.asJenaElement());

				if ( requestFilters.contains(filterExpressions) )
					return true;
			}
		}

		// Search the remaining subplans for an existing occurrence.
		for ( int i = 0; i < branch.numberOfSubPlans(); i++ ) {
			if ( containsFilter(branch.getSubPlan(i), filterExpressions) ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determines whether a branch can evaluate the given filter expressions.
	 *
	 * A filter is considered evaluable if every variable it references
	 * is certainly available in the branch.
	 */
	protected boolean canEvaluate( final ExprList filterExpressions, final LogicalPlan subPlan ) {
		final Set<Var> varsInFilter = filterExpressions.getVarsMentioned();

		final Set<Var> certainVars = subPlan.getExpectedVariables().getCertainVariables();

		return certainVars.containsAll(varsInFilter);
	}

	/**
	 * Checks whether a logical operator represents an inner join supported
	 * by this heuristic.
	 */
	protected boolean isInnerJoin( final LogicalOperator op ) {
		return op instanceof LogicalOpJoin || op instanceof LogicalOpMultiwayJoin;
	}

}
