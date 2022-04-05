package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.NodeFactory;
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
		SPARQLGraphPattern reqTranslation = fm.getVocabularyMapping().translateTriplePattern(req.getQueryPattern());
		if(reqTranslation instanceof TriplePattern) {
			final TPFResponse res = FederationAccessUtils.performRequest(fedAccessMgr, new TPFRequestImpl((TriplePattern) reqTranslation), fm);
			final List<Triple> resTranslation = new ArrayList<>();
			for(final Triple i : res.getPayload()) {
				BindingBuilder bb = BindingBuilder.create();
				final Var s = Var.alloc("s");
				final Var p = Var.alloc("p");
				final Var o = Var.alloc("o");
				bb.add(s, i.asJenaTriple().getSubject());
				bb.add(p, i.asJenaTriple().getPredicate());
				bb.add(o, i.asJenaTriple().getObject());
				SolutionMapping sm = new SolutionMappingImpl(bb.build());
				for (final SolutionMapping j : fm.getVocabularyMapping().translateSolutionMapping(sm)) {
					Binding b = j.asJenaBinding();
					resTranslation.add(new TripleImpl(b.get(s), b.get(p), b.get(o)));
				}
			}
			final List<Triple> allTriples = new ArrayList<>();
			res.getMetadata().forEach(allTriples::add);
			return new TPFResponseImpl(resTranslation, allTriples, res.getNextPageURL(), fm, req, res.getRequestStartTime());
		}
		
		else if(reqTranslation instanceof SPARQLUnionPattern) {
			final Set<Triple> resTranslation = new HashSet<>();
			for (final SPARQLGraphPattern i : ((SPARQLUnionPattern) reqTranslation).getSubPatterns()) {
				if (i instanceof TriplePattern) {
					final TPFResponse res =FederationAccessUtils.performRequest(fedAccessMgr, new TPFRequestImpl((TriplePattern) i), fm);
					for(final Triple j : res.getPayload()) {
						BindingBuilder bb = BindingBuilder.create();
						final Var s = Var.alloc("s");
						final Var p = Var.alloc("p");
						final Var o = Var.alloc("o");
						bb.add(s, j.asJenaTriple().getSubject());
						bb.add(p, j.asJenaTriple().getPredicate());
						bb.add(o, j.asJenaTriple().getObject());
						SolutionMapping sm = new SolutionMappingImpl(bb.build());
						for (final SolutionMapping k : fm.getVocabularyMapping().translateSolutionMapping(sm)) {
							Binding b = k.asJenaBinding();
							resTranslation.add(new TripleImpl(b.get(s), b.get(p), b.get(o)));
						}
					}
				}
				//TODO: Add all other option of instance
				else {
					throw new FederationAccessException(i.toString(), req, fm);
				}
			}
			final List<Triple> resTriples = new ArrayList<>();
			resTriples.addAll(resTranslation);
			return new TPFResponseImpl(resTriples ,resTriples, null, fm, req, null);
		}
		
		//Same for GroupPattern
		else if(reqTranslation instanceof SPARQLGroupPattern) {
			for(final SPARQLGraphPattern i : ((SPARQLGroupPattern) reqTranslation).getSubPatterns()) {
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
		
		else if(reqTranslation instanceof BGP) {
			Set<Triple> resTranslation = new HashSet<>();
			for(final TriplePattern i : ((BGP) reqTranslation).getTriplePatterns()) {
				final TPFResponse res =FederationAccessUtils.performRequest(fedAccessMgr, new TPFRequestImpl((TriplePattern) i), fm);
				final Set<Triple> partialRes = new HashSet<>();
				for(final Triple j : res.getPayload()) {
					BindingBuilder bb = BindingBuilder.create();
					final Var s = Var.alloc("s");
					final Var p = Var.alloc("p");
					final Var o = Var.alloc("o");
					bb.add(s, j.asJenaTriple().getSubject());
					bb.add(p, j.asJenaTriple().getPredicate());
					bb.add(o, j.asJenaTriple().getObject());
					SolutionMapping sm = new SolutionMappingImpl(bb.build());
					for (final SolutionMapping k : fm.getVocabularyMapping().translateSolutionMapping(sm)) {
						Binding b = k.asJenaBinding();
						Triple t = new TripleImpl(b.get(s), b.get(p), b.get(o));
						if (resTranslation.contains(t)) {
							partialRes.add(t);
						}
					}
				}
				resTranslation = partialRes;
			}
			final List<Triple> resTriples = new ArrayList<>();
			resTriples.addAll(resTranslation);
			return new TPFResponseImpl(resTriples ,resTriples, null, fm, req, null);
		}
		
		else {
			throw new FederationAccessException(reqTranslation.toString(), req, fm);
		}
		
		return null;
	}
}
