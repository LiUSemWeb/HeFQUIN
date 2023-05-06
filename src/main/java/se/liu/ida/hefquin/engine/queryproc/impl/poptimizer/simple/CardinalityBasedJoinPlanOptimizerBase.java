package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel.CFRNumberOfRequests;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithStatistics;

import java.util.*;

/**
 * As a basis for query optimizers that are based on cardinality estimation,
 * this abstract base class provides cardinality estimation and
 * basic statistical annotations for each subplan.
 *
 * The concrete optimizers that decide join order and construct query plan are implemented in
 * {@link CardinalityBasedGreedyJoinPlanOptimizerImpl}
 * and {@link ReduceVocRewritingJoinPlanOptimizerImpl}
 */
public abstract class CardinalityBasedJoinPlanOptimizerBase extends JoinPlanOptimizerBase
{
    protected final FederationAccessManager fedAccessMgr;

    public CardinalityBasedJoinPlanOptimizerBase(final FederationAccessManager fedAccessMgr ) {
        this.fedAccessMgr = fedAccessMgr;
    }

    protected abstract class GreedyConstructionAlgorithm implements EnumerationAlgorithm {
        protected final List<PhysicalPlan> subplans;

        public GreedyConstructionAlgorithm( final List<PhysicalPlan> subplans ) {
            this.subplans = subplans;
        }

        @Override
        public PhysicalPlan getResultingPlan() throws PhysicalOptimizationException
        {
            // Extract all request operators from the given list of subplans,
            // where the list of these operators is ordered in the order in which
            // the operators can be found by a depth-first traversal of the subplans.
            final List<LogicalOpRequest<?,?>> reqOpsOfAllSubPlans = new ArrayList<>();
            for ( final PhysicalPlan subplan : subplans ) {
            	reqOpsOfAllSubPlans.addAll( extractAllRequestOpsFromSourceAssignment(subplan) );
            }

            // Next, get a list of cardinality estimates by performing cardinality
            // requests for the above list of request operators.
            final CardinalityResponse[] resps;
            try {
                resps = FederationAccessUtils.performCardinalityRequests(fedAccessMgr, reqOpsOfAllSubPlans);
            } catch (final FederationAccessException e) {
                throw new PhysicalOptimizationException("Issuing a cardinality request caused an exception.", e);
            }

            // Then, annotate each subplan (in the 'subplans') with all statistics relevant for the join planning
            // ( including cardinality, number of access, and a list of relevant federation members )
            final List<PhysicalPlanWithStatistics> subPlansWithStatistics = associateCardWithSubPlans(subplans, reqOpsOfAllSubPlans, resps);

            // To build a left-deep query plan, first select the subplan with the lowest estimated cardinality
            final PhysicalPlanWithStatistics firstSubPlan = chooseFirstSubPlan(subPlansWithStatistics);
            subPlansWithStatistics.remove(firstSubPlan);

            // The nextSubPlan is selected from subplans that have join variables with selected subplans.
            // To achieve this, we implement a custom comparator and create a PriorityQueue to store candidate subPlans in cardinality order.
            final Comparator<PhysicalPlanWithStatistics> orderBasedOnCard = (o1, o2) -> Integer.compare( o1.getCardinality(), o2.getCardinality() );

            return determineJoinOrderAndConstructPlan( firstSubPlan, subPlansWithStatistics, orderBasedOnCard);
        }

        protected abstract PhysicalPlan determineJoinOrderAndConstructPlan(
                final PhysicalPlanWithStatistics firstSubPlan,
                final List<PhysicalPlanWithStatistics> subPlansWithStatistics,
                final Comparator<PhysicalPlanWithStatistics> orderBasedOnCard
        );

        /**
         * Extracts all request operators from the given plan, assuming
         * that this plan is a sub-plan of a source assignment (hence,
         * assuming that this plan can only be either a single request,
         * a filter over a request, or a union with requests).
         *
         * The extracted request operators will be order in the order in
         * which they are discovered by a depth-first traversal of the
         * given plan.
         */
        protected List<LogicalOpRequest<?,?>> extractAllRequestOpsFromSourceAssignment(final PhysicalPlan plan) {
            final PhysicalOperator pop = plan.getRootOperator();
            if (pop instanceof PhysicalOpRequest) {
                return Arrays.asList( ((PhysicalOpRequest<?,?>) pop).getLogicalOperator() );
            }
            else if (pop instanceof PhysicalOpFilter
                    && plan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest) {
                return Arrays.asList( ((PhysicalOpRequest<?,?>) plan.getSubPlan(0).getRootOperator()).getLogicalOperator() );
            }
            else if ( pop instanceof PhysicalOpLocalToGlobal
                    && plan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest
            ) {
                return Arrays.asList( ((PhysicalOpRequest<?,?>) plan.getSubPlan(0).getRootOperator()).getLogicalOperator() );
            }
            else if ( pop instanceof PhysicalOpLocalToGlobal
                    && plan.getSubPlan(0).getRootOperator() instanceof PhysicalOpFilter
                    && plan.getSubPlan(0).getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest
            ) {
                return Arrays.asList( ((PhysicalOpRequest<?,?>) plan.getSubPlan(0).getSubPlan(0).getRootOperator()).getLogicalOperator() );
            }
            else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
                final List<LogicalOpRequest<?,?>> reqOps = new ArrayList<>();
                final int numOfSubPlans = plan.numberOfSubPlans();
                for (int i = 0; i < numOfSubPlans; i++) {
                    reqOps.addAll( extractAllRequestOpsFromSourceAssignment(plan.getSubPlan(i)) );
                }
                return reqOps;
            }
            else
                throw new IllegalArgumentException("Unsupported type of subquery in source assignment (" + pop.getClass().getName() + ")");
        }

        /**
         *
         * @param reqOpsOfAllSubPlans A list of request operators, which is ordered in the order in which the request operators can be found by a depth-first traversal of the subplans.
         * @param resps A list of cardinality in the same order of 'reqOpsOfAllSubPlans'
         * @return A list of PhysicalPlanWithStatistics, with each object corresponding to a subplan in the 'subplans' list.
         * Each PhysicalPlanWithStatistics object contains the physical plan for the subplan, along with its cardinality,
         * a list of relevant federations members, and estimated number of access to federation members.
         *
         * During a depth-first traversal of the subplans, increase the index by 1 when visiting a request operator:
         * If the root operator of the subplan is a Request operator, get the corresponding cardinality from resps with the same index;
         * If the subPlan with a Union of requests, calculate the total cardinality by summing up the cardinality of individual requests.
         * (See page 1052 in the paper[1])
         *
         * [1] Heling, Lars, and Maribel Acosta. "Federated SPARQL query processing over heterogeneous linked data fragments." Proceedings of the ACM Web Conference 2022.
         */
        protected List<PhysicalPlanWithStatistics> associateCardWithSubPlans(
                final List<PhysicalPlan> subplans,
                final List<LogicalOpRequest<?,?>> reqOpsOfAllSubPlans,
                final CardinalityResponse[] resps ){
            final List<PhysicalPlanWithStatistics> subPlansWithStatistics = new ArrayList<>();

            int index = 0;
            for ( final PhysicalPlan subplan : subplans ) {
                final PhysicalOperator pop = subplan.getRootOperator();

                PhysicalPlanWithStatistics planWithStatistics = null;
                if ( pop instanceof PhysicalOpRequest
                        || (pop instanceof PhysicalOpFilter && subplan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest)) {
                    final int cardinality = resps[index].getCardinality();
                    final FederationMember fm = reqOpsOfAllSubPlans.get(index).getFederationMember();
                    final int numOfAccess = accessNumForReq(cardinality, fm);
                    final VocabularyMapping vm = fm.getVocabularyMapping();

                    if ( vm != null ) {
                        planWithStatistics = new PhysicalPlanWithStatistics(subplan, Arrays.asList(fm), Arrays.asList(vm), cardinality, numOfAccess);
                    }
                    else {
                        planWithStatistics = new PhysicalPlanWithStatistics(subplan, Arrays.asList(fm), null, cardinality, numOfAccess);
                    }
                    index++;
                }
                else if ( pop instanceof PhysicalOpLocalToGlobal ){
                    if (  subplan.getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest
                            || (subplan.getSubPlan(0).getRootOperator() instanceof PhysicalOpFilter && subplan.getSubPlan(0).getSubPlan(0).getRootOperator() instanceof PhysicalOpRequest)) {
                        final int cardinality = resps[index].getCardinality();
                        final FederationMember fm = reqOpsOfAllSubPlans.get(index).getFederationMember();
                        final VocabularyMapping vm = ((LogicalOpLocalToGlobal) ((PhysicalOpLocalToGlobal) pop).getLogicalOperator()).getVocabularyMapping();
                        final int numOfAccess = accessNumForReq(cardinality, fm);

                        planWithStatistics = new PhysicalPlanWithStatistics(subplan, Arrays.asList(fm), Arrays.asList(vm), cardinality, numOfAccess );
                        index++;
                    }
                }
                else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
                    int aggregatedCardinality = 0;
                    int numOfAccess = 0;
                    final List<FederationMember> fms = new ArrayList<>();
                    final List<VocabularyMapping> vms = new ArrayList<>();
                    for ( int count = 0; count < subplan.numberOfSubPlans(); count++ ) {
                        final int cardinality = resps[index].getCardinality();
                        aggregatedCardinality += (cardinality < 0 ) ? Integer.MAX_VALUE : cardinality;
                        if ( aggregatedCardinality < 0 ) aggregatedCardinality = Integer.MAX_VALUE;

                        final FederationMember fm = reqOpsOfAllSubPlans.get(index).getFederationMember();
                        numOfAccess += accessNumForReq(cardinality, fm);
                        fms.add( fm );

                        if ( subplan.getSubPlan(count).getRootOperator() instanceof PhysicalOpLocalToGlobal ){
                            final VocabularyMapping vm = ((LogicalOpLocalToGlobal) ((PhysicalOpLocalToGlobal) subplan.getSubPlan(count).getRootOperator()).getLogicalOperator()).getVocabularyMapping();
                            vms.add(vm);
                        }
                        index++;
                    }

                    planWithStatistics = new PhysicalPlanWithStatistics(subplan, fms, vms, aggregatedCardinality, numOfAccess );
                }
                else
                    throw new IllegalArgumentException("Unsupported type of subquery in source assignment (" + pop.getClass().getName() + ")");

                subPlansWithStatistics.add( planWithStatistics );
            }

            return subPlansWithStatistics;
        }

        /**
         * The number of requests depends on the page size of response.
         */
        protected int accessNumForReq( final int cardinality, final FederationMember fm ) {
            if ( fm instanceof SPARQLEndpoint){
                return 1;
            }
            else if ( (fm instanceof TPFServer) || (fm instanceof BRTPFServer) ){
                final double pageSize = CFRNumberOfRequests.defaultPageSize;
                return (int) Math.ceil( cardinality/pageSize );
            }
            else
                throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
        }

        /**
         * Compares all available subplans in terms of
         * their respective cardinality returns the one with the lowest cardinality.
         */
        protected PhysicalPlanWithStatistics chooseFirstSubPlan( final List<PhysicalPlanWithStatistics> subPlansWithStatistics ) {
            PhysicalPlanWithStatistics bestPlanWithCandidate = subPlansWithStatistics.get(0);
            int lowestCost = bestPlanWithCandidate.getCardinality();

            for ( int i = 1; i < subPlansWithStatistics.size(); i++ ) {
                PhysicalPlanWithStatistics current = subPlansWithStatistics.get(i);
                if ( current.getCardinality() < lowestCost ) {
                    lowestCost = current.getCardinality();
                    bestPlanWithCandidate = current;
                }
            }

            return bestPlanWithCandidate;
        }

        /**
         * Iterate through the remaining subplans and selected those that contain any of the variables in the given set of variables.
         */
        protected List<PhysicalPlanWithStatistics> getSubPlansContainVars( final ExpectedVariables vars, final List<PhysicalPlanWithStatistics> subPlansWithStatistics ) {
            final List<PhysicalPlanWithStatistics> subPlansContainsVars = new ArrayList<>();
            for ( final PhysicalPlanWithStatistics subplan: subPlansWithStatistics ) {
                final Set<Var> joinVars = ExpectedVariablesUtils.intersectionOfAllVariables( vars, subplan.plan.getExpectedVariables() );
                if ( joinVars != null && !joinVars.isEmpty() ){
                    subPlansContainsVars.add(subplan);
                }
            }

            return subPlansContainsVars;
        }
    }

}
