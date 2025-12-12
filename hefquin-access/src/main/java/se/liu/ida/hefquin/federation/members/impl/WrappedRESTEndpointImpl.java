package se.liu.ida.hefquin.federation.members.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;
import se.liu.ida.hefquin.jenaintegration.HeFQUINConstants;
import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorUtils;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationUtils;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExprConstant;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExprFunction;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExpression;
import se.liu.ida.hefquin.mappings.algebra.exprs.fcts.ExtnFct_ToBNode;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtend;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;
import se.liu.ida.hefquin.mappings.algebra.sources.json.JsonObject;
import se.liu.ida.hefquin.mappings.algebra.sources.json.JsonPathQuery;
import se.liu.ida.hefquin.mappings.algebra.sources.json.MappingOpExtractJSON;

public class WrappedRESTEndpointImpl extends RESTEndpointImpl
                                     implements WrappedRESTEndpoint
{
	protected final MappingOperator mappingExpression;
	protected final Set<SourceReference> srcRefs;

	public WrappedRESTEndpointImpl( final String url,
	                                final List<RESTEndpoint.Parameter> params,
	                                final MappingOperator mappingExpression ) {
		super(url, params);

		assert mappingExpression != null;
		this.mappingExpression = mappingExpression;

		srcRefs = MappingOperatorUtils.extractAllSrcRefs(mappingExpression);
	}

	@Override
	public String toString() { return "Wrapped REST endpoint at " + url; }

	@Override
	public boolean equals( final Object o ) {
		return super.equals(o);
	}

	@Override
	public List<SolutionMapping> evaluatePatternOverRDFView( final SPARQLGraphPattern pattern,
	                                                         final String data )
			throws DataConversionException
	{
		// The idea of this implementation is to materialize the RDF view of
		// the given data (i.e., convert the data into an RDF graph) and, then
		// query the RDF view with a SELECT * query that has the given graph
		// pattern as its WHERE clause, by using the Jena query execution
		// machinery.

		final Dataset ds = convertResponseDataIntoRDF(data, pattern);

		final Query q = new Query();
		q.setQuerySelectType();
		q.setQueryResultStar(true);
		q.setQueryPattern( QueryPatternUtils.convertToJenaElement(pattern) );

		final QueryExecution qe = QueryExecution
				.dataset(ds)
				.query(q)
				.set(HeFQUINConstants.sysExecuteWithJena, true)
				.build();

		final ResultSet rs = qe.execSelect();
		final List<SolutionMapping> solmaps = new ArrayList<>();
		while ( rs.hasNext() ) {
			final SolutionMapping sm = new SolutionMappingImpl( rs.nextBinding() );
			solmaps.add(sm);
		}

		rs.close();

		return solmaps;
	}

	/**
	 * Assuming the given string is the content of a response retrieved
	 * when issuing a request to this REST endpoint, this method returns
	 * an RDF view of this content.
	 * <p>
	 * A SPARQL graph pattern can be passed as an optional parameter to let
	 * the wrapper know which pattern is intended to be evaluated over the
	 * returned data. Some wrapper implementations may use this pattern to
	 * reduce their effort of converting the retrieved data into RDF by
	 * considering only the conversion rules that may produce RDF triples
	 * relevant to the given pattern. Wrapper implementations that do so
	 * have to guarantee that this does not have any effect on the result
	 * of evaluating the given pattern over the returned RDF triples.
	 *
	 * @param data - the content of a response retrieved via a
	 *               successful request to this REST endpoint
	 * @param pattern - the pattern that is intended to be evaluated over
	 *                  the returned RDF data; may be {@code null}
	 * @return an RDF dataset that represents the given data
	 * @throws DataConversionException if the conversion into RDF fails
	 */
	public Dataset convertResponseDataIntoRDF( final String data,
	                                           final SPARQLGraphPattern pattern )
			throws DataConversionException
	{
/*
		final SourceReference sr = new SourceReference() {};
		final JsonPathQuery query = new JsonPathQuery("$.current");

		final Map<String, JsonPathQuery> P1 = new HashMap<>();
		final Map<String, JsonPathQuery> P2 = new HashMap<>();
		P1.put( MappingRelation.oAttr, new JsonPathQuery("temperature_2m") );
		P2.put( MappingRelation.oAttr, new JsonPathQuery("wind_speed_10m") );

		final MappingOperator op1 = new MappingOpExtractJSON(sr, query, P1);
		final MappingOperator op2 = new MappingOpExtractJSON(sr, query, P2);

		final Node bnodeLabel = NodeFactory.createLiteralString("b");
		final ExtendExpression expr = new ExtendExprFunction( ExtnFct_ToBNode.instance,
		                                                      new ExtendExprConstant(bnodeLabel) );
		final MappingOperator op1S = new MappingOpExtend(op1, expr, MappingRelation.sAttr);
		final MappingOperator op2S = new MappingOpExtend(op2, expr, MappingRelation.sAttr);

		final Node uri1 = NodeFactory.createURI("http://example.org/temperature");
		final Node uri2 = NodeFactory.createURI("http://example.org/windSpeed");
		final ExtendExpression expr1 = new ExtendExprConstant(uri1);
		final ExtendExpression expr2 = new ExtendExprConstant(uri2);
		final MappingOperator op1P = new MappingOpExtend(op1S, expr1, MappingRelation.pAttr);
		final MappingOperator op2P = new MappingOpExtend(op2S, expr2, MappingRelation.pAttr);

		final MappingOperator op = new MappingOpUnion(op1P, op2P);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( sr, new JsonObject(data) );

		final MappingRelation r = op.evaluate(srMap);
*/

		final DataObject dataObj = new JsonObject(data);
		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		for ( final SourceReference sr : srcRefs ) {
			srMap.put(sr, dataObj);
		}

		final MappingRelation r = mappingExpression.evaluate(srMap);

		final Dataset ds = MappingRelationUtils.convertToRDF(r);

		return ds;
	}

}
