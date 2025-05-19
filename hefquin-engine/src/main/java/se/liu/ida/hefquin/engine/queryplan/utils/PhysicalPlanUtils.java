package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public class PhysicalPlanUtils
{
	/**
	 * Returns a set of all the certain variables in all the given
	 * plans. Returns null if no plan is given.
	 */
	public static Set<Var> unionOfCertainVariables( final PhysicalPlan plans ) {
		final ExpectedVariables[] array = getExpectedVariables(plans);
		return ExpectedVariablesUtils.unionOfCertainVariables(array);
	}

	/**
	 * Returns a set of all the possible variables in all the given
	 * plans. Returns null if no plan is given.
	 */
	public static Set<Var> unionOfPossibleVariables( final PhysicalPlan ... plans ) {
		final ExpectedVariables[] array = getExpectedVariables(plans);
		return ExpectedVariablesUtils.unionOfPossibleVariables(array);
	}

	/**
	 * Returns a set of all the variables (certain and possible) in all
	 * the given plans. Returns null if no plan is given.
	 */
	public static Set<Var> unionOfAllVariables( final PhysicalPlan ... plans ) {
		final ExpectedVariables[] array = getExpectedVariables(plans);
		return ExpectedVariablesUtils.unionOfAllVariables(array);
	}

	/**
	 * Returns an intersection of the sets of certain variables in all
	 * the given plans. Returns null if no plan is given.
	 */
	public static Set<Var> intersectionOfCertainVariables( final PhysicalPlan ... plans ) {
		final ExpectedVariables[] array = getExpectedVariables(plans);
		return ExpectedVariablesUtils.intersectionOfCertainVariables(array);
	}

	/**
	 * Returns an intersection of the sets of possible variables in all
	 * the given plans. Returns null if no plan is given.
	 */
	public static Set<Var> intersectionOfPossibleVariables( final PhysicalPlan ... plans ) {
		final ExpectedVariables[] array = getExpectedVariables(plans);
		return ExpectedVariablesUtils.intersectionOfPossibleVariables(array);
	}

	/**
	 * Returns an intersection of the sets of all variables (certain and
	 * possible) in all the given plans. Returns null if no plan is given.
	 */
	public static Set<Var> intersectionOfAllVariables( final PhysicalPlan ... plans ) {
		final ExpectedVariables[] array = getExpectedVariables(plans);
		return ExpectedVariablesUtils.intersectionOfAllVariables(array);
	}

	/**
	 * Returns an array of the {@link ExpectedVariables} objects of all
	 * given physical plans, in the order in which the plans are given.
	 */
	public static ExpectedVariables[] getExpectedVariables( final PhysicalPlan ... plans ) {
		final ExpectedVariables[] e = new ExpectedVariables[plans.length];
		for ( int i = 0; i < plans.length; ++i ) {
			e[i] = plans[i].getExpectedVariables();
		}
		return e;
	}

}
