package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BaseForExecOpParallelBindJoin;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BaseForExecOpSequentialBindJoin;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpParallelBindJoinSPARQLwithFILTER;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpParallelBindJoinSPARQLwithUNION;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpParallelBindJoinSPARQLwithVALUES;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpSequentialBindJoinSPARQLwithFILTER;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpSequentialBindJoinSPARQLwithUNION;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpSequentialBindJoinSPARQLwithVALUES;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpSequentialBindJoinSPARQLwithVALUESorFILTER;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpSequentialBindJoinSPARQLwithVarRenaming;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

/**
 * A physical operator that implements a batch-based bind-join algorithm for
 * SPARQL endpoints that can be used in different variations. The variation
 * to be used is determined by the arguments provided to the constructor of
 * the Factory class (see below). It is possible to use either a parallel or
 * a sequential implementation (see {@link BaseForExecOpParallelBindJoin} vs
 * {@link BaseForExecOpSequentialBindJoin}) and, additionally, the form of
 * the bind-join requests can be decided (e.g., VALUES-based, FILTER-based).
 *
 * <p>
 * <b>Semantics:</b> This operator implements the logical operators gpAdd
 * (see {@link LogicalOpGPAdd}) and gpOptAdd (see {@link LogicalOpGPOptAdd}).
 * That is, for a given graph pattern, a federation  member, and an input
 * sequence of solution mappings (produced by the sub-plan under this
 * operator), the operator produces the solutions resulting from the join
 * (inner or left outer) between the input solutions and the solutions of
 * evaluating the given graph pattern over the data of the federation
 * member.
 * </p>
 *
 * <p>
 * <b>Algorithm description:</b> For a detailed description of the
 * actual algorithm associated with this physical operator, refer
 * to {@link BaseForExecOpParallelBindJoin} (for the parallel version)
 * and to {@link BaseForExecOpSequentialBindJoin} (for the sequential
 * version), and to
 * {@link ExecOpSequentialBindJoinSPARQLwithVarRenaming},
 * {@link ExecOpSequentialBindJoinSPARQLwithVALUES},
 * {@link ExecOpSequentialBindJoinSPARQLwithFILTER},
 * {@link ExecOpSequentialBindJoinSPARQLwithUNION}, and
 * {@link ExecOpSequentialBindJoinSPARQLwithVALUESorFILTER},
 * for the different kinds of bind-join requests.
 * </p>
 */
public class PhysicalOpBindJoinSPARQL extends BaseForPhysicalOpSingleInputJoin
{
	public static final String VALUES_BASED       = "VALUES_BASED";
	public static final String FILTER_BASED       = "FILTER_BASED";
	public static final String UNION_BASED        = "UNION_BASED";
	public static final String VARIABLE_RENAMING  = "VARIABLE_RENAMING";
	public static final String VALUES_OR_FILTER   = "VALUES_OR_FILTER";

	public static final Set<String> POSSIBLE_TYPES = Set.of(
		VALUES_OR_FILTER, // <-- only as a sequential version, cannot be parallel!?
		VARIABLE_RENAMING, // <-- also only as a sequential version at the moment
		VALUES_BASED,
		FILTER_BASED,
		UNION_BASED );

	protected final Factory myFactory;

	protected PhysicalOpBindJoinSPARQL( final LogicalOpGPAdd lop,
	                                    final Factory myFactory ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
		assert ! lop.hasParameterVariables();

		this.myFactory = myFactory;
	}

	protected PhysicalOpBindJoinSPARQL( final LogicalOpGPOptAdd lop,
	                                    final Factory myFactory ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;

		this.myFactory = myFactory;
	}

	public String getType()              { return myFactory.type; }
	public boolean usesParallelVersion() { return myFactory.useParallelVersion; }
	public int getBatchSize()            { return myFactory.batchSize; }

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables... inputVars ) {
		final SPARQLGraphPattern gp;
		final FederationMember fm;
		final boolean useOuterJoin;

		if ( lop instanceof LogicalOpGPAdd gpAdd ) {
			gp = gpAdd.getPattern();
			fm = gpAdd.getFederationMember();
			useOuterJoin = false;
		}
		else if ( lop instanceof LogicalOpGPOptAdd gpOptAdd ) {
			gp = gpOptAdd.getPattern();
			fm = gpOptAdd.getFederationMember();
			useOuterJoin = true;
		}
		else {
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
		}

		if ( fm instanceof SPARQLEndpoint ep )
			return createExecOp(gp, ep, useOuterJoin, collectExceptions, qpInfo, inputVars);
		else
			throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
	}

	protected UnaryExecutableOp createExecOp( final SPARQLGraphPattern pattern,
	                                          final SPARQLEndpoint sparqlEndpoint,
	                                          final boolean useOuterJoinSemantics,
	                                          final boolean collectExceptions,
	                                          final QueryPlanningInfo qpInfo,
	                                          final ExpectedVariables... inputVars ) {
		if ( myFactory.useParallelVersion ) {
			if ( myFactory.type.equals(VALUES_BASED) ) {
				return new ExecOpParallelBindJoinSPARQLwithVALUES(
						pattern, sparqlEndpoint, inputVars[0],
						useOuterJoinSemantics, myFactory.batchSize,
						collectExceptions, qpInfo );
			}
			else if ( myFactory.type.equals(FILTER_BASED) ) {
				return new ExecOpParallelBindJoinSPARQLwithFILTER(
						pattern, sparqlEndpoint, inputVars[0],
						useOuterJoinSemantics, myFactory.batchSize,
						collectExceptions, qpInfo );
			}
			else if ( myFactory.type.equals(UNION_BASED) ) {
				return new ExecOpParallelBindJoinSPARQLwithUNION(
						pattern, sparqlEndpoint, inputVars[0],
						useOuterJoinSemantics, myFactory.batchSize,
						collectExceptions, qpInfo );
			}
			else if ( myFactory.type.equals(VALUES_OR_FILTER) ) {
				throw new IllegalArgumentException("There is no parallel version of the " + myFactory.type + " bind join.");
			}
			else if ( myFactory.type.equals(VARIABLE_RENAMING) ) {
				throw new IllegalArgumentException("There is no parallel version of the " + myFactory.type + " bind join.");
			}
			else {
				throw new UnsupportedOperationException("We do not yet have a parallel " + myFactory.type + " bind join.");
			}
		}
		else {
			if ( myFactory.type.equals(VALUES_BASED) ) {
				return new ExecOpSequentialBindJoinSPARQLwithVALUES(
						pattern, sparqlEndpoint, inputVars[0],
						useOuterJoinSemantics, myFactory.batchSize,
						collectExceptions, qpInfo );
			}
			else if ( myFactory.type.equals(FILTER_BASED) ) {
				return new ExecOpSequentialBindJoinSPARQLwithFILTER(
						pattern, sparqlEndpoint, inputVars[0],
						useOuterJoinSemantics, myFactory.batchSize,
						collectExceptions, qpInfo );
			}
			else if ( myFactory.type.equals(UNION_BASED) ) {
				return new ExecOpSequentialBindJoinSPARQLwithUNION(
						pattern, sparqlEndpoint, inputVars[0],
						useOuterJoinSemantics, myFactory.batchSize,
						collectExceptions, qpInfo );
			}
			else if ( myFactory.type.equals(VARIABLE_RENAMING) ) {
				return new ExecOpSequentialBindJoinSPARQLwithVarRenaming(
						pattern, sparqlEndpoint, inputVars[0],
						useOuterJoinSemantics, myFactory.batchSize,
						collectExceptions, qpInfo );
			}
			else if ( myFactory.type.equals(VALUES_OR_FILTER) ) {
				return new ExecOpSequentialBindJoinSPARQLwithVALUESorFILTER(
						pattern, sparqlEndpoint, inputVars[0],
						useOuterJoinSemantics, myFactory.batchSize,
						collectExceptions, qpInfo );
			}
			else {
				throw new UnsupportedOperationException("We do not yet have a parallel " + myFactory.type + " bind join.");
			}
		}
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinSPARQL oo
				&& oo.myFactory.equals(myFactory)
				&& oo.lop.equals(lop);
	}

	@Override
	public String toString() {
		return "> BindJoin(" + myFactory.type + "-" + myFactory.useParallelVersion + "-" + myFactory.batchSize + ") " + lop.toString();
	}

	public static class Factory implements PhysicalOpFactory
	{
		public final String type;
		public final boolean useParallelVersion;
		public final int batchSize;

		public Factory( final String type,
		                final boolean useParallelVersion,
		                final int batchSize ) {
			assert POSSIBLE_TYPES.contains(type);
			assert ! ( useParallelVersion && type.equals(VALUES_OR_FILTER) );
			assert batchSize > 0;

			this.type               = type;
			this.useParallelVersion = useParallelVersion;
			this.batchSize          = batchSize;
		}

		@Override
		public boolean equals( final Object o ) {
			return    o instanceof Factory oo
			       && oo.type.equals(type)
			       && oo.useParallelVersion == useParallelVersion
			       && oo.batchSize == batchSize;
		}

		@Override
		public boolean supports( final LogicalOperator lop,
		                         final ExpectedVariables... inputVars ) {
			final FederationMember fm;
			final SPARQLGraphPattern gp;

			if ( lop instanceof LogicalOpGPAdd op && op.hasParameterVariables() ) {
				return false;
			}
			else if ( lop instanceof LogicalOpGPAdd op ) {
				fm = op.getFederationMember();
				gp = op.getPattern();
			}
			else if ( lop instanceof LogicalOpGPOptAdd op ) {
				fm = op.getFederationMember();
				gp = op.getPattern();
			}
			else {
				return false;
			}

			if ( ! (fm instanceof SPARQLEndpoint) )
				return false;

			if ( type.equals(VARIABLE_RENAMING) )
				return ! useParallelVersion && hasNonJoiningVar( gp, inputVars[0] );
			else if ( type.equals(VALUES_OR_FILTER) )
				return ! useParallelVersion;
			else
				return true;
		}

		@Override
		public PhysicalOpBindJoinSPARQL create( final UnaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpGPAdd op ) {
				return new PhysicalOpBindJoinSPARQL(op, this);
			}
			else if ( lop instanceof LogicalOpGPOptAdd op ) {
				return new PhysicalOpBindJoinSPARQL(op, this);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}

	public static boolean hasNonJoiningVar( final SPARQLGraphPattern pattern,
	                                        final ExpectedVariables vars ) {
		if ( vars == null )
			return true;

		final Set<Var> certainVars = vars.getCertainVariables();
		final Set<Var> possibleVars = vars.getPossibleVariables();

		for ( final Var v : pattern.getCertainVariables() ) {
			if ( ! certainVars.contains(v) && ! possibleVars.contains(v) ) {
				return true;
			}
		}

		return false;
	}

}
