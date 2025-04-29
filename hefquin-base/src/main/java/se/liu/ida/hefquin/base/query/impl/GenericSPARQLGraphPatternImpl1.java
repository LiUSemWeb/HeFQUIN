package se.liu.ida.hefquin.base.query.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformSubst;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.jenaext.sparql.algebra.OpUtils;

/**
 * This class is a generic implementation of {@link SPARQLGraphPattern}
 * in which this graph pattern is given as an object of the class
 * {@link Element} of the Jena API.
 */
public class GenericSPARQLGraphPatternImpl1 implements SPARQLGraphPattern
{
	protected final Element jenaPatternElement;
	private Op jenaOp = null; // initialized if needed

	public GenericSPARQLGraphPatternImpl1( final Element jenaPatternElement ) {
		assert jenaPatternElement != null;
		this.jenaPatternElement = jenaPatternElement;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o instanceof GenericSPARQLGraphPatternImpl1 ) {
			final GenericSPARQLGraphPatternImpl1 oo = (GenericSPARQLGraphPatternImpl1) o;
			if ( oo.jenaPatternElement.equals(jenaPatternElement) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return jenaPatternElement.hashCode();
	}

	public Element asJenaElement() {
		return jenaPatternElement;
	}

	/**
	 * Avoid using this function because, when called, it compiles the
	 * internal {@link Element} object into an {@link Op} object.
	 */
	@Deprecated
	public Op asJenaOp() {
		if ( jenaOp != null ) return jenaOp;

		jenaOp = Algebra.compile(jenaPatternElement);
		return jenaOp;
	}

	@Override
	public String toString(){
		return asJenaElement().toString();
	}

	@Override
	public Set<TriplePattern> getAllMentionedTPs() {
		return getTPsInPattern(jenaPatternElement);
	}

	@Override
	public Set<Var> getCertainVariables() {
		final Op jenaOp = asJenaOp();
		return OpVars.fixedVars(jenaOp);
	}

	@Override
	public Set<Var> getPossibleVariables() {
		final Op jenaOp = asJenaOp();
		final Set<Var> certainVars = OpVars.fixedVars(jenaOp);
		final Set<Var> possibleVars = OpVars.visibleVars(jenaOp);
		possibleVars.removeAll(certainVars);
		return possibleVars;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		final Op jenaOp = asJenaOp();
		final Set<Var> certainVars = OpVars.fixedVars(jenaOp);
		final Set<Var> possibleVars = OpVars.visibleVars(jenaOp);
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public Set<Var> getAllMentionedVariables() {
		final Op jenaOp = asJenaOp();
		return OpUtils.getVariablesInPattern(jenaOp);
	}

	@Override
	public int getNumberOfVarMentions() {
		final Op jenaOp = asJenaOp();
		return OpUtils.getNumberOfVarMentions(jenaOp);
	}

	@Override
	public int getNumberOfTermMentions() {
		final Op jenaOp = asJenaOp();
		return OpUtils.getNumberOfTermMentions(jenaOp);
	}

	@Override
	public SPARQLGraphPattern applySolMapToGraphPattern( final SolutionMapping sm )
			throws VariableByBlankNodeSubstitutionException
	{
		final Map<Var, Node> map1 = new HashMap<>();
		final Map<String, Expr> map2 = new HashMap<>();
		sm.asJenaBinding().forEach( (v,n) -> { map1.put(v,n); map2.put(v.getVarName(),NodeValue.makeNode(n)); } );
		final ElementTransform t1 = new ElementTransformSubst(map1);
		final ExprTransformSubstitute t2 = new ExprTransformSubstitute(map2);

		final Element eNew = ElementTransformer.transform(jenaPatternElement, t1, t2);
		return ( jenaPatternElement == eNew ) ? this : new GenericSPARQLGraphPatternImpl1(eNew);
	}

	/**
	 * The implementation of this method is currently very simple; it just
	 * returns a new {@link SPARQLGroupPattern} that contains both this
	 * and the given pattern.
	 */
	@Override
	public SPARQLGraphPattern mergeWith( final SPARQLGraphPattern other ) {
		// TODO: create a more sophisticated implementation that tries to
		// merge the patterns on a deeper level (e.g., if the other pattern
		// is a GenericSPARQLGraphPatternImpl1 as well, then the merge
		// should be done by merging the respective Element objects.

		return new SPARQLGroupPatternImpl(this, other);
	}

	public static Set<TriplePattern> getTPsInPattern ( final Element e ) {
		// TODO: It is better to implement this function using an ElementVisitor.
		// If done, you can remove this function and use the visitor-based code
		// directly in getAllMentionedTPs() above.
		
		final Set<TriplePattern> tps = new HashSet<>();
		if ( e instanceof ElementTriplesBlock b ) {
			for ( final Triple tp : b.getPattern().getList() ) {
				tps.add( new TriplePatternImpl(tp) );
			}
		}
		else if ( e instanceof ElementPathBlock b ) {
			for ( final TriplePath tpp : b.getPattern().getList() ) {
				if ( ! tpp.isTriple() ) {
					throw new IllegalArgumentException( "Property paths patterns are not supported by HeFQUIN." );
				}
				tps.add( new TriplePatternImpl(tpp.asTriple()) );
			}
		}
		else if ( e instanceof ElementGroup eg ) {
			for ( Element el: eg.getElements() ){
				tps.addAll( getTPsInPattern(el) );
			}
		}
		else if ( e instanceof ElementFilter ) {
			// Do nothing
		}
		else if ( e instanceof ElementBind ) {
			// Do nothing
		}
		else
			throw new IllegalArgumentException( "Cannot get triple patterns of the operator (type: " + e.getClass().getName() + ")." );

		return tps;
	}
}
