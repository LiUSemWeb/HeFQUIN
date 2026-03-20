package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * To be used for UNFOLD clauses.
 *
 * See: <a href="https://awslabs.github.io/SPARQL-CDTs/spec/latest.html#description-of-unfold">https://awslabs.github.io/SPARQL-CDTs/spec/latest.html#description-of-unfold</a>
 */
public class ExecOpUnfold extends UnaryExecutableOpBaseWithoutBlocking
{
	protected final Expr expr;
	protected final Var var1;
	protected final Var var2;

	private long numberOfOutputMappingsProduced = 0L;
	private int numberOfExprEvalErrors = 0;
	private int numberOfUnfoldedCDTs = 0;
	private int minSizeOfUnfoldedCDTs = 0;
	private int maxSizeOfUnfoldedCDTs = 0;
	private long sumOfCDTSizes = 0L;

	public ExecOpUnfold( final Expr expr, final Var var1, final Var var2,
	                     final boolean collectExceptions,
	                     final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		assert expr != null;
		assert var1 != null;

		this.expr = expr;
		this.var1 = var1;
		this.var2 = var2;
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			 throws ExecOpExecutionException {
		final NodeValue nv;
		try {
			nv = ExprUtils.eval( expr, inputSolMap.asJenaBinding() );
		}
		catch ( final ExprEvalException ex ) {
			// If the expression failed to evaluate for a given
			// solution mapping, then the expected result is that
			// this solution mapping is returned as is.
			sink.send(inputSolMap);
			numberOfOutputMappingsProduced++;
			numberOfExprEvalErrors++;
			return;
		}

		final Node n = nv.asNode();
		if ( n.isLiteral() ) {
			final LiteralLabel lit = n.getLiteral();
			final String dtURI = lit.getDatatypeURI();

			if ( CompositeDatatypeList.uri.equals(dtURI)
			     && lit.isWellFormed() ) {
				numberOfUnfoldedCDTs++;
				unfoldList(lit, inputSolMap, sink);
				return;
			}

			if ( CompositeDatatypeMap.uri.equals(dtURI)
			     && lit.isWellFormed() ) {
				numberOfUnfoldedCDTs++;
				unfoldMap(lit, inputSolMap, sink);
				return;
			}
		}

		sink.send(inputSolMap);
		numberOfOutputMappingsProduced++;
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) {
		// nothing to be done here
	}

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfOutputMappingsProduced = 0L;
		numberOfExprEvalErrors = 0;
		numberOfUnfoldedCDTs = 0;
		minSizeOfUnfoldedCDTs = 0;
		maxSizeOfUnfoldedCDTs = 0;
		sumOfCDTSizes = 0L;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "numberOfOutputMappingsProduced",  Long.valueOf(numberOfOutputMappingsProduced) );

		s.put( "numberOfExprEvalErrors",  Integer.valueOf(numberOfExprEvalErrors) );
		s.put( "numberOfUnfoldedCDTs",    Integer.valueOf(numberOfUnfoldedCDTs) );

		if ( numberOfUnfoldedCDTs > 0 ) {
			final double avgSizeOfUnfoldedCDTs = sumOfCDTSizes / numberOfUnfoldedCDTs;
			s.put( "minSizeOfUnfoldedCDTs",  Integer.valueOf(minSizeOfUnfoldedCDTs) );
			s.put( "maxSizeOfUnfoldedCDTs",  Integer.valueOf(maxSizeOfUnfoldedCDTs) );
			s.put( "avgSizeOfUnfoldedCDTs",  Double.valueOf(avgSizeOfUnfoldedCDTs) );
		}

		return s;
	}


	protected void unfoldList( final LiteralLabel lit,
	                           final SolutionMapping inputSolMap,
	                           final IntermediateResultElementSink sink ) {
		@SuppressWarnings("unchecked")
		final List<CDTValue> list = (List<CDTValue>) lit.getValue();

		if ( list.isEmpty() ) return;

		final List<SolutionMapping> result;
		if ( var2 == null )
			result = unfoldList1(list, inputSolMap);
		else
			result = unfoldList2(list, inputSolMap);

		sink.send(result);

		numberOfOutputMappingsProduced += list.size();
		sumOfCDTSizes += list.size();
		minSizeOfUnfoldedCDTs = Math.min( minSizeOfUnfoldedCDTs, list.size() );
		maxSizeOfUnfoldedCDTs = Math.max( maxSizeOfUnfoldedCDTs, list.size() );
	}

	protected List<SolutionMapping> unfoldList1( final List<CDTValue> list,
	                                             final SolutionMapping smIn ) {
		final List<SolutionMapping> result = new ArrayList<>( list.size() );
		final Binding smInJena = smIn.asJenaBinding();

		for ( final CDTValue elmt : list ) {
			if ( elmt.isNull() ) {
				result.add(smIn);
			}
			else if ( elmt.isNode() ) {
				final Node elmtNode = elmt.asNode();
				final Binding smOut = BindingFactory.binding( smInJena,
				                                              var1,
				                                              elmtNode );
				result.add( new SolutionMappingImpl(smOut) );
			}
			else {
				 throw new UnsupportedOperationException( "unexpected list element: " + elmt.getClass().getName() );
			}
		}

		return result;
	}

	protected List<SolutionMapping> unfoldList2( final List<CDTValue> list,
	                                             final SolutionMapping smIn ) {
		final List<SolutionMapping> result = new ArrayList<>( list.size() );
		final Binding smInJena = smIn.asJenaBinding();

		int pos = 1; // position values to be assigned to the second variable start at 1
		for ( final CDTValue elmt : list ) {
			final Node posNode = NodeFactory.createLiteralDT( Integer.toString(pos++),
			                                                  XSDDatatype.XSDinteger );

			final Binding smOut;
			if ( elmt.isNull() ) {
				smOut = BindingFactory.binding( smInJena, var2, posNode );
			}
			else if ( elmt.isNode() ) {
				smOut = BindingFactory.binding( smInJena,
				                                var1, elmt.asNode(),
				                                var2, posNode );
			}
			else {
				throw new UnsupportedOperationException( "unexpected list element: " + elmt.getClass().getName() );
			}

			result.add( new SolutionMappingImpl(smOut) );
		}

		return result;
	}

	protected void unfoldMap( final LiteralLabel lit,
	                          final SolutionMapping smIn,
	                          final IntermediateResultElementSink sink ) {
		@SuppressWarnings("unchecked")
		final Map<CDTKey, CDTValue> map = (Map<CDTKey, CDTValue>) lit.getValue();

		if ( map.isEmpty() ) return;

		final List<SolutionMapping> result = new ArrayList<>( map.size() );
		final Binding smInJena = smIn.asJenaBinding();
		for ( final Map.Entry<CDTKey, CDTValue> e : map.entrySet() ) {
			final Binding smOut;
			if ( var2 == null )
				smOut = BindingFactory.binding( smInJena,
				                                var1, e.getKey().asNode() );
			else if ( e.getValue().isNull() )
				smOut = BindingFactory.binding( smInJena,
				                                var1, e.getKey().asNode() );
			else if ( e.getValue().isNode() )
				smOut = BindingFactory.binding( smInJena,
				                                var1, e.getKey().asNode(),
				                                var2, e.getValue().asNode() );
			else
				throw new UnsupportedOperationException( "unexpected map value: " + e.getValue().getClass().getName() );

			result.add( new SolutionMappingImpl(smOut) );
		}

		sink.send(result);

		numberOfOutputMappingsProduced += map.size();
		sumOfCDTSizes += map.size();
		minSizeOfUnfoldedCDTs = Math.min( minSizeOfUnfoldedCDTs, map.size() );
		maxSizeOfUnfoldedCDTs = Math.max( maxSizeOfUnfoldedCDTs, map.size() );
	}

}
