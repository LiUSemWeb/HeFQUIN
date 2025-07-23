package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformSubst;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

/**
 * Implementation of (a batching version of) the bound-join algorithm that
 * uses UNION clauses with variable renaming (as proposed in the FedX
 * paper by Schwarte et al. 2011). The variable that is renamed can be
 * any non-join variable.
 *
 * For more details about the actual implementation of the algorithm, and its
 * extra capabilities, refer to {@link BaseForExecOpBindJoinWithRequestOps}.
 */
public class ExecOpBindJoinSPARQLwithBoundJoin extends BaseForExecOpBindJoinSPARQL
{
	public final static int DEFAULT_BATCH_SIZE = BaseForExecOpBindJoinWithRequestOps.DEFAULT_BATCH_SIZE;

	protected final Element pattern;

	// Represents a list of input solution mappings (ordered)
	protected final List<Binding> solMapsList = new ArrayList<>();
	// Var used for renaming
	final protected Var renamedVar;
	// Prefix for renamed vars
	final protected String renamedVarPrefix;

	/**
	 * @param query - the graph pattern to be evaluated (in a bind-join
	 *          manner) at the federation member given as 'fm'
	 *
	 * @param fm - the federation member targeted by this operator
	 *
	 * @param inputVars - the variables to be expected in the solution
	 *          mappings that will be pushed as input to this operator
	 *
	 * @param useOuterJoinSemantics - <code>true</code> if the 'query' is to
	 *          be evaluated under outer-join semantics; <code>false</code>
	 *          for inner-join semantics
	 *
	 * @param batchSize - the number of solution mappings to be included in
	 *          each bind-join request; this value must not be smaller than
	 *          {@link #minimumRequestBlockSize}; as a default value for this
	 *          parameter, use {@link #DEFAULT_BATCH_SIZE}
	 *
	 * @param collectExceptions - <code>true</code> if this operator has to
	 *          collect exceptions (which is handled entirely by one of the
	 *          super classes); <code>false</code> if the operator should
	 *          immediately throw every {@link ExecOpExecutionException}
	 */
	public ExecOpBindJoinSPARQLwithBoundJoin( final SPARQLGraphPattern query,
	                                          final SPARQLEndpoint fm,
	                                          final ExpectedVariables inputVars,
	                                          final boolean useOuterJoinSemantics,
	                                          final int batchSize,
	                                          final boolean collectExceptions ) {
		super(query, fm, inputVars, useOuterJoinSemantics, batchSize, collectExceptions);
		pattern = QueryPatternUtils.convertToJenaElement(query);

		renamedVar = getVarForRenaming(query, inputVars);
		if( renamedVar == null ){
			// If there are no non-joining vars, we need to fall back to a non-renaming
			// version of the bind join strategy.
			throw new IllegalArgumentException("No suitable variable found for renaming");
		}
		renamedVarPrefix = renamedVar.getVarName() + "_";
	}

	/**
	 * Finds a variable in the given query that is not present in the expected input
	 * variables and can be used as a variable for renaming.
	 *
	 * The method iterates through the certain variables of the given SPARQL graph
	 * pattern and returns the first variable that is neither listed as a certain
	 * variable nor a possible variable among the input variables.
	 *
	 * @param query     the SPARQL graph pattern containing the query variables
	 * @param inputVars the input variables
	 * @return a {@code Var} that can be used as a variable for renaming, or
	 *         {@code null} if no such variable is found
	 */
	public Var getVarForRenaming( final SPARQLGraphPattern query,
	                              final ExpectedVariables inputVars ) {
		for ( final Var v : query.getCertainVariables() ) {
			if (    ! inputVars.getCertainVariables().contains(v) 
			     && ! inputVars.getPossibleVariables().contains(v) ) {
				return v;
			}
		}
		return null;
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOp( final Set<Binding> solMaps ) {
		final Element elmt = createUnion(solMaps);
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1(elmt);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		return new ExecOpRequestSPARQL(request, fm, false);
	}

	protected Element createUnion( final Iterable<Binding> solMaps ) {
		// Populate the ordered list of solution mappings (used for restoring renamed
		// vars and restoring the join partner vars)
		solMapsList.clear();
		solMaps.forEach(solMapsList::add);
		
		// Union element
		final ElementUnion union = new ElementUnion();

		// Generate the UNION pattern by iterating over all input solution mappings
		// and replacing the renamed variable
		int i = 0;
		for ( final Binding solMap : solMaps ) {
			// Create a new pattern in which we replace the variables
			// with values based on the incoming solution mapping
			SPARQLGraphPattern patternWithBindings;
			try {
				patternWithBindings = query.applySolMapToGraphPattern( new SolutionMappingImpl(solMap) );
			} catch ( VariableByBlankNodeSubstitutionException e ) {
				throw new IllegalArgumentException(e);
			}
			
			// Create new variable 
			final Var v = Var.alloc( renamedVarPrefix + i );
			
			// Rename the variable in the pattern
			final Element elt2 = renameVar(patternWithBindings, renamedVar, v);
			union.addElement(elt2);

			i++;
		}
		return union;
	}

	@Override
	protected MyIntermediateResultElementSink createMySink() {
		if (useOuterJoinSemantics) {
			return new MyIntermediateResultElementSinkOuterJoin2(currentBatch);
		} else {
			return new MyIntermediateResultElementSink2(currentBatch);
		}
	}

	// ------- helper classes ------

	protected class MyIntermediateResultElementSink2 extends MyIntermediateResultElementSink
	{
		public MyIntermediateResultElementSink2( final Iterable<SolutionMapping> inputSolutionMappings ) {
			super(inputSolutionMappings);
		}

		@Override
		protected void _send( final SolutionMapping smFromRequest ) {
			// Merge smFromRequest into the input solution mappings:
			// 1. Find the renamed variable and parse the integer part.
			// 2. Rename that variable and merge with the corresponding solMapList entry.
			// 3. For each compatible input mapping, merge and collect the results.

			final Binding requestBinding = smFromRequest.asJenaBinding();

			// Find renamed variable
			final Var matchedVar = findRenamedVar(requestBinding);

			// Parse index
			final int i = Integer.parseInt( matchedVar.getVarName().substring( renamedVarPrefix.length() ) );

			// Rename var and merge with input mapping
			final Binding renamedAndMerged = BindingLib.merge( renameVar(requestBinding, matchedVar, renamedVar),
			                                                   solMapsList.get(i) );

			// Merge with inputSolutionMappings
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible( smFromInput.asJenaBinding(), renamedAndMerged ) ) {
					final Binding updatedBinding = BindingLib.merge( smFromInput.asJenaBinding(), renamedAndMerged );
					solMapsForOutput.add( new SolutionMappingImpl(updatedBinding) );
				}
			}
		}
	} // end of helper class MyIntermediateResultElementSink2


	protected class MyIntermediateResultElementSinkOuterJoin2 extends MyIntermediateResultElementSinkOuterJoin
	{
		public MyIntermediateResultElementSinkOuterJoin2( final Iterable<SolutionMapping> inputSolutionMappings ) {
			super(inputSolutionMappings);
		}

		@Override
		public void _send( final SolutionMapping smFromRequest ) {
			// Merge smFromRequest into the input solution mappings:
			// 1. Find the renamed variable and parse the integer part.
			// 2. Rename that variable and merge with the corresponding solMapList entry.
			// 3. For each compatible input mapping, merge and collect the results.

			final Binding requestBinding = smFromRequest.asJenaBinding();

			// Find renamed variable
			final Var matchedVar = findRenamedVar(requestBinding);

			// Parse index
			final int i = Integer.parseInt( matchedVar.getVarName().substring( renamedVarPrefix.length() ) );

			// Rename var and merge with input mapping
			final Binding renamedAndMerged = BindingLib.merge( renameVar(requestBinding, matchedVar, renamedVar),
			                                                   solMapsList.get(i) );

			// Merge with inputSolutionMappings
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible( smFromInput.asJenaBinding(), renamedAndMerged ) ) {
					final Binding updatedBinding = BindingLib.merge( smFromInput.asJenaBinding(), renamedAndMerged );
					solMapsForOutput.add( new SolutionMappingImpl(updatedBinding) );
					inputSolutionMappingsWithJoinPartners.add(smFromInput);
				}
			}
		}
	} // end of helper class MyIntermediateResultElementSinkOuterJoin2
	

	// ------- helper functions ------

	/**
	 * Renames all occurences of variable within a SPARQL graph pattern element.
	 *
	 * @param pattern the SPARQL graph pattern in which to rename the variable
	 * @param oldVar  the variable to be replaced
	 * @param newVar  the variable to replace {@code oldVar}
	 * @return the modified element
	 */
	public static Element renameVar( final SPARQLGraphPattern pattern,
                                     final Var oldVar,
                                     final Var newVar ) {
		// Element transform
		final Element elt = QueryPatternUtils.convertToJenaElement(pattern);
		final Map<Var, Node> eltMap = Collections.singletonMap(oldVar, newVar);
		final ElementTransformSubst eltTransform = new ElementTransformSubst(eltMap);
		// Expression transform
		final ExprTransform exprTransform = new ExprTransformSubstitute( oldVar, new ExprVar(newVar) );
		// Apply to element
		return ElementTransformer.transform(elt, eltTransform, exprTransform);
	}

	/**
	 * Renames a variable within a single Jena binding.
	 * 
	 * Iterates over all (variable, value) pairs in the original binding and builds
	 * a new binding the occurrence of {@code oldVar} is replaced by {@code newVar}.
	 *
	 * @param binding the original binding
	 * @param oldVar  the variable to replace
	 * @param newVar  the new variable name
	 * @return a new binding
	 */
	public static Binding renameVar( final Binding binding,
	                                 final Var oldVar,
	                                 final Var newVar ) {
		final BindingBuilder builder = BindingFactory.builder();
		binding.forEach( (v, n) -> builder.add( v.equals(oldVar) ? newVar : v, n ) );
		return builder.build();
	}

	/**
	 * Finds the first variable in the given Jena {@link Binding} that matches the
	 * prefix for renamed variables. If no such variable is found, an
	 * {@link IllegalArgumentException} is thrown.
	 *
	 * @param binding the Jena {@link Binding} to search for a renamed variable
	 * @return the first {@link Var} whose name begins with {@code renamedVarPrefix}
	 * @throws IllegalArgumentException if the binding contains no variable whose
	 *                                  name starts with {@code renamedVarPrefix}
	 */
	public Var findRenamedVar( final Binding binding ) {
		final Iterator<Var> it = binding.vars();
		while ( it.hasNext() ) {
			final Var v = it.next();
			if ( v.getVarName().startsWith(renamedVarPrefix) ) {
				return v;
			}
		}
		throw new IllegalArgumentException( String.format( "No variable with prefix '%s' found in binding %s",
		                                                   renamedVarPrefix,
		                                                   binding ) );
	}
}
