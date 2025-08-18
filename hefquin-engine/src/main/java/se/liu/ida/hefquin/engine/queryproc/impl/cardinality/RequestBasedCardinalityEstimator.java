package se.liu.ida.hefquin.engine.queryproc.impl.cardinality;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRightJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryproc.CardinalityEstimator;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;

import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;
import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.MAX_CARDINALITY;
import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.MIN_CARDINALITY;

public class RequestBasedCardinalityEstimator implements CardinalityEstimator
{
	protected final FederationAccessManager fedAccMgr;

	public RequestBasedCardinalityEstimator( final FederationAccessManager fedAccMgr ) {
		assert fedAccMgr != null;
		this.fedAccMgr = fedAccMgr;
	}

	@Override
	public void addCardinalities( final LogicalPlan ... plans ) {
		// As a first step, make sure that all nullary subplans (i.e.,
		// with request operators) within the given plans are annotated
		// with a cardinality estimate.
		addCardinalitiesForRequests(plans);

		// Now, use the worker to determine the cardinality estimates
		// for the given plans recursively. 
		new CardinalityEstimationWorker().addCardinalities(plans);
	}


	public void addCardinalitiesForRequests( final LogicalPlan ... plans ) {
		// Recursively extract all relevant nullary subplans from the
		// given plan, where a nullary subplan is considered relevant
		// if it does not yet have a cardinality estimate.
		final List<LogicalPlan> subPlans = extractNullarySubPlans(plans);

		// Without any relevant nullary subplans there is nothing to do here.
		if ( subPlans.isEmpty() ) return;

		// As input for method that adds cardinality estimates to the
		// extracted subplans, obtain the request operator and the
		// QueryPlanningInfo object of each of these subplans.
		final List<LogicalOpRequest<?,?>> reqOps  = new ArrayList<>( subPlans.size() );
		final List<QueryPlanningInfo> infoObjs    = new ArrayList<>( subPlans.size() );
		for ( final LogicalPlan subPlan : subPlans ) {
			reqOps.add( (LogicalOpRequest<?,?>) subPlan.getRootOperator() );
			infoObjs.add( subPlan.getQueryPlanningInfo() );
		}

		// Now we are ready to add the cardinality estimates.
		addCardinalities(reqOps, infoObjs);
	}

	protected List<LogicalPlan> extractNullarySubPlans( final LogicalPlan ... plans ) {
		if ( plans.length == 0 )
			return List.of();

		final List<LogicalPlan> subPlans = new ArrayList<>();
		for ( int i = 0; i < plans.length; i++ ) {
			extractNullarySubPlans( plans[i], subPlans );
		}

		return subPlans;
	}

	protected void extractNullarySubPlans( final LogicalPlan plan,
	                                       final List<LogicalPlan> extractedSubPlans ) {
		if ( plan instanceof LogicalPlanWithNullaryRoot ) {
			final QueryPlanningInfo qpInfo = plan.getQueryPlanningInfo();
			if ( qpInfo.getProperty(CARDINALITY) == null ) {
				extractedSubPlans.add(plan);
			}
		}
		else {
			// Recursion: extract all relevant nullary plans
			// from the direct subplans of the given plan.
			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				extractNullarySubPlans( plan.getSubPlan(i), extractedSubPlans );
			}
		}
	}

	protected void addCardinalities( final List<LogicalOpRequest<?,?>> reqOps,
	                                 final List<QueryPlanningInfo> infoObjs ) {
		final CardinalityResponse[] resps;
		try {
			resps = FederationAccessUtils.performCardinalityRequests( fedAccMgr,
			                                                          reqOps );
		}
		catch ( final FederationAccessException e ) {
			// If the cardinality requests fail, we need to guess. For the
			// moment, we simply guess the worst possible cardinality (max).
// TODO: We should try to be a bit smarter, using some heuristic that takes
// the patterns of the given requests into account.
// TODO: Also, in cases in which only some of the cardinality requests failed,
// we should at least get the values for those that did not fail. 
			final QueryPlanProperty est = QueryPlanProperty.cardinality(Integer.MAX_VALUE,
			                                                            Quality.PURE_GUESS);
			final QueryPlanProperty min = QueryPlanProperty.minCardinality(0,
			                                                               Quality.MIN_OR_MAX_POSSIBLE);
			final QueryPlanProperty max = QueryPlanProperty.maxCardinality(Integer.MAX_VALUE,
			                                                               Quality.MIN_OR_MAX_POSSIBLE);
			for ( final QueryPlanningInfo qpInfo : infoObjs ) {
				qpInfo.addProperty(est);
				qpInfo.addProperty(min);
				qpInfo.addProperty(max);
			}
			return;
		}

		if ( resps.length != reqOps.size() )
			throw new IllegalStateException("Wrong number of cardinality responses (namely, " + resps.length + ", but " + reqOps.size() + " expected).");

		for ( int i = 0; i < resps.length; i++ ) {
			final LogicalOpRequest<?,?> reqOp = reqOps.get(i);
			final QueryPlanningInfo infoObj = infoObjs.get(i);

			final int cardValue;
			final int minCardValue;
			final int maxCardValue;
			final QueryPlanProperty.Quality cardQuality;
			final QueryPlanProperty.Quality minCardQuality;
			final QueryPlanProperty.Quality maxCardQuality;

			if ( resps[i].isError() ) {
				cardValue = Integer.MAX_VALUE;
				cardQuality = Quality.PURE_GUESS;
				minCardValue = 0;
				minCardQuality = Quality.MIN_OR_MAX_POSSIBLE;
				maxCardValue = Integer.MAX_VALUE;
				maxCardQuality = Quality.MIN_OR_MAX_POSSIBLE;
			}
			else {
				try {
					cardValue = resps[i].getCardinality();
				}
				catch ( final Exception e ) {
					// We should not get an exception here because we are
					// in the branch where resps[i].isError() is false.
					throw new IllegalStateException();
				}

				if ( reqOp.getFederationMember() instanceof SPARQLEndpoint ) {
					cardQuality = Quality.ACCURATE;
				}
				else {
					cardQuality = Quality.DIRECT_ESTIMATE;
				}

				minCardValue = cardValue;
				maxCardValue = cardValue;
				minCardQuality = cardQuality;
				maxCardQuality = cardQuality;
			}

			infoObj.addProperty( QueryPlanProperty.cardinality(cardValue,
			                                                   cardQuality) );

			infoObj.addProperty( QueryPlanProperty.minCardinality(minCardValue,
			                                                      minCardQuality) );

			infoObj.addProperty( QueryPlanProperty.maxCardinality(maxCardValue,
			                                                      maxCardQuality) );
		}
	}


	protected static class CardinalityEstimationWorker implements LogicalPlanVisitor {

		protected LogicalPlan currentSubPlan = null;

		/**
		 * Assumes that all nullary subplans within the given plans are
		 * already annotated with a cardinality estimate.
		 */
		public void addCardinalities( final LogicalPlan ... plans ) {
			// Determine the cardinality estimates for the given plans
			// recursively (bottom up).
			for ( int i = 0; i < plans.length; i++ ) {
				addCardinality( plans[i] );
			}
		}

		/**
		 * Assumes that all nullary subplans within the given plan are
		 * already annotated with a cardinality estimate.
		 */
		protected void addCardinality( final LogicalPlan plan ) {
			final QueryPlanningInfo qpInfo = plan.getQueryPlanningInfo();
			if ( qpInfo.getProperty(CARDINALITY) != null ) {
				return;
			}

			for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
				addCardinality( plan.getSubPlan(i) );
			}

			currentSubPlan = plan;
			plan.getRootOperator().visit(this);
		}

		@Override
		public void visit( final LogicalOpRequest<?,?> op ) {
			// Since we assume that the nullary plans (which have a request
			// operator) have all been annotated already, we should never
			// end up here.
			throw new IllegalArgumentException();
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			addCardinalityForInnerJoin();
		}

		@Override
		public void visit( LogicalOpMultiwayJoin op ) {
			addCardinalityForInnerJoin();
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			throw new UnsupportedOperationException("Cardinality estimation for gpOptAdd not supported yet.");
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			throw new UnsupportedOperationException("Cardinality estimation for gpOptAdd not supported yet.");
		}

		@Override
		public void visit( final LogicalOpRightJoin op ) {
			// As the estimated cardinality, we simply use the estimated
			// cardinality of right-hand-side input plan (i.e., the non-
			// optional input). This may be a gross underestimation!
			// TODO: There is probably a slightly better approach.

			// The min.cardinality is 0 and the max.cardinality is the
			// product of the max.cardinalities of the two input plans.

			final QueryPlanningInfo qpInfoSubPlan1 = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();
			final QueryPlanProperty crd1 = qpInfoSubPlan1.getProperty(CARDINALITY);
			final QueryPlanProperty max1 = qpInfoSubPlan1.getProperty(MAX_CARDINALITY);

			final QueryPlanningInfo qpInfoSubPlan2 = currentSubPlan.getSubPlan(1).getQueryPlanningInfo();
			final QueryPlanProperty max2 = qpInfoSubPlan2.getProperty(MAX_CARDINALITY);

			final int crdValue = crd1.getValue();
			final int maxValue = multiplyWithoutExceedingMax( max1.getValue(), max2.getValue() );

			final Quality crdQuality;
			if ( crd1.getQuality() == Quality.ACCURATE )
				crdQuality = Quality.ESTIMATE_BASED_ON_ACCURATES;
			else if (    crd1.getQuality() == Quality.DIRECT_ESTIMATE
			          || crd1.getQuality() == Quality.ESTIMATE_BASED_ON_ACCURATES
			          || crd1.getQuality() == Quality.ESTIMATE_BASED_ON_ESTIMATES )
				crdQuality = Quality.ESTIMATE_BASED_ON_ESTIMATES;
			else
				crdQuality = crd1.getQuality();

			final Quality maxQuality;
			if (    max1.getQuality() == Quality.ACCURATE
			     && max2.getQuality() == Quality.ACCURATE )
				maxQuality = Quality.ESTIMATE_BASED_ON_ACCURATES;
			else if (    max1.getQuality() == Quality.ACCURATE
			          && max2.getQuality() == Quality.DIRECT_ESTIMATE )
				maxQuality = Quality.ESTIMATE_BASED_ON_ESTIMATES;
			else if (    max2.getQuality() == Quality.ACCURATE
			          && max1.getQuality() == Quality.DIRECT_ESTIMATE )
				maxQuality = Quality.ESTIMATE_BASED_ON_ESTIMATES;
			else if (    max1.getQuality() == Quality.DIRECT_ESTIMATE
			          && max2.getQuality() == Quality.DIRECT_ESTIMATE )
				maxQuality = Quality.ESTIMATE_BASED_ON_ESTIMATES;
			else if (    max1.getQuality() == Quality.ESTIMATE_BASED_ON_ACCURATES
			          && max2.getQuality() == Quality.ESTIMATE_BASED_ON_ACCURATES )
				maxQuality = Quality.ESTIMATE_BASED_ON_ESTIMATES;
			else
				maxQuality = pickWorse( max1.getQuality(), max2.getQuality() );

			final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
			qpInfo.addProperty( QueryPlanProperty.cardinality(crdValue, crdQuality) );
			qpInfo.addProperty( QueryPlanProperty.maxCardinality(maxValue, maxQuality) );
			qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.MIN_OR_MAX_POSSIBLE) );
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			throw new UnsupportedOperationException("Cardinality estimation for multiway left join not supported yet.");
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
			final QueryPlanningInfo qpInfoSubPlan = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();

			final QueryPlanProperty crd = qpInfoSubPlan.getProperty(CARDINALITY);
			final QueryPlanProperty max = qpInfoSubPlan.getProperty(MAX_CARDINALITY);

			// TODO: perhaps we can be smarter here and somehow estimate the
			// selectivity of the filter expression.

			qpInfo.addProperty( QueryPlanProperty.copyWithReducedQuality(crd) );
			qpInfo.addProperty( QueryPlanProperty.copyWithReducedQuality(max) );
			qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.MIN_OR_MAX_POSSIBLE) );
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
			final QueryPlanningInfo qpInfoSubPlan = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();

			qpInfo.addProperty( qpInfoSubPlan.getProperty(CARDINALITY) );
			qpInfo.addProperty( qpInfoSubPlan.getProperty(MAX_CARDINALITY) );
			qpInfo.addProperty( qpInfoSubPlan.getProperty(MIN_CARDINALITY) );
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
			final QueryPlanningInfo qpInfoSubPlan = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();

			final QueryPlanProperty crd = qpInfoSubPlan.getProperty(CARDINALITY);
			final QueryPlanProperty max = qpInfoSubPlan.getProperty(MAX_CARDINALITY);
			final QueryPlanProperty min = qpInfoSubPlan.getProperty(MIN_CARDINALITY);

			if ( op.getVocabularyMapping().isEquivalenceOnly() ) {
				// If the vocabulary mapping contains only equivalence
				// rules, applying this vocabulary mapping to a set of
				// solution mappings cannot result in fewer or more
				// output solution mappings.
				qpInfo.addProperty(crd);
				qpInfo.addProperty(max);
				qpInfo.addProperty(min);
			}
			else {
				// Applying a vocabulary mapping that contains not only
				// equivalence rules may affect the result cardinality.
				// Yet, it is not clear how to estimate this effect. So,
				// let's simply carry over the estimates produced for the
				// input plan, but with reduced quality scores.
				qpInfo.addProperty( QueryPlanProperty.copyWithReducedQuality(crd) );
				qpInfo.addProperty( QueryPlanProperty.copyWithReducedQuality(max) );
				qpInfo.addProperty( QueryPlanProperty.copyWithReducedQuality(min) );
			}
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
			final QueryPlanningInfo qpInfoSubPlan = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();

			final QueryPlanProperty crd = qpInfoSubPlan.getProperty(CARDINALITY);
			final QueryPlanProperty max = qpInfoSubPlan.getProperty(MAX_CARDINALITY);
			final QueryPlanProperty min = qpInfoSubPlan.getProperty(MIN_CARDINALITY);

			if ( op.getVocabularyMapping().isEquivalenceOnly() ) {
				// If the vocabulary mapping contains only equivalence
				// rules, applying this vocabulary mapping to a set of
				// solution mappings cannot result in fewer or more
				// output solution mappings.
				qpInfo.addProperty(crd);
				qpInfo.addProperty(max);
				qpInfo.addProperty(min);
			}
			else {
				// Applying a vocabulary mapping that contains not only
				// equivalence rules may affect the result cardinality.
				// Yet, it is not clear how to estimate this effect. So,
				// let's simply carry over the estimates produced for the
				// input plan, but with reduced quality scores.
				qpInfo.addProperty( QueryPlanProperty.copyWithReducedQuality(crd) );
				qpInfo.addProperty( QueryPlanProperty.copyWithReducedQuality(max) );
				qpInfo.addProperty( QueryPlanProperty.copyWithReducedQuality(min) );
			}
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			addCardinalityForUnion();
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			addCardinalityForUnion();
		}

		public void addCardinalityForUnion() {
			int crdValue = 0;
			int maxValue = 0;
			int minValue = 0;
			Quality crdQuality = Quality.ACCURATE;
			Quality maxQuality = Quality.ACCURATE;
			Quality minQuality = Quality.ACCURATE;

			for ( int x = 0; x < currentSubPlan.numberOfSubPlans(); x++ ) {
				final QueryPlanningInfo qpInfoSubPlanX = currentSubPlan.getSubPlan(x).getQueryPlanningInfo();
				final QueryPlanProperty crdX = qpInfoSubPlanX.getProperty(CARDINALITY);
				final QueryPlanProperty maxX = qpInfoSubPlanX.getProperty(MAX_CARDINALITY);
				final QueryPlanProperty minX = qpInfoSubPlanX.getProperty(MIN_CARDINALITY);

				crdValue = addWithoutExceedingMax( crdValue, crdX.getValue() );
				maxValue = addWithoutExceedingMax( maxValue, maxX.getValue() );
				minValue = addWithoutExceedingMax( minValue, minX.getValue() );

				crdQuality = pickWorse( crdQuality, crdX.getQuality() );
				maxQuality = pickWorse( maxQuality, maxX.getQuality() );
				minQuality = pickWorse( minQuality, minX.getQuality() );
			}

			final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
			qpInfo.addProperty( QueryPlanProperty.cardinality(crdValue, crdQuality) );
			qpInfo.addProperty( QueryPlanProperty.maxCardinality(maxValue, maxQuality) );
			qpInfo.addProperty( QueryPlanProperty.minCardinality(minValue, minQuality) );
		}

		public void addCardinalityForInnerJoin() {
			// As the estimated cardinality, we simply use the maximum
			// of the estimated cardinality of every input plan. This
			// is a very inaccurate estimate!
			// TODO: There is probably a slightly better approach.
			// TODO: An initial addition that is certainly useful (and
			// easy to implement) is to consider the special case of a
			// join without join variables (i.e., a cross product), in
			// which case the cardinalities of the input plans need to
			// be multiplied.

			// The min.cardinality is 0 and the max.cardinality is the
			// product of the max.cardinalities of all input plans.

			int crdValue = 0;
			int maxValue = 1;
			Quality crdQuality = Quality.ACCURATE;
			Quality maxQuality = Quality.ACCURATE;

			for ( int x = 0; x < currentSubPlan.numberOfSubPlans(); x++ ) {
				final QueryPlanningInfo qpInfoSubPlanX = currentSubPlan.getSubPlan(x).getQueryPlanningInfo();
				final QueryPlanProperty crdX = qpInfoSubPlanX.getProperty(CARDINALITY);
				final QueryPlanProperty maxX = qpInfoSubPlanX.getProperty(MAX_CARDINALITY);

				maxValue = multiplyWithoutExceedingMax( maxValue, maxX.getValue() );
				crdValue = Math.max( crdValue, crdX.getValue() );

				maxQuality = pickWorse( maxQuality, maxX.getQuality() );

				if (    x == 1
				     || crdX.getQuality() == Quality.PURE_GUESS
				     || crdX.getQuality() == Quality.MIN_OR_MAX_POSSIBLE
				     || crdX.getQuality() == Quality.ESTIMATE_BASED_ON_ESTIMATES ) {
					crdQuality = crdX.getQuality();
				}
				else if (    crdQuality == Quality.PURE_GUESS
				          || crdQuality == Quality.MIN_OR_MAX_POSSIBLE
				          || crdQuality == Quality.ESTIMATE_BASED_ON_ESTIMATES ) {
					// crdQuality = crdQuality;  // do nothing
				}
				else if (    crdQuality == Quality.ACCURATE
				          && crdX.getQuality()  == Quality.ACCURATE ) {
					crdQuality = Quality.ESTIMATE_BASED_ON_ACCURATES;
				}
				else {
					crdQuality = Quality.ESTIMATE_BASED_ON_ESTIMATES;
				}
			}

			final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
			qpInfo.addProperty( QueryPlanProperty.cardinality(crdValue, crdQuality) );
			qpInfo.addProperty( QueryPlanProperty.maxCardinality(maxValue, maxQuality) );
			qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.MIN_OR_MAX_POSSIBLE) );
		}

	}


	public static int addWithoutExceedingMax( final int x, final int y ) {
		if ( x == Integer.MAX_VALUE || y == Integer.MAX_VALUE )
			return Integer.MAX_VALUE;

		final int result = x + y;

		// check for overflow
		return ( result < 0 ) ? Integer.MAX_VALUE : result;
	}

	public static int multiplyWithoutExceedingMax( final int x, final int y ) {
		if ( x == Integer.MAX_VALUE || y == Integer.MAX_VALUE )
			return Integer.MAX_VALUE;

		final int result = x * y;

		// check for overflow
		return ( result < 0 ) ? Integer.MAX_VALUE : result;
	}

	public static Quality pickWorse( final Quality q1, final Quality q2 ) {
		if ( q1 == Quality.ACCURATE ) return q2;
		if ( q2 == Quality.ACCURATE ) return q1;
		if ( q1 == Quality.DIRECT_ESTIMATE ) return q2;
		if ( q2 == Quality.DIRECT_ESTIMATE ) return q1;
		if ( q1 == Quality.ESTIMATE_BASED_ON_ACCURATES ) return q2;
		if ( q2 == Quality.ESTIMATE_BASED_ON_ACCURATES ) return q1;
		if ( q1 == Quality.ESTIMATE_BASED_ON_ESTIMATES ) return q2;
		if ( q2 == Quality.ESTIMATE_BASED_ON_ESTIMATES ) return q1;
		if ( q1 == Quality.MIN_OR_MAX_POSSIBLE ) return q2;
		if ( q2 == Quality.MIN_OR_MAX_POSSIBLE ) return q1;
		return q1;
	}

}
