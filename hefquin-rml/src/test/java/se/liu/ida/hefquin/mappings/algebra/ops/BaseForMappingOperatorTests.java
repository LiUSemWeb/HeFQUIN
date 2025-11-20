package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceType;

public class BaseForMappingOperatorTests
{
	static class TestDataObject1 implements DataObject {
		public final List<TestDataObject2> ds;
		public TestDataObject1( final TestDataObject2 ... ds ) { this.ds = Arrays.asList(ds); }
	}

	static class TestDataObject2 implements DataObject {
		public final List<TestDataObject3> ds;
		public TestDataObject2( final TestDataObject3 ... ds ) { this.ds = Arrays.asList(ds); }
	}

	static class TestDataObject3 implements DataObject {
		public final Node n;
		public TestDataObject3( final Node n ) { this.n = n; }
	}

	/**
	 * A super simple "query language" that can "express" only one query
	 * and this query simply returns all sub-objects of the queried data
	 * object (assuming the data object is of type {@link TestDataObject1}
	 * or {@link TestDataObject2}).
	 */
	static class TestQuery implements Query { }

	static class SourceTypeForTests implements SourceType< TestDataObject1,
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

	/**
	 * This operator simply returns the tuples given to its constructor.
	 */
	static class ConstantMappingOperatorForTests extends BaseForMappingOperator {
		protected final MappingRelation r;

		public ConstantMappingOperatorForTests( final MappingRelation r ) {
			this.r = r;
		}

		@Override
		public Set<String> getSchema() {
			return new HashSet<>( r.getSchema() );
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
			return true;
		}

		@Override
		public MappingRelation evaluate( final Map<SourceReference, DataObject> srMap ) {
			return r;
		}
		
	}

	static class MappingOpExtractForTests extends MappingOpExtract<TestDataObject1,
	                                                               TestDataObject2,
	                                                               TestDataObject3,
	                                                               TestQuery,
	                                                               TestQuery> {
		public MappingOpExtractForTests( final Map<String, TestQuery> P ) {
			super( new SourceReference() {},
			       new SourceTypeForTests(),
			       new TestQuery(),
			       P );
		}
	}

}
