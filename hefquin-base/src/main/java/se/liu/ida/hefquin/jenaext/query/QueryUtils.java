package se.liu.ida.hefquin.jenaext.query;

import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;

/**
 * HeFQUIN-specific helper functionality related to Jena's {@link Query} class.
 */
public class QueryUtils
{
	public static ExpectedVariables determineExpectedVariables( final Query q ) {
		final Op pattern = Algebra.compile( q.getQueryPattern() );
		final Set<Var> certainVars = OpVars.fixedVars(pattern);
		final Set<Var> possibleVars = OpVars.visibleVars(pattern);
		possibleVars.removeAll(certainVars);

		if ( ! q.isQueryResultStar() ) {
			certainVars.retainAll( q.getProjectVars() );
			possibleVars.retainAll( q.getProjectVars() );
		}

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

}
