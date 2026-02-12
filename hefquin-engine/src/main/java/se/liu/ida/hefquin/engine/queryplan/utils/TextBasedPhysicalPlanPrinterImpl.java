package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.utils.PlanPrinter;
import se.liu.ida.hefquin.base.utils.PlanPrinter.PrintablePlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithBinaryRoot;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithUnaryRoot;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

/**
 * Internally, the functionality of this class is implemented based on
 * a {@link PhysicalPlanVisitor}, which makes sure that we get a compiler
 * error whenever we add a new type of physical operator but forget to
 * extend this class here to cover that new operator.
 */
public class TextBasedPhysicalPlanPrinterImpl extends BaseForTextBasedPlanPrinters  implements PhysicalPlanPrinter
{
	public static final MyPropertiesExtractor pe = new MyPropertiesExtractor();

	@Override
	public void print( final PhysicalPlan plan, final PrintStream out ) {
		final ExtPrintablePlan pp = createPrintablePlan(plan);
		PlanPrinter.print(pp, out);
		printFullStringsForGraphPatterns(pp, out);
		out.flush();
	}

	public ExtPrintablePlan createPrintablePlan( final PhysicalPlan p ) {
		pe.graphPattern = null;
		pe.fullStringForGraphPattern = null;
		pe.rootOpString = null;
		pe.props = new ArrayList<>();

		p.getRootOperator().visit(pe);

		final SPARQLGraphPattern graphPattern  = pe.graphPattern;
		final String fullStringForGraphPattern = pe.fullStringForGraphPattern;
		final String rootOpString              = pe.rootOpString;
		final List<String> rootOpProps         = pe.props;

		addPropStrings( p.getExpectedVariables(), rootOpProps );
		addPropStrings( p.getQueryPlanningInfo(), rootOpProps );

		final List<PrintablePlan> subPlans = createPrintableSubPlans(p);

		return new ExtPrintablePlan( rootOpString, rootOpProps, subPlans,
		                             graphPattern, fullStringForGraphPattern );
	}

	public List<PrintablePlan> createPrintableSubPlans( final PhysicalPlan p ) {
		if ( p instanceof PhysicalPlanWithNullaryRoot ) {
			return null;
		}
		else if ( p instanceof PhysicalPlanWithUnaryRoot u ) {
			final PrintablePlan pp = createPrintablePlan( u.getSubPlan() );
			return List.of(pp);
		}
		else if ( p instanceof PhysicalPlanWithBinaryRoot b ) {
			final PrintablePlan pp1 = createPrintablePlan( b.getSubPlan1() );
			final PrintablePlan pp2 = createPrintablePlan( b.getSubPlan2() );
			return List.of(pp1, pp2);
		}
		else if ( p instanceof PhysicalPlanWithNaryRoot n ) {
			List<PrintablePlan> subPlans = new ArrayList<>( n.numberOfSubPlans() );
			final Iterator<PhysicalPlan> it = n.getSubPlans();
			while ( it.hasNext() ) {
				final PrintablePlan pp = createPrintablePlan( it.next() );
				subPlans.add(pp);
			}
			return subPlans;
		}
		else {
			throw new IllegalArgumentException( p.getClass().getName() );
		}
	}


	public static class MyPropertiesExtractor
			extends TextBasedLogicalPlanPrinterImpl.MyPropertiesExtractor
			implements PhysicalPlanVisitor {
		/**
		 * To be used as the root-operator string in
		 * an {@link ExtPrintablePlan} for the most
		 * recently visited operator.
		 */
		public String rootOpString = null;

		@Override
		public void visit( final PhysicalOpRequest<?, ?> op ) {
			rootOpString = "req (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpFixedSolMap op ) {
			rootOpString = "sm (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpBindJoinBRTPF op ) {
			rootOpString = "bind join for brTPF (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpBindJoinSPARQL op ) {
			rootOpString = "bind join for SPARQL (" + op.getID() + ")";
			props.add( "type: " + op.getType() );
			props.add( "parallel version: " + op.usesParallelVersion() );
			props.add( "batch size: " + op.getBatchSize() );
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpIndexNestedLoopsJoin op ) {
			rootOpString = "indexNLJ (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpLookupJoinViaWrapper op ) {
			rootOpString = "wrapper-based lookup join (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpHashJoin op ) {
			rootOpString = "hash join (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpSymmetricHashJoin op ) {
			rootOpString = "SHJ (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpNaiveNestedLoopsJoin op ) {
			rootOpString = "naive NLJ (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpHashRJoin op ) {
			rootOpString = "right-outer hash join (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpParallelMultiLeftJoin op ) {
			rootOpString = "parallel multiway left-outer join (" + op.getID() + ")";
		}

		@Override
		public void visit( final PhysicalOpBinaryUnion op ) {
			rootOpString = "union (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpMultiwayUnion op ) {
			rootOpString = "multiway union (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpFilter op ) {
			rootOpString = "filter (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpBind op ) {
			rootOpString = "bind (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpLocalToGlobal op ) {
			rootOpString = "l2g (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		@Override
		public void visit( final PhysicalOpGlobalToLocal op ) {
			rootOpString = "g2l (" + op.getID() + ")";
			record( op.getLogicalOperator() );
		}

		protected void record( final LogicalOperator lop ) {
			lop.visit(snc);
			props.add( "lop: " + snc.name + " (" + lop.getID()  + ")" );
			lop.visit(this);
		}
	}

}
