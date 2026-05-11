package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.NodeValue;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalOpUnfold extends BaseForLogicalOps implements UnaryLogicalOp
{
	protected final Expr expr;
	protected final Var var1;
	protected final Var var2;

	/**
	 * Create an unfold operator with the given expression and variables,
	 * where the second variable may be {@code null} (in case the UNFOLD
	 * clause does not contain two variables).
	 */
	public LogicalOpUnfold( final Expr expr, final Var var1, final Var var2, final boolean mayReduce ) {
		super( mayReduce );

		assert expr != null;
		assert var1 != null;

		this.expr = expr;
		this.var1 = var1;
		this.var2 = var2;
	}

	public Expr getExpr() { return expr; }

	public Var getVar1() { return var1; }

	public Var getVar2() { return var2; }

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final ExpectedVariables expVarsInput = inputVars[0];

		final Set<Var> certainVars = new HashSet<>( expVarsInput.getCertainVariables() );
		final Set<Var> possibleVars = new HashSet<>( expVarsInput.getPossibleVariables() );

		// In general, the variables in an UNFOLD clause are only possible,
		// not certain, because evaluating the expression of the UNFOLD
		// clause may result in an error or in something that is neither
		// a well-formed cdt:List literal *without* null values nor a
		// well-formed cdt:Map literal *without* null values, in which case
		// the UNFOLD variables remain unbound. Yet, for some expressions
		// we can be sure that their evaluation results in a well-formed
		// cdt:List literal without null values or in a well-formed cdt:Map
		// literal without null value (e.g., constants); hence, we check for
		// such cases and add their UNFOLD variables as certain ones, rather
		// than only a possible ones.
		if ( expr.isConstant() ) {
			addVariables( expr.getConstant(), certainVars, possibleVars );
		}
		else if ( expr.isFunction() ) {
			addVariables( expr.getFunction(), certainVars, possibleVars );
		}
		else {
			addVariables(false, false, certainVars, possibleVars);
		}

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return    o instanceof LogicalOpUnfold oo
		       && oo.expr.equals(expr)
		       && oo.var1.equals(var1)
		       && Objects.equals(oo.var2, var2) // oo.var2 may be null
		       && oo.mayReduce == mayReduce;
	}

	@Override
	public int hashCode(){
		return getClass().hashCode() ^ expr.hashCode() ^ var1.hashCode();
	}

	@Override
	public String toString() {
		return "Unfold ( " + expr.toString() + ")";
	}


	/**
	 * Returns <code>true</code> if it is <em>not</em> guaranteed that
	 * evaluating the given expression will result in a well-formed
	 * cdt:List literal that does not contain null values.
	 */
	protected void addVariables( final Expr expr,
	                             final Set<Var> certainVars,
	                             final Set<Var> possibleVars ) {
		final NodeValue nv = expr.getConstant();
		final ExprFunction fct = expr.getFunction();

		if ( nv != null ) {
			addVariables(nv, certainVars, possibleVars);
		}

		if ( fct != null ) {
			addVariables(fct, certainVars, possibleVars);
		}
	}

	/**
	 * Adds {@link #var1} and {@link #var2} (if not {@code null}) into the
	 * two given sets depending on the given {@link NodeValue}.
	 * <p>
	 * If the given {@link NodeValue} is a well-formed cdt:List literal
	 * that represents a nonempty list, then {@link #var2} will be added
	 * as a certain variable. Moreover, if none of the elements of this
	 * (nonempty) list is the null value, then {@link #var1} will also
	 * be added as a certain variable.
	 * <p>
	 * If the given {@link NodeValue} is a well-formed cdt:Map literal
	 * that represents a nonempty map, then {@link #var1} will be added
	 * as a certain variable. Moreover, if none of the values of the
	 * entries in this (nonempty) map is the null value, then
	 * {@link #var2} will also be added as a certain variable.
	 */
	protected void addVariables( final NodeValue nv,
	                             final Set<Var> certainVars,
	                             final Set<Var> possibleVars ) {
		final Node n = nv.asNode();

		if ( ! n.isLiteral() ) return;

		final LiteralLabel lit = n.getLiteral();
		if ( ! lit.isWellFormed() ) return;

		boolean var1IsCertain = false;
		boolean var2IsCertain = false;

		if ( lit.getDatatype().equals(CompositeDatatypeList.type) ) {
			@SuppressWarnings("unchecked")
			final List<CDTValue> list = (List<CDTValue>) lit.getValue();

			if ( list.isEmpty() ) return;

			var2IsCertain = true; // var2 is for the position counter

			// Check for null values.
			boolean nullValueFound = false;
			final Iterator<CDTValue> it = list.iterator();
			while ( ! nullValueFound && it.hasNext() ) {
				if ( it.next().isNull() ) nullValueFound = true;
			}

			var1IsCertain = ! nullValueFound;
		}

		if ( lit.getDatatype().equals(CompositeDatatypeMap.type) ) {
			@SuppressWarnings("unchecked")
			final Map<CDTKey, CDTValue> map = (Map<CDTKey, CDTValue>) lit.getValue();

			if ( map.isEmpty() ) return;

			var1IsCertain = true; // var1 is for the key

			// Check for null values.
			boolean nullValueFound = false;
			final Iterator<Map.Entry<CDTKey,CDTValue>> it = map.entrySet().iterator();
			while ( ! nullValueFound && it.hasNext() ) {
				if ( it.next().getValue().isNull() ) nullValueFound = true;
			}

			var2IsCertain = ! nullValueFound;
		}

		addVariables(var1IsCertain, var2IsCertain, certainVars, possibleVars);
	}

	/**
	 * Adds {@link #var1} and {@link #var2} (if not {@code null}) into the
	 * two given sets depending on the given function expression.
	 * <p>
	 * If the function expression is for the cdt:List constructor function
	 * with at least one argument, then {@link #var2} will be added as a
	 * certain variable. Moreover, if each of the arguments is either a
	 * constant or a variable that is one of the given certain variables,
	 * then {@link #var1} will also be added as a certain variable.
	 * <p>
	 * If the function expression is for the cdt:Map constructor function
	 * with an even number of arguments and every i-th argument, with i
	 * being an odd number, is an URI constant or a literal constant, then
	 * {@link #var1} will be added as a certain variable. Moreover, if
	 * every j-th argument, with j an even number, is either a constant or
	 * a variable that is one of the given certain variables, then
	 * {@link #var2} will also be added as a certain variable.
	 */
	protected void addVariables( final ExprFunction fct,
	                             final Set<Var> certainVars,
	                             final Set<Var> possibleVars ) {
		if ( fct.getArgs().isEmpty() ) return;

		boolean var1IsCertain = false;
		boolean var2IsCertain = false;

		final String fctIRI = fct.getFunctionIRI();

		if ( fctIRI.equals(ARQConstants.CDTFunctionLibraryURI + "List") ) {
			var2IsCertain = true; // var2 is for the position counter

			// Check for null values.
			boolean var1MayBeUnbound = false;
			final Iterator<Expr> it = fct.getArgs().iterator();
			while ( ! var1MayBeUnbound && it.hasNext() ) {
				final Expr arg = it.next();
				if ( arg.isVariable() ) {
					var1MayBeUnbound = ! certainVars.contains( arg.asVar() );
				}
				else if ( ! arg.isConstant() ) {
					var1MayBeUnbound = true;
				}
			}

			var1IsCertain = ! var1MayBeUnbound;
		}

		if ( fctIRI.equals(ARQConstants.CDTFunctionLibraryURI + "Map") ) {
			// Check whether the number of arguments is odd, in which
			// case the cdt:Map constructor function results in an
			// error and, thus, the UNFOLD operator returns the given
			// solution mapping without adding bindings for the given
			// variables.
			if ( (fct.getArgs().size() & 1) != 0 ) return;

			boolean currentArgIsForKey = true;
			boolean var1MayBeUnbound = false;
			boolean var2MayBeUnbound = false;
			final Iterator<Expr> it = fct.getArgs().iterator();
			while (    (!var1MayBeUnbound || !var2MayBeUnbound)
			        && it.hasNext() ) {
				final Expr arg = it.next();

				if ( currentArgIsForKey ) {
					if ( arg.isConstant() ) {
						final NodeValue nv = arg.getConstant();
						if ( ! nv.isIRI() && ! nv.isLiteral() )
							var1MayBeUnbound = true;
					}
					else {
						var1MayBeUnbound = true;
					}
				}
				else {
					if ( arg.isVariable() ) {
						if ( ! certainVars.contains(arg.asVar()) )
							var2MayBeUnbound = true;
					}
					else if ( ! arg.isConstant() ) {
						var2MayBeUnbound = true;
					}
				}
			}

			var1IsCertain = ! var1MayBeUnbound;
			var2IsCertain = ! var2MayBeUnbound;
		}

		addVariables(var1IsCertain, var2IsCertain, certainVars, possibleVars);
	}

	/**
	 * Adds {@link #var1} and {@link #var2} (if not {@code null})
	 * into the two given sets as per the given flags.
	 */
	protected void addVariables( final boolean var1IsCertain,
	                             final boolean var2IsCertain,
	                             final Set<Var> certainVars,
	                             final Set<Var> possibleVars ) {
		if ( var1IsCertain )
			certainVars.add(var1);
		else
			possibleVars.add(var1);

		if ( var2 != null ) {
			if ( var2IsCertain )
				certainVars.add(var2);
			else
				possibleVars.add(var2);
		}
	}

}
