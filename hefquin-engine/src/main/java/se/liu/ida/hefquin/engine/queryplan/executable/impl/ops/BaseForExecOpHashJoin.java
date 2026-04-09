package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.base.datastructures.impl.SolutionMappingsHashTable;
import se.liu.ida.hefquin.base.datastructures.impl.SolutionMappingsHashTableBasedOnOneVar;
import se.liu.ida.hefquin.base.datastructures.impl.SolutionMappingsHashTableBasedOnTwoVars;
import se.liu.ida.hefquin.base.datastructures.impl.SolutionMappingsIndexWithPostMatching;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;

public abstract class BaseForExecOpHashJoin extends BinaryExecutableOpBase
{
	protected final SolutionMappingsIndex index;

	protected BaseForExecOpHashJoin( final boolean mayReduce,
	                                 final ExpectedVariables inputVars1,
	                                 final ExpectedVariables inputVars2,
	                                 final boolean collectExceptions,
	                                 final QueryPlanningInfo qpInfo ) {
		super(mayReduce, collectExceptions, qpInfo);

		// determine the certain join variables
		final Set<Var> certainJoinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVars1, inputVars2);

		// set up the core part of the index first;
		// it is built on the certain join variables
		final SolutionMappingsIndex _index;
		if ( certainJoinVars.size() == 1 ) {
			final Var joinVar = certainJoinVars.iterator().next();
			_index = new SolutionMappingsHashTableBasedOnOneVar(joinVar);
		}
		else if ( certainJoinVars.size() == 2 ) {
			final Iterator<Var> liVar = certainJoinVars.iterator();
			final Var joinVar1 = liVar.next();
			final Var joinVar2 = liVar.next();
			_index = new SolutionMappingsHashTableBasedOnTwoVars(joinVar1, joinVar2);
		}
		else {
			_index = new SolutionMappingsHashTable(certainJoinVars);
		}

		// Check whether there are other variables that may be relevant for
		// the join and, if so, set up the index to use post-matching.
		final Set<Var> potentialJoinVars = ExpectedVariablesUtils.intersectionOfAllVariables(inputVars1, inputVars2);
		if ( ! potentialJoinVars.equals(certainJoinVars) )
			index = new SolutionMappingsIndexWithPostMatching(_index);
		else
			index = _index;
	}

}
