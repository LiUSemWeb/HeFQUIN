package se.liu.ida.hefquin.jenaintegration.sparql.engine.main;

import java.util.Iterator;

import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.algebra.table.Table1;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.util.Context;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.QueryProcException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.queryproc.impl.MaterializingQueryResultSinkImpl;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINEngineConstants;

public class OpExecutorHeFQUIN extends OpExecutor
{
	protected final QueryProcessor qProc;

	public OpExecutorHeFQUIN( final QueryProcessor qProc, final ExecutionContext execCxt ) {
		super(execCxt);

		assert qProc != null;
		this.qProc= qProc;
	}

	@Override
	protected QueryIterator exec(Op op, QueryIterator input) {
		return super.exec(op, input);
	}

	@Override
	protected QueryIterator execute( final OpBGP opBGP, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opBGP) ) {
			return executeSupportedOp( opBGP, input );
		}
		else {
			return super.execute(opBGP, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpSequence opSequence, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opSequence) ) {
			return executeSupportedOp( opSequence, input );
		}
		else {
			return super.execute(opSequence, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpJoin opJoin, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opJoin) ) {
			return executeSupportedOp( opJoin, input );
		}
		else {
			return super.execute(opJoin, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpLeftJoin opLeftJoin, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opLeftJoin) ) {
			return executeSupportedOp( opLeftJoin, input );
		}
		else {
			return super.execute(opLeftJoin, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpUnion opUnion, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opUnion) ) {
			return executeSupportedOp( opUnion, input );
		}
		else {
			return super.execute(opUnion, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpConditional opConditional, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opConditional) ) {
			return executeSupportedOp( opConditional, input );
		}
		else {
			return super.execute(opConditional, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpExtend opExtend, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opExtend) ) {
			return executeSupportedOp( opExtend, input );
		}
		else {
			return super.execute(opExtend, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpUnfold opUnfold, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opUnfold) ) {
			return executeSupportedOp( opUnfold, input );
		}
		else {
			return super.execute(opUnfold, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpFilter opFilter, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opFilter) ) {
			return executeSupportedOp( opFilter, input );
		}
		else {
			return super.execute(opFilter, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpService opService, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opService) ) {
			return executeSupportedOp( opService, input );
		}
		else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	protected QueryIterator execute( final OpDistinct opDistinct, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opDistinct) ) {
			return executeSupportedOp( opDistinct, input );
		}
		else {
			return super.execute(opDistinct, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpProject opProject, final QueryIterator input ) {
		if ( HeFQUINQuerySupportChecker.isSupported(opProject) ) {
			return executeSupportedOp( opProject, input );
		}
		else {
			return super.execute(opProject, input);
		}
	}

	protected QueryIterator executeSupportedOp( final Op op, final QueryIterator input ) {
		return new MainQueryIterator( op, input );
	}


	protected class MainQueryIterator extends QueryIterRepeatApply
	{
		protected final Op op;

		public MainQueryIterator( final Op op, final QueryIterator input ) {
			super(input, execCxt);

			assert op != null;
			this.op = op;
		}

		@Override
		protected QueryIterator nextStage( final Binding binding ) {
			final Op opForStage;
			if ( binding.isEmpty() ) {
				opForStage = op;
			}
			else {
				final OpTable opTable = OpTable.create( new Table1(binding) );
				opForStage = OpJoin.create(opTable, op);
			}

			final Query patternForStage = new GenericSPARQLGraphPatternImpl2(opForStage);

			final MaterializingQueryResultSinkImpl sink = new MaterializingQueryResultSinkImpl();

			final Context arqCxt = execCxt.getContext();
			final QueryProcContext ctx = arqCxt.get( HeFQUINEngineConstants.sysQueryProcContext );

			final QueryProcessingStatsAndExceptions statsAndExceptions;
			try {
				statsAndExceptions = qProc.processQuery(patternForStage, sink, ctx);
			}
			catch ( final QueryProcException ex ) {
				throw new QueryExecException("Processing the query using HeFQUIN caused an exception with the following message: " + ex.getMessage(), ex);
			}

			arqCxt.set( HeFQUINEngineConstants.sysQProcStatsAndExceptions,
			            statsAndExceptions );

			return new WrappingQueryIterator( sink.getSolMapsIter() );
		}
	}


	protected class WrappingQueryIterator extends QueryIter
	{
		protected final Iterator<SolutionMapping> it;

		public WrappingQueryIterator( final Iterator<SolutionMapping> it ) {
			super(execCxt);
			this.it = it;
		}

		@Override
		protected boolean hasNextBinding() { return it.hasNext(); }

		@Override
		protected Binding moveToNextBinding() { return it.next().asJenaBinding(); }

		@Override
		protected void closeIterator() {} // nothing to do here

		@Override
		protected void requestCancel() {} // nothing to do here
	}

}
