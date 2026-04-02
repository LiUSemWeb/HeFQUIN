package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import org.apache.jena.vocabulary.RDF;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.*;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.HeuristicForLogicalOptimization;

import java.util.HashSet;
import java.util.Set;

public class RemoveUnnecessaryL2gAndG2l implements HeuristicForLogicalOptimization
{
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		final int numberOfSubPlans = inputPlan.numberOfSubPlans();
		if ( numberOfSubPlans == 0 ) {
			return inputPlan;
		}

		final LogicalPlan[] newSubPlans = new LogicalPlan[numberOfSubPlans];
		boolean noChanges = true; // set to false if the heuristic changes any of the subplans
		for ( int i = 0; i < numberOfSubPlans; i++ ) {
			final LogicalPlan oldSubPlan = inputPlan.getSubPlan(i);
			newSubPlans[i] = apply(oldSubPlan);
			if ( ! newSubPlans[i].equals(oldSubPlan) ) {
				noChanges = false;
			}
		}

		final LogicalPlan newPlan;
		final LogicalOperator rootOp = inputPlan.getRootOperator();
		if ( noChanges )
			newPlan = inputPlan;
		else
			newPlan = LogicalPlanUtils.createPlanWithSubPlans( rootOp,
			                                                   null,
			                                                   newSubPlans );

		if ( (rootOp instanceof LogicalOpLocalToGlobal
				|| rootOp instanceof LogicalOpGlobalToLocal)
			&& !checkIfL2gOrG2lNeeded(newPlan.getSubPlan(0)) ) {
			return newPlan.getSubPlan(0);
		}
		else {
			return newPlan;
		}
	}

	/**
	 * This function is used to check if a l2g or g2l operator is needed.
	 * The current implementation assumes that only concepts and roles are being considered in vocabulary mapping
	 * and that the data of the federation members is only instance data (i.e., properties
	 * appear only in the predicate position of triples and classes appear only in the
	 * object position of rdf:type triples).
	 */
	protected boolean checkIfL2gOrG2lNeeded( final LogicalPlan plan ){
		final Set<TriplePattern> tps = extractTPs( plan );
		for ( final TriplePattern tp : tps ) {
			// If any triple pattern is in the form of (-, ?p, -) or (-, rdf:type, ?o),
			// the intermediate results might need to be rewritten, which
			// requires adding a L2G operator over the request.
			if ( tp.asJenaTriple().getPredicate().isVariable() ) {
				return true;
			}
			if ( tp.asJenaTriple().getPredicate().equals(RDF.Nodes.type)
					&& tp.asJenaTriple().getObject().isVariable() ) {
				return true;
			}
		}
		return false;
	}

	protected static Set<TriplePattern> extractTPs( final LogicalPlan plan ) {
		final LogicalOperator rootOp = plan.getRootOperator();
		final Worker worker = new Worker( plan );

		rootOp.visit(worker);
		final Set<TriplePattern> triplePattern = worker.getTriplePattern();
		if ( triplePattern != null ) return triplePattern;
		else {
			throw new IllegalArgumentException("Unsupported type of root operator (" + rootOp.getClass().getName() + ")");
		}
	}

	protected static class Worker implements LogicalPlanVisitor {
		protected final LogicalPlan plan;
		protected Set<TriplePattern> returnTriplePattern;

		public Worker( final LogicalPlan plan ) {
			this.plan = plan;
		}

		public Set<TriplePattern> getTriplePattern() { return returnTriplePattern; }

		@Override
		public void visit( final LogicalOpRequest<?, ?> op ) {
			returnTriplePattern = LogicalOpUtils.getTriplePatternsOfReq(op);
		}

		@Override
		public void visit( final LogicalOpFixedSolMap op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			final Set<TriplePattern> triplePatterns = op.getPattern().getAllMentionedTPs();
			triplePatterns.addAll( extractTPs( plan.getSubPlan(0) ) );
			returnTriplePattern = triplePatterns;
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			final Set<TriplePattern> triplePatterns = op.getPattern().getAllMentionedTPs();
			triplePatterns.addAll( extractTPs( plan.getSubPlan(0) ) );
			returnTriplePattern = triplePatterns;
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			final Set<TriplePattern> triplePatterns = new HashSet<>();
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				triplePatterns.addAll( extractTPs(plan.getSubPlan(i)) );
			}
			returnTriplePattern = triplePatterns;
		}

		@Override
		public void visit( final LogicalOpLeftJoin op ) {
			final Set<TriplePattern> triplePatterns = new HashSet<>();
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				triplePatterns.addAll( extractTPs(plan.getSubPlan(i)) );
			}
			returnTriplePattern = triplePatterns;
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			final Set<TriplePattern> triplePatterns = new HashSet<>();
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				triplePatterns.addAll( extractTPs(plan.getSubPlan(i)) );
			}
			returnTriplePattern = triplePatterns;
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			final Set<TriplePattern> triplePatterns = new HashSet<>();
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				triplePatterns.addAll( extractTPs(plan.getSubPlan(i)) );
			}
			returnTriplePattern = triplePatterns;
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			final Set<TriplePattern> triplePatterns = new HashSet<>();
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				triplePatterns.addAll( extractTPs(plan.getSubPlan(i)) );
			}
			returnTriplePattern = triplePatterns;
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			final Set<TriplePattern> triplePatterns = new HashSet<>();
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				triplePatterns.addAll( extractTPs(plan.getSubPlan(i)) );
			}
			returnTriplePattern = triplePatterns;
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			returnTriplePattern = extractTPs( plan.getSubPlan(0) );
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpUnfold op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			returnTriplePattern = extractTPs( plan.getSubPlan(0) );
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			returnTriplePattern = extractTPs( plan.getSubPlan(0) );
		}

		@Override
		public void visit( final LogicalOpDedup op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpProject op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

	} // end of Worker

}
