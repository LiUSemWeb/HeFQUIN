package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.core.BasicPattern;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.BGPImpl;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningException;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningStats;

/**
 * This implementation of {@link SourcePlanner} does not actually perform
 * query decomposition and source selection but simply assumes queries with
 * SERVICE clauses where, for the moment, all of these SERVICE clauses are
 * of the form "SERVICE uri {...}" (i.e., not "SERVICE var {...}"). Therefore,
 * all that this implementation does is to convert the given query pattern
 * into a logical plan.
 */
public class ServiceClauseBasedSourcePlannerImpl extends SourcePlannerBase
{
	public ServiceClauseBasedSourcePlannerImpl( final QueryProcContext ctxt ) {
		super(ctxt);
	}

	@Override
	protected Pair<LogicalPlan, SourcePlanningStats> createSourceAssignment( final Op jenaOp )
			throws SourcePlanningException
	{
		final LogicalPlan sa = createPlan(jenaOp);
		final SourcePlanningStats myStats = new SourcePlanningStatsImpl();

		return new Pair<>(sa, myStats);
	}

	protected LogicalPlan createPlan( final Op jenaOp ) {
		if ( jenaOp instanceof OpSequence ) {
			return createPlanForSequence( (OpSequence) jenaOp );
		}
		else if ( jenaOp instanceof OpJoin ) {
			return createPlanForJoin( (OpJoin) jenaOp );
		}
		else if ( jenaOp instanceof OpLeftJoin ) {
			return createPlanForLeftJoin( (OpLeftJoin) jenaOp );
		}
		else if ( jenaOp instanceof OpConditional ) {
			return createPlanForLeftJoin( (OpConditional) jenaOp );
		}
		else if ( jenaOp instanceof OpUnion ) {
			return createPlanForUnion( (OpUnion) jenaOp );
		}
		else if ( jenaOp instanceof OpFilter ) {
			return createPlanForFilter( (OpFilter) jenaOp );
		}
		else if ( jenaOp instanceof OpExtend ) {
			return createPlanForBind( (OpExtend) jenaOp );
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

		// convert the sequence of Op objects into a multiway join
		final List<LogicalPlan> subPlans = new ArrayList<>();
		for ( final Op subOp : jenaOp.getElements() ) {
			subPlans.add( createPlan(subOp) );
		}
		return mergeIntoMultiwayJoin(subPlans);
	}

	protected LogicalPlan createPlanForJoin( final OpJoin jenaOp ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft() );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight() );
		return mergeIntoMultiwayJoin(leftSubPlan,rightSubPlan);
	}

	protected LogicalPlan createPlanForLeftJoin( final OpLeftJoin jenaOp ) {
		if ( jenaOp.getExprs() != null && ! jenaOp.getExprs().isEmpty() ) {
			throw new IllegalArgumentException( "OpLeftJoin with filter condition is not supported" );
		}

		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft() );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight() );
		return mergeIntoMultiwayLeftJoin(leftSubPlan, rightSubPlan);
	}

	protected LogicalPlan createPlanForLeftJoin( final OpConditional jenaOp ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft() );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight() );
		return mergeIntoMultiwayLeftJoin(leftSubPlan, rightSubPlan);
	}

	protected LogicalPlan createPlanForUnion( final OpUnion jenaOp ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft() );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight() );
		return mergeIntoMultiwayUnion(leftSubPlan,rightSubPlan);
	}

	protected LogicalPlan createPlanForFilter( final OpFilter jenaOp ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp() );
		final LogicalOpFilter rootOp = new LogicalOpFilter( jenaOp.getExprs() );
		return new LogicalPlanWithUnaryRootImpl(rootOp, subPlan);
	}

	protected LogicalPlan createPlanForBind( final OpExtend jenaOp ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp() );
		final LogicalOpBind rootOp = new LogicalOpBind( jenaOp.getVarExprList() );
		return new LogicalPlanWithUnaryRootImpl(rootOp, subPlan);
	}

	protected LogicalPlan createPlanForServicePattern( final OpService jenaOp ) {
		if ( jenaOp.getService().isVariable() ) {
			throw new IllegalArgumentException( "unsupported SERVICE pattern" );
		}

		final FederationMember fm = ctxt.getFederationCatalog().getFederationMemberByURI( jenaOp.getService().getURI() );
		return createPlan( jenaOp.getSubOp(), fm );
	}

	protected LogicalPlan createPlan( final Op jenaOp, final FederationMember fm ) {
		// If the federation member has a SPARQL endpoint interface, then
		// we can simply wrap the whole query pattern in a single request.
		if ( fm instanceof SPARQLEndpoint ) {
			if ( jenaOp instanceof OpBGP ) {
				// If possible, create an explicit BGP request operator
				// rather than a general SPARQL pattern request operator
				// because that causes few checks and casts further down
				// in the query planning pipeline.
				return createPlanForBGP( (OpBGP) jenaOp, fm );
			}
			else {
				final SPARQLRequest req = new SPARQLRequestImpl( new GenericSPARQLGraphPatternImpl2(jenaOp) );
				final LogicalOpRequest<SPARQLRequest,SPARQLEndpoint> op = new LogicalOpRequest<>( (SPARQLEndpoint) fm, req );
				return new LogicalPlanWithNullaryRootImpl(op);
			}
		}

		// For all federation members with other types of interfaces,
		// the pattern must be broken into smaller parts.
		if ( jenaOp instanceof OpJoin ) {
			return createPlanForJoin( (OpJoin) jenaOp, fm );
		}
		else if ( jenaOp instanceof OpLeftJoin ) {
			return createPlanForLeftJoin( (OpLeftJoin) jenaOp, fm );
		}
		else if ( jenaOp instanceof OpConditional ) {
			return createPlanForLeftJoin( (OpConditional) jenaOp, fm );
		}
		else if ( jenaOp instanceof OpUnion ) {
			return createPlanForUnion( (OpUnion) jenaOp, fm );
		}
		else if ( jenaOp instanceof OpFilter ) {
			return createPlanForFilter( (OpFilter) jenaOp, fm );
		}
		else if ( jenaOp instanceof OpBGP ) {
			return createPlanForBGP( (OpBGP) jenaOp, fm );
		}
		else {
			throw new IllegalArgumentException( "unsupported type of query pattern: " + jenaOp.getClass().getName() );
		}
	}

	protected LogicalPlan createPlanForJoin( final OpJoin jenaOp, final FederationMember fm ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), fm );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), fm );
		return mergeIntoMultiwayJoin(leftSubPlan,rightSubPlan);
	}

	protected LogicalPlan createPlanForLeftJoin( final OpLeftJoin jenaOp, final FederationMember fm ) {
		if ( jenaOp.getExprs() != null && ! jenaOp.getExprs().isEmpty() ) {
			throw new IllegalArgumentException( "OpLeftJoin with filter condition is not supported" );
		}

		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), fm );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), fm );
		return mergeIntoMultiwayLeftJoin(leftSubPlan, rightSubPlan);
	}

	protected LogicalPlan createPlanForLeftJoin( final OpConditional jenaOp, final FederationMember fm ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), fm );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), fm );
		return mergeIntoMultiwayLeftJoin(leftSubPlan, rightSubPlan);
	}

	protected LogicalPlan createPlanForUnion( final OpUnion jenaOp, final FederationMember fm ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), fm );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), fm );
		return mergeIntoMultiwayUnion(leftSubPlan,rightSubPlan);
	}

	protected LogicalPlan createPlanForFilter( final OpFilter jenaOp, final FederationMember fm ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp(), fm );
		final LogicalOpFilter rootOp = new LogicalOpFilter( jenaOp.getExprs() );
		return new LogicalPlanWithUnaryRootImpl(rootOp, subPlan);
	}

	protected LogicalPlan createPlanForBGP( final OpBGP pattern, final FederationMember fm ) {
		return createPlanForBGP( pattern.getPattern(), fm );
	}

	protected LogicalPlan createPlanForBGP( final BasicPattern pattern, final FederationMember fm ) {
		return createPlanForBGP( new BGPImpl(pattern), fm );
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
		// a multiway join of triple pattern request operators.

		if ( ! fm.getInterface().supportsTriplePatternRequests() ) {
			throw new IllegalArgumentException( "the given federation member cannot handle triple patterns requests (" + fm.toString() + ")" );
		}

		if ( bgp.getTriplePatterns().size() == 0 ) {
			throw new IllegalArgumentException( "the given BGP is empty" );
		}

		final List<LogicalPlan> subPlans = new ArrayList<>();
		for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
			final TriplePatternRequest req = new TriplePatternRequestImpl(tp);
			final LogicalOpRequest<?,?> op = new LogicalOpRequest<>(fm, req);
			final LogicalPlan subPlan = new LogicalPlanWithNullaryRootImpl(op);
			subPlans.add( subPlan );
		}

		return mergeIntoMultiwayJoin(subPlans);
	}

	protected LogicalPlan mergeIntoMultiwayJoin( final LogicalPlan ... subPlans ) {
		if ( subPlans.length == 1 ) {
			return subPlans[0];
		}

		return mergeIntoMultiwayJoin( Arrays.asList(subPlans) );
	}

	protected LogicalPlan mergeIntoMultiwayJoin( final List<LogicalPlan> subPlans ) {
		if ( subPlans.size() == 1 ) {
			return subPlans.get(0);
		}

		final List<LogicalPlan> subPlansFlattened = new ArrayList<>();

		for ( final LogicalPlan subPlan : subPlans ) {
			if ( subPlan.getRootOperator() instanceof LogicalOpMultiwayJoin ) {
				for ( int j = 0; j < subPlan.numberOfSubPlans(); ++j ) {
					subPlansFlattened.add( subPlan.getSubPlan(j) );
				}
			}
			else {
				subPlansFlattened.add( subPlan );
			}
		}

		return new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayJoin.getInstance(),
		                                        subPlansFlattened );
	}

	protected LogicalPlan mergeIntoMultiwayLeftJoin( final LogicalPlan leftSubPlan,
	                                                 final LogicalPlan rightSubPlan ) {
		final List<LogicalPlan> children = new ArrayList<>();

		final LogicalOperator leftRootOp = leftSubPlan.getRootOperator();
		if ( leftRootOp instanceof LogicalOpMultiwayLeftJoin ) {
			for ( int i = 0; i < leftSubPlan.numberOfSubPlans(); i++ ) {
				children.add( leftSubPlan.getSubPlan(i) );
			}
		}
		else if ( leftRootOp instanceof LogicalOpRightJoin ) {
			children.add( leftSubPlan.getSubPlan(1) );
			children.add( leftSubPlan.getSubPlan(0) );
		}
		else {
			children.add( leftSubPlan );
		}

		children.add( rightSubPlan );

		return new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayLeftJoin.getInstance(), children );
	}

	protected LogicalPlan mergeIntoMultiwayUnion( final LogicalPlan ... subPlans ) {
		if ( subPlans.length == 1 ) {
			return subPlans[0];
		}

		final List<LogicalPlan> subPlansFlattened = new ArrayList<>();

		for ( int i = 0; i < subPlans.length; ++i ) {
			if ( subPlans[i].getRootOperator() instanceof LogicalOpMultiwayUnion ) {
				for ( int j = 0; j < subPlans[i].numberOfSubPlans(); ++j ) {
					subPlansFlattened.add( subPlans[i].getSubPlan(j) );
				}
			}
			else {
				subPlansFlattened.add( subPlans[i] );
			}
		}

		return new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(),
		                                        subPlansFlattened );
	}

}
