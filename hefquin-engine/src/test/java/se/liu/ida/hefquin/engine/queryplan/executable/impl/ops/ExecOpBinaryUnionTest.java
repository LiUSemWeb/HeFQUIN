package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;

import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpBinaryUnionTest extends TestsForUnionAlgorithms
{
	@Test
	public void testSimpleUnion_SeparateInput() throws ExecutionException {
		_testSimpleUnion(true);
	}

	@Test
	public void testSimpleUnion_CombinedInput() throws ExecutionException {
		_testSimpleUnion(false);
	}

	@Test
	public void testUnboundUnion_SeparateInput() throws ExecutionException {
		_testUnboundUnion(true);
	}

	@Test
	public void testUnboundUnion_CombinedInput() throws ExecutionException {
		_testUnboundUnion(false);
	}

	@Test
	public void testKeepUnionDuplicates_SeparateInput() throws ExecutionException {
		_testKeepUnionDuplicates(true);
	}

	@Test
	public void testKeepUnionDuplicates_CombinedInput() throws ExecutionException {
		_testKeepUnionDuplicates(false);
	}

	@Test
	public void testKeepUnionDuplicatesFromSameMember_SeparateInput() throws ExecutionException {
		_testKeepUnionDuplicatesFromSameMember(true);
	}

	@Test
	public void testKeepUnionDuplicatesFromSameMember_CombinedInput() throws ExecutionException {
		_testKeepUnionDuplicatesFromSameMember(false);
	}

	@Override
	protected BinaryExecutableOp createExecOpForTest() {
		return new ExecOpBinaryUnion(false);
	}
	
}
