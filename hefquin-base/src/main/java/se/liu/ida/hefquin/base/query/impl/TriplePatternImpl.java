package se.liu.ida.hefquin.base.query.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.jenaext.sparql.syntax.ElementUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class TriplePatternImpl implements TriplePattern
{
	protected final Triple jenaObj;

	public TriplePatternImpl( final Triple jenaObject ) {
		assert jenaObject != null;
		this.jenaObj = jenaObject;
	}

	public TriplePatternImpl( final Node s, final Node p, final Node o ) {
		this( Triple.create(s,p,o) );
	}

	@Override
	public Triple asJenaTriple() {
		return jenaObj;
	}

	@Override
	public int numberOfVars() {
		final String s = ( jenaObj.getSubject().isVariable() ) ? jenaObj.getSubject().getName() : null;
		final String p = ( jenaObj.getPredicate().isVariable() ) ? jenaObj.getPredicate().getName() : null;
		final String o = ( jenaObj.getObject().isVariable() ) ? jenaObj.getObject().getName() : null;

		int n = 0;
		if ( s != null )
			n++;

		if ( p != null && ! p.equals(s) )
			n++;

		if ( o != null && ! o.equals(s) && ! o.equals(p) )
			n++;

		return n;
	}

	@Override
	public String toString() {
		// wrapping the triple pattern into an ElementTriplesBlock
		// because the toString() function of that one uses pretty
		// printing via FormatterElement
		final ElementTriplesBlock e = new ElementTriplesBlock();
		e.addTriple( this.asJenaTriple() );
		return e.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TriplePattern)) return false;
		final TriplePattern that = (TriplePattern) o;
		return Objects.equals( jenaObj, that.asJenaTriple() );
	}

	@Override
	public int hashCode() {
		return jenaObj.hashCode();
	}

	@Override
	public Set<TriplePattern> getAllMentionedTPs() {
		return Collections.singleton(this);
	}

	@Override
	public Set<Var> getCertainVariables() {
		return getAllMentionedVariables();
	}

	@Override
	public Set<Var> getPossibleVariables() {
		return Collections.emptySet();
	}

	@Override
	public Set<Var> getAllMentionedVariables() {
		final Node s = jenaObj.getSubject();
		final Node p = jenaObj.getPredicate();
		final Node o = jenaObj.getObject();

		final Set<Var> vars = new HashSet<>();

		if ( s.isVariable() ) { vars.add( Var.alloc(s) ); }
		if ( p.isVariable() ) { vars.add( Var.alloc(p) ); }
		if ( o.isVariable() ) { vars.add( Var.alloc(o) ); }

		return vars;
	}

	@Override
	public int getNumberOfVarMentions() {
		int n = 0;
		if ( jenaObj.getSubject().isVariable() )   { n++; }
		if ( jenaObj.getPredicate().isVariable() ) { n++; }
		if ( jenaObj.getObject().isVariable() )    { n++; }
		return n;
	}

	@Override
	public int getNumberOfTermMentions() {
		int n = 0;
		if ( ! jenaObj.getSubject().isVariable() )   { n++; }
		if ( ! jenaObj.getPredicate().isVariable() ) { n++; }
		if ( ! jenaObj.getObject().isVariable() )    { n++; }
		return n;
	}

	@Override
	public TriplePattern applySolMapToGraphPattern( final SolutionMapping sm )
			throws VariableByBlankNodeSubstitutionException
	{
		final Binding b = sm.asJenaBinding();
		boolean unchanged = true;

		Node s = jenaObj.getSubject();
		if ( s.isVariable() ) {
			final Var var = Var.alloc(s);
			if ( b.contains(var) ) {
				s = b.get(var);
				unchanged = false;
				if ( s.isBlank() ) {
					throw new VariableByBlankNodeSubstitutionException();
				}
			}
		}

		Node p = jenaObj.getPredicate();
		if ( p.isVariable() ) {
			final Var var = Var.alloc(p);
			if ( b.contains(var) ) {
				p = b.get(var);
				unchanged = false;
				if ( p.isBlank() ) {
					throw new VariableByBlankNodeSubstitutionException();
				}
			}
		}

		Node o = jenaObj.getObject();
		if ( o.isVariable() ) {
			final Var var = Var.alloc(o);
			if ( b.contains(var) ) {
				o = b.get(var);
				unchanged = false;
				if ( o.isBlank() ) {
					throw new VariableByBlankNodeSubstitutionException();
				}
			}
		}

		return unchanged ? this : new TriplePatternImpl(s,p,o);
	}

	@Override
	public SPARQLGraphPattern mergeWith( final SPARQLGraphPattern other ) {
		if ( other instanceof TriplePattern tp )
			return mergeWith(tp);

		if ( other instanceof BGP bgp ) {
			return mergeWith(bgp);
		}

		final Element elmt = QueryPatternUtils.convertToJenaElement(other);
		return merge(this, elmt);
	}

	@Override
	public BGP mergeWith( final TriplePattern other ) {
		return new BGPImpl( this, other );
	}

	@Override
	public BGP mergeWith( final BGP bgp ) {
		return new BGPImpl(this, bgp);
	}

	/**
	 * Merges the given triple pattern into the given graph pattern. If the
	 * given graph pattern is also a triple pattern or a BGP, then the resulting
	 * graph pattern is a BGP to which the triple pattern was added. Otherwise,
	 * the resulting graph pattern is the given graph pattern with the triple
	 * pattern joined into it.
	 */
	public static SPARQLGraphPattern merge( final TriplePattern tp, final Element elmt ) {
		// If we can still create a BGP, then we do that.
		if ( elmt instanceof ElementTriplesBlock block ) {
			// create the BGP and add the given triple pattern into it
			final BGPImpl resultBGP = new BGPImpl(tp);

			// add the triple patterns from the given graph pattern into the BGP as well
			final Iterator<Triple> it = block.patternElts();
			while ( it.hasNext() ) {
				resultBGP.addTriplePattern( new TriplePatternImpl(it.next()) );
			}

			return resultBGP;
		}

		final Element resultElmt = ElementUtils.merge( tp.asJenaTriple(), elmt );
		return new GenericSPARQLGraphPatternImpl1(resultElmt);
	}

}
