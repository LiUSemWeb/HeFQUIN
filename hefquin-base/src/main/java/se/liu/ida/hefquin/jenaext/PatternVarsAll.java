package se.liu.ida.hefquin.jenaext;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementAssign;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnfold;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.util.VarUtils;

/**
 * In contrast to Jena's {@link PatternVars} class, this one collects
 * all the variables in a given {@link Element}; i.e., including the
 * ones inside MINUS and FILTERs.
 */
public class PatternVarsAll
{
	public static Collection<Var> vars( final Element element ) {
		return vars( new LinkedHashSet<Var>(), element );
	}

	public static Collection<Var> vars( final Collection<Var> s, final Element element ) {
		final MyPatternVarsVisitor v = new MyPatternVarsVisitor(s);
		vars(element, v);
		return s;
	}

	public static void vars( final Element element, final MyPatternVarsVisitor visitor ) {
		ElementWalker.walk(element, visitor);
	}


	public static class MyPatternVarsVisitor extends ElementVisitorBase {
		protected final Collection<Var> acc;

		public MyPatternVarsVisitor( final Collection<Var> s ) { acc = s; }

		// The following methods are different from PatternVarsPatternVars.Visitor

		@Override
		public void visit( final ElementExists el ) {
			PatternVarsAll.vars( acc, el.getElement() );
		}

		@Override
		public void visit( final ElementNotExists el ) {
			PatternVarsAll.vars( acc, el.getElement() );
		}

		@Override
		public void visit( final ElementMinus el ) {
			PatternVarsAll.vars( acc, el.getMinusElement() );
		}

		@Override
		public void visit( final ElementFilter el ) {
			acc.addAll( ExprVars.getVarsMentioned(el.getExpr()) );
		}

		// The following methods are the same as in PatternVarsPatternVars.Visitor

		@Override
		public void visit( final ElementTriplesBlock el) {
			final Iterator<Triple> it = el.patternElts();
			while ( it.hasNext() ) {
				VarUtils.addVarsFromTriple( acc, it.next() );
			}
		}

		@Override
		public void visit( final ElementPathBlock el ) {
			final Iterator<TriplePath> it = el.patternElts();
			while ( it.hasNext() ) {
				final TriplePath tpath = it.next();
				if ( tpath.isTriple() )
					VarUtils.addVarsFromTriple( acc, tpath.asTriple() );
				else
					VarUtils.addVarsFromTriplePath(acc, tpath);
			}
		}

		@Override
		public void visit( final ElementNamedGraph el ) {
			VarUtils.addVar( acc, el.getGraphNameNode() );
		}

		@Override
		public void visit( final ElementSubQuery el ) {
			acc.addAll( el.getQuery().getProject().getVars() );
		}

		@Override
		public void visit( final ElementAssign el ) {
			acc.add( el.getVar() );
		}

		@Override
		public void visit( final ElementBind el ) {
			acc.add( el.getVar() );
		}

		@Override
		public void visit( final ElementUnfold el ) {
			acc.add( el.getVar1() );
			if ( el.getVar2() != null )
				acc.add( el.getVar2() );
		}

		@Override
		public void visit( final ElementData el ) {
			acc.addAll( el.getVars() );
		}
	}

}
