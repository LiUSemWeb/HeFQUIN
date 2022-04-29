package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGroupPatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpGenericBindJoinWithRequestOps.MyIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBindJoinSPARQLwithFILTERandTranslation extends ExecOpBindJoinSPARQLwithFILTER{
	
	public ExecOpBindJoinSPARQLwithFILTERandTranslation(final SPARQLGraphPattern query, SPARQLEndpoint fm) {
		super(query, fm);
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> solMaps ) {
		
		final Set<SolutionMapping> solMapsTranslation = new HashSet<>();
		for (final SolutionMapping sm : solMaps) {
			solMapsTranslation.addAll(fm.getVocabularyMapping().translateSolutionMapping(sm, false));
		}
		
		final Op op = createFilter(solMapsTranslation);
		if ( op == null ) {
			return null;
		}
		
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl2(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		return new ExecOpRequestSPARQL(request, fm);
	}	
	
	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt)
			throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableRequestOperator( input.getSolutionMappings() );
		if ( reqOp != null ) {
			final IntermediateResultElementSink mySink = new MyIntermediateResultElementSinkWithTranslation(sink, input, fm.getVocabularyMapping());
			try {
				reqOp.execute(mySink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
			}
		}
	}
	
}
