package se.liu.ida.hefquin.jenaext;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.PatternVarsVisitor;

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
		final PatternVarsVisitor v = new MyPatternVarsVisitor(s);
		vars(element, v);
		return s;
	}

	public static void vars( final Element element, final PatternVarsVisitor visitor ) {
		ElementWalker.walk(element, visitor);
	}


	public static class MyPatternVarsVisitor extends PatternVarsVisitor {

		public MyPatternVarsVisitor( final Collection<Var> s ) { super(s); }

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
	}

}
