package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

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
	protected Var renamedVar = null;
	// Mapping from a renamed var to an index in solMapsList
	protected final Map<Var, Integer> renamedVars = new HashMap<>();

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
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOp( final Set<Binding> solMaps )
			throws ExecOpExecutionException
	{
		final Element elmt = createUnion(solMaps);
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1(elmt);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		return new ExecOpRequestSPARQL(request, fm, false);
	}

	protected Element createUnion( final Iterable<Binding> solMaps )
			throws ExecOpExecutionException
	{
		// Populate the ordered list of solution mappings (used for restoring renamed
		// vars and restoring the join partner vars)
		solMaps.forEach(solMapsList::add);
		
		// Union element
		final ElementUnion union = new ElementUnion();

		// Collect the variables bound by the given solution mappings
		// (which are guaranteed to include all join variables).
		final Set<Var> joinVars = new HashSet<>();
		for ( final Binding b : solMaps ) {
			final Iterator<Var> it = b.vars();
			while ( it.hasNext() ) {
				joinVars.add( it.next() );
			}
		}

		// Get first non-join variable for renaming
		for( final Var v : varsInQuery ){
			if( ! joinVars.contains(v) && query.getCertainVariables().contains(v)){
				renamedVar = v; // first certain non-join
				break;
			}
		}

		if ( renamedVar == null ) {
			throw new ExecOpExecutionException("No non-join variable found for renaming", this);
		}

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
				throw new ExecOpExecutionException(e, this);
			}
			
			// Create new variable 
			final Var v = Var.alloc( renamedVar.getVarName() + "_" + i );
			
			// Rename the variable in the pattern
			final Element elt2 = renameVar(patternWithBindings, renamedVar, v);
			union.addElement(elt2);
			
			// Add the solution mapping to the list of solution mappings
			solMapsList.add(solMap);
			// Map the renamed var to the index of the corresponding solution mapping
			renamedVars.put(v, i);
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
		protected final Set<SolutionMapping> inputSolutionMappingsWithoutJoinPartners = new HashSet<>();

		public MyIntermediateResultElementSink2( final Iterable<SolutionMapping> inputSolutionMappings ) {
			super(inputSolutionMappings);
		}

		protected void _send( final SolutionMapping smFromRequest ) {
			// Merge smFromRequest into the input solution mappings:
			// 1. For each renamed variable, check if the request binding contains it.
			// 2. Rename that variable and merge with the corresponding solMapList entry.
			// 3. For each compatible input mapping, merge and collect the results.
			for( final Entry<Var, Integer> entry : renamedVars.entrySet() ){
				final Var v = entry.getKey();
				final int i = entry.getValue();
				final Binding b = smFromRequest.asJenaBinding();

				if ( ! b.contains(v) ) {
					continue;
				}

				// Rename var and merge with input mapping
				final Binding renamedAndMerged = BindingLib.merge( renameVar(b, v, renamedVar),
				                                                             solMapsList.get(i) );
				final SolutionMapping updatedRequest = new SolutionMappingImpl(renamedAndMerged);

				// Merge with inputSolutionMappings
				for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
					if ( SolutionMappingUtils.compatible(smFromInput, updatedRequest ) ) {
						solMapsForOutput.add( SolutionMappingUtils.merge(smFromInput, updatedRequest) );
					}
				}
			}
		}
	} // end of helper class MyIntermediateResultElementSink2


	protected class MyIntermediateResultElementSinkOuterJoin2 extends MyIntermediateResultElementSink2
	{
		protected final Set<SolutionMapping> inputSolutionMappingsWithJoinPartners = new HashSet<>();

		public MyIntermediateResultElementSinkOuterJoin2( final Iterable<SolutionMapping> inputSolutionMappings ) {
			super(inputSolutionMappings);
		}

		protected void _send( final SolutionMapping smFromRequest ) {
			// Merge smFromRequest into the input solution mappings:
			// 1. For each renamed variable, check if the request binding contains it.
			// 2. Rename that variable and merge with the corresponding solMapList entry.
			// 3. For each compatible input mapping, merge and collect the results.
			for( final Entry<Var, Integer> entry : renamedVars.entrySet() ){
				final Var v = entry.getKey();
				final int i = entry.getValue();
				final Binding b = smFromRequest.asJenaBinding();

				if ( ! b.contains(v) ) {
					continue;
				}

				// Rename var and merge with input mapping
				final Binding renamedAndMerged = BindingLib.merge( renameVar(b, v, renamedVar),
				                                                             solMapsList.get(i) );
				final SolutionMapping updatedRequest = new SolutionMappingImpl(renamedAndMerged);

				// Merge with inputSolutionMappings
				for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
					if ( SolutionMappingUtils.compatible(smFromInput, updatedRequest ) ) {
						solMapsForOutput.add( SolutionMappingUtils.merge(smFromInput, updatedRequest) );
						inputSolutionMappingsWithJoinPartners.add(smFromInput);
					}
				}
			}
		}

		/**
		 * Sends to the output sink all input solution
		 * mappings that did not have a join partner.
		 */
		@Override
		public void flush() {
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( ! inputSolutionMappingsWithJoinPartners.contains(smFromInput) ) {
					solMapsForOutput.add(smFromInput);
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
}
