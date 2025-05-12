package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * A base class for all variations of the bind join algorithm that use
 * some form of SPARQL requests.
 */
public abstract class BaseForExecOpBindJoinSPARQL extends BaseForExecOpBindJoinWithRequestOps<SPARQLGraphPattern, SPARQLEndpoint>
{
	protected final List<Var> varsInSubQuery;

	public BaseForExecOpBindJoinSPARQL( final SPARQLGraphPattern p,
	                                    final SPARQLEndpoint fm,
	                                    final boolean useOuterJoinSemantics,
	                                    final int batchSize,
	                                    final boolean collectExceptions ) {
		super(p, fm, useOuterJoinSemantics, p.getAllMentionedVariables(), batchSize, collectExceptions);
		varsInSubQuery = new ArrayList<>(varsInPatternForFM);
	}

	// Change visibility to public in order to be able to call this function
	// directly from ExecOpBindJoinSPARQLwithVALUESorFILTER
	@Override
	public void _processBatch( final List<SolutionMapping> batchOfSolMaps,
	                           final IntermediateResultElementSink sink,
	                           final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		super._processBatch(batchOfSolMaps, sink, execCxt);
	}

}
