package se.liu.ida.hefquin.engine.queryplan.physical;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;

public class TestUtils
{
	protected static ExpectedVariables getExpectedVariables( final List<String> certainVars,
	                                                         final List<String> possibleVars )
	{
		return new ExpectedVariables() {

			@Override
			public Set<Var> getCertainVariables() {
				return certainVars.stream().map(Var::alloc).collect(Collectors.toSet());
			}

			@Override
			public Set<Var> getPossibleVariables() {
				return possibleVars.stream().map(Var::alloc).collect(Collectors.toSet());
			}

		};
	}

	protected static VocabularyMapping getVocabularyMappingForTest() {
		return new VocabularyMapping() {

			@Override
			public SPARQLGraphPattern translateTriplePattern( TriplePattern tp ) {
				throw new UnsupportedOperationException("Unimplemented method 'translateTriplePattern'");
			}

			@Override
			public Set<SolutionMapping> translateSolutionMapping( SolutionMapping sm ) {
				throw new UnsupportedOperationException("Unimplemented method 'translateSolutionMapping'");
			}

			@Override
			public Set<SolutionMapping> translateSolutionMappingFromGlobal( SolutionMapping sm ) {
				throw new UnsupportedOperationException("Unimplemented method 'translateSolutionMappingFromGlobal'");
			}

			@Override
			public boolean isEquivalenceOnly() {
				throw new UnsupportedOperationException("Unimplemented method 'isEquivalenceOnly'");
			}
		};
	}

}
