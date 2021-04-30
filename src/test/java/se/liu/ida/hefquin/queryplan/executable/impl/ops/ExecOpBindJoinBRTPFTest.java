package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;
import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMappingUtils;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedTripleUtils;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;
import se.liu.ida.hefquin.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.queryplan.executable.impl.MaterializingIntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class ExecOpBindJoinBRTPFTest extends ExecOpTestBase
{
    @Test
    public void tpWithJoinOnObject() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");
        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");

        final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
        input.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
                var1, x1,
                var2, y1) );
        input.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
                var1, x2,
                var2, y2) );

        final Node p = NodeFactory.createURI("http://example.org/p");
        final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(var2,p,var3);

        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");
        final Node z3 = NodeFactory.createURI("http://example.org/z3");

        final List<Triple> lResp = Arrays.asList(
                (Triple) JenaBasedTripleUtils.createJenaBasedTriple(y1, p, z1),
                (Triple) JenaBasedTripleUtils.createJenaBasedTriple(y1, p, z2),
                (Triple) JenaBasedTripleUtils.createJenaBasedTriple(y2, p, z3) );

        final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest(null, Arrays.asList(lResp).iterator());
        final ExecutionContext execCxt = new ExecutionContext(fedAccessMgr);
        final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
        final BRTPFServer fm = new BRTPFServerForTest();


        final ExecOpBindJoinBRTPF op = new ExecOpBindJoinBRTPF(tp, fm);
        op.process(input, sink, execCxt);
        op.concludeExecution(sink, execCxt);

        final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
        assertEquals( 3, b1.size() );
        assertEquals( "http://example.org/x1", b1.get(var1).getURI() );
        assertEquals( "http://example.org/y1", b1.get(var2).getURI() );
        assertEquals( "http://example.org/z1", b1.get(var3).getURI() );

        assertTrue( it.hasNext() );
        final Binding b2 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
        assertEquals( 3, b2.size() );
        assertEquals( "http://example.org/x1", b2.get(var1).getURI() );
        assertEquals( "http://example.org/y1", b2.get(var2).getURI() );
        assertEquals( "http://example.org/z2", b2.get(var3).getURI() );

        assertTrue( it.hasNext() );
        final Binding b3 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
        assertEquals( 3, b3.size() );
        assertEquals( "http://example.org/x2", b3.get(var1).getURI() );
        assertEquals( "http://example.org/y2", b3.get(var2).getURI() );
        assertEquals( "http://example.org/z3", b3.get(var3).getURI() );

        assertFalse( it.hasNext() );
    }

    @Test
    public void tpWithJoinOnSubjectAndObject() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");
        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");
        final Node y2 = NodeFactory.createURI("http://example.org/y2");

        final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
        input.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
                var1, x1,
                var2, y1) );
        input.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
                var1, x2,
                var2, y2) );

        final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(var1,var2,var3);

        final Node z1 = NodeFactory.createURI("http://example.org/z1");
        final Node z2 = NodeFactory.createURI("http://example.org/z2");

        final List<Triple> lResp = Arrays.asList(
                (Triple) JenaBasedTripleUtils.createJenaBasedTriple(x1, y1, z1),
                (Triple) JenaBasedTripleUtils.createJenaBasedTriple(x2, y2, z2) );

        final FederationAccessManager fedAccessMgr = new ExecOpTestBase.FederationAccessManagerForTest(null, Arrays.asList(lResp).iterator());
        final ExecutionContext execCxt = new ExecutionContext(fedAccessMgr);
        final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
        final BRTPFServer fm = new ExecOpTestBase.BRTPFServerForTest();

        final ExecOpBindJoinBRTPF op = new ExecOpBindJoinBRTPF(tp, fm);
        op.process(input, sink, execCxt);
        op.concludeExecution(sink, execCxt);

        final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
        assertEquals( 3, b1.size() );
        assertEquals( "http://example.org/x1", b1.get(var1).getURI() );
        assertEquals( "http://example.org/y1", b1.get(var2).getURI() );
        assertEquals( "http://example.org/z1", b1.get(var3).getURI() );

        assertTrue( it.hasNext() );
        final Binding b2 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
        assertEquals( 3, b2.size() );
        assertEquals( "http://example.org/x2", b2.get(var1).getURI() );
        assertEquals( "http://example.org/y2", b2.get(var2).getURI() );
        assertEquals( "http://example.org/z2", b2.get(var3).getURI() );

        assertFalse( it.hasNext() );
    }

    @Test
    public void tpWithoutJoinVariable() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");
        final Var var3 = Var.alloc("v3");
        final Node x1 = NodeFactory.createURI("http://example.org/x1");
        final Node x2 = NodeFactory.createURI("http://example.org/x2");

        final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
        input.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
                var1, x1) );
        input.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
                var1, x2) );

        final Node p = NodeFactory.createURI("http://example.org/p");
        final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(var2,p,var3);

        final Node y1 = NodeFactory.createURI("http://example.org/y1");
        final Node z1 = NodeFactory.createURI("http://example.org/z1");

        final List<Triple> lResp = Arrays.asList(
                (Triple) JenaBasedTripleUtils.createJenaBasedTriple(y1, p, z1) );

        final FederationAccessManager fedAccessMgr = new ExecOpTestBase.FederationAccessManagerForTest(null, Arrays.asList(lResp).iterator());
        final ExecutionContext execCxt = new ExecutionContext(fedAccessMgr);
        final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
        final BRTPFServer fm = new ExecOpTestBase.BRTPFServerForTest();

        final ExecOpBindJoinBRTPF op = new ExecOpBindJoinBRTPF(tp, fm);

        op.process(input, sink, execCxt);
        op.concludeExecution(sink, execCxt);

        final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

        assertTrue( it.hasNext() );
        final Binding b1 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
        assertEquals( 3, b1.size() );
        assertEquals( "http://example.org/x1", b1.get(var1).getURI() );
        assertEquals( "http://example.org/y1", b1.get(var2).getURI() );
        assertEquals( "http://example.org/z1", b1.get(var3).getURI() );

        assertTrue( it.hasNext() );
        final Binding b2 = ( (JenaBasedSolutionMapping) it.next() ).asJenaBinding();
        assertEquals( 3, b2.size() );
        assertEquals( "http://example.org/x2", b2.get(var1).getURI() );
        assertEquals( "http://example.org/y1", b2.get(var2).getURI() );
        assertEquals( "http://example.org/z1", b2.get(var3).getURI() );

        assertFalse( it.hasNext() );
    }

    @Test
    public void tpWithEmptyInput() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");

        final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();

        final Node p = NodeFactory.createURI("http://example.org/p");
        final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(var1,p,var2);

        final Node y1 = NodeFactory.createURI("http://example.org/y1");

        final List<Triple> lResp = Arrays.asList(
                (Triple) JenaBasedTripleUtils.createJenaBasedTriple(var1, p, y1) );

        final FederationAccessManager fedAccessMgr = new ExecOpTestBase.FederationAccessManagerForTest(null, Arrays.asList(lResp).iterator());
        final ExecutionContext execCxt = new ExecutionContext(fedAccessMgr);
        final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
        final BRTPFServer fm = new ExecOpTestBase.BRTPFServerForTest();

        final ExecOpBindJoinBRTPF op = new ExecOpBindJoinBRTPF(tp, fm);
        op.process(input, sink, execCxt);
        op.concludeExecution(sink, execCxt);

        final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

        assertFalse( it.hasNext() );
    }

    @Test
    public void tpWithEmptyResponses() {
        final Var var1 = Var.alloc("v1");
        final Var var2 = Var.alloc("v2");

        final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
        input.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
                var1, NodeFactory.createURI("http://example.org/x1")) );
        input.add( JenaBasedSolutionMappingUtils.createJenaBasedSolutionMapping(
                var1, NodeFactory.createURI("http://example.org/x2")) );

        final Node p = NodeFactory.createURI("http://example.org/p");
        final TriplePattern tp = JenaBasedQueryPatternUtils.createJenaBasedTriplePattern(var1,p,var2);

        final List<Triple> lResp1 = Arrays.asList();
        final List<Triple> lResp2 = Arrays.asList();

        final FederationAccessManager fedAccessMgr = new ExecOpTestBase.FederationAccessManagerForTest(null, Arrays.asList(lResp1,lResp2).iterator());
        final ExecutionContext execCxt = new ExecutionContext(fedAccessMgr);
        final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
        final BRTPFServer fm = new ExecOpTestBase.BRTPFServerForTest();

        final ExecOpBindJoinBRTPF op = new ExecOpBindJoinBRTPF(tp, fm);

        op.process(input, sink, execCxt);
        op.concludeExecution(sink, execCxt);

        final Iterator<SolutionMapping> it = sink.getMaterializedIntermediateResult().iterator();

        assertFalse( it.hasNext() );
    }
}