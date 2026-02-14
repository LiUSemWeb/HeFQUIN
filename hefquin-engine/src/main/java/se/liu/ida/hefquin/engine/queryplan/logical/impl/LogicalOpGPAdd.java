package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.members.RESTEndpoint.Parameter;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;

public class LogicalOpGPAdd implements UnaryLogicalOp
{
	protected final FederationMember fm;
	protected final SPARQLGraphPattern pattern;
	protected final Map<String,Var> paramVars;

	// will be initialized on demand 
	protected TriplePattern tp = null;
	protected boolean tpCheckDone = false;

	public LogicalOpGPAdd( final FederationMember fm,
	                       final SPARQLGraphPattern pattern,
	                       final Map<String,Var> paramVars ) {
		assert fm != null;
		assert pattern != null;

		this.fm = fm;
		this.pattern = pattern;

		if ( paramVars != null && ! paramVars.isEmpty() ) {
			assert    fm instanceof WrappedRESTEndpoint;
			final WrappedRESTEndpoint wrappedRestEndpoint = ((WrappedRESTEndpoint) fm);

			final Iterator<String> paramVarNamesIt = paramVars.keySet().iterator();
			while( paramVarNamesIt.hasNext() ) {
				final String paramName = paramVarNamesIt.next();
				if ( wrappedRestEndpoint.getParameterByName(paramName) == null ) {
					throw new IllegalArgumentException(
						"Invalid SERVICE clause: Parameter name " + paramName + " referred to in the PARAMS() clause is not defined for federation member " + fm.toString()
					);
				}
			}
			final Iterator<Parameter> declParamsIt = wrappedRestEndpoint.getParameters().iterator();
			while( declParamsIt.hasNext() ) {
				final Parameter p = declParamsIt.next();
				if ( p.isRequired() && ! paramVars.containsKey(p.getName()) ) {
					throw new IllegalArgumentException(
						"Invalid SERVICE clause: Required parameter " + p.getName() + " of federation member " + fm.toString() +
						" is not mapped to in the PARAMS() clause."
					);
				}
			}

			this.paramVars = paramVars;
		}
		else
			this.paramVars = null;
	}

	public FederationMember getFederationMember() {
		return fm;
	}

	public SPARQLGraphPattern getPattern() {
		if ( tp != null )
			return tp;
		else
			return pattern;
	}

	/**
	 * Returns {@code true} if this gpAdd operator has a (nonempty) list of
	 * variables as parameter. If so, {@link #getParameterVariables()} can
	 * be used to access this parameter.
	 * 
	 */
	public boolean hasParameterVariables() {
		return paramVars != null && ! paramVars.isEmpty();
	}

	/**
	 * Returns the (nonempty) list of variables that is a parameter
	 * of this gpAdd operator (if any).
	 * <p>
	 * If this gpAdd operator does not have such a parameter, then
	 * an {@link UnsupportedOperationException} is thrown. You can
	 * use {@link #hasParameterVariables()} to ask whether this gpAdd
	 * operator is this parameter.
	 */
	public Map<String,Var> getParameterVariables() {
		if ( ! hasParameterVariables() )
			throw new UnsupportedOperationException("Requesting variables of a gpAdd operator that does not have this parameter.");

		return paramVars;
	}

	/**
	 * Returns <code>true</code> if the graph pattern
	 * of this operator is only a triple pattern.
	 */
	public boolean containsTriplePatternOnly() {
		if ( ! tpCheckDone ) {
			tpCheckDone = true;
			tp = QueryPatternUtils.getAsTriplePattern(pattern);
		}

		return ( tp != null );
	}

	/**
	 * Returns the graph pattern of this operator as a triple pattern if this
	 * pattern is only a triple pattern.
	 * <p>
	 * Before calling this function, use {@link #containsTriplePatternOnly()}
	 * to check whether the pattern is indeed only a triple pattern.
	 * <p>
	 * If it is not, this method throws an {@link UnsupportedOperationException}.
	 */
	public TriplePattern getTP() {
		if ( ! containsTriplePatternOnly() ) {
			throw new UnsupportedOperationException("This graph pattern is not a triple pattern");
		}

		return tp;
	}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;

		final ExpectedVariables expVarsPattern = pattern.getExpectedVariables();
		final ExpectedVariables expVarsInput = inputVars[0];

		final Set<Var> certainVars = ExpectedVariablesUtils.unionOfCertainVariables(expVarsPattern, expVarsInput);
		final Set<Var> possibleVars = ExpectedVariablesUtils.unionOfPossibleVariables(expVarsPattern, expVarsInput);
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		if ( o instanceof LogicalOpGPAdd otherGPAdd ) {
			return    otherGPAdd.fm.equals(fm)
			       && otherGPAdd.pattern.equals(pattern)
			       && Objects.deepEquals(paramVars, otherGPAdd.paramVars);
		}

		return false;
	}

	@Override
	public int hashCode(){
		return fm.hashCode() ^ pattern.hashCode();
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		final int codeOfPattern = pattern.toString().hashCode();
		final int codeOfFm = fm.toString().hashCode();

		return "> gpAdd" +
				"[" + codeOfPattern + ", "+ codeOfFm + "]"+
				" ( "
				+ pattern.toString()
				+ ", "
				+ fm.toString()
				+ " )";
	}

}
