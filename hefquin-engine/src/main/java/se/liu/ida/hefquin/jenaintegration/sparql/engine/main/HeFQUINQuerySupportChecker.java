package se.liu.ida.hefquin.jenaintegration.sparql.engine.main;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnfold;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.walker.WalkerVisitorSkipService;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;

public class HeFQUINQuerySupportChecker
{
	public static boolean isSupported( final Query query ) {
		if ( query.hasGroupBy() )
			return false;

		if ( query.hasHaving() )
			return false;

		if ( query.hasOrderBy() )
			return false;

		if ( query.hasLimit() )
			return false;

		if ( query.hasOffset() )
			return false;

		if ( query.hasDatasetDescription() )
			return false;

		if ( query.hasAggregators() )
			return false;

		if ( ! isSupportedProjection(query.getProject()) )
			return false;

		return isSupported( Algebra.compile(query) );
	}

	public static boolean isSupported( final Op op ) {
		final UnsupportedOpFinder f = new UnsupportedOpFinder();
		new WalkerVisitorSkipService(f, null, null, null).walk(op);
		final boolean unsupportedOpFound = f.unsupportedOpFound();
		return ! unsupportedOpFound;
	}

	protected static boolean isSupportedProjection( final VarExprList project ) {
		for ( final Var var : project.getVars() ) {
			if ( project.getExpr(var) != null )
				return false;
		}

		return true;
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

		@Override public void visit(OpMinus op)        {} // supported

		@Override public void visit(OpUnion op)        {} // supported

		@Override public void visit(OpFilter op)       {} // supported

		@Override public void visit(OpGraph op)        { unsupportedOp = op; }

		@Override public void visit(OpService op)      {
			// HeFQUIN currently only supports SERVICE with a fixed endpoint
			if ( op.getService().isVariable() )
				unsupportedOp = op;
		}

		@Override public void visit(OpDatasetNames op) { unsupportedOp = op; }

		@Override public void visit(OpTable op)        {} // supported

		@Override public void visit(OpExt op)          { unsupportedOp = op; }

		@Override public void visit(OpNull op)         { unsupportedOp = op; }

		@Override public void visit(OpLabel op)        { unsupportedOp = op; }

		@Override public void visit(OpAssign op)       { unsupportedOp = op; }

		@Override public void visit(OpExtend op)       {} // supported

		@Override public void visit(OpUnfold op)       {} // supported

		@Override public void visit(OpList op)         { unsupportedOp = op; }

		@Override public void visit(OpOrder op)        { unsupportedOp = op; }

		@Override public void visit(OpProject op)      {} // supported

		@Override public void visit(OpDistinct op)     {} // supported

		@Override public void visit(OpReduced op)      { unsupportedOp = op; }

		@Override public void visit(OpSlice op)        { unsupportedOp = op; }

		@Override public void visit(OpGroup op)        { unsupportedOp = op; }

		@Override public void visit(OpTopN op)         { unsupportedOp = op; }
	}

}
