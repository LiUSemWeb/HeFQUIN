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
		else {
			newPlan = LogicalPlanUtils.createPlanWithSubPlans(rootOp, newSubPlans);
		}

		if ( (inputPlan.getRootOperator() instanceof LogicalOpLocalToGlobal
				|| inputPlan.getRootOperator() instanceof LogicalOpGlobalToLocal)
			&& !checkIfL2gOrG2lNeeded( newPlan.getSubPlan(0)) ) {
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

		if( rootOp instanceof LogicalOpRequest req ) {
			return LogicalOpUtils.getTriplePatternsOfReq(req);
		}
		else if ( rootOp instanceof LogicalOpUnion || rootOp instanceof LogicalOpMultiwayUnion
				|| rootOp instanceof LogicalOpJoin || rootOp instanceof LogicalOpMultiwayJoin
				|| rootOp instanceof LogicalOpMultiwayLeftJoin
				|| rootOp instanceof LogicalOpRightJoin ) {
			final Set<TriplePattern> triplePatterns = new HashSet<>();
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				triplePatterns.addAll( extractTPs(plan.getSubPlan(i)) );
			}
			return triplePatterns;
		}
		else if ( rootOp instanceof LogicalOpTPAdd tpAdd ) {
			final Set<TriplePattern> triplePatterns = extractTPs( plan.getSubPlan(0) );
			triplePatterns.add( tpAdd.getTP() );
			return triplePatterns;
		}
		else if ( rootOp instanceof LogicalOpBGPAdd bgpAdd ) {
			final Set<TriplePattern> triplePatterns = bgpAdd.getBGP().getTriplePatterns();
			triplePatterns.addAll( extractTPs( plan.getSubPlan(0) ) );
			return triplePatterns;
		}
		else if ( rootOp instanceof LogicalOpGPAdd gpAdd ) {
			final Set<TriplePattern> triplePatterns = gpAdd.getPattern().getAllMentionedTPs();
			triplePatterns.addAll( extractTPs( plan.getSubPlan(0) ) );
			return triplePatterns;
		}
		else if ( rootOp instanceof LogicalOpTPOptAdd tpOptAdd ) {
			final Set<TriplePattern> triplePatterns = extractTPs( plan.getSubPlan(0) );
			triplePatterns.add( tpOptAdd.getTP() );
			return triplePatterns;
		}
		else if ( rootOp instanceof LogicalOpBGPOptAdd bgpOptAdd ) {
			final Set<TriplePattern> triplePatterns = bgpOptAdd.getBGP().getTriplePatterns();
			triplePatterns.addAll( extractTPs( plan.getSubPlan(0) ) );
			return triplePatterns;
		}
		else if ( rootOp instanceof LogicalOpGPOptAdd gpOptAdd ) {
			final Set<TriplePattern> triplePatterns = gpOptAdd.getPattern().getAllMentionedTPs();
			triplePatterns.addAll( extractTPs( plan.getSubPlan(0) ) );
			return triplePatterns;
		}
		else if( rootOp instanceof LogicalOpFilter
				|| rootOp instanceof LogicalOpLocalToGlobal
				|| rootOp instanceof LogicalOpGlobalToLocal ) {
			return extractTPs( plan.getSubPlan(0) );
		}
		else {
			throw new IllegalArgumentException("Unsupported type of root operator (" + rootOp.getClass().getName() + ")");
		}
	}

}
