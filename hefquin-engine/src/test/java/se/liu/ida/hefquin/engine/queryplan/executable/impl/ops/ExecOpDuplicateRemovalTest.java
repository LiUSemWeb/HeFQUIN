package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;

public class ExecOpDuplicateRemovalTest 
{
    @Test
    public void distinctKeepDifferentSolutionMappings() throws ExecOpExecutionException {
        // Tests the case in which two different input 
        // solution mappings are given to the DISTINCT operator.

        final Node URI1 = NodeFactory.createURI("http://example.org/uri1");
		final Node URI2 = NodeFactory.createURI("http://example.org/uri2");
        final Var var1 = Var.alloc("var1");
		final Var var2 = Var.alloc("var2");

        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var1, URI1);
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var2, URI2);

        final ExecOpDuplicateRemoval op = new ExecOpDuplicateRemoval(false, null);
        final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		
        op.process(sm1, sink, null);
        op.process(sm2, sink, null);
        op.concludeExecution(sink, null);
        
        final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();
        
        final SolutionMapping expectedSM1 = SolutionMappingUtils.createSolutionMapping(var1, URI1);
        final SolutionMapping expectedSM2 = SolutionMappingUtils.createSolutionMapping(var2, URI2);

        assertEquals( it.next(), expectedSM1 );
        assertTrue( it.hasNext() );

        assertEquals( it.next(), expectedSM2 );
        assertFalse( it.hasNext() );
    }
    
    @Test
    public void distinctRemovesEqualSolutionMappings() throws ExecOpExecutionException {
        // Tests the case in which two value-equivalent solution 
        // mappings are given to the DISTINCT operator.

        final Node URI1 = NodeFactory.createURI("http://example.org/uri1");
        final Var var1 = Var.alloc("var1");

        final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var1, URI1);
        final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var1, URI1);

        final ExecOpDuplicateRemoval op = new ExecOpDuplicateRemoval(false, null);
        final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		
        op.process(sm1, sink, null);
        op.process(sm2, sink, null);
        op.concludeExecution(sink, null);

        final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();
        
        final SolutionMapping expectedSM1 = SolutionMappingUtils.createSolutionMapping(var1, URI1);

        assertEquals( it.next(), expectedSM1 );
        assertFalse( it.hasNext() );
    }
}
