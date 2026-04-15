package se.liu.ida.hefquin.mappings.algebra.exprs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.BaseForMappingTests;
import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.exec.UnionedMappingRelation;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtract;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;

public class MappingExpressionUtilsTest extends BaseForMappingTests
{
	@Test
	public void extractAllSrcRefs_SameSrcRefTwice() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery(),
		                                         "attr2", new TestQuery() );
		final SourceReference sr = new SourceReference() {};

		final MappingOperator subOp1 = new MappingOpExtractForTests(sr, P);
		final MappingOperator subOp2 = new MappingOpExtractForTests(sr, P);

		final MappingExpression expr = MappingExpressionFactory.create(
				MappingOpUnion.getInstance(),
				MappingExpressionFactory.create(subOp1),
				MappingExpressionFactory.create(subOp2) );

		final Set<SourceReference> result = MappingExpressionUtils.extractAllSrcRefs(expr);

		assertEquals( 1, result.size() );
		assertTrue( result.contains(sr) );
	}

	@Test
	public void extractAllSrcRefs_TwoSrcRefs() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery(),
		                                         "attr2", new TestQuery() );
		final SourceReference sr1 = new SourceReference() {};
		final SourceReference sr2 = new SourceReference() {};

		final MappingOperator subOp1 = new MappingOpExtractForTests(sr1, P);
		final MappingOperator subOp2 = new MappingOpExtractForTests(sr2, P);

		final MappingExpression expr = MappingExpressionFactory.create(
				MappingOpUnion.getInstance(),
				MappingExpressionFactory.create(subOp1),
				MappingExpressionFactory.create(subOp2) );

		final Set<SourceReference> result = MappingExpressionUtils.extractAllSrcRefs(expr);

		assertEquals( 2, result.size() );
		assertTrue( result.contains(sr1) );
		assertTrue( result.contains(sr2) );
	}

	@Test
	public void evaluate_TODO() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery(),
		                                         "attr2", new TestQuery() );
		final SourceReference sr1 = new SourceReference() {};
		final SourceReference sr2 = new SourceReference() {};

		final MappingOperator subOp1 = new MappingOpExtractForTests(sr1, P);
		final MappingOperator subOp2 = new MappingOpExtractForTests(sr2, P);

		final MappingExpression expr = MappingExpressionFactory.create(
				MappingOpUnion.getInstance(),
				MappingExpressionFactory.create(subOp1),
				MappingExpressionFactory.create(subOp2) );

		final TestDataObject1 d1 = new TestDataObject1();
		final TestDataObject1 d2 = new TestDataObject1();

		final Map<SourceReference, DataObject> srMap = Map.of(sr1, d1, sr2, d2);

		final MappingRelation result = MappingExpressionUtils.evaluate(expr, srMap);

		assertTrue( result instanceof UnionedMappingRelation );
		assertTrue( result.getSchema().contains("attr1") );
		assertTrue( result.getSchema().contains("attr2") );

		assertFalse( result.getCursor().hasNext() );
	}


	// ---------- helpers ----------

	static class MappingOpExtractForTests extends MappingOpExtract<TestDataObject1,
	                                                               TestDataObject2,
	                                                               TestDataObject3,
	                                                               TestQuery,
	                                                               TestQuery> {
		public MappingOpExtractForTests( final SourceReference sr,
		                                 final Map<String, TestQuery> P ) {
			super( sr,
			       new SourceTypeForTests(),
			       new TestQuery(),
			       P );
		}
	}

}
