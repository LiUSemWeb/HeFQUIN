package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;

public class ExecOpBinaryUnionTest extends TestsForUnionAlgorithms {
	
	@Test
	public void testSimpleUnion(){
		_testSimpleUnion();
	}
	
	@Test
	public void testUnboundUnion() {
		_testUnboundUnion();
	}
	
	@Test
	public void testKeepUnionDuplicates() {
		_testKeepUnionDuplicates();
	}

	@Override
	protected BinaryExecutableOp createExecOpForTest() {
		return new ExecOpBinaryUnion();
	}
	
}
