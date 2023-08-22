package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;

import se.liu.ida.hefquin.engine.queryplan.executable.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpMultiwayUnionTest extends TestsForUnionAlgorithms
{
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
	protected NaryExecutableOp createExecOpForTest() {
		// TODO Auto-generated method stub
		return new ExecOpMultiwayUnion(2, false);
	}

}
