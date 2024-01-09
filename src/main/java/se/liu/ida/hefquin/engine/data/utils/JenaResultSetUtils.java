package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorBase;
import org.apache.jena.sparql.serializer.SerializationContext;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class JenaResultSetUtils
{
	public static ResultSet convertToJenaResultSet( final List<SolutionMapping> result,
	                                                final List<String> vars ) {
		final QueryIterator it = new MyQueryIterator(result);
		return ResultSetFactory.create(it, vars);
	}


	static class MyQueryIterator extends QueryIteratorBase {

		final Iterator<SolutionMapping> it;

		public MyQueryIterator( final List<SolutionMapping> solmaps ) {
			it = solmaps.iterator();
		}

		@Override
		public void output( final IndentedWriter out, final SerializationContext sCxt ) {
			out.print("QueryIterator (HeFQUIN SolutionMappings -> Jena Bindings)");
		}

		@Override
		protected boolean hasNextBinding() {
			return it.hasNext();
		}

		@Override
		protected Binding moveToNextBinding() {
			return it.next().asJenaBinding();
		}

		@Override
		protected void closeIterator() {
			// nothing to do here
		}

		@Override
		protected void requestCancel() {
			// nothing to do here
		}
	}

}
