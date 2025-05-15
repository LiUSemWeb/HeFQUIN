package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;

public class MergeRequestsTest extends EngineTestBase
{
	@Test
	public void mergeJoinUnderUnionPossible1() {
		// a join of two triple pattern requests to the same fed.member,
		// under a union with another request to another fed.member;
		// the first fed.member is a SPARQL endpoint and, thus, the
		// join can be merged

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");
		final FederationMember fmB = new SPARQLEndpointForTest("http://exB.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp2) );

		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmB, new SPARQLRequestImpl(tp3) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2) );

		final LogicalPlan unionPlan = LogicalPlanUtils.createPlanWithBinaryUnion(
				joinSubPlan,
				new LogicalPlanWithNullaryRootImpl(reqOp3) );

		// test
		final LogicalPlan result = new MergeRequests().apply(unionPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> resultReqOp = (LogicalOpRequest<?,?>) subResult.getRootOperator();
		assertTrue( resultReqOp.getFederationMember() == fmA );
		assertTrue( resultReqOp.getRequest() instanceof SPARQLRequest );

		final SPARQLRequest resultReq = (SPARQLRequest) resultReqOp.getRequest();
		assertTrue( resultReq.getQueryPattern() instanceof BGP );

		final BGP resultBGP = (BGP) resultReq.getQueryPattern();
		assertEquals( 2, resultBGP.getTriplePatterns().size() );
		assertTrue( resultBGP.getTriplePatterns().contains(tp1) );
		assertTrue( resultBGP.getTriplePatterns().contains(tp2) );
	}

	@Test
	public void mergeJoinUnderUnionPossible2() {
		// a join of two triple pattern requests to the same fed.member,
		// under a union with another request to the same fed.member;
		// that fed.member is a SPARQL endpoint and, thus, the whole
		// plan can be merged into a single request operator
		// i.e., this test applies the merging multiple times

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fm = new SPARQLEndpointForTest("http://ex.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fm, new SPARQLRequestImpl(tp2) );

		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fm, new SPARQLRequestImpl(tp3) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2) );

		final LogicalPlan unionPlan = LogicalPlanUtils.createPlanWithBinaryUnion(
				joinSubPlan,
				new LogicalPlanWithNullaryRootImpl(reqOp3) );

		// test
		final LogicalPlan result = new MergeRequests().apply(unionPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> resultReqOp = (LogicalOpRequest<?,?>) result.getRootOperator();
		assertTrue( resultReqOp.getFederationMember() == fm );
		assertTrue( resultReqOp.getRequest() instanceof SPARQLRequest );

		final SPARQLRequest resultReq = (SPARQLRequest) resultReqOp.getRequest();
		assertTrue( resultReq.getQueryPattern() instanceof SPARQLUnionPattern );

		final Element resultElmt = QueryPatternUtils.convertToJenaElement( resultReq.getQueryPattern() );
		assertTrue( resultElmt instanceof ElementUnion );
		assertEquals( 2, ((ElementUnion) resultElmt).getElements().size() );
		assertTrue( ((ElementUnion) resultElmt).getElements().get(0) instanceof ElementTriplesBlock );
		assertTrue( ((ElementUnion) resultElmt).getElements().get(1) instanceof ElementTriplesBlock );

		final ElementTriplesBlock resultBGP1 = (ElementTriplesBlock) ((ElementUnion) resultElmt).getElements().get(0);
		final ElementTriplesBlock resultBGP2 = (ElementTriplesBlock) ((ElementUnion) resultElmt).getElements().get(1);

		assertTrue( resultBGP1.getPattern().getList().contains(tp1.asJenaTriple()) );
		assertTrue( resultBGP1.getPattern().getList().contains(tp2.asJenaTriple()) );
		assertTrue( resultBGP2.getPattern().getList().contains(tp3.asJenaTriple()) );
	}

	@Test
	public void mergeJoinUnderUnionImpossible() {
		// like mergeJoinUnderUnionPossible1 but with a TPF server,
		// for which the join cannot be merged

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fmA = new TPFServerForTest();
		final FederationMember fmB = new SPARQLEndpointForTest("http://exB.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new TriplePatternRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, new TriplePatternRequestImpl(tp2) );

		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmB, new SPARQLRequestImpl(tp3) );

		final LogicalPlan joinSubPlan = LogicalPlanUtils.createPlanWithBinaryJoin(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2) );

		final LogicalPlan unionPlan = LogicalPlanUtils.createPlanWithBinaryUnion(
				joinSubPlan,
				new LogicalPlanWithNullaryRootImpl(reqOp3) );

		// test
		final LogicalPlan result = new MergeRequests().apply(unionPlan);

		// check
		assertEquals(unionPlan, result); // the plan has not changed

		assertTrue( result.getRootOperator() instanceof LogicalOpUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan subResult = result.getSubPlan(0);
		assertTrue( subResult.getRootOperator() instanceof LogicalOpJoin );
	}

	@Test
	public void mergeMultiwayJoinPossible1() {
		// a multiway join of three triple pattern requests,
		// two of them to the same fed.member,
		// the third to another fed.member;
		// the first fed.member is a SPARQL endpoint and, thus,
		// the two requests can be merged

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");
		final FederationMember fmB = new SPARQLEndpointForTest("http://exB.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp2) );

		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmB, new SPARQLRequestImpl(tp3) );

		final LogicalPlan mjPlan = LogicalPlanUtils.createPlanWithMultiwayJoin(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2),
				new LogicalPlanWithNullaryRootImpl(reqOp3) );

		// test
		final LogicalPlan result = new MergeRequests().apply(mjPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayJoin );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		assertTrue( subResult1.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalPlan subResult2 = result.getSubPlan(1);
		assertTrue( subResult2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final List<LogicalOpRequest<?,?>> resultReqOps = new ArrayList<>();
		resultReqOps.add( (LogicalOpRequest<?,?>) subResult1.getRootOperator() );
		resultReqOps.add( (LogicalOpRequest<?,?>) subResult2.getRootOperator() );

		for ( final LogicalOpRequest<?,?> resultReqOp : resultReqOps ) {
			assertTrue( resultReqOp.getRequest() instanceof SPARQLRequest );

			final SPARQLRequest resultReq = (SPARQLRequest) resultReqOp.getRequest();

			if ( resultReqOp.getFederationMember() == fmA ) {
				assertTrue( resultReq.getQueryPattern() instanceof BGP );

				final BGP resultBGP = (BGP) resultReq.getQueryPattern();
				assertEquals( 2, resultBGP.getTriplePatterns().size() );
				assertTrue( resultBGP.getTriplePatterns().contains(tp1) );
				assertTrue( resultBGP.getTriplePatterns().contains(tp2) );
			}
			else {
				assertTrue( resultReqOp.getFederationMember() == fmB );
				assertTrue( resultReq.getQueryPattern() == tp3 );
			}
		}
	}

	@Test
	public void mergeMultiwayJoinPossible2() {
		// a multiway join of three triple pattern requests,
		// *all* of them to the same fed.member, which is a SPARQL endpoint;
		// thus, the whole plan can be merged into
		// a single request operator with a BGP

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fm, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fm, new SPARQLRequestImpl(tp2) );

		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fm, new SPARQLRequestImpl(tp3) );

		final LogicalPlan mjPlan = LogicalPlanUtils.createPlanWithMultiwayJoin(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2),
				new LogicalPlanWithNullaryRootImpl(reqOp3) );

		// test
		final LogicalPlan result = new MergeRequests().apply(mjPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> resultReqOp = (LogicalOpRequest<?,?>) result.getRootOperator();
		assertTrue( resultReqOp.getFederationMember() == fm );
		assertTrue( resultReqOp.getRequest() instanceof SPARQLRequest );

		final SPARQLRequest resultReq = (SPARQLRequest) resultReqOp.getRequest();
		assertTrue( resultReq.getQueryPattern() instanceof BGP );

		final BGP resultBGP = (BGP) resultReq.getQueryPattern();
		assertEquals( 3, resultBGP.getTriplePatterns().size() );
		assertTrue( resultBGP.getTriplePatterns().contains(tp1) );
		assertTrue( resultBGP.getTriplePatterns().contains(tp2) );
		assertTrue( resultBGP.getTriplePatterns().contains(tp3) );
	}

	@Test
	public void mergeMultiwayJoinImpossible() {
		// like mergeMultiwayJoinPossible1 but with a TPF server, for
		// which the requests under the multiway join cannot be merged

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fmA = new TPFServerForTest();
		final FederationMember fmB = new SPARQLEndpointForTest("http://exB.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new TriplePatternRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, new TriplePatternRequestImpl(tp2) );

		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmB, new SPARQLRequestImpl(tp3) );

		final LogicalPlan mjPlan = LogicalPlanUtils.createPlanWithMultiwayJoin(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2),
				new LogicalPlanWithNullaryRootImpl(reqOp3) );

		// test
		final LogicalPlan result = new MergeRequests().apply(mjPlan);

		// check
		assertEquals(mjPlan, result); // the plan has not changed

		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayJoin );
		assertEquals( 3, result.numberOfSubPlans() );
	}

	@Test
	public void mergeMultiwayUnionPossible() {
		// a multiway union of three triple pattern requests,
		// two of them to the same fed.member,
		// the third to another fed.member;
		// the first fed.member is a SPARQL endpoint and, thus,
		// the first two triple pattern requests can be merged
		// into a union request

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fmA = new SPARQLEndpointForTest("http://exA.org");
		final FederationMember fmB = new SPARQLEndpointForTest("http://exB.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, new SPARQLRequestImpl(tp2) );

		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmB, new SPARQLRequestImpl(tp3) );

		final LogicalPlan mjPlan = LogicalPlanUtils.createPlanWithMultiwayUnion(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2),
				new LogicalPlanWithNullaryRootImpl(reqOp3) );

		// test
		final LogicalPlan result = new MergeRequests().apply(mjPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 2, result.numberOfSubPlans() );

		final LogicalPlan subResult1 = result.getSubPlan(0);
		assertTrue( subResult1.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalPlan subResult2 = result.getSubPlan(1);
		assertTrue( subResult2.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final List<LogicalOpRequest<?,?>> resultReqOps = new ArrayList<>();
		resultReqOps.add( (LogicalOpRequest<?,?>) subResult1.getRootOperator() );
		resultReqOps.add( (LogicalOpRequest<?,?>) subResult2.getRootOperator() );

		for ( final LogicalOpRequest<?,?> resultReqOp : resultReqOps ) {
			assertTrue( resultReqOp.getRequest() instanceof SPARQLRequest );

			final SPARQLRequest resultReq = (SPARQLRequest) resultReqOp.getRequest();

			if ( resultReqOp.getFederationMember() == fmA ) {
				assertTrue( resultReq.getQueryPattern() instanceof SPARQLUnionPattern );

				final SPARQLUnionPattern resultSubUnion = (SPARQLUnionPattern) resultReq.getQueryPattern();
				assertEquals( 2, resultSubUnion.getNumberOfSubPatterns() );
				assertTrue( resultSubUnion.getSubPatterns(0) == tp1 || resultSubUnion.getSubPatterns(0) == tp2 );
				if ( resultSubUnion.getSubPatterns(0) == tp1 ) {
					assertTrue( resultSubUnion.getSubPatterns(1) == tp2 );					
				}
				else {
					assertTrue( resultSubUnion.getSubPatterns(1) == tp1 );
				}
			}
			else {
				assertTrue( resultReqOp.getFederationMember() == fmB );
				assertTrue( resultReq.getQueryPattern() == tp3 );
			}
		}
	}

	@Test
	public void mergeMultiwayUnionImpossible() {
		// like mergeMultiwayUnionPossible but with a TPF server, for
		// which the requests under the multiway union cannot be merged

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final FederationMember fmA = new TPFServerForTest();
		final FederationMember fmB = new SPARQLEndpointForTest("http://exB.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>( fmA, new TriplePatternRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>( fmA, new TriplePatternRequestImpl(tp2) );

		final TriplePattern tp3 = new TriplePatternImpl(v3 ,v3, v3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>( fmB, new SPARQLRequestImpl(tp3) );

		final LogicalPlan mjPlan = LogicalPlanUtils.createPlanWithMultiwayUnion(
				new LogicalPlanWithNullaryRootImpl(reqOp1),
				new LogicalPlanWithNullaryRootImpl(reqOp2),
				new LogicalPlanWithNullaryRootImpl(reqOp3) );

		// test
		final LogicalPlan result = new MergeRequests().apply(mjPlan);

		// check
		assertEquals(mjPlan, result); // the plan has not changed

		assertTrue( result.getRootOperator() instanceof LogicalOpMultiwayUnion );
		assertEquals( 3, result.numberOfSubPlans() );
	}

	@Test
	public void mergeTPAddPossible() {
		// a tpAdd over a triple pattern requests, both to the same fed.member;
		// this fed.member is a SPARQL endpoint and, thus, the whole plan can
		// be merged into a single request operator

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fm = new SPARQLEndpointForTest("http://exA.org");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new SPARQLRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);

		final LogicalPlan tpAddPlan = new LogicalPlanWithUnaryRootImpl(
				new LogicalOpTPAdd(fm, tp2),
				new LogicalPlanWithNullaryRootImpl(reqOp) );

		// test
		final LogicalPlan result = new MergeRequests().apply(tpAddPlan);

		// check
		assertTrue( result.getRootOperator() instanceof LogicalOpRequest<?,?> );

		final LogicalOpRequest<?,?> resultReqOp = (LogicalOpRequest<?,?>) result.getRootOperator();
		assertTrue( resultReqOp.getFederationMember() == fm );
		assertTrue( resultReqOp.getRequest() instanceof SPARQLRequest );

		final SPARQLRequest resultReq = (SPARQLRequest) resultReqOp.getRequest();
		assertTrue( resultReq.getQueryPattern() instanceof BGP );

		final BGP resultBGP = (BGP) resultReq.getQueryPattern();
		assertEquals( 2, resultBGP.getTriplePatterns().size() );
		assertTrue( resultBGP.getTriplePatterns().contains(tp1) );
		assertTrue( resultBGP.getTriplePatterns().contains(tp2) );
	}

	@Test
	public void mergeTPAddImpossible() {
		// like mergeTPAddPossible but with a TPF server,
		// for which the merge is not possible

		// set up
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final FederationMember fm = new TPFServerForTest();

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>( fm, new TriplePatternRequestImpl(tp1) );

		final TriplePattern tp2 = new TriplePatternImpl(v2 ,v2, v2);

		final LogicalPlan tpAddPlan = new LogicalPlanWithUnaryRootImpl(
				new LogicalOpTPAdd(fm, tp2),
				new LogicalPlanWithNullaryRootImpl(reqOp) );

		// test
		final LogicalPlan result = new MergeRequests().apply(tpAddPlan);

		// check
		assertEquals(tpAddPlan, result); // the plan has not changed

		assertTrue( result.getRootOperator() instanceof LogicalOpTPAdd );

		final LogicalOpTPAdd resultTPAddOp = (LogicalOpTPAdd) result.getRootOperator();
		assertTrue( resultTPAddOp.getFederationMember() == fm );
		assertTrue( resultTPAddOp.getTP() == tp2 );
	}

}
