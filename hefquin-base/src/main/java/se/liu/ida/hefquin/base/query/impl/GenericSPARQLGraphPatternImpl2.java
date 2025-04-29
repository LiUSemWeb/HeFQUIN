package se.liu.ida.hefquin.base.query.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

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
 * {@link Op} of the Jena API.
 */
public class GenericSPARQLGraphPatternImpl2 implements SPARQLGraphPattern
{
	protected final Op jenaPatternOp;

	public GenericSPARQLGraphPatternImpl2( final Op jenaPatternOp ) {
		assert jenaPatternOp != null;
		this.jenaPatternOp = jenaPatternOp;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o instanceof GenericSPARQLGraphPatternImpl2 ) {
			final GenericSPARQLGraphPatternImpl2 oo = (GenericSPARQLGraphPatternImpl2) o;
			if ( oo.jenaPatternOp.equals(jenaPatternOp) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return jenaPatternOp.hashCode();
	}

	/**
	 * Avoid using this function. It converts the internal {@link Op} object
	 * back into an {@link Element}, which might not work correctly in all
	 * cases?
	 */
	@Deprecated
	public Element asJenaElement() {
		return OpAsQuery.asQuery(jenaPatternOp).getQueryPattern();
	}

	public Op asJenaOp() {
		return jenaPatternOp;
	}

	@Override
	public String toString(){
		// converting the Op object into an Element object
		// because the toString() function of that one uses
		// pretty printing via FormatterElement
		return OpAsQuery.asQuery(jenaPatternOp).getQueryPattern().toString();
	}

	@Override
	public Set<TriplePattern> getAllMentionedTPs() {
		return getTPsInPattern(jenaPatternOp);
	}

	@Override
	public Set<Var> getCertainVariables() {
		return OpVars.fixedVars(jenaPatternOp);
	}

	@Override
	public Set<Var> getPossibleVariables() {
		final Set<Var> certainVars = OpVars.fixedVars(jenaPatternOp);
		final Set<Var> possibleVars = OpVars.visibleVars(jenaPatternOp);
		possibleVars.removeAll(certainVars);
		return possibleVars;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		final Set<Var> certainVars = OpVars.fixedVars(jenaPatternOp);
		final Set<Var> possibleVars = OpVars.visibleVars(jenaPatternOp);
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public Set<Var> getAllMentionedVariables() {
		return OpUtils.getVariablesInPattern(jenaPatternOp);
	}

	@Override
	public int getNumberOfVarMentions() {
		return OpUtils.getNumberOfVarMentions(jenaPatternOp);
	}

	@Override
	public int getNumberOfTermMentions() {
		return OpUtils.getNumberOfTermMentions(jenaPatternOp);
	}

	@Override
	public SPARQLGraphPattern applySolMapToGraphPattern( final SolutionMapping sm )
			throws VariableByBlankNodeSubstitutionException
	{
		final Map<Var, Node> map = new HashMap<>();
		sm.asJenaBinding().forEach( (v,n) -> map.put(v,n) );
		final NodeTransform t = new NodeTransformSubst(map);

		final Op opNew = NodeTransformLib.transform(t, jenaPatternOp);
		return ( jenaPatternOp == opNew ) ? this : new GenericSPARQLGraphPatternImpl2(opNew);
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
		// is a GenericSPARQLGraphPatternImpl2 as well, then the merge
		// should be done by merging the respective Op objects.

		return new SPARQLGroupPatternImpl(this, other);
	}

	public static Set<TriplePattern> getTPsInPattern( final Op op ) {
		// TODO: It is better to implement this function using an OpVisitor.
		// If done, you can remove this function and use the visitor-based code
		// directly in getAllMentionedTPs() above.

		if ( op instanceof OpJoin || op instanceof OpLeftJoin || op instanceof OpUnion ) {
			return getTPsInPattern( (Op2) op );
		}

		if ( op instanceof OpService || op instanceof OpFilter || op instanceof OpExtend ){
			return getTPsInPattern( ((Op1) op).getSubOp() );
		}

		if ( op instanceof OpBGP opBGP ) {
			final Set<TriplePattern> tps = new HashSet<>();
			for ( final Triple t : opBGP.getPattern().getList() ) {
				tps.add( new TriplePatternImpl(t) );
			}
			return tps;
		}

		throw new UnsupportedOperationException("Getting the triple patterns from arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + op.getClass().getName() + ").");
	}

	public static Set<TriplePattern> getTPsInPattern( final Op2 op ) {
		final Set<TriplePattern> varLeft = getTPsInPattern( op.getLeft() );
		final Set<TriplePattern> varRight = getTPsInPattern( op.getRight() );
		varLeft.addAll(varRight);
		return varLeft;
	}

}

