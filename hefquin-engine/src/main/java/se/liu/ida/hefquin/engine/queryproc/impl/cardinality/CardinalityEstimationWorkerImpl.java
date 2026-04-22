package se.liu.ida.hefquin.engine.queryproc.impl.cardinality;

import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.CARDINALITY;
import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.MAX_CARDINALITY;
import static se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.MIN_CARDINALITY;

import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.NodeValue;

import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.base.QueryPlan;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Quality;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
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
		throw new IllegalArgumentException( op.toString() );
	}

	@Override
	public void visit( final LogicalOpFixedSolMap op ) {
		// Since we assume that the nullary plans (which have a fin operator)
		// have all been annotated already, we should never end up here.
		throw new IllegalArgumentException( op.toString() );
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
		// TODO: add proper support for gpAdd
		// For the moment, we only handle gpAdd operators that have been
		// created for SERVICE clauses with a PARAMS clause, which are
		// meant to access Web APIs. The current quick-and-dirty solution
		// for these kinds of gpAdd operators is to assume that their
		// graph pattern does not affect the cardinality of the result,
		// which can easily be a wrong assumption for many cases.
		if ( op.hasParameterVariables() ) {
			final QueryPlanningInfo qpInfoSubPlan = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();
			final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
			qpInfo.addProperty( qpInfoSubPlan.getProperty(CARDINALITY) );
			qpInfo.addProperty( qpInfoSubPlan.getProperty(MIN_CARDINALITY) );
			qpInfo.addProperty( qpInfoSubPlan.getProperty(MAX_CARDINALITY) );
			return;
		}

		throw new UnsupportedOperationException("Cardinality estimation for gpAdd not supported yet.");
	}

	@Override
	public void visit( final LogicalOpGPOptAdd op ) {
		// TODO: add support for gpOptAdd
		throw new UnsupportedOperationException("Cardinality estimation for gpOptAdd not supported yet.");
	}

	@Override
	public void visit( final LogicalOpLeftJoin op ) {
		// As the estimated cardinality, we simply use the estimated
		// cardinality of left-hand-side input plan (i.e., the non-
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

		// Check first whether we have a case in which the left subplan is
		// guaranteed to produce an empty result. If so, we can stop
		// here because the join cardinality is then guaranteed to be
		// zero as well.
		if ( crdValue == 0 && crd1.getQuality() == Quality.ACCURATE ) {
			final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
			qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
			qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
			qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );
			return;
		}

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
		// Use the cardinality estimate from the first subplan
		// Use the min-cardinality from the first subplan
		// Use the product of the max-cardinalities from all subplans
		// as max-cardinality

		final QueryPlanningInfo qpInfoSubPlan0 = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();
		final QueryPlanProperty crd0 = qpInfoSubPlan0.getProperty(CARDINALITY);
		final QueryPlanProperty max0 = qpInfoSubPlan0.getProperty(MAX_CARDINALITY);
		final QueryPlanProperty min0 = qpInfoSubPlan0.getProperty(MIN_CARDINALITY);

		final int crdValue = crd0.getValue();
		final int minValue = min0.getValue();
		final Quality minQuality = min0.getQuality();

		final Quality crdQuality;
		if ( crd0.getQuality() == Quality.ACCURATE )
			crdQuality = Quality.ESTIMATE_BASED_ON_ACCURATES;
		else if (    crd0.getQuality() == Quality.DIRECT_ESTIMATE
		          || crd0.getQuality() == Quality.ESTIMATE_BASED_ON_ACCURATES )
			crdQuality = Quality.ESTIMATE_BASED_ON_ESTIMATES;
		else
			crdQuality = crd0.getQuality();

		// Compute an upper bound for the result size by multiplying the
		// max-cardinalities of all input subplans. Propagate the worst
		// max-cardinality quality.
		int maxValue = max0.getValue();
		Quality maxQuality = max0.getQuality();
		for ( int x = 1; x < currentSubPlan.numberOfSubPlans(); x++ ) {
			final QueryPlanningInfo qpInfoSubPlanX = currentSubPlan.getSubPlan(x).getQueryPlanningInfo();
			final QueryPlanProperty maxX = qpInfoSubPlanX.getProperty(MAX_CARDINALITY);
			maxValue = multiplyWithoutExceedingMax( maxValue, maxX.getValue() );
			maxQuality = pickWorse( maxQuality, maxX.getQuality() );
		}

		// The max-cardinality combines several cardinality values, but the
		// resulting quality is a derived estimate.
		if ( maxQuality == Quality.ACCURATE )
			maxQuality = Quality.ESTIMATE_BASED_ON_ACCURATES;
		else if (    maxQuality == Quality.DIRECT_ESTIMATE
		          || maxQuality == Quality.ESTIMATE_BASED_ON_ACCURATES )
			maxQuality = Quality.ESTIMATE_BASED_ON_ESTIMATES;

		final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
		qpInfo.addProperty( QueryPlanProperty.cardinality(crdValue, crdQuality) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(maxValue, maxQuality) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(minValue, minQuality) );
	}

	@Override
	public void visit( final LogicalOpFilter op ) {
		final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
		final QueryPlanningInfo qpInfoSubPlan = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();

		final QueryPlanProperty crd = qpInfoSubPlan.getProperty(CARDINALITY);
		final QueryPlanProperty max = qpInfoSubPlan.getProperty(MAX_CARDINALITY);

		// TODO: perhaps we can be smarter here and somehow estimate the
		// selectivity of the filter expression.

		// If the filter is above a fixed solution mapping operator, evaluate
		// the filter condition for the fixed solution mapping of that operator.
		if ( currentSubPlan.getSubPlan(0).getRootOperator() instanceof LogicalOpFixedSolMap childOp ) {
			if ( SolutionMappingUtils.checkSolutionMapping(childOp.getSolutionMapping(), op.getFilterExpressions()) ) {
				qpInfo.addProperty( QueryPlanProperty.cardinality(1, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.maxCardinality(1, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.minCardinality(1, Quality.ACCURATE) );
			}
			else {
				qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );
			}
		}
		else {
			qpInfo.addProperty( QueryPlanProperty.copyWithReducedQuality(crd) );
			qpInfo.addProperty( QueryPlanProperty.copyWithReducedQuality(max) );
			qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.MIN_OR_MAX_POSSIBLE) );
		}
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
	public void visit( final LogicalOpUnfold op ) {
		final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
		final QueryPlanningInfo qpInfoSubPlan = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();

		// Before considering the general case, we consider a few special
		// cases for which we may be more accurate than in the general case.

		// Special case 1: if the expression of the UNFOLD clause is
		// a constant that is *not* a well-formed cdt:List or cdt:Map
		// literal, then we know that the output cardinality will be
		// the same as the input cardinality.
		final NodeValue nv = op.getExpr().getConstant();
		if ( nv != null ) {
			final Node n = nv.asNode();

			boolean isWellFormedCDTList = false;
			boolean isWellFormedCDTMap = false;
			if ( n.isLiteral() ) {
				final String dtURI = n.getLiteralDatatypeURI();

				isWellFormedCDTList = ( CompositeDatatypeList.uri.equals(dtURI)
				                        && n.getLiteral().isWellFormed() );

				isWellFormedCDTMap = ( CompositeDatatypeMap.uri.equals(dtURI)
				                       && n.getLiteral().isWellFormed() );
			}

			if ( ! isWellFormedCDTList && ! isWellFormedCDTMap ) {
				qpInfo.addProperty( qpInfoSubPlan.getProperty(CARDINALITY) );
				qpInfo.addProperty( qpInfoSubPlan.getProperty(MAX_CARDINALITY) );
				qpInfo.addProperty( qpInfoSubPlan.getProperty(MIN_CARDINALITY) );
				return;
			}

			// Special case 2: if the expression is a well-formed cdt:List
			// or cdt:Map literal, then we can use the size of the list/map
			// for estimating the output cardinality.
			final int size;
			if ( isWellFormedCDTList ) {
				@SuppressWarnings("unchecked")
				final List<CDTValue> list = (List<CDTValue>) n.getLiteralValue();
				size = list.size();
			}
			else if ( isWellFormedCDTMap ) {
				@SuppressWarnings("unchecked")
				final Map<CDTKey,CDTValue> list = (Map<CDTKey,CDTValue>) n.getLiteralValue();
				size = list.size();
			}
			else {
				size = -1;
			}

			if ( size == 0 ) {
				qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );
				return;
			}

			if ( size > 0 ) {
				final QueryPlanProperty crdIn = qpInfoSubPlan.getProperty(CARDINALITY);
				final QueryPlanProperty maxIn = qpInfoSubPlan.getProperty(MAX_CARDINALITY);
				final QueryPlanProperty minIn = qpInfoSubPlan.getProperty(MIN_CARDINALITY);

				final int crd = multiplyWithoutExceedingMax( crdIn.getValue(), size );
				final int max = multiplyWithoutExceedingMax( maxIn.getValue(), size );
				final Quality crdQ = QueryPlanProperty.getReducedQuality( crdIn.getQuality() );
				final Quality maxQ = QueryPlanProperty.getReducedQuality( maxIn.getQuality() );

				qpInfo.addProperty( QueryPlanProperty.cardinality(crd, crdQ) );
				qpInfo.addProperty( QueryPlanProperty.maxCardinality(max, maxQ) );
				qpInfo.addProperty( minIn );
				return;
			}
		}

		// Special case 3: if the expression of the UNFOLD clause is
		// function call using the cdt:List or the cdt:Map constructor
		// function and there are no arguments for this function call,
		// then we know that the produced list/map will be empty and,
		// thus, the output cardinality will be zero.
		final ExprFunction fct = op.getExpr().getFunction();
		if ( fct != null ) {
			final String fctIRI = fct.getFunctionIRI();

			final boolean case3Found;
			if (    ! fctIRI.equals(ARQConstants.CDTFunctionLibraryURI + "List")
			     && ! fctIRI.equals(ARQConstants.CDTFunctionLibraryURI + "Map") )
				case3Found = false;
			else
				case3Found = fct.getArgs().isEmpty();

			if ( case3Found ) {
				qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );
				return;
			}
		}

		// Now we come to the general case.

		// We take a wild guess and assume that, for every input solution
		// mapping, the list or map to be unfolded contains 10 elements.
		final QueryPlanProperty crdIn = qpInfoSubPlan.getProperty(CARDINALITY);
		final QueryPlanProperty crd = QueryPlanProperty.cardinality(
				multiplyWithoutExceedingMax( crdIn.getValue(), 10 ),
				QueryPlanProperty.getReducedQuality( crdIn.getQuality() ) );
		qpInfo.addProperty(crd);

		// The minimum cardinality is not changed by UNFOLD.
		qpInfo.addProperty( qpInfoSubPlan.getProperty(MIN_CARDINALITY) );

		// The maximum cardinality is simply the maximum possible because
		// the lists and maps to be unfolded may be unboundedly large.
		final int max = Integer.MAX_VALUE;
		final Quality maxQuality = Quality.MIN_OR_MAX_POSSIBLE;
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(max, maxQuality) );
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

	@Override
	public void visit( final LogicalOpDedup op ) {
		final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
		final QueryPlanningInfo qpInfoSubPlan = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();

		final QueryPlanProperty crdIn = qpInfoSubPlan.getProperty(CARDINALITY);
		final QueryPlanProperty maxIn = qpInfoSubPlan.getProperty(MAX_CARDINALITY);
		final QueryPlanProperty minIn = qpInfoSubPlan.getProperty(MIN_CARDINALITY);

		final int crdValue;
		final Quality crdQuality;
		if ( crdIn.getValue() == 0 && crdIn.getQuality() == Quality.ACCURATE ) {
			// If the input plan is guaranteed to produce an empty result,
			// then the result of the DEDUP operator is guaranteed to be empty as well.
			crdValue = 0;
			crdQuality = Quality.ACCURATE;
		}
		else if ( crdIn.getValue() == 0 || crdIn.getValue() == 1 ) {
			crdValue = crdIn.getValue();
			crdQuality = crdIn.getQuality();
		}
		else {
			// Heuristic: assume 50% duplicates
			crdValue = crdIn.getValue() / 2;
			crdQuality = QueryPlanProperty.getReducedQuality( crdIn.getQuality() );
		}

		final int maxValue = maxIn.getValue();
		final Quality maxQuality = maxIn.getQuality();

		final int minValue;
		final Quality minQuality;
		if ( minIn.getValue() == 0 ) {
			minValue = minIn.getValue();
			minQuality = minIn.getQuality();
		}
		else {
			minValue = 1;
			minQuality = Quality.MIN_OR_MAX_POSSIBLE;
		}

		qpInfo.addProperty( QueryPlanProperty.cardinality(crdValue, crdQuality) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(maxValue, maxQuality) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(minValue, minQuality) );
	}

	@Override
	public void visit( final LogicalOpProject op ) {
		final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
		final QueryPlanningInfo qpInfoSubPlan = currentSubPlan.getSubPlan(0).getQueryPlanningInfo();
		final QueryPlanProperty crdSubPlan = qpInfoSubPlan.getProperty(CARDINALITY);

		if ( crdSubPlan.getValue() == 0 && crdSubPlan.getQuality() == Quality.ACCURATE ) {
			// If the input plan is guaranteed to produce an empty result,
			// then the result of the PROJECT operator is guaranteed to be empty as well.
			qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
			qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
			qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );
		}
		else {
			qpInfo.addProperty( qpInfoSubPlan.getProperty(CARDINALITY) );
			qpInfo.addProperty( qpInfoSubPlan.getProperty(MAX_CARDINALITY) );
			qpInfo.addProperty( qpInfoSubPlan.getProperty(MIN_CARDINALITY) );
		}
	}

	@Override
	public void visit( final LogicalOpMinus op ) {
		final int crdValue;
		final Quality crdQuality;

		final QueryPlan lhs = currentSubPlan.getSubPlan(0);
		final QueryPlan rhs = currentSubPlan.getSubPlan(1);

		final QueryPlanningInfo qpInfoSubPlan1 = lhs.getQueryPlanningInfo();
		final QueryPlanningInfo qpInfoSubPlan2 = rhs.getQueryPlanningInfo();

		final QueryPlanProperty lhsCrd = qpInfoSubPlan1.getProperty(CARDINALITY);
		final QueryPlanProperty lhsMaxCrd = qpInfoSubPlan1.getProperty(MAX_CARDINALITY);
		final QueryPlanProperty rhsCrd = qpInfoSubPlan2.getProperty(CARDINALITY);

		if ( lhsCrd.getValue() == 0 && lhsCrd.getQuality() == Quality.ACCURATE ) {
			// If the first input plan is guaranteed to produce an empty result,
			// then the result of the MINUS operator is guaranteed to be empty as well.
			crdValue = 0;
			crdQuality = Quality.ACCURATE;
		}
		else if ( rhsCrd.getValue() == 0 && rhsCrd.getQuality() == Quality.ACCURATE ) {
			// If the second input plan is guaranteed to produce an empty result,
			// then the result of the MINUS operator is guaranteed to be the same as the first input plan.
			crdValue = lhsCrd.getValue();
			crdQuality = lhsCrd.getQuality();
		}
		else {
			// Else, estimate the cardinality of the MINUS result by subtracting the estimated cardinality
			// of the second input plan from the estimated cardinality of the first input plan.
			// This is a very crude estimate that is likely to be a significant underestimation in many cases,
			// but it is not clear how to do better without considering the actual query patterns and data statistics.
			crdValue = Math.max(lhsCrd.getValue() - rhsCrd.getValue(), 0);
			crdQuality = QueryPlanProperty.getReducedQuality( lhsCrd.getQuality() );
		}

		final int maxValue = lhsMaxCrd.getValue();
		final Quality maxQuality = lhsMaxCrd.getQuality();

		final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
		qpInfo.addProperty( QueryPlanProperty.cardinality(crdValue, crdQuality) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(maxValue, maxQuality) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.MIN_OR_MAX_POSSIBLE) );
	}

	public void addCardinalityForUnion() {
		int crdValue = 0;
		int maxValue = 0;
		int minValue = 0;
		Quality crdQuality = Quality.ACCURATE;
		Quality maxQuality = Quality.ACCURATE;
		Quality minQuality = Quality.ACCURATE;

		boolean allEmpty = true;
		for ( int x = 0; x < currentSubPlan.numberOfSubPlans(); x++ ) {
			final QueryPlanningInfo qpInfoSubPlanX = currentSubPlan.getSubPlan(x).getQueryPlanningInfo();
			final QueryPlanProperty crdX = qpInfoSubPlanX.getProperty(CARDINALITY);
			final QueryPlanProperty maxX = qpInfoSubPlanX.getProperty(MAX_CARDINALITY);
			final QueryPlanProperty minX = qpInfoSubPlanX.getProperty(MIN_CARDINALITY);

			if ( crdX.getValue() != 0 || crdX.getQuality() != Quality.ACCURATE ) {
				allEmpty = false;
			}

			crdValue = addWithoutExceedingMax( crdValue, crdX.getValue() );
			maxValue = addWithoutExceedingMax( maxValue, maxX.getValue() );
			minValue = addWithoutExceedingMax( minValue, minX.getValue() );

			crdQuality = pickWorse( crdQuality, crdX.getQuality() );
			maxQuality = pickWorse( maxQuality, maxX.getQuality() );
			minQuality = pickWorse( minQuality, minX.getQuality() );
		}

		// If all of the subplans under the UNION is guaranteed to produce an empty result,
		// then the result of the UNION operator is guaranteed to be empty as well.
		if ( allEmpty ) {
			final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
			qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
			qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
			qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );
			return;
		}

		final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
		qpInfo.addProperty( QueryPlanProperty.cardinality(crdValue, crdQuality) );
		qpInfo.addProperty( QueryPlanProperty.maxCardinality(maxValue, maxQuality) );
		qpInfo.addProperty( QueryPlanProperty.minCardinality(minValue, minQuality) );
	}

	public void addCardinalityForInnerJoin() {
		// As the estimated cardinality, we simply use the maximum
		// of the estimated cardinality of every input plan. This
		// is a very inaccurate estimate! (The only exception are
		// cases in which one of the input plans has a cardinality
		// estimate of 0 that is ACCURATE, which means that the join
		// result is guaranteed to be empty and, thus, we copy the
		// cardinality of 0 over as the join cardinality.)
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

			// Check first whether we have a case in which the subplan is
			// guaranteed to produce an empty result. If so, we can stop
			// here because the join cardinality is then guaranteed to be
			// zero as well.
			if ( crdX.getValue() == 0 && crdX.getQuality() == Quality.ACCURATE ) {
				final QueryPlanningInfo qpInfo = currentSubPlan.getQueryPlanningInfo();
				qpInfo.addProperty( QueryPlanProperty.cardinality(0, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.maxCardinality(0, Quality.ACCURATE) );
				qpInfo.addProperty( QueryPlanProperty.minCardinality(0, Quality.ACCURATE) );
				return;
			}

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
