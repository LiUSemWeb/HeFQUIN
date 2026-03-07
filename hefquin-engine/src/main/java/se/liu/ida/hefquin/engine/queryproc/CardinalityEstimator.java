package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public interface CardinalityEstimator
{
	/**
	 * Annotates each of the given plans (including, recursively, each of
	 * their subplans) with estimates regarding their result cardinalities.
	 * <p>
	 * More specifically, this method adds three query plan properties to
	 * the {@link QueryPlanningInfo} of each of the plans:
	 * <ul>
	 * <li>{@link QueryPlanProperty#CARDINALITY}</li>
	 * <li>{@link QueryPlanProperty#MAX_CARDINALITY}</li>
	 * <li>{@link QueryPlanProperty#MIN_CARDINALITY}</li>
	 * </ul>
	 *
	 * @param plans - the plans to be annotated with cardinality estimates
	 * @see {@link LogicalPlan#getQueryPlanningInfo()}
	 */
	void addCardinalities( LogicalPlan ... plans );

	/**
	 * Annotates each of the given plans (including, recursively, each of
	 * their subplans) with estimates regarding their result cardinalities.
	 * <p>
	 * More specifically, this method adds three query plan properties to
	 * the {@link QueryPlanningInfo} of each of the plans:
	 * <ul>
	 * <li>{@link QueryPlanProperty#CARDINALITY}</li>
	 * <li>{@link QueryPlanProperty#MAX_CARDINALITY}</li>
	 * <li>{@link QueryPlanProperty#MIN_CARDINALITY}</li>
	 * </ul>
	 *
	 * @param plans - the plans to be annotated with cardinality estimates
	 * @see {@link PhysicalPlan#getQueryPlanningInfo()}
	 */
	void addCardinalities( PhysicalPlan ... plans );
}
