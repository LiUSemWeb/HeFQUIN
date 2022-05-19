package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.JoiningIteratorForSolMaps;
import se.liu.ida.hefquin.engine.data.utils.UnionIteratorForSolMaps;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSinkWithTranslation;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Implementation of an operator to request a (complete) TPF from a TPF server.
 * This implementation handles pagination of the TPF; that is, it requests all
 * the pages, one after another.
 */
public class ExecOpRequestTPFatTPFServerWithTranslation extends ExecOpGenericRequest<TriplePatternRequest, TPFServer>
{
	public ExecOpRequestTPFatTPFServerWithTranslation( final TriplePatternRequest req, final TPFServer fm ) {
		super( req, fm );

		assert fm.getVocabularyMapping() != null;
	}

	
	@Override
	protected void _execute( final IntermediateResultElementSink sink, final ExecutionContext execCxt)
			throws ExecOpExecutionException {
		
		final FederationAccessManager fedAccessMgr = execCxt.getFederationAccessMgr();
		final SPARQLGraphPattern reqTranslation = fm.getVocabularyMapping().translateTriplePattern(req.getQueryPattern());
		Iterator<SolutionMapping> res;
		if(reqTranslation instanceof TriplePattern) {
			res = handleTriplePattern((TriplePattern) reqTranslation, fedAccessMgr);
		}
		else if(reqTranslation instanceof SPARQLUnionPattern) {
			res = handleUnionPattern(((SPARQLUnionPattern) reqTranslation), fedAccessMgr);
		}
		else if(reqTranslation instanceof SPARQLGroupPattern) {
			res = handleGroupPattern(((SPARQLGroupPattern) reqTranslation), fedAccessMgr);
		}
		else if(reqTranslation instanceof BGP) {
			res = handleBGP((BGP) reqTranslation, fedAccessMgr);
		}
		else {
			throw new ExecOpExecutionException(reqTranslation.toString(), this);
		}
		
		while(res.hasNext()) {
			sink.send(res.next());
		}
		
	}
	
	protected Iterator<SolutionMapping> handlePattern(final SPARQLGraphPattern p, final FederationAccessManager fedAccessMgr) throws ExecOpExecutionException{
		if (p instanceof TriplePattern) {
			return handleTriplePattern((TriplePattern) p, fedAccessMgr);
		} else if (p instanceof SPARQLUnionPattern) {
			return handleUnionPattern(((SPARQLUnionPattern) p), fedAccessMgr);
		} else if (p instanceof SPARQLGroupPattern) {
			return handleGroupPattern(((SPARQLGroupPattern) p), fedAccessMgr);
		} else if (p instanceof BGP) {
			return handleBGP(((BGP) p), fedAccessMgr);
		} else {
			throw new ExecOpExecutionException(p.toString(), this);
		}
	}
	
	protected Iterator<SolutionMapping> handleTriplePattern(final TriplePattern tp, final FederationAccessManager fedAccessMgr) throws ExecOpExecutionException{
		
		final TriplePatternRequest newReq = new TriplePatternRequestImpl(tp);
		final ExecOpRequestTPFatTPFServer op = new ExecOpRequestTPFatTPFServer(newReq, fm);
		final MaterializingIntermediateResultElementSinkWithTranslation sink = new MaterializingIntermediateResultElementSinkWithTranslation(fm.getVocabularyMapping());
		
		TPFResponse currentPage = null;
		while ( currentPage == null || ! op.isLastPage(currentPage) ) {
			// create the request for the next page (which is the first page if currentPage is null)
			final TPFRequest pageRequest = op.createPageRequest(currentPage);

			// perform the page request
			try {
				currentPage = op.performPageRequest( pageRequest, fedAccessMgr );
			}
			catch ( final FederationAccessException e ) {
				throw new ExecOpExecutionException("Issuing a page request caused an exception: " + e.toString(), this);
			}
			
			// consume the matching triples retrieved via the page request
			op.consumeMatchingTriples( currentPage.getPayload(), sink );

		}	
	
		return sink.getMaterializedIntermediateResult().iterator();

	}
	
	protected Iterator<SolutionMapping> handleUnionPattern(final SPARQLUnionPattern up, final FederationAccessManager fedAccessMgr) throws ExecOpExecutionException {
		final Iterator<SPARQLGraphPattern> i = up.getSubPatterns().iterator();
		Iterator<SolutionMapping> unionTranslation = handlePattern(i.next(), fedAccessMgr);
		while(i.hasNext()){
			unionTranslation = new UnionIteratorForSolMaps(unionTranslation, handlePattern(i.next(), fedAccessMgr));
		}
		return unionTranslation;
	}
	
	protected Iterator<SolutionMapping> handleGroupPattern(final SPARQLGroupPattern gp, final FederationAccessManager fedAccessMgr) throws ExecOpExecutionException {
		final Iterator<SPARQLGraphPattern> i = gp.getSubPatterns().iterator();
		Iterator<SolutionMapping> groupTranslation = handlePattern(i.next(), fedAccessMgr);
		while(i.hasNext()){
			groupTranslation = new JoiningIteratorForSolMaps(getIterableFromIterator(groupTranslation), getIterableFromIterator(handlePattern(i.next(), fedAccessMgr)));
		}
		return groupTranslation;
	}
	
	protected Iterator<SolutionMapping> handleBGP(final BGP bgp, final FederationAccessManager fedAccessMgr) throws ExecOpExecutionException {
		Iterator<SolutionMapping> bgpTranslation = null;
		for(final TriplePattern i : bgp.getTriplePatterns()) {
			if (bgpTranslation == null) {
				bgpTranslation = handleTriplePattern(i, fedAccessMgr);
			} else {
				bgpTranslation = new JoiningIteratorForSolMaps(getIterableFromIterator(bgpTranslation), getIterableFromIterator(handleTriplePattern(i, fedAccessMgr)));
			}
		}
		return bgpTranslation;
	}

	
	//Source: https://www.geeksforgeeks.org/convert-iterator-to-iterable-in-java/
	// Function to get the Spliterator
    public static <T> Iterable<T>
    getIterableFromIterator(Iterator<T> iterator)
    {
        return () -> iterator;
    }
	
}