package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import org.apache.jena.sparql.algebra.Op;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.JoiningIteratorForSolMaps;
import se.liu.ida.hefquin.engine.data.utils.UnionIteratorForSolMaps;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSinkWithTranslation;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class BaseForExecOpTriplePatternRequestWithTranslation<MemberType extends FederationMember> extends BaseForExecOpRequest<TriplePatternRequest, MemberType>{
	
	public BaseForExecOpTriplePatternRequestWithTranslation( final TriplePatternRequest req,
	                                                         final MemberType fm,
	                                                         final boolean collectExceptions ) {
		super( req, fm, collectExceptions );

		assert fm.getVocabularyMapping() != null;
	}
	
	@Override
	protected void _execute( final IntermediateResultElementSink sink, final ExecutionContext execCxt)
			throws ExecOpExecutionException {
		
		final SPARQLGraphPattern reqTranslation = fm.getVocabularyMapping().translateTriplePattern(req.getQueryPattern());
		for ( final SolutionMapping sm : handlePattern(reqTranslation, execCxt) ) {
			sink.send(sm);
		}	
	}
	
	protected Iterable<SolutionMapping> handlePattern(final SPARQLGraphPattern p, final ExecutionContext execCxt) throws ExecOpExecutionException{
		if (p instanceof TriplePattern) {
			return handleTriplePattern((TriplePattern) p, execCxt);
		} else if (p instanceof SPARQLUnionPattern) {
			return handleUnionPattern(((SPARQLUnionPattern) p), execCxt);
		} else if (p instanceof SPARQLGroupPattern) {
			return handleGroupPattern(((SPARQLGroupPattern) p), execCxt);
		} else if (p instanceof BGP) {
			return handleBGP(((BGP) p), execCxt);
		} else {
			throw new ExecOpExecutionException(p.toString(), this);
		}
	}
	
	protected Iterable<SolutionMapping> handleTriplePattern( final TriplePattern tp,
	                                                         final ExecutionContext execCxt ) throws ExecOpExecutionException {
		final TriplePatternRequest newReq = new TriplePatternRequestImpl(tp);
		final CollectingIntermediateResultElementSinkWithTranslation sink = new CollectingIntermediateResultElementSinkWithTranslation(fm.getVocabularyMapping());
		if (fm instanceof TPFServer) {
			final ExecOpRequestTPFatTPFServer op = new ExecOpRequestTPFatTPFServer(newReq, (TPFServer) fm, false);
			op.execute(sink, execCxt);		
		} else if (fm instanceof BRTPFServer) {
			final ExecOpRequestTPFatBRTPFServer op = new ExecOpRequestTPFatBRTPFServer(newReq, (BRTPFServer) fm, false);
			op.execute(sink, execCxt);
		} else {
			throw new ExecOpExecutionException(Op.class.toString(), this);
		}
		
		return sink.getCollectedSolutionMappings();
	}
	
	protected Iterable<SolutionMapping> handleUnionPattern(final SPARQLUnionPattern up, final ExecutionContext execCxt) throws ExecOpExecutionException {
		final Iterator<SPARQLGraphPattern> i = up.getSubPatterns().iterator();
		Iterable<SolutionMapping> unionTranslation = handlePattern(i.next(), execCxt);
		while(i.hasNext()){
			unionTranslation = UnionIteratorForSolMaps.createAsIterable(unionTranslation, handlePattern(i.next(), execCxt));
		}
		return unionTranslation;
	}
	
	protected Iterable<SolutionMapping> handleGroupPattern(final SPARQLGroupPattern gp, final ExecutionContext execCxt) throws ExecOpExecutionException {
		final Iterator<SPARQLGraphPattern> i = gp.getSubPatterns().iterator();
		Iterable<SolutionMapping> groupTranslation = handlePattern(i.next(), execCxt);
		while(i.hasNext()){
			groupTranslation = JoiningIteratorForSolMaps.createAsIterable(groupTranslation, handlePattern(i.next(), execCxt));
		}
		return groupTranslation;
	}
	
	protected Iterable<SolutionMapping> handleBGP(final BGP bgp, final ExecutionContext execCxt) throws ExecOpExecutionException {
		Iterable<SolutionMapping> bgpTranslation = null;
		for(final TriplePattern i : bgp.getTriplePatterns()) {
			if (bgpTranslation == null) {
				bgpTranslation = handleTriplePattern(i, execCxt);
			} else {
				bgpTranslation = JoiningIteratorForSolMaps.createAsIterable(bgpTranslation, handleTriplePattern(i, execCxt));
			}
		}
		return bgpTranslation;
	}

}
