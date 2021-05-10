package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Set;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBindJoinSPARQLwithVALUES extends ExecOpGenericBindJoin<TriplePattern,SPARQLEndpoint>{

	public ExecOpBindJoinSPARQLwithVALUES(TriplePattern query, SPARQLEndpoint fm) {
		super(query, fm);
	}

	@Override
	protected Iterable<? extends SolutionMapping> fetchSolutionMappings(Set<SolutionMapping> solMaps,
			ExecutionContext execCxt) {
		// TODO Auto-generated method stub
		return null;
	}

}
