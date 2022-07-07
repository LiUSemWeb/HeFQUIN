package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.SolutionGraph2SolutionMappings;

public class SolutionGraph2SolutionMappingImpl implements SolutionGraph2SolutionMappings {

    @Override
    public List<SolutionMapping> querySolutionGraph(final Model solutionGraph, 
                                                    final SPARQLQuery query) throws RuntimeException {
        final List<SolutionMapping> solutionMappings = new ArrayList<>();

        try (final QueryExecution qexec = QueryExecutionFactory.create(query.asJenaQuery(), solutionGraph)) {
            final ResultSet results = qexec.execSelect();
            for (; results.hasNext();) {
                solutionMappings.add(new SolutionMappingImpl(results.nextBinding()));
            }
        }
        catch(final RuntimeException e){
            throw new RuntimeException("Something went wrong when executing the SPARQL query over the provided solutionGraph!",e);
        }

        return solutionMappings;
    }
}