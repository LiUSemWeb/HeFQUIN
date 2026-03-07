package se.liu.ida.hefquin.jenaext.sparql.engine.main;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.util.Context;

import se.liu.ida.hefquin.jenaext.sparql.algebra.AlgebraGeneratorForHeFQUIN;

public class QueryEngineMainForHeFQUIN extends QueryEngineMain
{
	static public QueryEngineFactory getFactory() { return factory ; }

	protected QueryEngineMainForHeFQUIN( final Op op,
	                                     final DatasetGraph dataset,
	                                     final Binding input,
	                                     final Context context ) {
		super(op, dataset, input, context);
	}

	protected QueryEngineMainForHeFQUIN( final Query query,
	                                     final DatasetGraph dataset,
	                                     final Binding input,
	                                     final Context context ) {
		super(query, dataset, input, context);
	}

	protected Op createOp( final Query query ) {
		if ( query == null ) {
			return null;
		}

		return new AlgebraGeneratorForHeFQUIN().compile(query);
	}

	public static final QueryEngineFactory factory = new QueryEngineFactory() {
		@Override
		public boolean accept( final Query q, final DatasetGraph ds, final Context cxt ) {
			return true;
		}

		@Override
		public Plan create( final Query q, final DatasetGraph ds, final Binding input, final Context cxt ) {
			return new QueryEngineMainForHeFQUIN(q, ds, input, cxt).getPlan();
		}

		@Override
		public boolean accept( final Op op, final DatasetGraph ds, final Context cxt ) {
			return true;
		}

		@Override
		public Plan create( final Op op, final DatasetGraph ds, final Binding input, final Context cxt ) {
			return new QueryEngineMainForHeFQUIN(op, ds, input, cxt).getPlan();
		}
	};

}
