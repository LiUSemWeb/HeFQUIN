package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.table.TableData;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

/**
 * Implementation of (a batching version of) the bind join algorithm that uses
 * a VALUES clause to capture the potential join partners that are sent to the
 * federation member.
 *
 * For every batch of solution mappings from the input, the algorithm sends a
 * SPARQL request to the federation member; this request consists of the given
 * graph pattern, extended with a VALUES clause that contains the solutions of
 * the current input batch (in fact, only their projection to the join variables;
 * also, the algorithm may decide to split the input batch into smaller batches
 * for multiple requests).
 * The response to such a request is the subset of the solutions for the graph
 * pattern that are join partners for at least one of the solutions that were
 * used for creating the request.
 * After receiving such a response, the algorithm locally joins the solutions
 * from the response with the solutions in the batch used for creating the
 * request, and outputs the resulting joined solutions (if any).
 * Thereafter, the algorithm moves on to the next batch of solutions from
 * the input.
 */
public class ExecOpBindJoinSPARQLwithVALUES extends BaseForExecOpBindJoinSPARQL
{
	public ExecOpBindJoinSPARQLwithVALUES( final SPARQLGraphPattern query,
	                                       final SPARQLEndpoint fm,
	                                       final boolean useOuterJoinSemantics,
	                                       final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOp( final Iterable<SolutionMapping> solMaps ) {
		final Set<Binding> bindings = new HashSet<>();
		final Set<Var> joinVars = new HashSet<>();

		boolean noJoinVars = false;
		for ( final SolutionMapping s : solMaps ) {
			final Binding b = SolutionMappingUtils.restrict( s.asJenaBinding(), varsInSubQuery );

			// If there exists a solution mapping that does not have any variables in common with the triple pattern of this operator
			// retrieve all matching triples of the given query
			if ( b.isEmpty() ) {
				noJoinVars = true;
				break;
			}

			if ( ! SolutionMappingUtils.containsBlankNodes(b) ) {
				bindings.add(b);

				final Iterator<Var> it = b.vars();
				while ( it.hasNext() ) {
					joinVars.add( it.next() );
				}
			}
		}

		if (noJoinVars) {
			return new ExecOpRequestSPARQL( new SPARQLRequestImpl(query), fm, false );
		}

		if ( bindings.isEmpty() ) {
			return null;
		}

		final Table table = new TableData( new ArrayList<>(joinVars), new ArrayList<>(bindings) );
		final Op op = OpSequence.create( OpTable.create(table), QueryPatternUtils.convertToJenaOp(query) );
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl2(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		return new ExecOpRequestSPARQL(request, fm, false);
	}

}
