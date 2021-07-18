package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.table.TableData;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBindJoinSPARQLwithVALUES extends ExecOpGenericBindJoin<TriplePattern,SPARQLEndpoint>
{
	protected final List<Var> varsInTP;
	
	public ExecOpBindJoinSPARQLwithVALUES( final TriplePattern query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInTP = new ArrayList<>(QueryPatternUtils.getVariablesInPattern(query));
	}

	@Override
	protected Iterable<SolutionMapping> fetchSolutionMappings (
			final Iterable<SolutionMapping> solMaps,
			final ExecutionContext execCxt )
					throws ExecOpExecutionException
	{
		final Set<Binding> bindings = new HashSet<Binding>();
		for ( final SolutionMapping s : solMaps ) {
			bindings.add( SolutionMappingUtils.restrict(s, varsInTP).asJenaBinding() );
		}
		final Table table = new TableData( varsInTP, new ArrayList<>(bindings) );
		final Op op = OpSequence.create( OpTable.create(table), new OpTriple(query.asJenaTriple()) );
		final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		final SolMapsResponse response;
		try {
			response = execCxt.getFederationAccessMgr().performRequest(request, fm);
		}
		catch ( final FederationAccessException ex ) {
			throw new ExecOpExecutionException("An exception occurred when executing a request issued by this bind join.", ex, this);
		}
		return response.getSolutionMappings();
	}

}
