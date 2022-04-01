package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
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
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningException;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningStats;
import se.liu.ida.hefquin.engine.utils.Pair;

public class SourcePlannerImpl implements SourcePlanner
{
	protected final QueryProcContext ctxt;

	public SourcePlannerImpl( final QueryProcContext ctxt ) {
		assert ctxt != null;
		this.ctxt = ctxt;
	}

	@Override
	public Pair<LogicalPlan, SourcePlanningStats> createSourceAssignment( final Query query )
			throws SourcePlanningException
	{
		// The current implementation here does not actually perform
		// query decomposition and source selection but simply assumes
		// queries with SERVICE clauses where, for the moment, all of
		// these SERVICE clauses are of the form "SERVICE uri {...}"
		// (i.e., not "SERVICE var {...}"). Therefore, all that this
		// implementation here does is to convert the given query
		// pattern into a logical plan.
		final Op jenaOp;
		if ( query instanceof GenericSPARQLGraphPatternImpl1 ) {
			@SuppressWarnings("deprecation")
			final Op o = ( (GenericSPARQLGraphPatternImpl1) query ).asJenaOp();
			jenaOp = o;
		}
		else if ( query instanceof GenericSPARQLGraphPatternImpl2 ) {
			jenaOp = ( (GenericSPARQLGraphPatternImpl2) query ).asJenaOp();
		}
		else {
			throw new UnsupportedOperationException( query.getClass().getName() );
		}

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
		else if ( jenaOp instanceof OpUnion ) {
			return createPlanForUnion( (OpUnion) jenaOp );
		}
		else if ( jenaOp instanceof OpFilter ) {
			return createPlanForFilter( (OpFilter) jenaOp );
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
