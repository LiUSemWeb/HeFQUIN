package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import se.liu.ida.hefquin.engine.query.impl.SPARQLGroupPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLUnionPatternImpl;

public class ExecOpRequestSPARQLWithTranslation extends ExecOpGenericSolMapsRequest<SPARQLRequest, SPARQLEndpoint>
{
	public ExecOpRequestSPARQLWithTranslation( final SPARQLRequest req, final SPARQLEndpoint fm ) {
		super( req, fm );
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
		}
		else {
			throw new FederationAccessException(query.toString(), req, fm);
		}
		
		final SPARQLRequest newReq = new SPARQLRequestImpl(reqTranslation);
		final SolMapsResponse res = FederationAccessUtils.performRequest(fedAccessMgr, newReq, fm);	
		
		List<SolutionMapping> resList = new ArrayList<>();
		for(final SolutionMapping i : res.getSolutionMappings()) {
			resList.addAll(fm.getVocabularyMapping().translateSolutionMapping(i));
		}
		
		return new SolMapsResponseImpl(resList, fm, req, null);
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

}
