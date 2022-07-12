package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import java.util.List;

import org.apache.jena.rdf.model.Model;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;

public interface GraphQLSolutionGraphSolver {
    
    /**
     * Queries @param solutionGraph using the SELECT query @param query and
     * @return a list of corresponding solution mappings.
     * @throws RunTimeException if for some reason the execution of the query over the
     * given solutionGraph did not work correctly.
     */
    public List<SolutionMapping> execSelectQuery(final Model solutionGraph, 
                                                 final SPARQLQuery query);
}