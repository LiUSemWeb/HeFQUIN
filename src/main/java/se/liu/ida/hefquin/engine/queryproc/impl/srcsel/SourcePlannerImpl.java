package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementUnion;

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
import se.liu.ida.hefquin.engine.query.jenaimpl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.jenaimpl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
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
		final Element pattern = ( (SPARQLGraphPattern) query ).asJenaElement();
		return createPlan(pattern);
	}

	protected LogicalPlan createPlan( final Element pattern ) {
		if ( pattern instanceof ElementService ) {
			return createPlanForServicePattern( (ElementService) pattern ); 
		}
		else if ( pattern instanceof ElementGroup ) {
			final ElementGroup group = (ElementGroup) pattern;
			final int groupSize = group.size();

			if ( groupSize == 0 ) {
				throw new IllegalArgumentException( "empty group pattern" );
			}

			// create a left-deep join tree for group patterns
			LogicalPlan currentSubPlan = createPlan( group.get(0) );
			for ( int i = 1; i < groupSize; ++i ) {
				currentSubPlan = new LogicalPlanWithBinaryRootImpl( new LogicalOpJoin(),
				                                                    currentSubPlan,
				                                                    createPlan(group.get(i)) );
			}
			return currentSubPlan;
		}
		else {
			throw new IllegalArgumentException( "unsupported type of query pattern: " + pattern.getClass().getName() );
		}
	}

	protected LogicalPlan createPlanForServicePattern( final ElementService pattern ) {
		if ( pattern.getServiceNode().isVariable() ) {
			throw new IllegalArgumentException( "unsupported SERVICE pattern" );
		}

		final FederationMember fm = fedCat.getFederationMemberByURI( pattern.getServiceNode().getURI() );
		return createPlan( pattern.getElement(), fm );
	}

	protected LogicalPlan createPlan( final Element pattern, final FederationMember fm ) {
		// If the federation member has a SPARQL endpoint interface, then
		// we can simply wrap the whole query pattern in a single request. 
		if ( fm instanceof SPARQLEndpoint ) {
			final SPARQLRequest req = new SPARQLRequestImpl( new SPARQLGraphPatternImpl(pattern) );
			final LogicalOpRequest<SPARQLRequest,SPARQLEndpoint> op = new LogicalOpRequest<>( (SPARQLEndpoint) fm, req );
			return new LogicalPlanWithNullaryRootImpl(op);
		}

		// For all federation members with other types of interfaces,
		// the pattern must be broken into smaller parts.
		if ( pattern instanceof ElementGroup ) {
			return createPlanForGroupPattern( (ElementGroup) pattern, fm );
		}
		else if ( pattern instanceof ElementUnion ) {
			return createPlanForUnionPattern( (ElementUnion) pattern, fm );
		}
		else if ( pattern instanceof ElementPathBlock ) {
			return createPlanForBGP( ((ElementPathBlock) pattern).getPattern(), fm );
		}
		else {
			throw new IllegalArgumentException( "unsupported type of query pattern: " + pattern.getClass().getName() );
		}
	}

	protected LogicalPlan createPlanForGroupPattern( final ElementGroup group, final FederationMember fm ) {
		if ( group.size() == 0 ) {
			throw new IllegalArgumentException( "empty group pattern" );
		}

		// create a left-deep join tree for group patterns
		final Iterator<Element> it = group.getElements().iterator();
		LogicalPlan currentSubPlan = createPlan( it.next(), fm );
		while ( it.hasNext() ) {
			currentSubPlan = new LogicalPlanWithBinaryRootImpl( new LogicalOpJoin(),
			                                                    currentSubPlan,
			                                                    createPlan(it.next(),fm) );
		}
		return currentSubPlan;
	}

	protected LogicalPlan createPlanForUnionPattern( final ElementUnion pattern, final FederationMember fm ) {
		// create multiway union operator for a union pattern
		final List<LogicalPlan> subPlans = new ArrayList<>();
		final Iterator<Element> it = pattern.getElements().iterator();
		while ( it.hasNext() ) {
			final LogicalPlan subPlan = createPlan( it.next(), fm );
			subPlans.add(subPlan);
		}

		if ( subPlans.size() == 0 ) {
			throw new IllegalArgumentException( "empty union pattern" );
		}

		// If there was only one subpattern in the union pattern,
		// then there is no need to create the union operator.
		if ( subPlans.size() == 1 ) {
			return subPlans.get(0);
		}

		return new LogicalPlanWithNaryRootImpl( new LogicalOpMultiwayUnion(), subPlans );
	}

	protected LogicalPlan createPlanForBGP( final PathBlock pattern, final FederationMember fm ) {
		return createPlanForBGP( QueryPatternUtils.createBGP(pattern), fm );
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
