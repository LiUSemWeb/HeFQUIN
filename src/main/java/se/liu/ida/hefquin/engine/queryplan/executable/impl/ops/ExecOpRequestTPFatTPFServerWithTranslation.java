package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;
import java.util.ListIterator;
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
			tripleTranslation.addAll(translateResultTriple(i, newReq.getQueryPattern()));
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
		List<Set<Triple>> partialTranslations = new ArrayList<>();
		for(final SPARQLGraphPattern i : gp.getSubPatterns()) {
			if (i instanceof TriplePattern) {
				partialTranslations.add(handleTriplePattern((TriplePattern) i, fedAccessMgr));
			} else if (i instanceof SPARQLUnionPattern) {
				partialTranslations.add(handleUnionPattern(((SPARQLUnionPattern) i), fedAccessMgr));
			} else if (i instanceof SPARQLGroupPattern) {
				partialTranslations.add(handleGroupPattern(((SPARQLGroupPattern) i), fedAccessMgr));
			} else if (i instanceof BGP) {
				partialTranslations.add(handleBGP(((BGP) i), fedAccessMgr));
			} else {
				throw new FederationAccessException(i.toString(), req, fm);
			}
		}
		Set<Triple> groupTranslation = new HashSet<>();
		for(final Triple j : partialTranslations.get(0)) {
			boolean join = true;
			ListIterator<Set<Triple>> k = partialTranslations.listIterator(1);
			while(k.hasNext()) {
				if (!k.next().contains(j)){
					join = false;
					break;
				}
			}
			if (join) {
				groupTranslation.add(j);
			}
		}
		return groupTranslation;
	}
	
	protected Set<Triple> handleBGP(final BGP bgp, final FederationAccessManager fedAccessMgr) throws FederationAccessException{
		List<Set<Triple>> partialTranslations = new ArrayList<>();
		for(final TriplePattern i : bgp.getTriplePatterns()) {
			partialTranslations.add(handleTriplePattern(i, fedAccessMgr));
		}
		Set<Triple> bgpTranslation = new HashSet<>();
		for(final Triple j : partialTranslations.get(0)) {
			boolean join = true;
			ListIterator<Set<Triple>> k = partialTranslations.listIterator(1);
			while(k.hasNext()) {
				if (!k.next().contains(j)){
					join = false;
					break;
				}
			}
			if (join) {
				bgpTranslation.add(j);
			}
		}
		return bgpTranslation;
	}
	
	protected Set<Triple> translateResultTriple(final Triple t, final TriplePattern q){
		final BindingBuilder bb = BindingBuilder.create();
		final org.apache.jena.graph.Triple jq = q.asJenaTriple();
		final org.apache.jena.graph.Triple jt = t.asJenaTriple();
		boolean sVar = false;
		boolean pVar = false;
		boolean oVar = false;
		if (jq.getSubject().isVariable()) {
			sVar = true;
			bb.add((Var) jq.getSubject(), jt.getSubject());
		}
		if (jq.getPredicate().isVariable()) {
			pVar = true;
			bb.add((Var) jq.getPredicate(), jt.getPredicate());
		}
		if (jq.getObject().isVariable()) {
			oVar = true;
			bb.add((Var) jq.getObject(), jt.getObject());
		}
		final SolutionMapping sm = new SolutionMappingImpl(bb.build());
		
		Set<Triple> res = new HashSet<>();
		for (final SolutionMapping k : fm.getVocabularyMapping().translateSolutionMapping(sm)) {
			final Binding b = k.asJenaBinding();
			Triple newT = null;
			
			if (sVar) {
				if (pVar) {
					if (oVar) {
						newT = new TripleImpl(b.get((Var) jq.getSubject()), b.get((Var) jq.getPredicate()), b.get((Var) jq.getObject()));
					} else {
						newT = new TripleImpl(b.get((Var) jq.getSubject()), b.get((Var) jq.getPredicate()), jt.getObject());
					}
				} else {
					if (oVar) {
						newT = new TripleImpl(b.get((Var) jq.getSubject()), jt.getPredicate(), b.get((Var) jq.getObject()));
					} else {
						newT = new TripleImpl(b.get((Var) jq.getSubject()), jt.getPredicate(), jt.getObject());
					}
				}
			} else {
				if (pVar) {
					if (oVar) {
						newT = new TripleImpl(jt.getSubject(), b.get((Var) jq.getPredicate()), b.get((Var) jq.getObject()));
					} else {
						newT = new TripleImpl(jt.getSubject(), b.get((Var) jq.getPredicate()), jt.getObject());
					}
				} else {
					if (oVar) {
						newT = new TripleImpl(jt.getSubject(), jt.getPredicate(), b.get((Var) jq.getObject()));
					} else {
						newT = new TripleImpl(jt.getSubject(), jt.getPredicate(), jt.getObject());
					}
				}
			}
			
			res.add(newT);
		}
		return res;
	}
}
