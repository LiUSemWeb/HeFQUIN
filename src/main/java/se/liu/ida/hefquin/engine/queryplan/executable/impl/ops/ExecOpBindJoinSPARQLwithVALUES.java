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
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBindJoinSPARQLwithVALUES extends ExecOpGenericBindJoin<TriplePattern,SPARQLEndpoint>{

	protected final Set<Var> varsInTP;
	protected final List<Var> varsInTPAsList;
	
	public ExecOpBindJoinSPARQLwithVALUES( final TriplePattern query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInTP = QueryPatternUtils.getVariablesInPattern(query);
		varsInTPAsList = new ArrayList<>(varsInTP);
	}

	@Override
	protected Iterable<? extends SolutionMapping> fetchSolutionMappings(final Iterable<SolutionMapping> solMaps,
			final ExecutionContext execCxt) {
		final Set<Binding> bindings = new HashSet<Binding>();
		for (SolutionMapping s : solMaps) {
			bindings.add( SolutionMappingUtils.restrict(s, varsInTP).asJenaBinding() );
		}
		final Table table = new TableData(varsInTPAsList, new ArrayList<>(bindings));
		final Op op = OpSequence.create( OpTable.create(table), new OpTriple(query.asJenaTriple()));
		final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		final SolMapsResponse response = execCxt.getFederationAccessMgr().performRequest(request, fm);
		return response.getSolutionMappings();
	}

}
