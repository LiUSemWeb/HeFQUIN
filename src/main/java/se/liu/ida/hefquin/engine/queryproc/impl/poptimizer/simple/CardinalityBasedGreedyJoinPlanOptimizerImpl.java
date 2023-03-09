package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.federation.access.utils.RequestMemberPair;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationException;

import java.util.*;

/**
 * This class implements a query optimizer that builds left-deep query plans,
 * for which it uses a greedy approach to determine the join order based on cardinality estimation,
 * and then choose physical algorithm according to the estimated number of request to execute the join.
 */
public class CardinalityBasedGreedyJoinPlanOptimizerImpl extends JoinPlanOptimizerBase
{
    protected final FederationAccessManager fedAccessMgr;

    public CardinalityBasedGreedyJoinPlanOptimizerImpl(final FederationAccessManager fedAccessMgr ) {
        this.fedAccessMgr = fedAccessMgr;
    }

    @Override
	public EnumerationAlgorithm initializeEnumerationAlgorithm( final List<PhysicalPlan> subplans ) {
		return new GreedyConstructionAlgorithm(subplans);
	}

	protected class GreedyConstructionAlgorithm implements EnumerationAlgorithm {
        protected final List<PhysicalPlan> subplans;

        public GreedyConstructionAlgorithm( final List<PhysicalPlan> subplans ) {
            this.subplans = subplans;
        }

        public PhysicalPlan getResultingPlan() throws PhysicalOptimizationException {
            final List<RequestMemberPair> flattenRequestMemPairs = new ArrayList<>();
            for ( final PhysicalPlan plan : subplans ) {
                flattenRequestMemPairs.addAll( createRequestMemPairsFromSourceAssignment(plan) );
            }

            final CardinalityResponse[] resps;
            try {
                resps = FederationAccessUtils.performCardinalityRequests(fedAccessMgr, flattenRequestMemPairs.toArray(new RequestMemberPair[0]));
            } catch (final FederationAccessException e) {
                throw new RuntimeException("Issuing a cardinality request caused an exception.", e);
            }

            final List<PhysicalPlanWithStatistics> subPlansWithStatistics = associateCardWithSubPlans( flattenRequestMemPairs, resps);

            PhysicalPlanWithStatistics currentPlan = null;
            for ( int k = 0; k < subplans.size(); k++ ) {
                final PhysicalPlanWithStatistics nextPlan = chooseNextSubPlanBasedOnCard(subPlansWithStatistics);
                if ( k == 0 ) {
                    currentPlan = nextPlan;
                }
                else {
                    currentPlan = decidePhysicalAlgorithm( currentPlan, nextPlan );
                }

                subPlansWithStatistics.remove( nextPlan );
            }

            return currentPlan.plan;
        }

        protected List<RequestMemberPair> createRequestMemPairsFromSourceAssignment( final PhysicalPlan plan ) {
            final PhysicalOperator pop = plan.getRootOperator();

            if (pop instanceof PhysicalOpRequest) {
                return new ArrayList<>( Arrays.asList(getRequestMemPairFromRequest((PhysicalOpRequest) pop)) );
            }
            else if (pop instanceof PhysicalOpFilter) {
                return new ArrayList<>( Arrays.asList( getRequestMemPairFromRequest((PhysicalOpRequest) plan.getSubPlan(0))) );
            }
            else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
                final List<RequestMemberPair> requestMemPairs = new ArrayList<>();
                final int numOfSubPlans = plan.numberOfSubPlans();
                for (int i = 0; i < numOfSubPlans; i++) {
                    requestMemPairs.addAll( createRequestMemPairsFromSourceAssignment(plan.getSubPlan(i)) );
                }
                return requestMemPairs;
            } else
                throw new IllegalArgumentException("Unsupported type of subquery in source assignment (" + pop.getClass().getName() + ")");
        }

        protected RequestMemberPair getRequestMemPairFromRequest( final PhysicalOpRequest pop ) {
            final FederationMember fm = pop.getLogicalOperator().getFederationMember();
            DataRetrievalRequest req = pop.getLogicalOperator().getRequest();
            if ( fm instanceof TPFServer ) {
                req = ensureTPFRequest( (TriplePatternRequest) req );
            }
            else if ( fm instanceof BRTPFServer && req instanceof TriplePatternRequest ) {
                req = ensureTPFRequest( (TriplePatternRequest) req );
            }
            else if ( fm instanceof BRTPFServer && req instanceof BindingsRestrictedTriplePatternRequest ) {
                req = ensureBRTPFRequest( (BindingsRestrictedTriplePatternRequest) req );
            }

            return new RequestMemberPair(req, fm);
        }

        protected TPFRequest ensureTPFRequest( final TriplePatternRequest req ) {
            if ( req instanceof TPFRequest ) {
                return (TPFRequest) req;
            }
            else {
                return new TPFRequestImpl( req.getQueryPattern() );
            }
        }

        protected BRTPFRequest ensureBRTPFRequest( final BindingsRestrictedTriplePatternRequest req ) {
            if ( req instanceof BRTPFRequest ) {
                return (BRTPFRequest) req;
            }
            else {
                return new BRTPFRequestImpl( req.getTriplePattern(), req.getSolutionMappings() );
            }
        }

        protected List<PhysicalPlanWithStatistics> associateCardWithSubPlans( final List<RequestMemberPair> flattenRequestMemPairs, final CardinalityResponse[] resps ){
            final List<PhysicalPlanWithStatistics> subPlansWithStatistics = new ArrayList<>();

            int index = 0;
            for ( final PhysicalPlan plan : subplans ) {
                final PhysicalOperator pop = plan.getRootOperator();

                final PhysicalPlanWithStatistics planWithStatistics = new PhysicalPlanWithStatistics(plan);
                if ( pop instanceof PhysicalOpRequest || pop instanceof PhysicalOpFilter ) {
                    planWithStatistics.addCandidate( resps[index].getCardinality() );

                    final FederationMember fm = flattenRequestMemPairs.get(index).getMember();
                    planWithStatistics.addNumOfAccess( accessNumForReq( resps[index].getCardinality(), fm ) );
                    planWithStatistics.addFederationMembers( new ArrayList<>( Arrays.asList(fm) ) );

                    index++;
                }
                else if (pop instanceof PhysicalOpBinaryUnion || pop instanceof PhysicalOpMultiwayUnion) {
                    int sumCard = 0, sumAccess = 0;
                    final List<FederationMember> fms = new ArrayList<>();
                    for ( int count = 0; count < plan.numberOfSubPlans(); count++ ) {
                        sumCard += resps[index].getCardinality();

                        final FederationMember fm = flattenRequestMemPairs.get(index).getMember();
                        sumAccess += accessNumForReq( resps[index].getCardinality(), fm );
                        fms.add( fm );

                        index++;
                    }

                    planWithStatistics.addCandidate( sumCard );
                    planWithStatistics.addNumOfAccess( sumAccess );
                    planWithStatistics.addFederationMembers( fms );
                }
                else
                    throw new IllegalArgumentException("Unsupported type of subquery in source assignment (" + pop.getClass().getName() + ")");

                subPlansWithStatistics.add( planWithStatistics );
            }

            return subPlansWithStatistics;
        }

        /**
         * Compares all available subplans in terms of their cardinality estimation
         * and returns the one with the lowest estimated cardinality.
         */
        protected PhysicalPlanWithStatistics chooseNextSubPlanBasedOnCard( final List<PhysicalPlanWithStatistics> subPlansWithStatistics ) {
            PhysicalPlanWithStatistics bestPlanWithCandidate = subPlansWithStatistics.get(0);
            int lowestCost = bestPlanWithCandidate.getCandidate();

            for ( int i = 1; i < subPlansWithStatistics.size(); i++ ) {
                PhysicalPlanWithStatistics current = subPlansWithStatistics.get(i);
                if ( current.getCandidate() < lowestCost ) {
                    lowestCost = current.getCandidate();
                    bestPlanWithCandidate = current;
                }
            }

            return bestPlanWithCandidate;
        }

        /**
         * The physical algorithm is determined based on the estimated number of requests to execute the join
         * - SHJ: Card(T1)/PageSize + Card(T2)/PageSize
         * - BJ: Card(T1)/PageSize + Card(T1)/blockSize
         * For the comparison, only keeping the second element is enough.
         */
        protected PhysicalPlanWithStatistics decidePhysicalAlgorithm( final PhysicalPlanWithStatistics currentPlan, final PhysicalPlanWithStatistics nextPlan ) {
            final double accNumSHJ = nextPlan.getNumOfAccess();

            final List<FederationMember> fms = nextPlan.getFederationMembers();
            double accNumBJ = 0;
            for ( final FederationMember fm: fms ) {
                double blockSize = setBlockSize(fm);
                accNumBJ += currentPlan.getCandidate() / blockSize;
            }

            PhysicalPlan newPlan;
            if ( accNumSHJ <= accNumBJ ) {
                newPlan = PhysicalPlanFactory.createPlanWithJoin(currentPlan.plan, nextPlan.plan);
            }
            else {
                newPlan = PhysicalPlanFactory.createPlanWithDefaultUnaryOpIfPossible(currentPlan.plan, nextPlan.plan);
            }

            final PhysicalPlanWithStatistics newPlanWithStatistics = new PhysicalPlanWithStatistics( newPlan );

            final int joinCardinality = Math.min( currentPlan.getCandidate(), nextPlan.getCandidate() );
            newPlanWithStatistics.addCandidate( joinCardinality );

            return newPlanWithStatistics;
        }

        /**
         * The number of requests depends on the page size of response.
         */
        protected int accessNumForReq( final int cardinality, final FederationMember fm ) {
            if ( fm instanceof SPARQLEndpoint){
                return 1;
            }
            else if ( (fm instanceof TPFServer) || (fm instanceof BRTPFServer) ){
                final double pageSize = 100.0;
                return (int) Math.ceil( cardinality/pageSize );
            }
            else
                throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
        }

        /**
         * The block size (number of bindings can be attached) depends on the type of interface
         */
        protected double setBlockSize( final FederationMember fm ) {
            if ( fm instanceof SPARQLEndpoint ){
                return 50;
            }
            else if ( fm instanceof BRTPFServer ){
                return 30;
            }
            else if ( fm instanceof TPFServer ){
                return 1;
            }
            else
                throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
        }

    }
        
}

class PhysicalPlanWithStatistics {
    public final PhysicalPlan plan;
    protected int candidate;
    protected int numOfAccess;
    protected List<FederationMember> fms;

    public PhysicalPlanWithStatistics( final PhysicalPlan plan ){
        this.plan = plan;
    }

    public void addCandidate( final int candidate ){
        this.candidate = candidate;
    }

    public void addFederationMembers( final List<FederationMember> fms ){
        this.fms = fms;
    }

    public void addNumOfAccess( final int numOfAccess ){
        this.numOfAccess = numOfAccess;
    }

    public int getCandidate( ){
        return candidate;
    }

    public int getNumOfAccess( ){
        return numOfAccess;
    }

    public List<FederationMember> getFederationMembers( ){
        return fms;
    }

}
