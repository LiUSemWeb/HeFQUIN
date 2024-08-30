package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQLSolutionGraphSolver;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.QueryExecutionException;

public class GraphQLSolutionGraphSolverImpl implements GraphQLSolutionGraphSolver {

    @Override
    public List<SolutionMapping> execSelectQuery(final Model solutionGraph, 
                                                 final SPARQLQuery query) throws QueryExecutionException {
        final List<SolutionMapping> solutionMappings = new ArrayList<>();

        try (final QueryExecution qexec = QueryExecutionFactory.create(query.asJenaQuery(), solutionGraph)) {
            final ResultSet results = qexec.execSelect();
            for (; results.hasNext();) {
                solutionMappings.add(new SolutionMappingImpl(results.nextBinding()));
            }
        }
        catch(final RuntimeException e){
            throw new QueryExecutionException("Something went wrong while querying the RDF solution graph generated from the GraphQL endpoint!",e);
        }

        return solutionMappings;
    }
}