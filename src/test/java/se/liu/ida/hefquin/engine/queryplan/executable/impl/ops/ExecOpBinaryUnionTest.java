package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;

import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;

public class ExecOpBinaryUnionTest extends TestsForUnionAlgorithms<SPARQLEndpoint>{
	
	@Test
	public void testSimpleUnion(){
		_testSimpleUnion();	
	}
}
