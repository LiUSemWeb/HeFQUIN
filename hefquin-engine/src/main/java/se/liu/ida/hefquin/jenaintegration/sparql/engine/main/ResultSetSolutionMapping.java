package se.liu.ida.hefquin.jenaintegration.sparql.engine.main;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;

public class ResultSetSolutionMapping implements ResultSet
{
	// Could use QueryIteratorWrapper
	private Iterator<SolutionMapping> queryExecutionIter;
	private List<String> resultVars;
	private QuerySolution currentQuerySolution;
	private int rowNumber;

	/** Create a streaming ResultSet, with resources sharing a common Model */
	public static ResultSet create(List<String> resultVars, Iterator<SolutionMapping> iter) {
		return new ResultSetSolutionMapping(resultVars, iter);
	}

	protected ResultSetSolutionMapping(List<String> resultVars, Iterator<SolutionMapping> iter) {
		this.queryExecutionIter = iter;
		this.resultVars = resultVars;
		this.currentQuerySolution = null;
		this.rowNumber = 0;
	}

	@Override
	public boolean hasNext() {
		if ( queryExecutionIter == null )
			return false;
		boolean r = queryExecutionIter.hasNext();
		if ( !r )
			close();
		return r;
	}

	@Override
	public Binding nextBinding() {
		if ( queryExecutionIter == null )
			throw new NoSuchElementException(this.getClass() + ".next");

		try {
			final SolutionMapping solutionMapping = queryExecutionIter.next();
			if ( solutionMapping != null )
				rowNumber++;
			return solutionMapping.asJenaBinding();
		} catch (NoSuchElementException ex) {
			close();
			throw ex;
		}
	}

	@Override
	public void close() {
		// ARQ QueryIterators are org.apache.jena.atlas.lib.Closable.
		Iter.close(queryExecutionIter);
		queryExecutionIter = null;
	}

	@Override
	public QuerySolution nextSolution() {
		if ( queryExecutionIter == null )
			throw new NoSuchElementException(this.getClass() + ".next");
		Binding binding = nextBinding();
		currentQuerySolution = new ResultBinding(null, binding);
		return currentQuerySolution;
	}

	@Override
	public QuerySolution next() { return nextSolution(); }

	@Override
	public void forEachRemaining(Consumer<? super QuerySolution> action) {
		if ( queryExecutionIter == null )
			return;
		queryExecutionIter.forEachRemaining(solutionMapping -> {
			rowNumber++;
			action.accept(new ResultBinding(null, solutionMapping.asJenaBinding()));
		});
		close();
	}

	@Override
	public int getRowNumber() {
		return rowNumber;
	}

	@Override
	public List<String> getResultVars() { return resultVars; }

	@Override
	public Model getResourceModel() { return null; }

}
