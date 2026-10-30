package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVars;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpDedup;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFixedSolMap;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMinus;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnfold;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.FilterCollectingUtils;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

/**
 * Eliminates redundant filter expressions from logical GPAdd and GPOptAdd
 * operators when those expressions are guaranteed by the operators' input
 * plans.
 *
 * Candidate filter expressions are collected from the graph pattern of the
 * operator being optimized. The input plan is then analyzed to determine
 * which candidate expressions are guaranteed to hold for its output. Any
 * such expressions are removed from the operator's graph pattern, while
 * all other expressions and graph pattern elements are preserved.
 *
 * Filter guarantees are propagated according to operator-specific rules.
 * For example, inner joins propagate guarantees from their children, union
 * operators propagate only guarantees common to every branch, and left
 * joins and MINUS operators propagate guarantees only from their left
 * child. Projection operators invalidate guarantees that reference variables
 * not included in the projection.
 *
 * This heuristic recursively optimizes subplans before eliminating
 * redundant filters from GPAdd and GPOptAdd operators.
 */
public class RedundantFilterElimination implements HeuristicForLogicalOptimization
{
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan,
	                          final QueryProcContext ctxt ) {
		return apply(inputPlan);
	}

	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		// Recursively search for GPAdd and GPOptAdd operators.
		// When one is found, eliminate redundant filters from its graph pattern.
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		// Recursively apply the heuristic to each subplan.
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
		boolean noChanges = true; // set to false if the heuristic changes any of the subplans
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
			newSubPlans[i] = apply(oldSubPlan);
			if ( ! newSubPlans[i].equals(oldSubPlan) ) {
				noChanges = false;
			}
		}

		final LogicalOperator rootOp = inputPlan.getRootOperator();

		final LogicalPlan plan = noChanges
			? inputPlan
			: LogicalPlanUtils.createPlanWithSubPlans(rootOp, null, newSubPlans);

		// Eliminate redundant filters at this operator after processing its children.
		if ( rootOp instanceof LogicalOpGPAdd opGPAdd )
			return eliminateFilterRedundancies(opGPAdd, plan);
		else if ( rootOp instanceof LogicalOpGPOptAdd opGPOptAdd )
			return eliminateFilterRedundancies(opGPOptAdd, plan);

		return plan;
	}

	/**
	 * Eliminates redundant filters from a GPAdd operator's graph pattern when
	 * those filters are guaranteed by its input plan.
	 *
	 * @param opGPAdd   the GPAdd operator to optimize
	 * @param inputPlan the logical plan rooted at the operator
	 * @return the input plan if no filters are removed, or a plan with an
	 *         optimized GPAdd operator otherwise
	 */
	protected LogicalPlan eliminateFilterRedundancies( final LogicalOpGPAdd opGPAdd, final LogicalPlan inputPlan ) {
		final SPARQLGraphPattern originalPattern = opGPAdd.getPattern();
		final SPARQLGraphPattern optimizedPattern = removeRedundantFilters(originalPattern, inputPlan.getSubPlan(0));

		if ( optimizedPattern.equals(originalPattern) )
			return inputPlan;

		final LogicalOpGPAdd newGPAdd;
		if ( opGPAdd.hasParameterVariables() )
			newGPAdd = new LogicalOpGPAdd( opGPAdd.getFederationMember(), optimizedPattern, opGPAdd.getParameterVariables(), opGPAdd.mayReduce() );
		else
			newGPAdd = new LogicalOpGPAdd( opGPAdd.getFederationMember(), optimizedPattern, null, opGPAdd.mayReduce() );

		return new LogicalPlanWithUnaryRootImpl( newGPAdd, null, inputPlan.getSubPlan(0) );
	}

	/**
	 * Eliminates redundant filters from a GPOptAdd operator's graph pattern when
	 * those filters are guaranteed by its input plan.
	 *
	 * @param opGPOptAdd the GPOptAdd operator to optimize
	 * @param inputPlan  the logical plan rooted at the operator
	 * @return the input plan if no filters are removed, or a plan with an
	 *         optimized GPOptAdd operator otherwise
	 */
	protected LogicalPlan eliminateFilterRedundancies( final LogicalOpGPOptAdd opGPOptAdd, final LogicalPlan inputPlan ) {
		final SPARQLGraphPattern originalPattern = opGPOptAdd.getPattern();
		final SPARQLGraphPattern optimizedPattern = removeRedundantFilters(originalPattern, inputPlan.getSubPlan(0));

		if ( optimizedPattern.equals(originalPattern) )
			return inputPlan;

		final LogicalOpGPOptAdd newGPOptAdd = new LogicalOpGPOptAdd( opGPOptAdd.getFederationMember(), optimizedPattern, opGPOptAdd.mayReduce() );

		return new LogicalPlanWithUnaryRootImpl( newGPOptAdd, null, inputPlan.getSubPlan(0) );
	}

	/**
	 * Removes candidate filters from a graph pattern when they are guaranteed
	 * by the given input plan.
	 *
	 * @param pattern   the graph pattern from which redundant filters are removed
	 * @param inputPlan the logical plan used to determine which filters are guaranteed
	 * @return the original pattern if no redundant filters are found, or a pattern
	 *         with the redundant filters removed
	 */
	protected SPARQLGraphPattern removeRedundantFilters( final SPARQLGraphPattern pattern, final LogicalPlan inputPlan ) {
		final Set<Expr> candidates = new HashSet<>();
		FilterCollectingUtils.collectFiltersFromPattern(pattern, candidates);

		final Set<Expr> guaranteedFilters = getGuaranteedFilters(inputPlan, candidates);

		if ( guaranteedFilters.isEmpty() )
			return pattern;

		return pattern.removeFilters(guaranteedFilters);
	}

	/**
	 * Determines which candidate filter expressions are guaranteed to hold for
	 * the output of the given logical plan.
	 *
	 * @param inputPlan  the logical plan to analyze
	 * @param candidates the filter expressions to check for guarantees
	 * @return the candidate filter expressions guaranteed by the input plan
	 */
	protected Set<Expr> getGuaranteedFilters( final LogicalPlan inputPlan, final Set<Expr> candidates ) {
		final GuaranteedFilterAnalyzer guaranteedFilterAnalyzer = new GuaranteedFilterAnalyzer(inputPlan, candidates);
		inputPlan.getRootOperator().visit(guaranteedFilterAnalyzer);
		return guaranteedFilterAnalyzer.getGuaranteedFilters();
	}

	/**
	 * Analyzes a logical plan to determine which candidate filter expressions
	 * are guaranteed to hold for its output.
	 *
	 * Filter guarantees are collected from SPARQL request patterns, GPAdd
	 * operators, and filter operators. Guarantees are propagated according to
	 * the semantics of each operator: joins combine guarantees from all
	 * children, unions retain only guarantees common to every branch, and
	 * left joins and MINUS propagate guarantees only from their left child.
	 * GPOptAdd propagates guarantees from its input but does not introduce
	 * guarantees from its optional graph pattern. Projection removes guarantees
	 * that reference variables not included in the projection.
	 *
	 * Only expressions contained in the candidate set are tracked. The analyzer
	 * recursively visits child plans and maintains the set of guaranteed
	 * expressions separately for each analysis.
	 */
	protected class GuaranteedFilterAnalyzer implements LogicalPlanVisitor {
		protected LogicalPlan branch;
		protected Set<Expr> guaranteedFilters = new HashSet<>();
		protected final Set<Expr> candidates;

		public GuaranteedFilterAnalyzer( final LogicalPlan branch, final Set<Expr> candidates ) {
			this.branch = branch;
			this.candidates = candidates;
		}

		public Set<Expr> getGuaranteedFilters() { return guaranteedFilters; }

		/**
		 * Analyzes the specified child plan and returns the candidate filters
		 * guaranteed by its output.
		 *
		 * The current plan and set of guaranteed filters are restored after the
		 * analysis, allowing the same analyzer instance to be reused for multiple
		 * child plans.
		 *
		 * @param index the index of the child plan to analyze
		 * @return the candidate filter expressions guaranteed by the child plan
		 */
		protected Set<Expr> analyzeSubPlan( final int index ) {
			final LogicalPlan currentBranch = branch;
			final Set<Expr> currentGuaranteedFilters = guaranteedFilters;

			try {
				branch = currentBranch.getSubPlan(index);
				guaranteedFilters = new HashSet<>();

				branch.getRootOperator().visit(this);

				return guaranteedFilters;
			}
			finally {
				branch = currentBranch;
				guaranteedFilters = currentGuaranteedFilters;
			}
		}

		/**
		 * Analyzes every child plan.
		 */
		protected Set<Expr> analyzeAllSubPlans() {
			final Set<Expr> filters = new HashSet<>();

			for ( int i = 0; i < branch.numberOfSubPlans(); i++ ) {
				filters.addAll(analyzeSubPlan(i));
			}

			return filters;
		}

		protected void addGuaranteedFilters( final SPARQLGraphPattern pattern ) {
			final Set<Expr> filters = new HashSet<>();
			FilterCollectingUtils.collectFiltersFromPattern(pattern, filters);

			filters.retainAll(candidates);
			guaranteedFilters.addAll(filters);
		}

		protected Set<Expr> analyzeCommonFiltersAcrossSubPlans() {
			Set<Expr> commonFilters = null;

			for ( int i = 0; i < branch.numberOfSubPlans(); i++ ) {
				final Set<Expr> branchFilters = analyzeSubPlan(i);

				if ( commonFilters == null )
					commonFilters = new HashSet<>(branchFilters);
				else
					commonFilters.retainAll(branchFilters);
			}

			return commonFilters != null ? commonFilters : new HashSet<>();
		}

		@Override
		public void visit( final LogicalOpRequest<?, ?> op ) {
			// Collect filter expressions embedded in a SPARQL request.
			if ( op.getRequest() instanceof SPARQLRequest req ) {
				addGuaranteedFilters( req.getQueryPattern() );
			}
		}

		@Override
		public void visit( final LogicalOpMultiRequest op ) {
			// Collect filter expressions embedded in a SPARQL request.
			addGuaranteedFilters( op.getRequest().getQueryPattern() );
		}

		@Override
		public void visit( final LogicalOpFixedSolMap op ) {
			// No filter expressions to collect.
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			// Collect filter conditions imposed by this graph pattern.
			addGuaranteedFilters( op.getPattern() );

			// Filters guaranteed by the input are also guaranteed by the output.
			guaranteedFilters.addAll(analyzeSubPlan(0));
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			// Only filters guaranteed by the input are guaranteed by the output.
			guaranteedFilters.addAll(analyzeSubPlan(0));
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			guaranteedFilters.addAll(analyzeAllSubPlans());
		}

		@Override
		public void visit( final LogicalOpLeftJoin op ) {
			// Only collect filters from the left child of a left join.
			guaranteedFilters.addAll(analyzeSubPlan(0));
		}

		@Override
		public void visit( final LogicalOpMinus op ) {
			// Only collect filters from the left child of a minus.
			guaranteedFilters.addAll(analyzeSubPlan(0));
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			// Filter is guaranteed only if every branch guarantees it.
			guaranteedFilters.addAll(analyzeCommonFiltersAcrossSubPlans());
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			guaranteedFilters.addAll(analyzeAllSubPlans());
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			// Only collect filters from the mandatory left input.
			guaranteedFilters.addAll(analyzeSubPlan(0));
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			// Filter is guaranteed only if every branch guarantees it.
			guaranteedFilters.addAll(analyzeCommonFiltersAcrossSubPlans());
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			// The filter operator guarantees its own filter conditions.
			final Set<Expr> filters = new HashSet<>(op.getFilterExpressions().getList());
			filters.retainAll(candidates);
			guaranteedFilters.addAll(filters);

			// Filters guaranteed by the input are also guaranteed by the output.
			guaranteedFilters.addAll(analyzeSubPlan(0));
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			// Assumes BIND preserves guarantee.
			guaranteedFilters.addAll(analyzeSubPlan(0));
		}

		@Override
		public void visit( final LogicalOpUnfold op ) {
			// Assumes UNFOLD preserves guarantee.
			guaranteedFilters.addAll(analyzeSubPlan(0));
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			// Do not propagate guarantees across the vocabulary boundary.
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			// Do not propagate guarantees across the vocabulary boundary.
		}

		@Override
		public void visit( final LogicalOpDedup op ) {
			guaranteedFilters.addAll(analyzeSubPlan(0));
		}

		@Override
		public void visit( final LogicalOpProject op ) {
			// Projection can remove variables referenced by a filter expression
			guaranteedFilters.addAll(analyzeSubPlan(0));

			final Set<Var> projectedVars = op.getVariables();
			guaranteedFilters.removeIf(filter ->
				! projectedVars.containsAll(ExprVars.getVarsMentioned(filter))
			);
		}

	}
}
