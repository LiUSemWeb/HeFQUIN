package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.data.impl.TripleImpl;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseImpl;
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
		final SPARQLGraphPattern reqTranslation = fm.getVocabularyMapping().translateTriplePattern(req.getQueryPattern());
		final List<Triple> resList = new ArrayList<>();
		if(reqTranslation instanceof TriplePattern) {
			resList.addAll(handleTriplePattern((TriplePattern) reqTranslation, fedAccessMgr));
		}
		
		else if(reqTranslation instanceof SPARQLUnionPattern) {
			resList.addAll(handleUnionPattern(((SPARQLUnionPattern) reqTranslation), fedAccessMgr));
		}
		
		else if(reqTranslation instanceof SPARQLGroupPattern) {
			resList.addAll(handleGroupPattern(((SPARQLGroupPattern) reqTranslation), fedAccessMgr));
		}
		
		else if(reqTranslation instanceof BGP) {
			resList.addAll(handleBGP((BGP) reqTranslation, fedAccessMgr));
		}
		
		else {
			throw new FederationAccessException(reqTranslation.toString(), req, fm);
		}
		
		return new TPFResponseImpl(resList, resList, null, fm, req, null);
	}
	
	protected Set<Triple> handleTriplePattern(final TriplePattern tp, final FederationAccessManager fedAccessMgr) throws FederationAccessException{
		final TPFRequest newReq = new TPFRequestImpl(tp);
		final TPFResponse res = FederationAccessUtils.performRequest(fedAccessMgr, newReq, fm);
		final Set<Triple> tripleTranslation = new HashSet<>();
		for(final Triple i : res.getPayload()) {
			tripleTranslation.addAll(translateResultTriple(i, newReq));
		}
		return tripleTranslation;
	}
	
	protected Set<Triple> handleUnionPattern(final SPARQLUnionPattern up, final FederationAccessManager fedAccessMgr) throws FederationAccessException{
		final Set<Triple> unionTranslation = new HashSet<>();
		for(final SPARQLGraphPattern i : up.getSubPatterns()) {
			if (i instanceof TriplePattern) {
				unionTranslation.addAll(handleTriplePattern(((TriplePattern) i), fedAccessMgr));
			} else if (i instanceof SPARQLUnionPattern) {
				unionTranslation.addAll(handleUnionPattern(((SPARQLUnionPattern) i), fedAccessMgr));
			} else if (i instanceof SPARQLGroupPattern) {
				unionTranslation.addAll(handleGroupPattern(((SPARQLGroupPattern) i), fedAccessMgr));
			} else if (i instanceof BGP) {
				unionTranslation.addAll(handleBGP(((BGP) i), fedAccessMgr));
			} else {
				throw new FederationAccessException(i.toString(), req, fm);
			}
		}
		return unionTranslation;
	}
	
	protected Set<Triple> handleGroupPattern(final SPARQLGroupPattern gp, final FederationAccessManager fedAccessMgr) throws FederationAccessException{
		Set<Triple> groupTranslation = null;
		for(final SPARQLGraphPattern i : gp.getSubPatterns()) {
			if (i instanceof TriplePattern) {
				if (groupTranslation == null) {
					groupTranslation = handleTriplePattern((TriplePattern) i, fedAccessMgr);
				} else {
					final Set<Triple> partialTranslation = new HashSet<>();
					for(final Triple j : handleTriplePattern((TriplePattern) i, fedAccessMgr)) {
						if (groupTranslation.contains(j)) {
							partialTranslation.add(j);
						}
					}
					groupTranslation = partialTranslation;
				}
			} else if (i instanceof SPARQLUnionPattern) {
				if (groupTranslation == null) {
					groupTranslation = handleUnionPattern(((SPARQLUnionPattern) i), fedAccessMgr);
				} else {
					final Set<Triple> partialTranslation = new HashSet<>();
					for(final Triple j : handleUnionPattern(((SPARQLUnionPattern) i), fedAccessMgr)) {
						if (groupTranslation.contains(j)) {
							partialTranslation.add(j);
						}
					}
					groupTranslation = partialTranslation;
				}
			} else if (i instanceof SPARQLGroupPattern) {
				if (groupTranslation == null) {
					groupTranslation = handleGroupPattern(((SPARQLGroupPattern) i), fedAccessMgr);
				} else {
					final Set<Triple> partialTranslation = new HashSet<>();
					for(final Triple j : handleGroupPattern(((SPARQLGroupPattern) i), fedAccessMgr)) {
						if (groupTranslation.contains(j)) {
							partialTranslation.add(j);
						}
					}
					groupTranslation = partialTranslation;
				}
				
			} else if (i instanceof BGP) {
				if (groupTranslation == null) {
					groupTranslation = handleBGP(((BGP) i), fedAccessMgr);
				} else {
					final Set<Triple> partialTranslation = new HashSet<>();
					for(final Triple j : handleBGP(((BGP) i), fedAccessMgr)) {
						if (groupTranslation.contains(j)) {
							partialTranslation.add(j);
						}
					}
					groupTranslation = partialTranslation;
				}
			} else {
				throw new FederationAccessException(i.toString(), req, fm);
			}
		}
		return groupTranslation;
	}
	
	protected Set<Triple> handleBGP(final BGP bgp, final FederationAccessManager fedAccessMgr) throws FederationAccessException{
		Set<Triple> bgpTranslation = null;
		for(final TriplePattern i : bgp.getTriplePatterns()) {
			if (bgpTranslation == null) {
				bgpTranslation = handleTriplePattern(i, fedAccessMgr);
			} else {
				final Set<Triple> partialRes = new HashSet<>();
				for (Triple j : handleTriplePattern(i, fedAccessMgr)) {
					if (bgpTranslation.contains(j)) {
						partialRes.add(j);
					}
				}
				bgpTranslation = partialRes;
			}
		}
		return bgpTranslation;
	}
	
	protected Set<Triple> translateResultTriple(final Triple t, final TPFRequest q){
		final BindingBuilder bb = BindingBuilder.create();
		final org.apache.jena.graph.Triple jt = q.getQueryPattern().asJenaTriple();
		boolean sVar = false;
		boolean pVar = false;
		boolean oVar = false;
		if (jt.getSubject().isVariable()) {
			sVar = true;
			bb.add((Var) jt.getSubject(), t.asJenaTriple().getSubject());
		}
		if (jt.getPredicate().isVariable()) {
			pVar = true;
			bb.add((Var) jt.getPredicate(), t.asJenaTriple().getPredicate());
		}
		if (jt.getObject().isVariable()) {
			oVar = true;
			bb.add((Var) jt.getObject(), t.asJenaTriple().getObject());
		}
		final SolutionMapping sm = new SolutionMappingImpl(bb.build());
		
		Set<Triple> res = new HashSet<>();
		for (final SolutionMapping k : fm.getVocabularyMapping().translateSolutionMapping(sm)) {
			final Binding b = k.asJenaBinding();
			Triple newT = null;
			
			if (sVar) {
				if (pVar) {
					if (oVar) {
						newT = new TripleImpl(b.get((Var) jt.getSubject()), b.get((Var) jt.getPredicate()), b.get((Var) jt.getObject()));
					} else {
						newT = new TripleImpl(b.get((Var) jt.getSubject()), b.get((Var) jt.getPredicate()), t.asJenaTriple().getObject());
					}
				} else {
					if (oVar) {
						newT = new TripleImpl(b.get((Var) jt.getSubject()), t.asJenaTriple().getPredicate(), b.get((Var) jt.getObject()));
					} else {
						newT = new TripleImpl(b.get((Var) jt.getSubject()), t.asJenaTriple().getPredicate(), t.asJenaTriple().getObject());
					}
				}
			} else {
				if (pVar) {
					if (oVar) {
						newT = new TripleImpl(t.asJenaTriple().getSubject(), b.get((Var) jt.getPredicate()), b.get((Var) jt.getObject()));
					} else {
						newT = new TripleImpl(t.asJenaTriple().getSubject(), b.get((Var) jt.getPredicate()), t.asJenaTriple().getObject());
					}
				} else {
					if (oVar) {
						newT = new TripleImpl(t.asJenaTriple().getSubject(), t.asJenaTriple().getPredicate(), b.get((Var) jt.getObject()));
					} else {
						newT = new TripleImpl(t.asJenaTriple().getSubject(), t.asJenaTriple().getPredicate(), t.asJenaTriple().getObject());
					}
				}
			}
			
			res.add(newT);
		}
		return res;
	}
}
