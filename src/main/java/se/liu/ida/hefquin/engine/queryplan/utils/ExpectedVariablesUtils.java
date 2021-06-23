package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class ExpectedVariablesUtils
{
	/**
	 * Returns a set of all the certain variables in all the given
	 * {@link ExpectedVariables} objects. Returns null if no such
	 * object is given.
	 */
	public static Set<Var> unionOfCertainVariables( final ExpectedVariables ... e ) {
		if ( e.length == 0 ) {
			return null;
		}

		final Set<Var> result = new HashSet<>( e[0].getCertainVariables() );

		for ( int i = 1; i < e.length; ++i ) {
			result.addAll( e[i].getCertainVariables() );
		}

		return result;
	}

	/**
	 * Returns a set of all the possible variables in all the given
	 * {@link ExpectedVariables} objects. Returns null if no such
	 * object is given.
	 */
	public static Set<Var> unionOfPossibleVariables( final ExpectedVariables ... e ) {
		if ( e.length == 0 ) {
			return null;
		}

		final Set<Var> result = new HashSet<>( e[0].getPossibleVariables() );

		for ( int i = 1; i < e.length; ++i ) {
			result.addAll( e[i].getPossibleVariables() );
		}

		return result;
	}

	/**
	 * Returns a set of all the variables (certain and possible) in all
	 * the given {@link ExpectedVariables} objects. Returns null if no
	 * such object is given.
	 */
	public static Set<Var> unionOfAllVariables( final ExpectedVariables ... e ) {
		if ( e.length == 0 ) {
			return null;
		}

		final Set<Var> result = new HashSet<>( e[0].getCertainVariables() );
		result.addAll( e[0].getPossibleVariables() );

		for ( int i = 1; i < e.length; ++i ) {
			result.addAll( e[i].getCertainVariables() );
			result.addAll( e[i].getPossibleVariables() );
		}

		return result;
	}

	/**
	 * Returns an intersection of the sets of certain variables in all
	 * the given {@link ExpectedVariables} objects. Returns null if no
	 * such object is given.
	 */
	public static Set<Var> intersectionOfCertainVariables( final ExpectedVariables ... e ) {
		if ( e.length == 0 ) {
			return null;
		}

		final Set<Var> result = new HashSet<>( e[0].getCertainVariables() );

		for ( int i = 1; i < e.length; ++i ) {
			result.retainAll( e[i].getCertainVariables() );
		}

		return result;
	}

	/**
	 * Returns an intersection of the sets of possible variables in all
	 * the given {@link ExpectedVariables} objects. Returns null if no
	 * such object is given.
	 */
	public static Set<Var> intersectionOfPossibleVariables( final ExpectedVariables ... e ) {
		if ( e.length == 0 ) {
			return null;
		}

		final Set<Var> result = new HashSet<>( e[0].getPossibleVariables() );

		for ( int i = 1; i < e.length; ++i ) {
			result.retainAll( e[i].getPossibleVariables() );
		}

		return result;
	}

	/**
	 * Returns an intersection of the sets of all variables (certain and
	 * possible) in all the given {@link ExpectedVariables} objects.
	 * Returns null if no such object is given.
	 */
	public static Set<Var> intersectionOfAllVariables( final ExpectedVariables ... e ) {
		if ( e.length == 0 ) {
			return null;
		}

		final Set<Var> result = new HashSet<>( e[0].getCertainVariables() );
		result.addAll( e[0].getPossibleVariables() );

		for ( int i = 1; i < e.length; ++i ) {
			final Set<Var> allVarsInCurrentObject = unionOfAllVariables( e[i] );
			result.retainAll( allVarsInCurrentObject );
		}

		return result;
	}

}
