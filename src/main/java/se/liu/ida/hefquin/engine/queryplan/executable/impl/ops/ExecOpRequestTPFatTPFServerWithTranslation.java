package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;

/**
 * Implementation of an operator to request a (complete) TPF from a TPF server.
 * This implementation handles pagination of the TPF; that is, it requests all
 * the pages, one after another.
 */
public class ExecOpRequestTPFatTPFServerWithTranslation extends ExecOpGenericTriplePatternRequestWithTPF<TPFServer>
{
	public ExecOpRequestTPFatTPFServerWithTranslation( final TriplePatternRequest req, final TPFServer fm ) {
		super( req, fm );
	}

	@Override
	protected TPFResponse performPageRequest( final TPFRequest req,
	                                          final FederationAccessManager fedAccessMgr )
			throws FederationAccessException
	{
		SPARQLGraphPattern translation = fm.getVocabularyMapping().translateTriplePattern(req.getQueryPattern());
		if(translation instanceof TriplePattern) {
			TPFResponse res = FederationAccessUtils.performRequest(fedAccessMgr, new TPFRequestImpl((TriplePattern) translation), fm);
			//How do I get solutionMapping from res?
			//Return translation of res
			return res;
		}
		
		//How do I create a union of all results?
		else if(translation instanceof SPARQLUnionPattern) {
			for (final SPARQLGraphPattern i : ((SPARQLUnionPattern) translation).getSubPatterns()) {
				if (i instanceof TriplePattern) {
					FederationAccessUtils.performRequest(fedAccessMgr, new TPFRequestImpl((TriplePattern) i), fm);
				}
				//TODO: Add all other option of instance
				else {
					throw new FederationAccessException(i.toString(), req, fm);
				}
			}
			//Return union of all results
		}
		
		//Same for GroupPattern
		else if(translation instanceof SPARQLGroupPattern) {
			for(final SPARQLGraphPattern i : ((SPARQLGroupPattern) translation).getSubPatterns()) {
				if (i instanceof TriplePattern) {
					FederationAccessUtils.performRequest(fedAccessMgr, new TPFRequestImpl((TriplePattern) i), fm);
					//Translate solutionMapping
				}
				//TODO: Add all other option of instance
				else {
					throw new FederationAccessException(i.toString(), req, fm);
				}
			}
			//Return intersection of all results
		}
		
		//And same for BGP
		else if(translation instanceof BGP) {
			for(final TriplePattern i : ((BGP) translation).getTriplePatterns()) {
				FederationAccessUtils.performRequest(fedAccessMgr, new TPFRequestImpl((TriplePattern) i), fm);
				//Translate solutionMapping
			}
			//Return intersection
		}
		
		else {
			throw new FederationAccessException(translation.toString(), req, fm);
		}
		
		return null;
	}
}