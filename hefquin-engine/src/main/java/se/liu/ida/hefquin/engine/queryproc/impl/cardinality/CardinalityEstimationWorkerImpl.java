package se.liu.ida.hefquin.engine.queryproc.impl.cardinality;

import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;
import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.MAX_CARDINALITY;
import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.MIN_CARDINALITY;

import se.liu.ida.hefquin.engine.queryplan.base.QueryPlan;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
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
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public class CardinalityEstimationWorkerImpl implements CardinalityEstimationWorker,
                                                        LogicalPlanVisitor
{
	protected QueryPlan currentSubPlan = null;

	public void addCardinalities( final LogicalPlan ... plans ) {
		// Determine the cardinality estimates for the given plans
		// recursively (bottom up).
		for ( int i = 0; i < plans.length; i++ ) {
			addCardinality( plans[i] );
		}
	}

	public void addCardinalities( final PhysicalPlan ... plans ) {
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

	/**
	 * Assumes that all nullary subplans within the given plan are
	 * already annotated with a cardinality estimate.
	 */
	protected void addCardinality( final PhysicalPlan plan ) {
		final QueryPlanningInfo qpInfo = plan.getQueryPlanningInfo();
		if ( qpInfo.getProperty(CARDINALITY) != null ) {
			return;
		}

		for ( int i = 0; i < plan.numberOfSubPlans(); i++ ) {
			addCardinality( plan.getSubPlan(i) );
		}

		currentSubPlan = plan;
		final PhysicalOperatorForLogicalOperator pRoot = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
		pRoot.getLogicalOperator().visit(this);
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
		// TODO: add support for gpAdd
		throw new UnsupportedOperationException("Cardinality estimation for gpAdd not supported yet.");
	}

	@Override
	public void visit( final LogicalOpGPOptAdd op ) {
		// TODO: add support for gpOptAdd
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
		// TODO: add support for mlj
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
