package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
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
	
	public ExecOpBindJoinSPARQLwithVALUES( final TriplePattern query, final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInTP = QueryPatternUtils.getVariablesInPattern(query);
	}

	@Override
	protected Iterable<? extends SolutionMapping> fetchSolutionMappings(final Set<SolutionMapping> solMaps,
			final ExecutionContext execCxt) {
		List<Binding> bindings = new ArrayList<Binding>();
		for (SolutionMapping s : solMaps) {
			bindings.add(s.asJenaBinding());
		}
		Table table = new TableData(new ArrayList<Var>(varsInTP), bindings);
		Op op = OpSequence.create( OpTable.create(table), new OpTriple(query.asJenaTriple()));
		SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl(op);
		SPARQLRequest request = new SPARQLRequestImpl(pattern);
		SolMapsResponse response = execCxt.getFederationAccessMgr().performRequest(request, fm);
		return response.getSolutionMappings();
	}

}
