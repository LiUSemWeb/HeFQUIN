package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;

import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpBinaryUnionTest extends TestsForUnionAlgorithms {
	
	@Test
	public void testSimpleUnion() throws ExecutionException {
		_testSimpleUnion();
	}
	
	@Test
	public void testUnboundUnion() throws ExecutionException {
		_testUnboundUnion();
	}
	
	@Test
	public void testKeepUnionDuplicates() throws ExecutionException {
		_testKeepUnionDuplicates();
	}
	
	@Test
	public void testKeepUnionDuplicatesFromSameMember() throws ExecutionException {
		_testKeepUnionDuplicatesFromSameMember();
	}

	@Override
	protected BinaryExecutableOp createExecOpForTest() {
		return new ExecOpBinaryUnion();
	}
	
}
