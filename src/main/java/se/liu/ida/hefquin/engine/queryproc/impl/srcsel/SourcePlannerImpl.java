package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import java.util.Iterator;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.BasicPattern;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;

public class SourcePlannerImpl implements SourcePlanner
{
	protected final FederationCatalog fedCat;

	public SourcePlannerImpl( final FederationCatalog fedCat ) {
		assert fedCat != null;
		this.fedCat = fedCat;
	}

	@Override
	public LogicalPlan createSourceAssignment( final Query query ) {
		// The current implementation here does not actually perform
		// query decomposition and source selection but simply assumes
		// queries with SERVICE clauses where, for the moment, all of
		// these SERVICE clauses are of the form "SERVICE uri {...}"
		// (i.e., not "SERVICE var {...}"). Therefore, all that this
		// implementation here does is to convert the given query
		// pattern into a logical plan.
		final Op jenaOp = ( (SPARQLGraphPattern) query ).asJenaOp();
		return createPlan(jenaOp);
	}

	protected LogicalPlan createPlan( final Op jenaOp ) {
		if ( jenaOp instanceof OpSequence ) {
			return createPlanForSequence( (OpSequence) jenaOp );
		}
		else if ( jenaOp instanceof OpJoin ) {
			return createPlanForJoin( (OpJoin) jenaOp );
		}
		else if ( jenaOp instanceof OpUnion ) {
			return createPlanForUnion( (OpUnion) jenaOp );
		}
		else if ( jenaOp instanceof OpService ) {
			return createPlanForServicePattern( (OpService) jenaOp ); 
		}
		else {
			throw new IllegalArgumentException( "unsupported type of query pattern: " + jenaOp.getClass().getName() );
		}
	}

	protected LogicalPlan createPlanForSequence( final OpSequence jenaOp ) {
		if ( jenaOp.size() == 0 ) {
			throw new IllegalArgumentException( "empty sequence of operators" );
		}

		// convert the sequence of Op objects into a left-deep join tree
		final Iterator<Op> it = jenaOp.iterator();
		LogicalPlan currentSubPlan = createPlan( it.next() );
		while ( it.hasNext() ) {
			currentSubPlan = new LogicalPlanWithBinaryRootImpl(
					new LogicalOpJoin(),
					currentSubPlan,
					createPlan(it.next()) );
		}

		return currentSubPlan;
	}

	protected LogicalPlan createPlanForJoin( final OpJoin jenaOp ) {
		return new LogicalPlanWithBinaryRootImpl( new LogicalOpJoin(),
		                                          createPlan(jenaOp.getLeft()),
		                                          createPlan(jenaOp.getRight()) );
	}

	protected LogicalPlan createPlanForUnion( final OpUnion jenaOp ) {
		return new LogicalPlanWithBinaryRootImpl( new LogicalOpUnion(),
		                                          createPlan(jenaOp.getLeft()),
		                                          createPlan(jenaOp.getRight()) );
	}

	protected LogicalPlan createPlanForServicePattern( final OpService jenaOp ) {
		if ( jenaOp.getService().isVariable() ) {
			throw new IllegalArgumentException( "unsupported SERVICE pattern" );
		}

		final FederationMember fm = fedCat.getFederationMemberByURI( jenaOp.getService().getURI() );
		return createPlan( jenaOp.getSubOp(), fm );
	}

	protected LogicalPlan createPlan( final Op jenaOp, final FederationMember fm ) {
		// If the federation member has a SPARQL endpoint interface, then
		// we can simply wrap the whole query pattern in a single request. 
		if ( fm instanceof SPARQLEndpoint ) {
			final SPARQLRequest req = new SPARQLRequestImpl( new SPARQLGraphPatternImpl(jenaOp) );
			final LogicalOpRequest<SPARQLRequest,SPARQLEndpoint> op = new LogicalOpRequest<>( (SPARQLEndpoint) fm, req );
			return new LogicalPlanWithNullaryRootImpl(op);
		}

		// For all federation members with other types of interfaces,
		// the pattern must be broken into smaller parts.
		if ( jenaOp instanceof OpJoin ) {
			return createPlanForJoin( (OpJoin) jenaOp, fm );
		}
		else if ( jenaOp instanceof OpUnion ) {
			return createPlanForUnion( (OpUnion) jenaOp, fm );
		}
		else if ( jenaOp instanceof OpBGP ) {
			return createPlanForBGP( (OpBGP) jenaOp, fm );
		}
		else {
			throw new IllegalArgumentException( "unsupported type of query pattern: " + jenaOp.getClass().getName() );
		}
	}

	protected LogicalPlan createPlanForJoin( final OpJoin jenaOp, final FederationMember fm ) {
		return new LogicalPlanWithBinaryRootImpl( new LogicalOpJoin(),
		                                          createPlan(jenaOp.getLeft(),fm),
		                                          createPlan(jenaOp.getRight(),fm) );
	}

	protected LogicalPlan createPlanForUnion( final OpUnion jenaOp, final FederationMember fm ) {
		return new LogicalPlanWithBinaryRootImpl( new LogicalOpUnion(),
		                                          createPlan(jenaOp.getLeft(),fm),
		                                          createPlan(jenaOp.getRight(),fm) );
	}

	protected LogicalPlan createPlanForBGP( final OpBGP pattern, final FederationMember fm ) {
		return createPlanForBGP( pattern.getPattern(), fm );
	}

	protected LogicalPlan createPlanForBGP( final BasicPattern pattern, final FederationMember fm ) {
		return createPlanForBGP( QueryPatternUtils.createBGP(pattern), fm );
	}

	protected LogicalPlan createPlanForBGP( final BGP bgp, final FederationMember fm ) {
		// If the federation member has an interface that supports BGP
		// requests, then we can simply create a BGP request operator.
		if ( fm.getInterface().supportsBGPRequests() ) {
			final BGPRequest req = new BGPRequestImpl(bgp);
			final LogicalOpRequest<?,?> op = new LogicalOpRequest<>(fm, req);
			return new LogicalPlanWithNullaryRootImpl(op);
		}

		// If the interface of the federation member does not support
		// BGP requests (but triple pattern requests), then we create
		// a chain of tpAdd operators.

		if ( ! fm.getInterface().supportsTriplePatternRequests() ) {
			throw new IllegalArgumentException( "the given federation member cannot handle triple patterns requests (" + fm.toString() + ")" );
		}

		if ( bgp.getTriplePatterns().size() == 0 ) {
			throw new IllegalArgumentException( "the given BGP is empty" );
		}

		final Iterator<? extends TriplePattern> it = bgp.getTriplePatterns().iterator();

		// first operator in the chain must be a request operator
		final TriplePatternRequest req1 = new TriplePatternRequestImpl( it.next() );
		final LogicalOpRequest<?,?> op1 = new LogicalOpRequest<>( fm, req1 );
		LogicalPlan currentSubPlan = new LogicalPlanWithNullaryRootImpl(op1);

		// add a tpAdd operator for each of the remaining triple patterns
		while ( it.hasNext() ) {
			final LogicalOpTPAdd op = new LogicalOpTPAdd( fm, it.next() );
			currentSubPlan = new LogicalPlanWithUnaryRootImpl( op, currentSubPlan );
		}

		return currentSubPlan;
	}

}
