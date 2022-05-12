package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpUnion;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGroupPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;

public class ExecOpRequestSPARQLWithTranslation extends ExecOpGenericSolMapsRequest<SPARQLRequest, SPARQLEndpoint>
{
	public ExecOpRequestSPARQLWithTranslation( final SPARQLRequest req, final SPARQLEndpoint fm ) {
		super( req, fm );
	}

	@Override
	protected void process( final SolMapsResponse response, final IntermediateResultElementSink sink )
	{
		for ( SolutionMapping sm : response.getSolutionMappings() ) {
			for (SolutionMapping smTranslated : fm.getVocabularyMapping().translateSolutionMapping(sm)) {
				sink.send( smTranslated );
			}
		}
	}
	
	@Override
	protected SolMapsResponse performRequest( final FederationAccessManager fedAccessMgr ) throws FederationAccessException {
		final SPARQLGraphPattern query = req.getQueryPattern();
		SPARQLGraphPattern reqTranslation = null;
		if (query instanceof TriplePattern) {
			reqTranslation = handleTriplePattern((TriplePattern) query);	
		} 
		else if (query instanceof BGP) {
			reqTranslation = handleBGP((BGP) query);
		} else if (query instanceof SPARQLGroupPattern) {
			reqTranslation = handleGroup((SPARQLGroupPattern) query);
		} else if (query instanceof SPARQLUnionPattern) {
			reqTranslation = handleUnion((SPARQLUnionPattern) query);
		} else if (query instanceof GenericSPARQLGraphPatternImpl1) {
			Op gp = ((GenericSPARQLGraphPatternImpl1) query).asJenaOp();
			reqTranslation = handleOp(gp);		
		} else if (query instanceof GenericSPARQLGraphPatternImpl2) {
			Op gp = ((GenericSPARQLGraphPatternImpl2) query).asJenaOp();
			reqTranslation = handleOp(gp);	
		}
		else {
			throw new FederationAccessException("Unsupported type of pattern: " + query.getClass().getName(), req, fm);
		}
		
		final SPARQLRequest newReq = new SPARQLRequestImpl(reqTranslation);
		return FederationAccessUtils.performRequest(fedAccessMgr, newReq, fm);	
	}
	
	
	
	protected SPARQLGraphPattern handleTriplePattern(TriplePattern tp) {
		return fm.getVocabularyMapping().translateTriplePattern((TriplePattern) tp);
	}
	
	protected SPARQLGraphPattern handleBGP(BGP bgp) {
		final List<SPARQLGraphPattern> allSubPatterns = new ArrayList<>();
		final Set<TriplePattern> tpSubPatterns = new HashSet<>();
		boolean allSubPatternsAreTriplePatterns = true; // assume yes

		for( final TriplePattern i : bgp.getTriplePatterns() ) {
			final SPARQLGraphPattern iTranslation = fm.getVocabularyMapping().translateTriplePattern(i); 
			allSubPatterns.add(iTranslation);

			if ( allSubPatternsAreTriplePatterns && iTranslation instanceof TriplePattern ) {
				tpSubPatterns.add( (TriplePattern) iTranslation );
			}
			else {
				allSubPatternsAreTriplePatterns = false;
			}
		}

		if ( allSubPatternsAreTriplePatterns ) {
			return new BGPImpl(tpSubPatterns);
		}
		else {
			return new SPARQLGroupPatternImpl(allSubPatterns);
		}
	}
	
	protected SPARQLGraphPattern handleUnion(SPARQLUnionPattern up) throws FederationAccessException {
		final SPARQLUnionPatternImpl unionTranslation = new SPARQLUnionPatternImpl();
		for (final SPARQLGraphPattern i : up.getSubPatterns()) {
			if (i instanceof TriplePattern) {
				unionTranslation.addSubPattern(handleTriplePattern((TriplePattern) i));
			} else if (i instanceof BGP) {
				unionTranslation.addSubPattern(handleBGP((BGP) i));
			} else if (i instanceof SPARQLUnionPattern) {
				unionTranslation.addSubPattern(handleUnion((SPARQLUnionPattern) i));
			} else if (i instanceof SPARQLGroupPattern) {
				unionTranslation.addSubPattern(handleGroup((SPARQLGroupPattern) i));
			} else {
				throw new FederationAccessException(i.toString(), req, fm);
			}
		}
		return unionTranslation;
	}
	
	protected SPARQLGraphPattern handleGroup(SPARQLGroupPattern gp) throws FederationAccessException {
		List<SPARQLGraphPattern> subPatterns = new ArrayList<>();
		for (final SPARQLGraphPattern i : gp.getSubPatterns()) {
			if (i instanceof TriplePattern) {
				subPatterns.add(handleTriplePattern((TriplePattern) i));
			} else if (i instanceof BGP) {
				subPatterns.add(handleBGP((BGP) i));
			} else if (i instanceof SPARQLUnionPattern) {
				subPatterns.add(handleUnion((SPARQLUnionPattern) i));
			} else if (i instanceof SPARQLGroupPattern) {
				subPatterns.add(handleGroup((SPARQLGroupPattern) i));
			} else {
				throw new FederationAccessException(i.toString(), req, fm);
			}
		}
		
		return new SPARQLGroupPatternImpl(subPatterns);
	}
	
	protected SPARQLGraphPattern handleOp(Op op) throws FederationAccessException {
		if (op instanceof OpJoin) {
			return handleJoin((OpJoin) op);
		} else if (op instanceof OpUnion) {
			return handleOpUnion((OpUnion) op);
		} else if (op instanceof OpFilter) {
			return handleOpFilter((OpFilter) op);
		} else if (op instanceof OpBGP) {
			return handleOpBGP((OpBGP) op);
		} else if (op instanceof OpSequence) {
			return handleOpSequence((OpSequence) op);
		} else {
			throw new FederationAccessException("Unsupported type of pattern: " + op.getClass().getName(), req, fm);
		}
	}
	
	protected SPARQLGraphPattern handleJoin(OpJoin oj) throws FederationAccessException {
		List<SPARQLGraphPattern> subPatterns = new ArrayList<>();
		Op left = oj.getLeft();
		subPatterns.add(handleOp(left));
		Op right = oj.getRight();
		subPatterns.add(handleOp(right));
		return new SPARQLGroupPatternImpl(subPatterns);
	}
	
	protected SPARQLGraphPattern handleOpUnion(OpUnion ou) throws FederationAccessException {
		final SPARQLUnionPatternImpl unionTranslation = new SPARQLUnionPatternImpl();
		Op left = ou.getLeft();
		unionTranslation.addSubPattern(handleOp(left));
		Op right = ou.getRight();
		unionTranslation.addSubPattern(handleOp(right));
		return unionTranslation;
	}
	
	//TODO: Handle OpFilter
	protected SPARQLGraphPattern handleOpFilter(OpFilter of) throws FederationAccessException {
		return null;
	}
	
	protected SPARQLGraphPattern handleOpBGP(OpBGP obgp) throws FederationAccessException {	
		final List<SPARQLGraphPattern> allSubPatterns = new ArrayList<>();
		final Set<TriplePattern> tpSubPatterns = new HashSet<>();
		boolean allSubPatternsAreTriplePatterns = true; // assume yes

		for( final Triple i : obgp.getPattern().getList() ) {
			TriplePattern itp = new TriplePatternImpl(i);
			final SPARQLGraphPattern iTranslation = fm.getVocabularyMapping().translateTriplePattern(itp); 
			allSubPatterns.add(iTranslation);

			if ( allSubPatternsAreTriplePatterns && iTranslation instanceof TriplePattern ) {
				tpSubPatterns.add( (TriplePattern) iTranslation );
			}
			else {
				allSubPatternsAreTriplePatterns = false;
			}
		}

		if ( allSubPatternsAreTriplePatterns ) {
			return new BGPImpl(tpSubPatterns);
		}
		else {
			return new SPARQLGroupPatternImpl(allSubPatterns);
		}
	}
	
	protected SPARQLGraphPattern handleOpSequence(OpSequence os) throws FederationAccessException {
		List<SPARQLGraphPattern> subPatterns = new ArrayList<>();
		for( final Op i : os.getElements()) {
			subPatterns.add(handleOp(i));
		}
		return new SPARQLGroupPatternImpl(subPatterns);
	}

}
