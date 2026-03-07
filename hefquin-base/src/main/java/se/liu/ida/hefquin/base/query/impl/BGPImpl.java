package se.liu.ida.hefquin.base.query.impl;

import java.util.*;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.FormatterElement;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;

public class BGPImpl implements BGP
{
	private final Set<TriplePattern> tps;

	public BGPImpl() {
		this.tps = new HashSet<>();
	}

	public BGPImpl( final Set<TriplePattern> tps ) {
		this.tps = new HashSet<>(tps);
	}

	public BGPImpl( final TriplePattern tp, final TriplePattern ... tps ) {
		this.tps = new HashSet<>();
		this.tps.add(tp);
		this.tps.addAll( Arrays.asList(tps) );
	}

	public BGPImpl( final BGP otherBGP, final BGP ... otherBGPs ) {
		tps = new HashSet<>( otherBGP.getTriplePatterns() );
		for ( final BGP anotherBGP : otherBGPs ) {
			tps.addAll( anotherBGP.getTriplePatterns() );
		}
	}

	public BGPImpl( final TriplePattern tp, final BGP otherBGP ) {
		tps = new HashSet<>();
		tps.add(tp);
		tps.addAll( otherBGP.getTriplePatterns() );
	}

	public BGPImpl( final BGP otherBGP, final TriplePattern tp ) {
		tps = new HashSet<>();
		tps.addAll( otherBGP.getTriplePatterns() );
		tps.add(tp);
	}

	public BGPImpl( final List<Triple> tps ) {
		this.tps = new HashSet<>();
		for ( final Triple tp : tps ) {
			this.tps.add( new TriplePatternImpl(tp) );
		}
	}

	public BGPImpl( final BasicPattern pattern ) {
		this( pattern.getList() );
	}

	/**
	 * Assumes that the given {@link ElementPathBlock} does not contain
	 * property path patterns (but only triple patterns). If it does,
	 * this methods throws an {@link IllegalArgumentException}.
	 */
	public BGPImpl( final ElementPathBlock pattern ) {
		this( pattern.getPattern() );
	}

	/**
	 * Assumes that the given {@link PathBlock} does not contain property path
	 * patterns (but only triple patterns). If it does, this methods throws an
	 * {@link IllegalArgumentException}.
	 */
	public BGPImpl( final PathBlock pattern ) {
		tps = new HashSet<>();
		for ( final TriplePath tp : pattern.getList() ) {
			if ( ! tp.isTriple() ) {
				throw new IllegalArgumentException( "the given PathBlock contains a property path pattern (" + tp.toString() + ")" );
			}
			tps.add( new TriplePatternImpl(tp.asTriple()) );
		}
	}

	@Override
	public Set<TriplePattern> getTriplePatterns() {
		return Collections.unmodifiableSet(tps);
	}

	public void addTriplePattern( final TriplePattern tp ) {
		tps.add(tp);
	}

	@Override
	public String toString(){
		final StringBuilder builder = new StringBuilder();

		builder.append( "(bgp ");
		for ( TriplePattern tp: tps){
			builder.append( tp.toString() );
			builder.append( " ." );
		}
		builder.append( " )");

		return builder.toString();
	}

	@Override
	public boolean equals( final Object o ) {
		if (this == o) return true;
		if ( !(o instanceof BGP) ) return false;

		if ( o instanceof BGPImpl ) {
			final BGPImpl bgp = (BGPImpl) o;
			return Objects.equals(tps, bgp.tps);
		}
		else {
			final BGP bgp = (BGP) o;
			final Set<? extends TriplePattern> tpsOther = bgp.getTriplePatterns();
			return (tps.size() == tpsOther.size()) && tps.containsAll(tpsOther);
		}
	}

	@Override
	public int hashCode() {
		int code = Objects.hash( super.getClass().getName() );
		for( TriplePattern tp: tps){
			code = code ^ tp.hashCode();
		}
		return code;
	}

	@Override
	public Set<TriplePattern> getAllMentionedTPs() {
		return getTriplePatterns();
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
		// To reduce the number of HashSet objects being created,
		// this implementation inspects the triple patterns directly
		// instead of simply using their getAllMentionedVariables()
		// method.
		final Set<Var> vars = new HashSet<>();
		for ( final TriplePattern tp : tps ) {
			final Node s = tp.asJenaTriple().getSubject();
			final Node p = tp.asJenaTriple().getPredicate();
			final Node o = tp.asJenaTriple().getObject();

			if ( s.isVariable() ) { vars.add( Var.alloc(s) ); }
			if ( p.isVariable() ) { vars.add( Var.alloc(p) ); }
			if ( o.isVariable() ) { vars.add( Var.alloc(o) ); }
		}

		return vars;
	}

	@Override
	public int getNumberOfVarMentions() {
		int n = 0;
		for ( final TriplePattern tp : tps ) {
			n += tp.getNumberOfVarMentions();
		}

		return n;
	}

	@Override
	public int getNumberOfTermMentions() {
		int n = 0;
		for ( final TriplePattern tp : tps ) {
			n += tp.getNumberOfTermMentions();
		}

		return n;
	}

	@Override
	public BGP applySolMapToGraphPattern( final Binding sm )
			throws VariableByBlankNodeSubstitutionException
	{
		final Set<TriplePattern> tps2 = new HashSet<>();
		boolean unchanged = true;

		for ( final TriplePattern tp : tps ) {
			final TriplePattern tp2 = tp.applySolMapToGraphPattern(sm);
			tps2.add(tp2);

			if ( tp != tp2 ) unchanged = false;
		}

		return unchanged ? this : new BGPImpl(tps2);
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
	public BGP mergeWith( final TriplePattern tp ) {
		return new BGPImpl(this, tp);
	}

	@Override
	public BGP mergeWith( final BGP otherBGP ) {
		return new BGPImpl(this, otherBGP);
	}

	/**
	 * Merges the given BGP into the given graph pattern. If the given graph
	 * pattern is also a BGP, then the resulting graph pattern is a BGP that
	 * is the union of the two given BGPs. Otherwise, the resulting graph
	 * pattern is the given graph pattern with the BGP joined into it.
	 */
	public static SPARQLGraphPattern merge( final BGP bgp, final Element elmt ) {
		// If we can still create a BGP, then we do that.
		if ( elmt instanceof ElementTriplesBlock block ) {
			// create the BGP
			final BGPImpl resultBGP = new BGPImpl();

			// add the triple patterns from the given graph pattern to the BGP
			final Iterator<Triple> it = block.patternElts();
			while ( it.hasNext() ) {
				resultBGP.addTriplePattern( new TriplePatternImpl(it.next()) );
			}

			// add the triple patterns of the given BGP as well
			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				resultBGP.addTriplePattern(tp);
			}

			return resultBGP;
		}

		// At this point it is clear that we will return a GenericSPARQLGraphPatternImpl1,
		// for which we need to create an Element object first. The type of Element that
		// we create depends on the type of the Element that the given graph pattern was
		// converted to.
		final Element resultElmt;

		if ( elmt instanceof ElementPathBlock block ) {
			// If the given graph pattern was converted to an ElementPathBlock,
			// create a copy of that ElementPathBlock, and add the triple patterns
			// of the given BGP into that copy.
			final ElementPathBlock copy = new ElementPathBlock();

			final Iterator<TriplePath> it = block.patternElts();
			while ( it.hasNext() ) {
				copy.addTriple( it.next() );
			}

			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				copy.addTriple( tp.asJenaTriple() );
			}

			resultElmt = copy;
		}
		else if ( elmt instanceof ElementGroup eg ) {
			// If the given graph pattern was converted to an ElementGroup,
			// create a copy of that ElementGroup with the same sub-elements.
			// When creating the copy, try to add the triple patterns of the
			// given BGP into a copy of one of the sub-elements. If that's not
			// possible (i.e., none of the sub-elements is of a suitable type),
			// then add the BGP as an additional sub-element to the copy in the
			// end.
			final ElementGroup newGroup = new ElementGroup();
			boolean bgpAdded = false;
			for ( final Element subElmt : eg.getElements() )
			{
				if ( ! bgpAdded && subElmt instanceof ElementTriplesBlock block ) {
					final ElementTriplesBlock copy = new ElementTriplesBlock();

					final Iterator<Triple> it = block.patternElts();
					while ( it.hasNext() ) {
						copy.addTriple( it.next() );
					}

					for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
						copy.addTriple( tp.asJenaTriple() );
					}
					bgpAdded = true;

					newGroup.addElement(copy);
				}
				else if ( ! bgpAdded && subElmt instanceof ElementPathBlock block ) {
					final ElementPathBlock copy = new ElementPathBlock();

					final Iterator<TriplePath> it = block.patternElts();
					while ( it.hasNext() ) {
						copy.addTriplePath( it.next() );
					}

					for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
						copy.addTriple( tp.asJenaTriple() );
					}
					bgpAdded = true;

					newGroup.addElement(copy);
				}
				else {
					newGroup.addElement(subElmt);
				}
			}

			if ( ! bgpAdded ) {
				final ElementTriplesBlock bgpToAdd = new ElementTriplesBlock();

				for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
					bgpToAdd.addTriple( tp.asJenaTriple() );
				}

				newGroup.addElement(bgpToAdd);
			}

			resultElmt = newGroup;
		}
		else {
			// In all other cases, create an ElementGroup, ...
			final ElementGroup newGroup = new ElementGroup();

			// ... add the Element obtained for the given graph
			// pattern as one sub-element of the group, and ...
			newGroup.addElement(elmt);

			// ... add the given BGP as another sub-element.
			final ElementTriplesBlock bgpToAdd = new ElementTriplesBlock();
			for ( final TriplePattern tp : bgp.getTriplePatterns() ) {
				bgpToAdd.addTriple( tp.asJenaTriple() );
			}
			newGroup.addElement(bgpToAdd);

			resultElmt = newGroup;
		}

		return new GenericSPARQLGraphPatternImpl1(resultElmt);
	}

	@Override
	public String toStringForPlanPrinters() {
		// convert into an Element object and use
		// pretty printing via FormatterElement
		final ElementTriplesBlock block = new ElementTriplesBlock();
		for ( final TriplePattern t : tps ) {
			block.addTriple( t.asJenaTriple() );
		}
		final IndentedLineBuffer buf = new IndentedLineBuffer();
		final SerializationContext sCxt = new SerializationContext( ARQConstants.getGlobalPrefixMap() );
		FormatterElement.format( buf, sCxt, block );
		return "{ " + buf.asString() + " }";
	}
}
