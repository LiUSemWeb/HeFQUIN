package se.liu.ida.hefquin.engine.queryproc.impl.srcsel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

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
		final LogicalPlan sa = createPlan(jenaOp, false, ctxt);
		final SourcePlanningStats myStats = new SourcePlanningStatsImpl();

		return new Pair<>(sa, myStats);
	}

	protected LogicalPlan createPlan( final Op jenaOp, final boolean mayReduce, final QueryProcContext ctxt ) {
		if ( jenaOp instanceof OpSequence opSeq ) {
			return createPlanForSequence(opSeq, mayReduce, ctxt);
		}
		else if ( jenaOp instanceof OpJoin opJoin ) {
			return createPlanForJoin(opJoin, mayReduce, ctxt);
		}
		else if ( jenaOp instanceof OpLeftJoin opLJoin ) {
			return createPlanForLeftJoin(opLJoin, mayReduce, ctxt);
		}
		else if ( jenaOp instanceof OpMinus opMinus ) {
			return createPlanForMinus(opMinus, mayReduce, ctxt);
		}
		else if ( jenaOp instanceof OpConditional opCond ) {
			return createPlanForLeftJoin(opCond, mayReduce, ctxt);
		}
		else if ( jenaOp instanceof OpUnion opUnion ) {
			return createPlanForUnion(opUnion, mayReduce, ctxt);
		}
		else if ( jenaOp instanceof OpFilter opFilter ) {
			return createPlanForFilter(opFilter, mayReduce, ctxt);
		}
		else if ( jenaOp instanceof OpExtend opExtend ) {
			return createPlanForBind(opExtend, mayReduce, ctxt);
		}
		else if ( jenaOp instanceof OpUnfold opUnfold ) {
			return createPlanForUnfold(opUnfold, mayReduce, ctxt);
		}
		else if ( jenaOp instanceof OpTable opTable ) {
			return createPlanForValues(opTable, ctxt);
		}
		else if ( jenaOp instanceof OpService opService ) {
			return createPlanForServicePattern(opService, mayReduce, ctxt);
		}
		else if ( jenaOp instanceof OpDistinct opDistinct ) {
			return createPlanForDistinct(opDistinct, ctxt);
		}
		else if ( jenaOp instanceof OpProject opProject ) {
			return createPlanForProject(opProject, mayReduce, ctxt);
		}
		else {
			throw new IllegalArgumentException( "unsupported type of query pattern: " + jenaOp.getClass().getName() );
		}
	}

	protected LogicalPlan createPlanForSequence( final OpSequence jenaOp,
	                                             final boolean mayReduce,
	                                             final QueryProcContext ctxt ) {
		if ( jenaOp.size() == 0 ) {
			throw new IllegalArgumentException( "empty sequence of operators" );
		}

		return createPlanForJoin( jenaOp.getElements(), mayReduce, ctxt );
	}

	protected LogicalPlan createPlanForJoin( final OpJoin jenaOp,
	                                         final boolean mayReduce,
	                                         final QueryProcContext ctxt ) {
		final List<Op> ops = List.of( jenaOp.getLeft(), jenaOp.getRight() );
		return createPlanForJoin(ops, mayReduce, ctxt);
	}

	protected LogicalPlan createPlanForJoin( final List<Op> ops,
	                                         final boolean mayReduce,
	                                         final QueryProcContext ctxt ) {
		// Convert the list of Op objects into a multiway join,
		// but ignore OpServiceWithParams objects in this step,
		// as well as OpExtend objects that have an OpTable with
		// the empty solution mapping as their sub-operator (but
		// collect them for the next step).
		final List<OpServiceWithParams> collectedOpSWP = new ArrayList<>();
		final List<OpExtend> collectedOpExtend = new ArrayList<>();
		final List<LogicalPlan> subPlans = new ArrayList<>();
		for ( final Op subOp : ops ) {
			if ( subOp instanceof OpServiceWithParams opService )
				collectedOpSWP.add(opService);
			else if (    subOp instanceof OpExtend opExtend
			          && opExtend.getSubOp() instanceof OpTable opTable
			          && opTable.isJoinIdentity() )
				collectedOpExtend.add(opExtend);
			else
				subPlans.add( createPlan(subOp, mayReduce, ctxt) );
		}

		if ( subPlans.isEmpty() && collectedOpExtend.isEmpty() )
			throw new IllegalArgumentException( "Unsupported SERVICE clause: group graph patterns that begin with a SERVICE clause with PARAMS are not supported yet." );

		LogicalPlan plan;
		if ( ! subPlans.isEmpty() )
			plan = mergeIntoMultiwayJoin(subPlans, mayReduce);
		else
			plan = createPlan( OpTable.unit(), mayReduce, ctxt );

		// Now handle the collected OpExtend objects.
		for ( final OpExtend opExtend : collectedOpExtend ) {
			final LogicalOpBind lop = new LogicalOpBind( opExtend.getVarExprList(), mayReduce );
			plan = new LogicalPlanWithUnaryRootImpl(lop, null, plan);
		}

		// Now handle the collected OpServiceWithParams objects.
		for ( final OpServiceWithParams opService : collectedOpSWP ) {
			plan = createPlanForServiceWithParams(opService, mayReduce, plan, ctxt);
		}

		return plan;
	}

	protected LogicalPlan createPlanForLeftJoin( final OpLeftJoin jenaOp,
	                                             final boolean mayReduce,
	                                             final QueryProcContext ctxt) {
		if ( jenaOp.getExprs() != null && ! jenaOp.getExprs().isEmpty() ) {
			throw new IllegalArgumentException( "OpLeftJoin with filter condition is not supported" );
		}

		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), mayReduce, ctxt );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), mayReduce, ctxt );
		return mergeIntoMultiwayLeftJoin(leftSubPlan, rightSubPlan, mayReduce);
	}

	protected LogicalPlan createPlanForLeftJoin( final OpConditional jenaOp,
	                                             final boolean mayReduce,
	                                             final QueryProcContext ctxt ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), mayReduce, ctxt );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), mayReduce, ctxt );
		return mergeIntoMultiwayLeftJoin(leftSubPlan, rightSubPlan, mayReduce);
	}

	protected LogicalPlan createPlanForMinus( final OpMinus jenaOp,
	                                          final boolean mayReduce,
	                                          final QueryProcContext ctxt ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), mayReduce, ctxt );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), mayReduce, ctxt );
		final LogicalOpMinus rootOp = LogicalOpMinus.getInstance(mayReduce);
		return new LogicalPlanWithBinaryRootImpl(rootOp,null, leftSubPlan, rightSubPlan);
	}

	protected LogicalPlan createPlanForUnion( final OpUnion jenaOp,
	                                          final boolean mayReduce,
	                                          final QueryProcContext ctxt ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), mayReduce, ctxt );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), mayReduce, ctxt );
		return mergeIntoMultiwayUnion(mayReduce, leftSubPlan,rightSubPlan);
	}

	protected LogicalPlan createPlanForFilter( final OpFilter jenaOp,
	                                           final boolean mayReduce,
	                                           final QueryProcContext ctxt ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp(), mayReduce, ctxt );
		final LogicalOpFilter rootOp = new LogicalOpFilter( jenaOp.getExprs(), mayReduce );
		return new LogicalPlanWithUnaryRootImpl(rootOp, null, subPlan);
	}

	protected LogicalPlan createPlanForBind( final OpExtend jenaOp,
	                                         final boolean mayReduce,
	                                         final QueryProcContext ctxt ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp(), mayReduce, ctxt );
		final LogicalOpBind rootOp = new LogicalOpBind( jenaOp.getVarExprList(),  mayReduce );
		return new LogicalPlanWithUnaryRootImpl(rootOp, null, subPlan);
	}

	protected LogicalPlan createPlanForUnfold( final OpUnfold jenaOp,
	                                           final boolean mayReduce,
	                                           final QueryProcContext ctxt ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp(), mayReduce, ctxt );
		final LogicalOpUnfold rootOp = new LogicalOpUnfold( jenaOp.getExpr(),
		                                                    jenaOp.getVar1(),
		                                                    jenaOp.getVar2(),
		                                                    mayReduce );
		return new LogicalPlanWithUnaryRootImpl(rootOp, null, subPlan);
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
		final LogicalOpFixedSolMap rootOp = new LogicalOpFixedSolMap(solmap);
		return new LogicalPlanWithNullaryRootImpl(rootOp, null);
	}

	/**
	 * This function assumes that the given operator comes from a SERVICE
	 * clause that did not have a PARAMS part (or an empty one), and it
	 * produces a plan with a request operator as root.
	 */
	protected LogicalPlan createPlanForServicePattern( final OpService jenaOp,
	                                                   final boolean mayReduce,
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
			final LogicalOpRequest<?,?> op = new LogicalOpRequest<>(ep, mayReduce, req);
			return new LogicalPlanWithNullaryRootImpl(op, null);
		}

		return createPlan( jenaOp.getSubOp(), mayReduce, fm );
	}

	/**
	 * This function assumes that the given operator comes from a SERVICE
	 * clause that had a nonempty PARAMS part, and it produces a plan with
	 * a gpAdd operator as root and the given subplan as input to this
	 * root operator.
	 */
	protected LogicalPlan createPlanForServiceWithParams( final OpServiceWithParams jenaOp,
	                                                      final boolean mayReduce,
	                                                      final LogicalPlan subplan,
	                                                      final QueryProcContext ctxt ) {
		if ( jenaOp.getService().isVariable() )
			throw new IllegalArgumentException( "unsupported SERVICE pattern" );

		final FederationMember fm = ctxt.getFederationCatalog().getFederationMemberByURI( jenaOp.getService().getURI() );

		if ( ! (fm instanceof WrappedRESTEndpoint) )
			throw new IllegalArgumentException( "Invalid SERVICE clause: PARAMS cannot be used for " + fm.toString() );

		final WrappedRESTEndpoint ep = (WrappedRESTEndpoint) fm;

		final Map<String,Var> paramVars = jenaOp.getParamVars();
		assert paramVars != null;
		assert ! paramVars.isEmpty();

		final SPARQLGraphPattern p =  new GenericSPARQLGraphPatternImpl2( jenaOp.getSubOp() );
		final LogicalOpGPAdd op = new LogicalOpGPAdd(ep, p, paramVars, mayReduce);
		return new LogicalPlanWithUnaryRootImpl(op, null, subplan);
	}

	protected LogicalPlan createPlanForDistinct( final OpDistinct jenaOp,
	                                             final QueryProcContext ctxt ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp(), true, ctxt );
		final LogicalOpDedup rootOp = LogicalOpDedup.getInstance();
		return new LogicalPlanWithUnaryRootImpl(rootOp, null, subPlan);
	}

	protected LogicalPlan createPlanForProject( final OpProject jenaOp,
	                                            final boolean mayReduce,
	                                            final QueryProcContext ctxt ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp(), mayReduce, ctxt );
		final LogicalOpProject rootOp = new LogicalOpProject( jenaOp.getVars(), mayReduce );
		return new LogicalPlanWithUnaryRootImpl(rootOp, null, subPlan);
	}

	protected LogicalPlan createPlan( final Op jenaOp, final boolean mayReduce, final FederationMember fm ) {
		// If the federation member has a SPARQL endpoint interface, then
		// we can simply wrap the whole query pattern in a single request.
		if ( fm instanceof SPARQLEndpoint ) {
			if ( jenaOp instanceof OpBGP opBGP ) {
				// If possible, create an explicit BGP request operator
				// rather than a general SPARQL pattern request operator
				// because that causes fewer checks and casts further
				// down in the query planning pipeline.
				return createPlanForBGP(opBGP, mayReduce, fm);
			}
			else if ( jenaOp instanceof OpTriple opTP ) {
				// Likewise for triple patterns
				return createPlanForTriplePattern(opTP, mayReduce, fm);
			}
			else {
				final SPARQLRequest req = new SPARQLRequestImpl( new GenericSPARQLGraphPatternImpl2(jenaOp) );
				final LogicalOpRequest<SPARQLRequest,SPARQLEndpoint> op = new LogicalOpRequest<>( (SPARQLEndpoint) fm, mayReduce, req );
				return new LogicalPlanWithNullaryRootImpl(op, null);
			}
		}

		// For all federation members with other types of interfaces,
		// the pattern must be broken into smaller parts.
		if ( jenaOp instanceof OpJoin opJoin ) {
			return createPlanForJoin(opJoin, mayReduce, fm);
		}
		else if ( jenaOp instanceof OpLeftJoin opLJ ) {
			return createPlanForLeftJoin(opLJ, mayReduce, fm);
		}
		else if ( jenaOp instanceof OpConditional opCond ) {
			return createPlanForLeftJoin(opCond, mayReduce, fm);
		}
		else if ( jenaOp instanceof OpUnion opUnion ) {
			return createPlanForUnion(opUnion, mayReduce, fm);
		}
		else if ( jenaOp instanceof OpFilter opFilter ) {
			return createPlanForFilter(opFilter, mayReduce, fm);
		}
		else if ( jenaOp instanceof OpBGP opBGP ) {
			return createPlanForBGP(opBGP, mayReduce, fm);
		}
		else if ( jenaOp instanceof OpTriple opTP ) {
			return createPlanForTriplePattern(opTP, mayReduce, fm);
		}
		else if ( jenaOp instanceof OpTable opTable ) {
			return createPlanForOpTable(opTable);
		}
		else {
			throw new IllegalArgumentException( "unsupported type of query pattern: " + jenaOp.getClass().getName() );
		}
	}

	protected LogicalPlan createPlanForJoin( final OpJoin jenaOp, final boolean mayReduce, final FederationMember fm ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), mayReduce, fm );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), mayReduce, fm );
		return mergeIntoMultiwayJoin(mayReduce,leftSubPlan,rightSubPlan);
	}

	protected LogicalPlan createPlanForLeftJoin( final OpLeftJoin jenaOp, final boolean mayReduce, final FederationMember fm ) {
		if ( jenaOp.getExprs() != null && ! jenaOp.getExprs().isEmpty() ) {
			throw new IllegalArgumentException( "OpLeftJoin with filter condition is not supported" );
		}

		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), mayReduce, fm );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), mayReduce, fm );
		return mergeIntoMultiwayLeftJoin(leftSubPlan, rightSubPlan, mayReduce);
	}

	protected LogicalPlan createPlanForLeftJoin( final OpConditional jenaOp, final boolean mayReduce, final FederationMember fm ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), mayReduce, fm );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), mayReduce, fm );
		return mergeIntoMultiwayLeftJoin(leftSubPlan, rightSubPlan, mayReduce);
	}

	protected LogicalPlan createPlanForUnion( final OpUnion jenaOp, final boolean mayReduce, final FederationMember fm ) {
		final LogicalPlan leftSubPlan = createPlan( jenaOp.getLeft(), mayReduce, fm );
		final LogicalPlan rightSubPlan = createPlan( jenaOp.getRight(), mayReduce, fm );
		return mergeIntoMultiwayUnion(mayReduce,leftSubPlan,rightSubPlan);
	}

	protected LogicalPlan createPlanForFilter( final OpFilter jenaOp, final boolean mayReduce, final FederationMember fm ) {
		final LogicalPlan subPlan = createPlan( jenaOp.getSubOp(), mayReduce, fm );
		final LogicalOpFilter rootOp = new LogicalOpFilter( jenaOp.getExprs(), mayReduce );
		return new LogicalPlanWithUnaryRootImpl(rootOp, null, subPlan);
	}

	protected LogicalPlan createPlanForBGP( final OpBGP pattern, final boolean mayReduce, final FederationMember fm ) {
		return createPlanForBGP( pattern.getPattern(), mayReduce, fm );
	}

	protected LogicalPlan createPlanForBGP( final BasicPattern pattern, final boolean mayReduce, final FederationMember fm ) {
		return createPlanForBGP( new BGPImpl(pattern), mayReduce, fm );
	}

	protected LogicalPlan createPlanForTriplePattern( final OpTriple pattern,
	                                                  final boolean mayReduce,
	                                                  final FederationMember fm ) {
		final TriplePattern tp = new TriplePatternImpl( pattern.getTriple() );
		final TriplePatternRequest req = new TriplePatternRequestImpl(tp);
		final LogicalOpRequest<?,?> op = new LogicalOpRequest<>(fm, mayReduce, req);
		return new LogicalPlanWithNullaryRootImpl(op, null);
	}

	protected LogicalPlan createPlanForBGP( final BGP bgp, final boolean mayReduce, final FederationMember fm ) {
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
				final LogicalOpRequest<?,?> op = new LogicalOpRequest<>(fm, mayReduce, req);
				final LogicalPlan subPlan = new LogicalPlanWithNullaryRootImpl(op, null);
				subPlans.add( subPlan );
			}

			return mergeIntoMultiwayJoin(subPlans, mayReduce);
		}

		// Otherwise, if the federation member supports BGP requests, ...
		if ( fm.isSupportedPattern(bgp) ) {
			// ... then we can simply create a BGP request operator.
			final BGPRequest req = new BGPRequestImpl(bgp);
			final LogicalOpRequest<?,?> op = new LogicalOpRequest<>(fm, mayReduce, req);
			return new LogicalPlanWithNullaryRootImpl(op, null);
		}

		throw new IllegalArgumentException( "the given federation member cannot handle triple patterns requests (" + fm.toString() + ")" );
	}

	protected LogicalPlan createPlanForOpTable( final OpTable opTable ) {
		// Jena rewrites a pattern consisting only of OPTIONAL into
		// OpLeftJoin(OpTable.unit(), pattern). The only OpTable we
		// expect here is the join identity (one empty solution mapping).
		if ( ! opTable.isJoinIdentity() ) {
			throw new IllegalStateException();
		}

		final SolutionMapping solmap = new SolutionMappingImpl(); // empty solution mapping
		final LogicalOpFixedSolMap rootOp = new LogicalOpFixedSolMap(solmap);
		return new LogicalPlanWithNullaryRootImpl(rootOp, null);
	}

	protected LogicalPlan mergeIntoMultiwayJoin( final boolean mayReduce, final LogicalPlan ... subPlans ) {
		if ( subPlans.length == 1 ) {
			return subPlans[0];
		}

		return mergeIntoMultiwayJoin( Arrays.asList(subPlans), mayReduce );
	}

	protected LogicalPlan mergeIntoMultiwayJoin( final List<LogicalPlan> subPlans, final boolean mayReduce ) {
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

		return new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayJoin.getInstance(mayReduce),
		                                        null,
		                                        subPlansFlattened );
	}

	protected LogicalPlan mergeIntoMultiwayLeftJoin( final LogicalPlan leftSubPlan,
	                                                 final LogicalPlan rightSubPlan,
	                                                 final boolean mayReduce ) {
		final List<LogicalPlan> children = new ArrayList<>();

		final LogicalOperator leftRootOp = leftSubPlan.getRootOperator();
		if ( leftRootOp instanceof LogicalOpMultiwayLeftJoin ) {
			for ( int i = 0; i < leftSubPlan.numberOfSubPlans(); i++ ) {
				children.add( leftSubPlan.getSubPlan(i) );
			}
		}
		else if ( leftRootOp instanceof LogicalOpLeftJoin ) {
			children.add( leftSubPlan.getSubPlan(0) );
			children.add( leftSubPlan.getSubPlan(1) );
		}
		else {
			children.add( leftSubPlan );
		}

		children.add( rightSubPlan );

		return new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayLeftJoin.getInstance(mayReduce), null, children );
	}

	protected LogicalPlan mergeIntoMultiwayUnion( final boolean mayReduce, final LogicalPlan ... subPlans ) {
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

		return new LogicalPlanWithNaryRootImpl( LogicalOpMultiwayUnion.getInstance(mayReduce),
		                                        null,
		                                        subPlansFlattened );
	}

}
