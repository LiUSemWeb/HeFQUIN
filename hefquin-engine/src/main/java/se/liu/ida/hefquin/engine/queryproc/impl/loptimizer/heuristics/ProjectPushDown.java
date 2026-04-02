package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
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
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnfold;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

public class ProjectPushDown implements HeuristicForLogicalOptimization
{

/**
 * Pushes project variables as much as possible towards the leaf nodes
 *  of a given logical plan.
 */
	@Override
	public LogicalPlan apply( LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( rootOp instanceof LogicalOpProject projectOp ) {
			return applyToPlanWithProjectAsRootOperator ( projectOp,
			                                              inputPlan.getSubPlan(0),
			                                              inputPlan );
		}
		else {
			// For any other type of root operator, simply apply
			// this heuristic recursively to all of the subplans.
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

	protected LogicalPlan applyToPlanWithProjectAsRootOperator( final LogicalOpProject projectOp,
	                                                            final LogicalPlan subPlanUnderProject,
	                                                            final LogicalPlan inputPlan ) {
		final LogicalOperator childOpUnderProject = subPlanUnderProject.getRootOperator();
		final Worker worker = new Worker( projectOp, subPlanUnderProject, inputPlan );

		childOpUnderProject.visit(worker);
		final LogicalPlan createdPlan = worker.getCreatedPlan();
		if ( createdPlan != null ) return createdPlan;
		else
		{
			throw new IllegalArgumentException( childOpUnderProject.getClass().getName() );
		}
	}

	protected class Worker implements LogicalPlanVisitor {
		protected final LogicalOpProject projectOp;
		protected final LogicalPlan subPlanUnderProject;
		protected final LogicalPlan inputPlan;
		protected LogicalPlan createdPlan;

		public Worker( final LogicalOpProject projectOp, final LogicalPlan subPlanUnderProject, final LogicalPlan inputPlan ) {
			this.projectOp = projectOp;
			this.subPlanUnderProject = subPlanUnderProject;
			this.inputPlan = inputPlan;
		}

		public LogicalPlan getCreatedPlan() { return createdPlan; }

		@Override
		public void visit( final LogicalOpRequest<?, ?> op ) {
			createdPlan = createPlanForRequestUnderProject( projectOp,
			                                                op,
			                                                inputPlan );
		}

		@Override
		public void visit( final LogicalOpFixedSolMap op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			createdPlan = createPlanForAddOpUnderProject( projectOp,
			                                             op,
			                                             subPlanUnderProject.getSubPlan(0),
			                                             inputPlan );
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpLeftJoin op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			createdPlan = createPlanForBindUnderProject( projectOp,
			                                             op,
			                                             subPlanUnderProject,
			                                             inputPlan );
		}

		@Override
		public void visit( final LogicalOpUnfold op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpDedup op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpProject op ) {
			createdPlan = createPlanForProjectUnderProject( projectOp,
			                                                op,
			                                                subPlanUnderProject.getSubPlan(0) );
		}

	} // end of Worker

	protected LogicalPlan createPlanForRequestUnderProject( final LogicalOpProject projectOp,
	                                                        final LogicalOpRequest<?,?> reqOp,
	                                                        final LogicalPlan inputPlan ) {
		// Pushing a project operator into request operator is not possible yet TODO #570
		return inputPlan;
	}

	protected LogicalPlan createPlanForProjectUnderProject( final LogicalOpProject parentProjectOp,
	                                                        final LogicalOpProject childProjectOp,
	                                                        final LogicalPlan subPlanUnderChildProjectOp ) {
		final Set<Var> combinedVariables = new HashSet<>();
		combinedVariables.addAll(parentProjectOp.getVariables());
		combinedVariables.addAll(childProjectOp.getVariables());

		final LogicalPlan newPlan = new LogicalPlanWithUnaryRootImpl(
			new LogicalOpProject(combinedVariables),
			null,
			subPlanUnderChildProjectOp );

		return apply(newPlan);
	}

	protected LogicalPlan createPlanForBindUnderProject( final LogicalOpProject projectOp,
	                                                    final LogicalOpBind bindOp,
	                                                    final LogicalPlan subPlanUnderBind,
	                                                    final LogicalPlan inputPlan ) {
		// Check whether the project can be pushed under the given bind operator,
		// which is possible only if all of the projected variables are assigned
		// either by the bind operator or by its children (i.e., they are available
		// after the bind). Otherwise, the project must stay above the bind.
		final Set<Var> varsInProject = projectOp.getVariables();
		final List<Var> varsProducedByBindAndChild = bindOp.getBindExpressions().getVars();
		//varsProducedByBindAndChild.addAll(subPlanUnderBind.getExpectedVariables()) CHECK THIS
		if ( ! varsProducedByBindAndChild.containsAll(varsInProject) )
			return inputPlan;

		// The project can be pushed.
		return createPlanForUnaryOpUnderProject(projectOp, bindOp, subPlanUnderBind);
	}

	protected LogicalPlan createPlanForAddOpUnderProject( final LogicalOpProject projectOp,
	                                                      final UnaryLogicalOp childOp,
	                                                      final LogicalPlan subPlanUnderChildOp,
	                                                      final LogicalPlan inputPlan ) {
		return inputPlan;
	}

	protected LogicalPlan createPlanForJoinUnderProject( final LogicalOpProject projectOp,
	                                                     final LogicalPlan subPlanUnderProject,
	                                                     final LogicalPlan inputPlan ) {
		return inputPlan;
	}

	protected LogicalPlan createPlanForUnaryOpUnderProject( final LogicalOpProject projectOp, final UnaryLogicalOp op, final LogicalPlan subPlanUnderOp ) {
		// Create a new subplan with the project as root operator on top of the
		// subplan that was under the given unary operator, and apply this
		// heuristic recursively to this new subplan.
		final LogicalPlan newSubPlan1 = LogicalPlanUtils.createPlanWithSubPlans(
				projectOp,
				null,
				subPlanUnderOp );
		final LogicalPlan newSubPlan2 = apply(newSubPlan1);

		// Put together the new plan with the given unary operator as root.
		return LogicalPlanUtils.createPlanWithSubPlans(op, null, newSubPlan2);
	}


}
