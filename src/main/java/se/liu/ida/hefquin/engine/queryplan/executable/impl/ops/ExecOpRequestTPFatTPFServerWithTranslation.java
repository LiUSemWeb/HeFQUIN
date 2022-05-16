package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
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
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ClosableIntermediateResultElementSink;
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
	protected void _execute(IntermediateResultElementSink sink, ExecutionContext execCxt)
			throws ExecOpExecutionException {
		
		FederationAccessManager fedAccessMgr = execCxt.getFederationAccessMgr();
		final SPARQLGraphPattern reqTranslation = fm.getVocabularyMapping().translateTriplePattern(req.getQueryPattern());
		Iterator<SolutionMapping> res;
		if(reqTranslation instanceof TriplePattern) {
			res = handleTriplePattern((TriplePattern) reqTranslation, fedAccessMgr).iterator();
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
	
	protected Iterable<SolutionMapping> handleTriplePattern(final TriplePattern tp, final FederationAccessManager fedAccessMgr) throws ExecOpExecutionException{
		
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
	
		return sink.getMaterializedIntermediateResult();

	}
	
	protected Iterator<SolutionMapping> handleUnionPattern(final SPARQLUnionPattern up, final FederationAccessManager fedAccessMgr) throws ExecOpExecutionException {
		Iterator<SolutionMapping> unionTranslation = null;
		for(final SPARQLGraphPattern i : up.getSubPatterns()) {
			if (i instanceof TriplePattern) {
				if (unionTranslation == null) {
					unionTranslation = handleTriplePattern(((TriplePattern) i), fedAccessMgr).iterator();
				} else {
					unionTranslation = new UnionIteratorForSolMaps(unionTranslation, handleTriplePattern(((TriplePattern) i), fedAccessMgr).iterator());
				}
			} else if (i instanceof SPARQLUnionPattern) {
				if (unionTranslation == null) {
					unionTranslation = handleUnionPattern(((SPARQLUnionPattern) i), fedAccessMgr);
				} else {
					unionTranslation = new UnionIteratorForSolMaps(unionTranslation, handleUnionPattern(((SPARQLUnionPattern) i), fedAccessMgr));
				}
			} else if (i instanceof SPARQLGroupPattern) {
				if (unionTranslation == null) {
					unionTranslation = handleGroupPattern(((SPARQLGroupPattern) i), fedAccessMgr);
				} else {
					unionTranslation = new UnionIteratorForSolMaps(unionTranslation, handleGroupPattern(((SPARQLGroupPattern) i), fedAccessMgr));
				}
			} else if (i instanceof BGP) {
				if (unionTranslation == null) {
					unionTranslation = handleBGP(((BGP) i), fedAccessMgr);
				} else {
					unionTranslation = new UnionIteratorForSolMaps(unionTranslation, handleBGP(((BGP) i), fedAccessMgr));
				}
			} else {
				throw new ExecOpExecutionException(i.toString(), this);
			}
		}
		return unionTranslation;
	}
	
	protected Iterator<SolutionMapping> handleGroupPattern(final SPARQLGroupPattern gp, final FederationAccessManager fedAccessMgr) throws ExecOpExecutionException {
		Iterator<SolutionMapping> groupTranslation = null;
		for(final SPARQLGraphPattern i : gp.getSubPatterns()) {
			if (i instanceof TriplePattern) {
				if (groupTranslation == null) {
					groupTranslation = handleTriplePattern((TriplePattern) i, fedAccessMgr).iterator();
				} else {
					groupTranslation = new JoiningIteratorForSolMaps(getIterableFromIterator(groupTranslation), handleTriplePattern((TriplePattern) i, fedAccessMgr));
				}
			} else if (i instanceof SPARQLUnionPattern) {
				if (groupTranslation == null) {
					groupTranslation = handleUnionPattern(((SPARQLUnionPattern) i), fedAccessMgr);
				} else {
					groupTranslation = new JoiningIteratorForSolMaps(getIterableFromIterator(groupTranslation), getIterableFromIterator(handleUnionPattern(((SPARQLUnionPattern) i), fedAccessMgr)));
				}
			} else if (i instanceof SPARQLGroupPattern) {
				if (groupTranslation == null) {
					groupTranslation = handleGroupPattern(((SPARQLGroupPattern) i), fedAccessMgr);
				} else {
					groupTranslation = new JoiningIteratorForSolMaps(getIterableFromIterator(groupTranslation), getIterableFromIterator(handleGroupPattern(((SPARQLGroupPattern) i), fedAccessMgr)));
				}
			} else if (i instanceof BGP) {
				if (groupTranslation == null) {
					groupTranslation = handleBGP(((BGP) i), fedAccessMgr);
				} else {
					groupTranslation = new JoiningIteratorForSolMaps(getIterableFromIterator(groupTranslation), getIterableFromIterator(handleBGP(((BGP) i), fedAccessMgr)));
				}
			} else {
				throw new ExecOpExecutionException(i.toString(), this);
			}
		}
		return groupTranslation;
	}
	
	protected Iterator<SolutionMapping> handleBGP(final BGP bgp, final FederationAccessManager fedAccessMgr) throws ExecOpExecutionException {
		Iterator<SolutionMapping> bgpTranslation = null;
		for(final TriplePattern i : bgp.getTriplePatterns()) {
			if (bgpTranslation == null) {
				bgpTranslation = handleTriplePattern((TriplePattern) i, fedAccessMgr).iterator();
			} else {
				bgpTranslation = new JoiningIteratorForSolMaps(getIterableFromIterator(bgpTranslation), handleTriplePattern((TriplePattern) i, fedAccessMgr));
			}
		}
		return bgpTranslation;
	}
	
	//Helper class
	protected static class MaterializingIntermediateResultElementSinkWithTranslation implements ClosableIntermediateResultElementSink {
		
		protected final VocabularyMapping vocabularyMapping;
		protected final List<SolutionMapping> l = new ArrayList<>();
		private boolean closed = false;
		
		public MaterializingIntermediateResultElementSinkWithTranslation(final VocabularyMapping vm) {
			this.vocabularyMapping = vm;
		}
		
		@Override
		public void send(SolutionMapping element) {
			if ( ! closed ) {
				for (SolutionMapping sm : vocabularyMapping.translateSolutionMapping(element)) {
					l.add(sm);
				}
			}
		}
		
		@Override
		public void close() {
			closed = true;
		}
		
		@Override
		public boolean isClosed() {
			return closed;
		}
		
		public Iterable<SolutionMapping> getMaterializedIntermediateResult() {
			return l;
		}
	}

	
	//Source: https://www.geeksforgeeks.org/convert-iterator-to-iterable-in-java/
	// Function to get the Spliterator
    public static <T> Iterable<T>
    getIterableFromIterator(Iterator<T> iterator)
    {
        return () -> iterator;
    }
	
}