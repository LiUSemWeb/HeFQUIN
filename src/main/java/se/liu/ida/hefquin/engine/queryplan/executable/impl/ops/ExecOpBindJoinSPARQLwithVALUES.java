package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.table.TableData;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;

public class ExecOpBindJoinSPARQLwithVALUES extends ExecOpGenericBindJoinWithRequestOps<SPARQLGraphPattern, SPARQLEndpoint>
{
	protected final List<Var> varsInTP;
	
	public ExecOpBindJoinSPARQLwithVALUES( final TriplePattern query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInTP = new ArrayList<>( QueryPatternUtils.getVariablesInPattern(query) );
	}

	public ExecOpBindJoinSPARQLwithVALUES( final BGP query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInTP = new ArrayList<>( QueryPatternUtils.getVariablesInPattern(query) );
	}

	public ExecOpBindJoinSPARQLwithVALUES( final SPARQLGraphPattern query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInTP = new ArrayList<>( QueryPatternUtils.getVariablesInPattern(query) );
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> solMaps ) {
		final Set<Binding> bindings = new HashSet<Binding>();
		for ( final SolutionMapping s : solMaps ) {
			final Binding b = SolutionMappingUtils.restrict( s.asJenaBinding(), varsInTP );
			if ( ! SolutionMappingUtils.containsBlankNodes(b) ) {
				bindings.add(b);
			}
		}

		if ( bindings.isEmpty() ) {
			return null;
		}

		final Table table = new TableData( varsInTP, new ArrayList<>(bindings) );
		final Op op = OpSequence.create( OpTable.create(table), createOpBasedOnQuery(query) );
		final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		return new ExecOpRequestSPARQL(request, fm);
	}

}
