package se.liu.ida.hefquin.mappings.algebra;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtract;
import se.liu.ida.hefquin.mappings.algebra.ops.extexprs.ExtendExpression;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;
import se.liu.ida.hefquin.mappings.sources.SourceType;

public abstract class BaseForMappingTests
{
	public static class TestDataObject1 implements DataObject {
		public final List<TestDataObject2> ds;
		public TestDataObject1( final TestDataObject2 ... ds ) { this.ds = Arrays.asList(ds); }
	}

	public static class TestDataObject2 implements DataObject {
		public final List<TestDataObject3> ds;
		public TestDataObject2( final TestDataObject3 ... ds ) { this.ds = Arrays.asList(ds); }
	}

	public static class TestDataObject3 implements DataObject {
		public final Node n;
		public TestDataObject3( final Node n ) { this.n = n; }
	}

	/**
	 * A super simple "query language" that can "express" only one query
	 * and this query simply returns all sub-objects of the queried data
	 * object (assuming the data object is of type {@link TestDataObject1}
	 * or {@link TestDataObject2}).
	 */
	public static class TestQuery implements Query { }

	public static class SourceTypeForTests implements SourceType< TestDataObject1,
	                                                              TestDataObject2,
	                                                              TestDataObject3,
	                                                              TestQuery,
	                                                              TestQuery > {
		@Override
		public boolean isRelevantDataObject( final DataObject d ) {
			return d instanceof TestDataObject1;
		}

		@Override
		public List<TestDataObject2> eval( final TestQuery query,
		                                   final TestDataObject1 input ) {
			return input.ds;
		}

		@Override
		public List<TestDataObject3> eval( final TestQuery query,
		                                   final TestDataObject1 input,
		                                   final TestDataObject2 cxtObj ) {
			return cxtObj.ds;
		}

		@Override
		public Node cast( final TestDataObject3 d ) {
			return d.n;
		}
	}

	public static class ExtendExprForTests implements ExtendExpression {
		final Set<String> A;

		public ExtendExprForTests( final String ... A ) { this( Arrays.asList(A) ); }

		public ExtendExprForTests( final List<String> A ) { this( new HashSet<>(A) ); }

		public ExtendExprForTests( final Set<String> A ) { this.A = A; }

		@Override
		public Set<String> getAllMentionedAttributes() { return A; }

		@Override
		public Node evaluate( final Map<String, Node> assignment ) {
			int sum = 0;
			for ( final Map.Entry<String, Node> e : assignment.entrySet() ) {
				final Object v = e.getValue().getLiteralValue();
				if ( v instanceof Integer i ) {
					sum += i;
				}
			}

			final Node n = NodeFactory.createLiteralByValue(sum);
			return n;
		}
	}

	protected static class MappingOpExtractForTests
			extends MappingOpExtract<TestDataObject1,
			                         TestDataObject2,
			                         TestDataObject3,
			                         TestQuery,
			                         TestQuery>
	{
		public MappingOpExtractForTests( final Map<String, TestQuery> P ) {
			super( new SourceReference() {},
			       new SourceTypeForTests(),
			       new TestQuery(),
			       P );
		}
	}

}
