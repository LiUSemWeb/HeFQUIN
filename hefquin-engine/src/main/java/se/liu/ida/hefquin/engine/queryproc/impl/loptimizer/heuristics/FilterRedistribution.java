package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExtendAssign;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpUnfold;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
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
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpOrderBy;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnfold;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

/**
 * Redistributes filter expressions across the branches of inner joins
 * and multiway inner joins in a logical plan. An expression can be
 * redistributed to another branch if all variables it references are
 * certain variables in that branch and the expression is not already
 * present there.
 *
 * Filter expressions are collected from logical filter operators,
 * logical GPAdd and GPOptAdd operators, and SPARQL request patterns.
 * The collection traverses the logical plan according to operator-specific
 * rules: for example, only the left child of left joins and MINUS operators
 * is visited, while the children of union operators are ignored.
 * Logical AND expressions are split into individual expressions before
 * redistribution.
 *
 * Expressions found inside SPARQL patterns are added to eligible branches
 * as logical filter operators. This rule is designed to be combined with
 * the FilterPushDown rule, which is applied afterwards.
 */
public class FilterRedistribution implements HeuristicForLogicalOptimization
{
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan,
	                          final QueryProcContext ctxt ) {
		return apply(inputPlan);
	}

	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		// Recursively search for subplans rooted at inner joins.
		// When one is found, redistribute filters across its branches.
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

		// Redistribute filters at this join after processing its children.
		if ( isInnerJoin(rootOp) )
			return redistributeFilters(plan, numberOfSubPlans, rootOp.mayReduce());

		return plan;
	}

	/**
	 * Redistributes filter expressions across the branches of a join plan.
	 *
	 * Collects the filter expressions from each branch and copies each
	 * condition to other branches in which all variables referenced by the condition are
	 * certain variables (see {@link #canEvaluate(ExprList, LogicalPlan)},
	 * provided that the condition is not already present in the target branch.
	 *
	 * @param joinPlan the logical plan rooted at an inner join
	 * @param numberOfSubPlans the number of branches in the join
	 * @param mayReduce whether the join may reduce duplicate solution mappings
	 * @return a logical plan with the redistributed filter expressions
	 */
	protected LogicalPlan redistributeFilters( final LogicalPlan joinPlan, final int numberOfSubPlans, final boolean mayReduce ) {
		// Collect the filter expressions of each of the join branches.
		final List<Set<Expr>> branchFilters = new ArrayList<>(numberOfSubPlans);
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan branch = joinPlan.getSubPlan(i);
			branchFilters.add( collectFilters(branch) );
		}

		// Copy filter expressions to other branches where they can be evaluated.
		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
		boolean noChanges = true; // set to false if any filter is redistributed
		for ( int j = 0; j < numberOfSubPlans; j++ ) {
			final LogicalPlan branch = joinPlan.getSubPlan(j);
			ExprList expressionsToCopy = null;

			for ( int i = 0; i < numberOfSubPlans; i++ ) {
				if ( i == j )
					continue;

				for ( final Expr expr : branchFilters.get(i) ) {
					// Avoid duplicating expressions and ensure that all required
					// variables are certain variables in the target branch.
					if ( ! branchFilters.get(j).contains(expr)
					  && canEvaluate(branch, expr) ) {
						branchFilters.get(j).add(expr);

						if ( expressionsToCopy == null )
							expressionsToCopy = new ExprList();

						expressionsToCopy.add(expr);
					}
				}
			}

			// Create a single filter operator containing all eligible expressions.
			if ( expressionsToCopy != null ) {
				final LogicalOperator filterOp = new LogicalOpFilter(expressionsToCopy, mayReduce);

				newSubPlans[j] = LogicalPlanUtils.createPlanWithSubPlans(filterOp, null, branch);
				noChanges = false;
			}
			else {
				newSubPlans[j] = branch;
			}
		}

		if ( noChanges )
			return joinPlan;

		// Rebuild the join with the updated branches.
		return LogicalPlanUtils.createPlanWithSubPlans(joinPlan.getRootOperator(), null, newSubPlans);
	}

	/**
	 * Collects filter expressions from a logical plan using a {@link FilterCollector}.
	 *
	 * @param branch the logical plan from which to collect filter expressions
	 * @return the set of collected filter expressions
	 */
	protected Set<Expr> collectFilters( final LogicalPlan branch ) {
		final Set<Expr> filters = new HashSet<>();
		branch.getRootOperator().visit(new FilterCollector(branch, filters));
		return filters;
	}

	/**
	 * A visitor that collects filter expressions from a logical plan.
	 *
	 * Filter expressions are collected from filter operators, gpAdd operators,
	 * and request operators (with SPARQL requests). Child plans are traversed
	 * according to the semantics of their parent operators: all children are visited for
	 * ordinary joins, only the left child is visited for left joins and MINUS,
	 * and children of union operators are ignored.
	 *
	 * The visitor recursively creates new instances for child plans while
	 * sharing the same set of collected filter expressions.
	 */
	protected class FilterCollector implements LogicalPlanVisitor {
		protected LogicalPlan branch;
		protected final Set<Expr> filters;

		public FilterCollector( final LogicalPlan branch, final Set<Expr> filters ) {
			this.branch = branch;
			this.filters = filters;
		}

		/**
		 * Visits the specified child plan using the same filter set.
		 */
		protected void visitSubPlan( final int index ) {
			final LogicalPlan currentBranch = branch;
			branch = branch.getSubPlan(index);
			branch.getRootOperator().visit(this);
			branch = currentBranch;
		}

		/**
		 * Visits every child plan.
		 */
		protected void visitAllSubPlans() {
			for (int i = 0; i < branch.numberOfSubPlans(); i++) {
				visitSubPlan(i);
			}
		}

		@Override
		public void visit( final LogicalOpRequest<?, ?> op ) {
			// Collect filter expressions embedded in a SPARQL request.
			if ( op.getRequest() instanceof SPARQLRequest req )
				collectFiltersFromPattern(req.getQueryPattern(), filters);
		}

		@Override
		public void visit( final LogicalOpMultiRequest op ) {
			// Collect filter expressions embedded in a SPARQL request.
			collectFiltersFromPattern(op.getRequest().getQueryPattern(), filters);
		}

		@Override
		public void visit( final LogicalOpFixedSolMap op ) {
			// No filter expressions to collect.
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			collectFiltersFromPattern(op.getPattern(), filters);
			visitAllSubPlans();
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			visitAllSubPlans();
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			visitAllSubPlans();
		}

		@Override
		public void visit( final LogicalOpLeftJoin op ) {
			// Only collect filters from the left child of a left join.
			visitSubPlan(0);
		}

		@Override
		public void visit( final LogicalOpMinus op ) {
			// Only collect filters from the left child of a minus.
			visitSubPlan(0);
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			// Do not collect filters from the children of a union.
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			visitAllSubPlans();
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			// Only collect filters from the mandatory left input.
			visitSubPlan(0);
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			// Do not collect filters from the children of a union.
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			filters.addAll(op.getFilterExpressions().getList());
			visitAllSubPlans();
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			// No filter expressions to collect from the bind operation itself.
			visitAllSubPlans();
		}

		@Override
		public void visit( final LogicalOpUnfold op ) {
			// No filter expressions to collect from the unfold operation itself.
			visitAllSubPlans();
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			// Do not collect filter expressions from under an l2g operator
			// because such filter expressions may be based on a different
			// vocabulary.
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			// Do not collect filter expressions from under a g2l operator
			// because such filter expressions may be based on a different
			// vocabulary.
		}

		@Override
		public void visit( final LogicalOpDedup op ) {
			visitAllSubPlans();
		}

		@Override
		public void visit( final LogicalOpProject op ) {
			visitAllSubPlans();
		}

		@Override
		public void visit( final LogicalOpOrderBy op ) {
			// No filter expressions to collect from the order by operation itself.
			visitAllSubPlans();
		}
	} // end of FilterCollector

	/**
	 * Collects filter expressions from a Jena SPARQL pattern element.
	 *
	 * Extracts expressions from {@link ElementFilter} elements, recursively
	 * examines the children of {@link ElementGroup} elements, and splits
	 * logical AND expressions into their individual expressions.
	 * Only {@link ElementFilter} and {@link ElementGroup} sub-elements are
	 * traversed; other element types are intentionally ignored to avoid
	 * collecting filters from nested constructs such as OPTIONAL and MINUS.
	 */
	protected void collectFiltersFromPattern( final Element element, final Set<Expr> filters ) {
		// Extract and split the filter condition from a SPARQL FILTER element.
		if ( element instanceof ElementFilter filter ) {
			collectFilterExpressions(filter.getExpr(), filters);
		}
		// Recursively inspect the elements of a SPARQL group pattern.
		else if ( element instanceof ElementGroup group ) {
			for ( final Element subElement : group.getElements() ) {
				if ( subElement instanceof ElementFilter || subElement instanceof ElementGroup )
					collectFiltersFromPattern(subElement, filters);
			}
		}
		// Additional SPARQL element types can be handled as needed.
	}

	/**
	 * Collects filter expressions from a SPARQL graph pattern by delegating
	 * to the appropriate method for the underlying Jena representation.
	 */
	protected void collectFiltersFromPattern( final SPARQLGraphPattern pattern, final Set<Expr> filters ) {
		if ( pattern instanceof GenericSPARQLGraphPatternImpl1 pattern1 )
			collectFiltersFromPattern(pattern1.asJenaElement(), filters);
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 pattern2 )
			collectFiltersFromOp(pattern2.asJenaOp(), filters);
	}

	/**
	 * Collects filter expressions from a Jena algebra tree.
	 *
	 * Extracts expressions from OpFilter operators and splits logical AND
	 * expressions into individual expressions. Filters inside UNION branches
	 * are not collected, and only the left branch of an OPTIONAL (OpLeftJoin)
	 * is traversed.
	 */
	protected void collectFiltersFromOp( final Op op, final Set<Expr> filters ) {
		// Do not collect filters from the branches of a union.
		if ( op instanceof OpUnion ) {
			return;
		}
		// Only collect filters from the left branch of an optional.
		else if ( op instanceof OpLeftJoin leftJoin ) {
			collectFiltersFromOp(leftJoin.getLeft(), filters);
		}
		// Traverse only into the left sub-operator of MINUS.
		else if ( op instanceof OpMinus opMinus ) {
			collectFiltersFromOp(opMinus.getLeft(), filters);
		}
		// Traverse into the sub-operator of unary operators that are supported.
		else if ( op instanceof OpExtendAssign opExtendAssign ) {
			collectFiltersFromOp(opExtendAssign.getSubOp(), filters);
		}
		else if ( op instanceof OpUnfold opUnfold ) {
			collectFiltersFromOp(opUnfold.getSubOp(), filters);
		}
		else if ( op instanceof OpFilter opFilter ) {
			for ( final Expr expr : opFilter.getExprs() ) {
				collectFilterExpressions(expr, filters);
			}
			collectFiltersFromOp(opFilter.getSubOp(), filters);
		}
		// Traverse into all sub-operators of supported joins.
		else if ( op instanceof OpJoin opJoin ) {
			collectFiltersFromOp(opJoin.getLeft(), filters);
			collectFiltersFromOp(opJoin.getRight(), filters);
		}
		else if ( op instanceof OpSequence opSequence ) {
			for ( final Op subOp : opSequence.getElements() ) {
				collectFiltersFromOp(subOp, filters);
			}
		}
		// Other operator types are intentionally not traversed.
	}

	/**
	 * Recursively decomposes logical AND expressions into their individual
	 * expressions and adds them to the given set.
	 *
	 * Expressions that are not logical AND expressions are added to the set
	 * as-is.
	 */
	protected void collectFilterExpressions( final Expr expr, final Set<Expr> filters ) {
		if ( expr instanceof E_LogicalAnd logicalAnd ) {
			collectFilterExpressions(logicalAnd.getArg1(), filters);
			collectFilterExpressions(logicalAnd.getArg2(), filters);
		}
		else {
			filters.add(expr);
		}
	}

	/**
	 * Determines whether a branch can evaluate the given filter expressions.
	 *
	 * A filter is considered evaluable if every variable it references
	 * is a certain variable in the branch.
	 *
	 * We only check certain variables to conservatively ensure that every variable
	 * referenced by the filter is bound in the branch. Possible variables may
	 * be unbound in some solution mappings, so using them could change the
	 * query results when the filter is evaluated in this branch.
	 */
	protected boolean canEvaluate( final LogicalPlan subPlan, final Expr filterExpression ) {
		final Set<Var> varsInFilter = filterExpression.getVarsMentioned();

		final Set<Var> certainVars = subPlan.getExpectedVariables().getCertainVariables();

		return certainVars.containsAll(varsInFilter);
	}

	/**
	 * Checks whether a logical operator represents an inner join.
	 */
	protected boolean isInnerJoin( final LogicalOperator op ) {
		return op instanceof LogicalOpJoin || op instanceof LogicalOpMultiwayJoin;
	}

}
