package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.mappings.VocabularyMappingUtils;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpDedup;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFixedSolMap;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMinus;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpProject;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnfold;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BGPRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.federation.members.RDFBasedFederationMember;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

public class ApplyVocabularyMappings implements HeuristicForLogicalOptimization {
	/**
	 * Rewrites an initial logical plan into a second plan which incorporates translations of local to global vocabulary and request-operator rewriting.
	 * This method implements the rewriteLogPlan pseudocode of Helgesson's B.Sc thesis.
	 */
	@Override
	public LogicalPlan apply( final LogicalPlan inputPlan ) {
		final LogicalOperator rootOp = inputPlan.getRootOperator();
		final Worker worker = new Worker(inputPlan);
		rootOp.visit(worker);

		final LogicalPlan rewrittenPlan = worker.getRewrittenPlan();
		return rewrittenPlan;
	}

	protected class Worker implements LogicalPlanVisitor {
		protected final LogicalPlan inputPlan;
		protected LogicalPlan rewrittenPlan;

		public Worker( final LogicalPlan inputPlan ) {
			this.inputPlan = inputPlan;
		}

		public LogicalPlan getRewrittenPlan() { return rewrittenPlan; }

		@Override
		public void visit( final LogicalOpRequest<?, ?> op ) {
			if (    op.getFederationMember() instanceof RDFBasedFederationMember fm
			     && fm.getVocabularyMapping() != null )
			{
				final LogicalPlan newInputPlan = rewriteToUseLocalVocabulary(inputPlan);
				final boolean mayReduce = op.mayReduce();

				final VocabularyMapping vm = fm.getVocabularyMapping();
				final LogicalOpLocalToGlobal l2g = new LogicalOpLocalToGlobal(vm, mayReduce);

				rewrittenPlan = new LogicalPlanWithUnaryRootImpl(l2g, null, newInputPlan);
			}
			else {
				rewrittenPlan = inputPlan;
			}
		}

		@Override
		public void visit( final LogicalOpFixedSolMap op ) {
			rewrittenPlan = inputPlan;
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			if (    op.getFederationMember() instanceof RDFBasedFederationMember fm
			     && fm.getVocabularyMapping() != null ) {
				throw new IllegalArgumentException("The given logical plan is not supported by this function because it has a gpAdd operator with a federation member for which a vocabulary mapping is specified." );
			}

			final LogicalPlan rewrittenSubPlan = apply( inputPlan.getSubPlan(0) );
			rewrittenPlan = new LogicalPlanWithUnaryRootImpl( op,
			                                                  null,
			                                                rewrittenSubPlan );
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpLeftJoin op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			final List<LogicalPlan> rewrittenSubplans = new ArrayList<>();
			final Iterator<LogicalPlan> it = ((LogicalPlanWithNaryRoot) inputPlan).getSubPlans();
			boolean rewritten = false;
			while(it.hasNext()) {
				final LogicalPlan subPlan = it.next();
				final LogicalPlan rewrittenSubplan = apply(subPlan);
				rewrittenSubplans.add(rewrittenSubplan);
				if(!subPlan.equals(rewrittenSubplan)) {
					rewritten = true;
				}
			}

			if ( rewritten )
				rewrittenPlan = new LogicalPlanWithNaryRootImpl(  op,
				                                                  null,
				                                                  rewrittenSubplans );
			else
				rewrittenPlan = inputPlan;
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			final List<LogicalPlan> rewrittenSubplans = new ArrayList<>();
			final Iterator<LogicalPlan> it = ((LogicalPlanWithNaryRoot) inputPlan).getSubPlans();
			boolean rewritten = false;
			while(it.hasNext()) {
				final LogicalPlan subPlan = it.next();
				final LogicalPlan rewrittenSubplan = apply(subPlan);
				rewrittenSubplans.add(rewrittenSubplan);
				if(!subPlan.equals(rewrittenSubplan)) {
					rewritten = true;
				}
			}

			if ( rewritten )
				rewrittenPlan = new LogicalPlanWithNaryRootImpl(  op,
				                                                  null,
				                                                  rewrittenSubplans );
			else
				rewrittenPlan = inputPlan;
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			final List<LogicalPlan> rewrittenSubplans = new ArrayList<>();
			final Iterator<LogicalPlan> it = ((LogicalPlanWithNaryRoot) inputPlan).getSubPlans();
			boolean rewritten = false;
			while(it.hasNext()) {
				final LogicalPlan subPlan = it.next();
				final LogicalPlan rewrittenSubplan = apply(subPlan);
				rewrittenSubplans.add(rewrittenSubplan);
				if(!subPlan.equals(rewrittenSubplan)) {
					rewritten = true;
				}
			}

			if ( rewritten )
				rewrittenPlan = new LogicalPlanWithNaryRootImpl(  op,
				                                                  null,
				                                                  rewrittenSubplans );
			else
				rewrittenPlan = inputPlan;
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			final LogicalPlan rewrittenSubPlan = apply( inputPlan.getSubPlan(0) );
			// TODO: the expressions of 'filterOp' should be rewritten too
			rewrittenPlan = new LogicalPlanWithUnaryRootImpl( op,
			                                                  null,
			                                                  rewrittenSubPlan );
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			final LogicalPlan rewrittenSubPlan = apply( inputPlan.getSubPlan(0) );
			// TODO: the expressions of 'bindOp' should be rewritten too
			rewrittenPlan = new LogicalPlanWithUnaryRootImpl( op,
			                                                  null,
			                                                  rewrittenSubPlan );
		}

		@Override
		public void visit( final LogicalOpUnfold op ) {
			final LogicalPlan rewrittenSubPlan = apply( inputPlan.getSubPlan(0) );
			// TODO: the expressions of 'unfoldOp' should be rewritten too
			rewrittenPlan = new LogicalPlanWithUnaryRootImpl( op,
			                                                  null,
			                                                  rewrittenSubPlan );
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}

		@Override
		public void visit( final LogicalOpDedup op ) {
			final LogicalPlan rewrittenSubPlan = apply( inputPlan.getSubPlan(0) );
			rewrittenPlan = new LogicalPlanWithUnaryRootImpl( op,
			                                                  null,
			                                                  rewrittenSubPlan );
		}

		@Override
		public void visit( final LogicalOpProject op ) {
			final LogicalPlan rewrittenSubPlan = apply( inputPlan.getSubPlan(0) );
			rewrittenPlan = new LogicalPlanWithUnaryRootImpl( op,
			                                                  null,
			                                                  rewrittenSubPlan );
		}

		@Override
		public void visit( final LogicalOpMinus op ) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'visit'");
		}
	}

	/**
	 * Rewrites the given logical plan with a request operator as root into
	 * a logical plan that uses the local vocabulary of the federation member of
	 * the request.
	 */
	public static LogicalPlan rewriteToUseLocalVocabulary( final LogicalPlan inputPlan ) {
		if(!(inputPlan.getRootOperator() instanceof LogicalOpRequest)) {
			throw new IllegalArgumentException( "Input plan does not have a request operator as root: " + inputPlan.getRootOperator().getClass().getName() );
		}

		final LogicalOpRequest<?, ?> reqOp = (LogicalOpRequest<?, ?>) inputPlan.getRootOperator();

		if (    reqOp.getFederationMember() instanceof RDFBasedFederationMember fm
		     && fm.getVocabularyMapping() != null )
		{
			if(!(reqOp.getRequest() instanceof SPARQLRequest)) {
				throw new IllegalArgumentException( "Request must be a SPARQLRequest: " + reqOp.getRequest().getClass().getName() );
			}

			final SPARQLRequest req = (SPARQLRequest) reqOp.getRequest();
			final SPARQLGraphPattern p = req.getQueryPattern();
			final boolean mayReduce = reqOp.mayReduce();

			final SPARQLGraphPattern newP = VocabularyMappingUtils.translateGraphPattern(p, fm.getVocabularyMapping());
			return ( newP.equals(p) ) ? inputPlan : rewriteReqOf(newP, fm, mayReduce);
		}
		else  { // If no vocabulary mapping, nothing to translate.
			return inputPlan;
		}
	}

	/**
	 * Creates a logical plan where all requests are TriplePatternRequests
	 * for use when a federation member's interface is a TPF-server.
	 */
	public static LogicalPlan rewriteReqOf( final SPARQLGraphPattern pattern, final FederationMember fm, final boolean mayReduce ) {
		// Right now there are just TPF-servers and SPARQL endpoints, but there may be more in the future.
		// For now, we will not assume that third types of interfaces will necessarily support all patterns.

		// For SPARQL endpoints, the whole graph pattern can be sent in a single request.
		if ( fm instanceof SPARQLEndpoint ) {
			final SPARQLRequest reqP = new SPARQLRequestImpl(pattern);
			final LogicalOpRequest<SPARQLRequest, SPARQLEndpoint> req = new LogicalOpRequest<>( (SPARQLEndpoint) fm, mayReduce, reqP );
			return new LogicalPlanWithNullaryRootImpl(req, null);
		}
		else if( pattern instanceof TriplePattern tp ) {
			final TriplePatternRequest req = new TriplePatternRequestImpl(tp);
			final LogicalOpRequest<TriplePatternRequest, FederationMember> reqOp = new LogicalOpRequest<>(fm,mayReduce,req);
			return new LogicalPlanWithNullaryRootImpl(reqOp, null);
		}
		else if( pattern instanceof BGP bgp ) {
			if ( fm.isSupportedPattern(bgp) ) {
				final BGPRequest req = new BGPRequestImpl(bgp);
				final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(fm,mayReduce,req);
				return new LogicalPlanWithNullaryRootImpl(reqOp, null);
			}

			// For federation members that do not support BGP requests,
			// break the BGP into triple pattern requests.
			final List<LogicalPlan> subPlans = new ArrayList<>();
			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				final TriplePatternRequest req = new TriplePatternRequestImpl(tp);
				final LogicalOpRequest<?,?> reqOp = new LogicalOpRequest<>(fm,mayReduce,req);
				subPlans.add( new LogicalPlanWithNullaryRootImpl(reqOp,null) );
			}

			return LogicalPlanUtils.createPlanWithMultiwayJoin(mayReduce, subPlans, null);
		}
		else if( pattern instanceof SPARQLUnionPattern up ) {
			final List<LogicalPlan> subPlans = new ArrayList<>();
			for ( final SPARQLGraphPattern subP : up.getSubPatterns() ) {
				final LogicalPlan subPlan = rewriteReqOf(subP,fm,mayReduce);
				subPlans.add(subPlan);
			}

			return LogicalPlanUtils.createPlanWithMultiwayUnion(mayReduce, subPlans, null);
		}
		else if( pattern instanceof SPARQLGroupPattern gp ) {
			final List<LogicalPlan> subPlans = new ArrayList<>();
			for ( final SPARQLGraphPattern subP : gp.getSubPatterns() ) {
				final LogicalPlan subPlan = rewriteReqOf(subP,fm,mayReduce);
				subPlans.add(subPlan);
			}

			return LogicalPlanUtils.createPlanWithMultiwayJoin(mayReduce, subPlans, null);
		}
		else {
			throw new IllegalArgumentException( pattern.getClass().getName() );
		}
	}

}
