package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.JoiningIteratorForSolMaps;
import se.liu.ida.hefquin.engine.data.utils.UnionIteratorForSolMaps;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSinkWithTranslation;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpRequestBRTPFWithTranslation extends BaseForExecOpRequest<BindingsRestrictedTriplePatternRequest,BRTPFServer>
{
	public ExecOpRequestBRTPFWithTranslation( final BindingsRestrictedTriplePatternRequest req,
	                                          final BRTPFServer fm,
	                                          final boolean collectExceptions ) {
		super( req, fm, collectExceptions );
		assert fm.getVocabularyMapping() != null;	
	}

	
	@Override
	protected void _execute(IntermediateResultElementSink sink, ExecutionContext execCxt)
			throws ExecOpExecutionException {
		
		final SPARQLGraphPattern reqTranslation = fm.getVocabularyMapping().translateTriplePattern(req.getTriplePattern());
		
		final Set<SolutionMapping> translatedReqSM = new HashSet<>();
		for (final SolutionMapping sm : req.getSolutionMappings()) {
			translatedReqSM.addAll(fm.getVocabularyMapping().translateSolutionMappingFromGlobal(sm));
		}
		
		for ( final SolutionMapping sm : handlePattern(reqTranslation, execCxt, translatedReqSM) ) {
			sink.send(sm);
		}
	}
	
	protected Iterable<SolutionMapping> handlePattern(final SPARQLGraphPattern p, final ExecutionContext execCxt, final Set<SolutionMapping> sms) throws ExecOpExecutionException{
		if (p instanceof TriplePattern) {
			return handleTriplePattern((TriplePattern) p, execCxt, sms);
		} else if (p instanceof SPARQLUnionPattern) {
			return handleUnionPattern(((SPARQLUnionPattern) p), execCxt, sms);
		} else if (p instanceof SPARQLGroupPattern) {
			return handleGroupPattern(((SPARQLGroupPattern) p), execCxt, sms);
		} else if (p instanceof BGP) {
			return handleBGP(((BGP) p), execCxt, sms);
		} else {
			throw new ExecOpExecutionException(p.toString(), this);
		}
	}
	
	protected Iterable<SolutionMapping> handleTriplePattern( final TriplePattern tp,
	                                                         final ExecutionContext execCxt,
	                                                         final Set<SolutionMapping> sms ) throws ExecOpExecutionException {
		final BRTPFRequest newReq = new BRTPFRequestImpl(tp, sms);
		final ExecOpRequestBRTPF op = new ExecOpRequestBRTPF(newReq, fm, false);
		final CollectingIntermediateResultElementSinkWithTranslation sink = new CollectingIntermediateResultElementSinkWithTranslation(fm.getVocabularyMapping());

		op.execute(sink, execCxt);

		return sink.getCollectedSolutionMappings();
	}
	
	protected Iterable<SolutionMapping> handleUnionPattern(final SPARQLUnionPattern up, final ExecutionContext execCxt, final Set<SolutionMapping> sms) throws ExecOpExecutionException {
		final Iterator<SPARQLGraphPattern> i = up.getSubPatterns().iterator();
		Iterable<SolutionMapping> unionTranslation = handlePattern(i.next(), execCxt, sms);
		while(i.hasNext()){
			unionTranslation = UnionIteratorForSolMaps.createAsIterable(unionTranslation, handlePattern(i.next(), execCxt, sms));
		}
		return unionTranslation;
	}
	
	protected Iterable<SolutionMapping> handleGroupPattern(final SPARQLGroupPattern gp, final ExecutionContext execCxt, final Set<SolutionMapping> sms) throws ExecOpExecutionException {
		final Iterator<SPARQLGraphPattern> i = gp.getSubPatterns().iterator();
		Iterable<SolutionMapping> groupTranslation = handlePattern(i.next(), execCxt, sms);
		while(i.hasNext()){
			groupTranslation = JoiningIteratorForSolMaps.createAsIterable(groupTranslation, handlePattern(i.next(), execCxt, sms));
		}
		return groupTranslation;
	}
	
	protected Iterable<SolutionMapping> handleBGP(final BGP bgp, final ExecutionContext execCxt, final Set<SolutionMapping> sms) throws ExecOpExecutionException {
		Iterable<SolutionMapping> bgpTranslation = null;
		for(final TriplePattern i : bgp.getTriplePatterns()) {
			if (bgpTranslation == null) {
				bgpTranslation = handleTriplePattern(i, execCxt, sms);
			} else {
				bgpTranslation = JoiningIteratorForSolMaps.createAsIterable(bgpTranslation, handleTriplePattern(i, execCxt, sms));
			}
		}
		return bgpTranslation;
	}

}

