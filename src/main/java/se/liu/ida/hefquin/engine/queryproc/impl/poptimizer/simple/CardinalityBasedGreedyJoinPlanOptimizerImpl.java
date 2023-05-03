package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BaseForExecOpBindJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithStatistics;

import java.util.*;

/**
 * This class implements a query optimizer[1] that builds left-deep query plans,
 * for which it uses a greedy approach to determine the join order based on cardinality estimation,
 * and then choose physical algorithm according to the estimated number of request to execute the join.
 *
 * [1] Heling, Lars, and Maribel Acosta. "Federated SPARQL query processing over heterogeneous linked data fragments." Proceedings of the ACM Web Conference 2022.
 */
public class CardinalityBasedGreedyJoinPlanOptimizerImpl extends CardinalityBasedJoinPlanOptimizerBase
{
    public CardinalityBasedGreedyJoinPlanOptimizerImpl(final FederationAccessManager fedAccessMgr ) {
        super(fedAccessMgr);
    }

    @Override
    public EnumerationAlgorithm initializeEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {

        return new CardinalityBasedJoinPlanOptimizerBase.GreedyConstructionAlgorithm(subplans) {
            @Override
            protected PhysicalPlan determineJoinOrderAndConstructPlan( final PhysicalPlanWithStatistics firstSubPlan, final List<PhysicalPlanWithStatistics> subPlansWithStatistics, final Comparator<PhysicalPlanWithStatistics> orderBasedOnCard ) {
                final PriorityQueue<PhysicalPlanWithStatistics> orderedCandidateSubPlans = new PriorityQueue<>(orderBasedOnCard);
                PhysicalPlanWithStatistics nextSelectedSubPlan = firstSubPlan;
                PhysicalPlanWithStatistics currentJoinPlan = nextSelectedSubPlan;

                while ( !orderedCandidateSubPlans.isEmpty() || !subPlansWithStatistics.isEmpty() ){

                    if ( !subPlansWithStatistics.isEmpty() ) {
                        // Identify subplans that have join variables with the selected subplan as new candidateSubPlans.
                        List<PhysicalPlanWithStatistics> candidateSubPlans = getSubPlansContainVars( nextSelectedSubPlan.plan.getExpectedVariables(), subPlansWithStatistics );
                        if ( candidateSubPlans.isEmpty() && orderedCandidateSubPlans.isEmpty() ) {
                            // Independent subplans exist, and there are no more candidate subplans to be consumed in orderedCandidateSubPlans
                            // In this case, choose the subplan with the lowest candidate from subPlansWithStatistics as candidate plan
                            candidateSubPlans = Arrays.asList( chooseFirstSubPlan(subPlansWithStatistics) );
                        }

                        // The candidateSubPlans are added to the ordered list 'orderedCandidateSubPlans' and removed from the remaining subPlansWithStatistics
                        orderedCandidateSubPlans.addAll(candidateSubPlans);
                        subPlansWithStatistics.removeAll(candidateSubPlans);
                    }

                    // select the first element (with the lowest cardinality) from orderedCandidateSubPlans as the nextSelectedSubPlan
                    nextSelectedSubPlan = orderedCandidateSubPlans.poll();
                    // Decide the physical algorithm depending on the estimated number of requests
                    currentJoinPlan = decidePhysicalAlgorithm( currentJoinPlan, nextSelectedSubPlan );
                }
                return currentJoinPlan.plan;
            }
        };
    }

    /**
     * The physical algorithm (symmetric hash join and bind join) is determined based on the estimated number of requests to execute the join (see formulas on page 1052 of the paper[1])
     * - SHJ: Card(T1)/PageSize + Card(T2)/PageSize
     * - BJ: Card(T1)/PageSize + Card(T1)/blockSize
     * [1] Heling, Lars, and Maribel Acosta. "Federated SPARQL query processing over heterogeneous linked data fragments." Proceedings of the ACM Web Conference 2022.
     *
     * For the comparison, only keeping the second element is enough.
     */
    protected PhysicalPlanWithStatistics decidePhysicalAlgorithm( final PhysicalPlanWithStatistics currentPlan, final PhysicalPlanWithStatistics nextPlan ) {
        final double accNumSHJ = nextPlan.getNumOfAccess();

        double accNumBJ = 0;
        for ( final FederationMember fm: nextPlan.getFederationMembers() ) {
            final double blockSize = determineBlockSize(fm);
            accNumBJ += currentPlan.getCardinality() / blockSize;
        }

        final PhysicalPlan newPlan;
        if ( accNumSHJ <= accNumBJ ) {
            newPlan = PhysicalPlanFactory.createPlanWithJoin(currentPlan.plan, nextPlan.plan);
        }
        else {
            newPlan = PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentPlan.plan, nextPlan.plan);
        }

        // "We estimate the join cardinality of two subexpressions SEi and SEj as the minimum of their cardinalities."(see page 1052 in paper[1])
        // [1] Heling, Lars, and Maribel Acosta. "Federated SPARQL query processing over heterogeneous linked data fragments." Proceedings of the ACM Web Conference 2022.
        final int joinCardinality = Math.min( currentPlan.getCardinality(), nextPlan.getCardinality() );

        // The list of fed.members and numOfAccess will not be accessed anymore for this new PhysicalPlanWithStatistics object
        return new PhysicalPlanWithStatistics( newPlan, null, null, joinCardinality, -1 );
    }

    /**
     * The block size (number of bindings can be attached) depends on the type of interface
     */
    protected double determineBlockSize( final FederationMember fm ) {
        if ( (fm instanceof SPARQLEndpoint) || (fm instanceof BRTPFServer) ){
            return BaseForExecOpBindJoin.defaultPreferredInputBlockSize;
        }
        else if ( fm instanceof TPFServer ){
            return 1;
        }
        else
            throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
    }

}
