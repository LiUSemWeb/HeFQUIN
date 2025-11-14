package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.BGPImpl;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningException;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningStats;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BGPRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;
import se.liu.ida.hefquin.jenaext.sparql.algebra.op.OpServiceWithParams;

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
	@Override
	protected Pair<LogicalPlan, SourcePlanningStats> createSourceAssignment( final Op jenaOp,
	                                                                         final QueryProcContext ctxt )
			throws SourcePlanningException
	{
		final LogicalPlan sa = createPlan(jenaOp, ctxt);
		final SourcePlanningStats myStats = new SourcePlanningStatsImpl();

		return new Pair<>(sa, myStats);
	}

	protected LogicalPlan createPlan( final Op jenaOp, final QueryProcContext ctxt ) {
		if ( jenaOp instanceof OpSequence opSeq ) {
			return createPlanForSequence(opSeq, ctxt);
		}
		else if ( jenaOp instanceof OpJoin opJoin ) {
			return createPlanForJoin(opJoin, ctxt);
		}
		else if ( jenaOp instanceof OpLeftJoin opLJoin ) {
			return createPlanForLeftJoin(opLJoin, ctxt);
		}
		else if ( jenaOp instanceof OpConditional opCond ) {
			return createPlanForLeftJoin(opCond, ctxt);
		}
		else if ( jenaOp instanceof OpUnion opUnion ) {
			return createPlanForUnion(opUnion, ctxt);
		}
		else if ( jenaOp instanceof OpFilter opFilter ) {
			return createPlanForFilter(opFilter, ctxt);
		}
		else if ( jenaOp instanceof OpExtend opExtend ) {
			return createPlanForBind(opExtend, ctxt);
		}
		else if ( jenaOp instanceof OpTable opTable ) {
			return createPlanForValues(opTable, ctxt);
		}
		else if ( jenaOp instanceof OpService opService ) {
			return createPlanForServicePattern(opService, ctxt); 
		}
		else {
			throw new IllegalArgumentException( "unsupported type of query pattern: " + jenaOp.getClass().getName() );
		}
	}

	protected LogicalPlan createPlanForSequence( final OpSequence jenaOp,
	                                             final QueryProcContext ctxt ) {
		if ( jenaOp.size() == 0 ) {
			throw new IllegalArgumentException( "empty sequence of operators" );
		}

		// convert the sequence of Op objects into a multiway join
		final List<LogicalPlan> subPlans = new ArrayList<>();
		for ( final Op subOp : jenaOp.getElements() ) {
			subPlans.add( createPlan(subOp, ctxt) );
		}
		return mergeIntoMultiwayJoin(subPlans);
	}

	protected LogicalPlan createPlanForJoin( final OpJoin jenaOp,
	                                         final QueryProcContext ctxt ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), ctxt );

		if ( jenaOp.getRight() instanceof OpServiceWithParams opService )
			return createPlanForServiceWithParams(opService, leftSubPlan, ctxt);
		else {
			final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), ctxt );
			return mergeIntoMultiwayJoin(leftSubPlan,rightSubPlan);
		}
	}

	protected LogicalPlan createPlanForLeftJoin( final OpLeftJoin jenaOp,
	                                             final QueryProcContext ctxt) {
		if ( jenaOp.getExprs() != null && ! jenaOp.getExprs().isEmpty() ) {
			throw new IllegalArgumentException( "OpLeftJoin with filter condition is not supported" );
		}

		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), ctxt );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), ctxt );
		return mergeIntoMultiwayLeftJoin(leftSubPlan, rightSubPlan);
	}

	protected LogicalPlan createPlanForLeftJoin( final OpConditional jenaOp,
	                                             final QueryProcContext ctxt ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), ctxt );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), ctxt );
		return mergeIntoMultiwayLeftJoin(leftSubPlan, rightSubPlan);
	}

	protected LogicalPlan createPlanForUnion( final OpUnion jenaOp,
	                                          final QueryProcContext ctxt ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), ctxt );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), ctxt );
		return mergeIntoMultiwayUnion(leftSubPlan,rightSubPlan);
	}

	protected LogicalPlan createPlanForFilter( final OpFilter jenaOp,
	                                           final QueryProcContext ctxt ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp(), ctxt );
		final LogicalOpFilter rootOp = new LogicalOpFilter( jenaOp.getExprs() );
		return new LogicalPlanWithUnaryRootImpl(rootOp, subPlan);
	}

	protected LogicalPlan createPlanForBind( final OpExtend jenaOp,
	                                         final QueryProcContext ctxt ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp(), ctxt );
		final LogicalOpBind rootOp = new LogicalOpBind( jenaOp.getVarExprList() );
		return new LogicalPlanWithUnaryRootImpl(rootOp, subPlan);
	}

	protected LogicalPlan createPlanForValues( final OpTable jenaOp,
	                                           final QueryProcContext ctxt ) {
		if ( jenaOp.getTable().size() != 1 )
			// We shouldn't end up here. The only case in which we have
			// an OpTable is if 'nextStage' of 'MainQueryIterator' in
			// 'OpExecutorHeFQUIN' adds it explicitly.
			throw new IllegalStateException();

		final Binding sm = jenaOp.getTable().rows().next();
		final SolutionMapping solmap = new SolutionMappingImpl(sm);
		final LogicalOpFixedInput rootOp = new LogicalOpFixedInput(solmap);
		return new LogicalPlanWithNullaryRootImpl(rootOp);
	}

	/**
	 * This function assumes that the given operator comes from a SERVICE
	 * clause that did not have a PARAMS part (or an empty one), and it
	 * produces a plan with a request operator as root.
	 */
	protected LogicalPlan createPlanForServicePattern( final OpService jenaOp,
	                                                   final QueryProcContext ctxt ) {
		if ( jenaOp.getService().isVariable() )
			throw new IllegalArgumentException( "unsupported SERVICE clause" );

		if (    jenaOp instanceof OpServiceWithParams op
		     && op.getParamVars() != null
		     && ! op.getParamVars().isEmpty() )
			throw new IllegalArgumentException( "Unsupported SERVICE clause: group graph patterns that begin with a SERVICE clause with PARAMS are not supported yet." );

		final FederationMember fm = ctxt.getFederationCatalog().getFederationMemberByURI( jenaOp.getService().getURI() );

		if ( fm instanceof WrappedRESTEndpoint ep ) {
			if ( ep.getNumberOfParameters() != 0 )
				throw new IllegalArgumentException( "Invalid SERVICE clause: missing PARAMS for " + ep.toString() );

			final SPARQLGraphPattern p =  new GenericSPARQLGraphPatternImpl2( jenaOp.getSubOp() );
			final SPARQLRequest req = new SPARQLRequestImpl(p);
			final LogicalOpRequest<?,?> op = new LogicalOpRequest<>(ep, req);
			return new LogicalPlanWithNullaryRootImpl(op);
		}

		return createPlan( jenaOp.getSubOp(), fm );
	}

	/**
	 * This function assumes that the given operator comes from a SERVICE
	 * clause that had a nonempty PARAMS part, and it produces a plan with
	 * a gpAdd operator as root and the given subplan as input to this
	 * root operator.
	 */
	protected LogicalPlan createPlanForServiceWithParams( final OpServiceWithParams jenaOp,
	                                                      final LogicalPlan subplan,
	                                                      final QueryProcContext ctxt ) {
		if ( jenaOp.getService().isVariable() )
			throw new IllegalArgumentException( "unsupported SERVICE pattern" );

		final FederationMember fm = ctxt.getFederationCatalog().getFederationMemberByURI( jenaOp.getService().getURI() );

		if ( ! (fm instanceof WrappedRESTEndpoint) )
			throw new IllegalArgumentException( "Invalid SERVICE clause: PARAMS cannot be used for " + fm.toString() );

		final WrappedRESTEndpoint ep = (WrappedRESTEndpoint) fm;

		final List<Var> paramVars = jenaOp.getParamVars();
		assert paramVars != null;
		assert ! paramVars.isEmpty();

		if ( ep.getNumberOfParameters() != paramVars.size() )
			throw new IllegalArgumentException( "Invalid SERVICE clause: wrong number of PARAMS for " + ep.toString() );

		final SPARQLGraphPattern p =  new GenericSPARQLGraphPatternImpl2( jenaOp.getSubOp() );
		final LogicalOpGPAdd op = new LogicalOpGPAdd(ep, p, paramVars);
		return new LogicalPlanWithUnaryRootImpl(op, subplan);
	}

	protected LogicalPlan createPlan( final Op jenaOp, final FederationMember fm ) {
		// If the federation member has a SPARQL endpoint interface, then
		// we can simply wrap the whole query pattern in a single request.
		if ( fm instanceof SPARQLEndpoint ) {
			if ( jenaOp instanceof OpBGP opBGP ) {
				// If possible, create an explicit BGP request operator
				// rather than a general SPARQL pattern request operator
				// because that causes fewer checks and casts further
				// down in the query planning pipeline.
				return createPlanForBGP(opBGP, fm);
			}
			else if ( jenaOp instanceof OpTriple opTP ) {
				// Likewise for triple patterns
				return createPlanForTriplePattern(opTP, fm);
			}
			else {
				final SPARQLRequest req = new SPARQLRequestImpl( new GenericSPARQLGraphPatternImpl2(jenaOp) );
				final LogicalOpRequest<SPARQLRequest,SPARQLEndpoint> op = new LogicalOpRequest<>( (SPARQLEndpoint) fm, req );
				return new LogicalPlanWithNullaryRootImpl(op);
			}
		}

		// For all federation members with other types of interfaces,
		// the pattern must be broken into smaller parts.
		if ( jenaOp instanceof OpJoin opJoin ) {
			return createPlanForJoin(opJoin, fm);
		}
		else if ( jenaOp instanceof OpLeftJoin opLJ ) {
			return createPlanForLeftJoin(opLJ, fm);
		}
		else if ( jenaOp instanceof OpConditional opCond ) {
			return createPlanForLeftJoin(opCond, fm);
		}
		else if ( jenaOp instanceof OpUnion opUnion ) {
			return createPlanForUnion(opUnion, fm);
		}
		else if ( jenaOp instanceof OpFilter opFilter ) {
			return createPlanForFilter(opFilter, fm);
		}
		else if ( jenaOp instanceof OpBGP opBGP ) {
			return createPlanForBGP(opBGP, fm);
		}
		else if ( jenaOp instanceof OpTriple opTP ) {
			return createPlanForTriplePattern(opTP, fm);
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

	protected LogicalPlan createPlanForTriplePattern( final OpTriple pattern,
	                                                  final FederationMember fm ) {
		final TriplePattern tp = new TriplePatternImpl( pattern.getTriple() );
		final TriplePatternRequest req = new TriplePatternRequestImpl(tp);
		final LogicalOpRequest<?,?> op = new LogicalOpRequest<>(fm, req);
		return new LogicalPlanWithNullaryRootImpl(op);
	}

	protected LogicalPlan createPlanForBGP( final BGP bgp, final FederationMember fm ) {
		// If the federation member has an interface that supports only
		// triple pattern requests, ...
		if ( ! fm.supportsMoreThanTriplePatterns() ) {
			// ... then we create a multiway join of triple pattern request
			// operators.
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

		// Otherwise, if the federation member supports BGP requests, ...
		if ( fm.isSupportedPattern(bgp) ) {
			// ... then we can simply create a BGP request operator.
			final BGPRequest req = new BGPRequestImpl(bgp);
			final LogicalOpRequest<?,?> op = new LogicalOpRequest<>(fm, req);
			return new LogicalPlanWithNullaryRootImpl(op);
		}

		throw new IllegalArgumentException( "the given federation member cannot handle triple patterns requests (" + fm.toString() + ")" );
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
