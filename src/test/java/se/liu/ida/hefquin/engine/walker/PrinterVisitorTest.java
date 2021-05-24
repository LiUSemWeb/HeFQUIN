package se.liu.ida.hefquin.engine.walker;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.junit.Test;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;

public class PrinterVisitorTest extends EngineTestBase {
	
	@Test
	public void printTest() {
		LogicalPlan plan = new LogicalPlanWithBinaryRootImpl(
					new LogicalOpUnion(), 
					new LogicalPlanWithUnaryRootImpl(
							new LogicalOpTPAdd(
									new SPARQLEndpointForTest(), 
									new TriplePatternImpl(NodeFactory.createVariable("s"), NodeFactory.createURI("http://ex.com/p"), NodeFactory.createVariable("o"))), 
							new LogicalPlanWithNullaryRootImpl(
									new LogicalOpRequest<SPARQLRequest, SPARQLEndpoint>(
											new SPARQLEndpointForTest(), 
											new SPARQLRequestImpl(
													new SPARQLGraphPatternImpl(
															new OpTriple(
																	new Triple(NodeFactory.createVariable("s"), NodeFactory.createURI("y"), NodeFactory.createVariable("o"))))
													)))), 
					new LogicalPlanWithNullaryRootImpl(
							new LogicalOpRequest<SPARQLRequest, SPARQLEndpoint>(
									new SPARQLEndpointForTest(), 
									new SPARQLRequestImpl(
											new SPARQLGraphPatternImpl(
													new OpTriple(
															new Triple(NodeFactory.createURI("x"),NodeFactory.createURI("y"), NodeFactory.createURI("z"))))
											))
							)
				);
		System.out.println(LogicalPlanPrinterVisitor.print(plan));
	}
	
	@Test
	public void printTest2() {
		LogicalPlan plan = new LogicalPlanWithBinaryRootImpl(
					new LogicalOpUnion(), 
					new LogicalPlanWithNullaryRootImpl(
							new LogicalOpRequest<SPARQLRequest, SPARQLEndpoint>(
									new SPARQLEndpointForTest(), 
									new SPARQLRequestImpl(
											new SPARQLGraphPatternImpl(
													new OpTriple(
															new Triple(NodeFactory.createURI("x"),NodeFactory.createURI("y"), NodeFactory.createURI("z"))))
											))
							),
					new LogicalPlanWithUnaryRootImpl(
							new LogicalOpTPAdd(
									new SPARQLEndpointForTest(), 
									new TriplePatternImpl(NodeFactory.createVariable("s"), NodeFactory.createURI("http://ex.com/p"), NodeFactory.createVariable("o"))), 
							new LogicalPlanWithNullaryRootImpl(
									new LogicalOpRequest<SPARQLRequest, SPARQLEndpoint>(
											new SPARQLEndpointForTest(), 
											new SPARQLRequestImpl(
													new SPARQLGraphPatternImpl(
															new OpTriple(
																	new Triple(NodeFactory.createVariable("s"), NodeFactory.createURI("y"), NodeFactory.createVariable("o"))))
													))))
				);
		System.out.println(LogicalPlanPrinterVisitor.print(plan));
	}

}
