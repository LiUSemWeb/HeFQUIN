package se.liu.ida.hefquin.jenaintegration.sparql.engine.main;

import java.util.Iterator;

import org.apache.jena.query.QueryExecException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.algebra.table.Table1;
import org.apache.jena.sparql.algebra.walker.WalkerVisitorSkipService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply;
import org.apache.jena.sparql.engine.main.OpExecutor;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
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
		if ( isSupportedOp(opBGP) ) {
			return executeSupportedOp( opBGP, input );
		}
		else {
			return super.execute(opBGP, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpSequence opSequence, final QueryIterator input ) {
		if ( isSupportedOp(opSequence) ) {
			return executeSupportedOp( opSequence, input );
		}
		else {
			return super.execute(opSequence, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpJoin opJoin, final QueryIterator input ) {
		if ( isSupportedOp(opJoin) ) {
			return executeSupportedOp( opJoin, input );
		}
		else {
			return super.execute(opJoin, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpLeftJoin opLeftJoin, final QueryIterator input ) {
		if ( isSupportedOp(opLeftJoin) ) {
			return executeSupportedOp( opLeftJoin, input );
		}
		else {
			return super.execute(opLeftJoin, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpUnion opUnion, final QueryIterator input ) {
		if ( isSupportedOp(opUnion) ) {
			return executeSupportedOp( opUnion, input );
		}
		else {
			return super.execute(opUnion, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpConditional opConditional, final QueryIterator input ) {
		if ( isSupportedOp(opConditional) ) {
			return executeSupportedOp( opConditional, input );
		}
		else {
			return super.execute(opConditional, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpExtend opExtend, final QueryIterator input ) {
		if ( isSupportedOp(opExtend) ) {
			return executeSupportedOp( opExtend, input );
		}
		else {
			return super.execute(opExtend, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpFilter opFilter, final QueryIterator input ) {
		if ( isSupportedOp(opFilter) ) {
			return executeSupportedOp( opFilter, input );
		}
		else {
			return super.execute(opFilter, input);
		}
	}

	@Override
	protected QueryIterator execute( final OpService opService, final QueryIterator input ) {
		if ( isSupportedOp(opService) ) {
			return executeSupportedOp( opService, input );
		}
		else {
			throw new UnsupportedOperationException();
		}
	}


	protected boolean isSupportedOp( final Op op ) {
		final UnsupportedOpFinder f = new UnsupportedOpFinder();
		new WalkerVisitorSkipService(f, null, null, null).walk(op);
		final boolean unsupportedOpFound = f.unsupportedOpFound();
		return ! unsupportedOpFound;
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

			final MaterializingQueryResultSinkImpl sink = new MaterializingQueryResultSinkImpl();
			final QueryProcessingStatsAndExceptions statsAndExceptions;

			try {
				statsAndExceptions = qProc.processQuery( new GenericSPARQLGraphPatternImpl2(opForStage), sink );
			}
			catch ( final QueryProcException ex ) {
				throw new QueryExecException("Processing the query operator using HeFQUIN failed.", ex);
			}

			execCxt.getContext().set( HeFQUINEngineConstants.sysQProcStatsAndExceptions,
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


	protected static class UnsupportedOpFinder extends OpVisitorBase
	{
		protected Op unsupportedOp = null;

		public boolean unsupportedOpFound() { return unsupportedOp != null; }

		public Op getUnsupportedOp() { return unsupportedOp; }

		@Override public void visit(OpBGP op)          {}

		@Override public void visit(OpQuadPattern op)  { unsupportedOp = op; }

		@Override public void visit(OpQuadBlock op)    { unsupportedOp = op; }

		@Override public void visit(OpTriple op)       { unsupportedOp = op; }

		@Override public void visit(OpQuad op)         { unsupportedOp = op; }

		@Override public void visit(OpPath op)         { unsupportedOp = op; }

		@Override public void visit(OpProcedure op)    { unsupportedOp = op; }

		@Override public void visit(OpPropFunc op)     { unsupportedOp = op; }

		@Override public void visit(OpJoin op)         {} // supported

		@Override public void visit(OpSequence op)     {} // supported

		@Override public void visit(OpDisjunction op)  { unsupportedOp = op; }

		@Override public void visit(OpLeftJoin op)     {} // supported

		@Override public void visit(OpConditional op)  {} // supported

		@Override public void visit(OpMinus op)        { unsupportedOp = op; }

		@Override public void visit(OpUnion op)        {} // supported

		@Override public void visit(OpFilter op)       {} // supported

		@Override public void visit(OpGraph op)        { unsupportedOp = op; }

		@Override public void visit(OpService op)      {} // supported

		@Override public void visit(OpDatasetNames op) { unsupportedOp = op; }

		@Override public void visit(OpTable op)        {} // supported

		@Override public void visit(OpExt op)          { unsupportedOp = op; }

		@Override public void visit(OpNull op)         { unsupportedOp = op; }

		@Override public void visit(OpLabel op)        { unsupportedOp = op; }

		@Override public void visit(OpAssign op)       { unsupportedOp = op; }

		@Override public void visit(OpExtend op)       {} // supported

		@Override public void visit(OpUnfold op)       { unsupportedOp = op; }

		@Override public void visit(OpList op)         { unsupportedOp = op; }

		@Override public void visit(OpOrder op)        { unsupportedOp = op; }

		@Override public void visit(OpProject op)      { unsupportedOp = op; }

		@Override public void visit(OpDistinct op)     { unsupportedOp = op; }

		@Override public void visit(OpReduced op)      { unsupportedOp = op; }

		@Override public void visit(OpSlice op)        { unsupportedOp = op; }

		@Override public void visit(OpGroup op)        { unsupportedOp = op; }

		@Override public void visit(OpTopN op)         { unsupportedOp = op; }
	}

}
