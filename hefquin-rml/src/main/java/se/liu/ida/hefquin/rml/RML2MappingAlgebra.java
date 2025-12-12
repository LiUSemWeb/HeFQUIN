package se.liu.ida.hefquin.rml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.XSD;

import se.liu.ida.hefquin.jenaext.ModelUtils;
import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExprAttribute;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExprConstant;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExprFunction;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExpression;
import se.liu.ida.hefquin.mappings.algebra.exprs.fcts.ExtnFct_Concat;
import se.liu.ida.hefquin.mappings.algebra.exprs.fcts.ExtnFct_ToBNode;
import se.liu.ida.hefquin.mappings.algebra.exprs.fcts.ExtnFct_ToIRI;
import se.liu.ida.hefquin.mappings.algebra.exprs.fcts.ExtnFct_ToLiteral;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtend;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpJoin;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;
import se.liu.ida.hefquin.mappings.algebra.sources.json.JsonPathQuery;
import se.liu.ida.hefquin.mappings.algebra.sources.json.MappingOpExtractJSON;
import se.liu.ida.hefquin.rml.vocabulary.RMLVocab;

/**
 * This class can be used to translated RML mappings into the mapping algebra.
 * <p>
 * The translation is implemented using the algorithms introduced in the
 * following research paper.
 * <p>
 * Sitt Min Oo and Olaf Hartig: "An Algebraic Foundation for Knowledge Graph
 * Construction." In Proceedings of the 22nd Extended Semantic Web Conference
 * (ESWC), 2025.
 */
public class RML2MappingAlgebra
{
	/**
	 * This implements lines 4-26 of Algorithm 1 of the paper.
	 *
	 * @param tm
	 * @param rmlDescription
	 * @param baseIRI
	 * @return
	 * @throws RMLParserException
	 */
	public static MappingOperator convert( final Resource tm,
	                                       final Model rmlDescription,
	                                       final Node baseIRI )
			throws RMLParserException
	{
		// line 4 of Algorithm 1
		final SourceReference sr = createSourceReference(tm);
		final JsonPathQuery rootQuery = checkSourceAndGetRootQuery(tm);
		final Set<String> queryStrings = extractQueries(tm);
		final Map<String, String> PwithStrings = createPwithStrings(queryStrings);
		final Map<String, JsonPathQuery> P = createP(PwithStrings, tm);
		MappingOperator op = new MappingOpExtractJSON(sr, rootQuery, P);

		// lines 5-8 of Algorithm 1
		final Resource sm, pom, pm, om;
		try {
			sm = ModelUtils.getSingleMandatoryResourceProperty( tm, RMLVocab.subjectMap );
			pom = ModelUtils.getSingleMandatoryResourceProperty( tm, RMLVocab.predicateObjectMap );
			pm = ModelUtils.getSingleMandatoryResourceProperty( pom, RMLVocab.predicateMap );
			om = ModelUtils.getSingleMandatoryResourceProperty( pom, RMLVocab.objectMap );
		}
		catch ( final IllegalArgumentException e ) {
			throw new RMLParserException( e.getMessage() );
		}

		// data structure needed for the following steps
		// (maps query strings to attributes)
		final Map<String, String> reversePwithStrings = createReverseP(PwithStrings);

		// line 9 of Algorithm 1
		final ExtendExpression sExt = createExtendExpression( sm, baseIRI,
		                                                      reversePwithStrings );
		op = new MappingOpExtend( op, sExt, MappingRelation.sAttr );

		// line 10 of Algorithm 1
		final ExtendExpression pExt = createExtendExpression( pm, baseIRI,
		                                                      reversePwithStrings );
		op = new MappingOpExtend( op, pExt, MappingRelation.pAttr );

		// lines 10-22 of Algorithm 1
		final Resource ptm;
		try {
			ptm = ModelUtils.getSingleOptionalResourceProperty(om, RMLVocab.parentTriplesMap);
		}
		catch ( final IllegalArgumentException e ) {
			throw new RMLParserException( e.getMessage() );
		}

		if ( ptm != null ) {
			// lines 12-20 of Algorithm 1
			// - line 12
			final SourceReference sr2 = createSourceReference(ptm);
			final JsonPathQuery rootQuery2 = checkSourceAndGetRootQuery(ptm);
			final Set<String> queryStrings2 = extractQueries(ptm);
			final Map<String, String> PwithStrings2 = createPwithStrings(queryStrings2);
			final Map<String, JsonPathQuery> P2 = createP(PwithStrings2, ptm);
			final MappingOperator op2 = new MappingOpExtractJSON(sr2, rootQuery2, P2);

			// - line 13
			final List<Pair<String,String>> J = new ArrayList<>();

			// -line 14-17
			final StmtIterator it = om.listProperties( RMLVocab.joinCondition );
			while ( it.hasNext() ) {
				final RDFNode o = it.next().getObject();
				if ( ! o.isResource() )
					throw new RMLParserException("There is a triples map (" + tm.toString() + ") with an rml:joinCondition that is not a resource (IRI or bnode).");

				// - lines 15-16
				final Resource jc = o.asResource();
				final Literal c, p;
				try {
					c = ModelUtils.getSingleMandatoryLiteralProperty(jc, RMLVocab.child);
					p = ModelUtils.getSingleMandatoryLiteralProperty(jc, RMLVocab.parent);
				}
				catch ( final IllegalArgumentException e ) {
					throw new RMLParserException( e.getMessage() );
				}

				final String cAttr = reversePwithStrings.get( c.getLexicalForm() );
				final String pAttr = reversePwithStrings.get( p.getLexicalForm() );
				if ( cAttr == null || pAttr == null )
					throw new IllegalArgumentException(); // this shouldn't happen

				// - line 17
				J.add( Pair.create(cAttr,pAttr) );
			}

			// - line 18
			op = new MappingOpJoin(op, op2, J);

			// - line 19
			final Resource sptm;
			try {
				sptm = ModelUtils.getSingleMandatoryResourceProperty( ptm, RMLVocab.subjectMap );
			}
			catch ( final IllegalArgumentException e ) {
				throw new RMLParserException( e.getMessage() );
			}

			// - line 20
			final Map<String, String> reversePwithStrings2 = createReverseP(PwithStrings2);
			final ExtendExpression oExt = createExtendExpression( sptm, baseIRI,
			                                                      reversePwithStrings2 );
			op = new MappingOpExtend( op, oExt, MappingRelation.oAttr );
		}
		else  {
			// line 22 of Algorithm 1
			final ExtendExpression oExt = createExtendExpression( om, baseIRI,
			                                                      reversePwithStrings );
			op = new MappingOpExtend( op, oExt, MappingRelation.oAttr );
		}

		// preparation for line 23 of Algorithm 1
		final Resource gm1, gm2;
		try {
			gm1 = ModelUtils.getSingleOptionalResourceProperty(sm, RMLVocab.graphMap);
			gm2 = ModelUtils.getSingleOptionalResourceProperty(pom, RMLVocab.graphMap);
		}
		catch ( final IllegalArgumentException e ) {
			throw new RMLParserException( e.getMessage() );
		}

		final Resource gm = (gm1 != null) ? gm1 : (gm2 != null) ? gm2 : null;

		// lines 23-26 of Algorithm 1
		if ( gm != null ) {
			// - line 24
			final ExtendExpression gExt = createExtendExpression( gm, baseIRI,
			                                                      reversePwithStrings );
			op = new MappingOpExtend( op, gExt, MappingRelation.gAttr );
		}
		else {
			// - line 26
			final Node dfltGraphURI = RMLVocab.defaultGraph.asNode();
			final ExtendExpression gExt = new ExtendExprConstant(dfltGraphURI);
			op = new MappingOpExtend( op, gExt, MappingRelation.gAttr );
		}

		return op;
	}

	/**
	 * Checks that the logical source of the given triples map has
	 * rml:JSONPath as its reference formulation and obtains the
	 * root iterator query of the logical source.
	 * 
	 * @param tm - assumed to represent an RML triples map
	 * @return JSONPath query created from the rml:iterator of the logical source
	 * @throws RMLParserException
	 */
	public static JsonPathQuery checkSourceAndGetRootQuery( final Resource tm )
			throws RMLParserException
	{
		// Get the rml:logicalSource of the given triples map.
		final Resource ls;
		try {
			ls = ModelUtils.getSingleMandatoryResourceProperty( tm, RMLVocab.logicalSource );
		}
		catch ( final Exception e ) {
			throw new RMLParserException("There is a triples map (" + tm.toString() + ") that does not have a logical source.", e);
		}

		// Get the rml:referenceFormulation of the logical source.
		final Resource rf;
		try {
			rf = ModelUtils.getSingleMandatoryResourceProperty( ls, RMLVocab.referenceFormulation );
		}
		catch ( final Exception e ) {
			throw new RMLParserException("There is a triples map (" + tm.toString() + ") whose logical source does not have a reference formulation.", e);
		}

		// Check that the reference formulation is rml:JSONPath.
		if ( ! rf.equals(RMLVocab.JSONPath) ) {
			throw new RMLParserException("There is a triples map (" + tm.toString() + ") whose logical source does not have JSONPath as reference formulation, but: " + rf.toString() );
		}

		// Now extract the root query from the logical source.
		final String rootQueryString;
		try {
			rootQueryString = ModelUtils.getSingleMandatoryProperty_XSDString( ls, RMLVocab.iterator );
		}
		catch ( final Exception e ) {
			throw new RMLParserException("There is a triples map (" + tm.toString() + ") whose logical source does not have an iterator.", e);
		}

		try {
			return new JsonPathQuery(rootQueryString);
		}
		catch ( final Exception e ) {
			throw new RMLParserException("There is a triples map (" + tm.toString() + ") whose logical source has an iterator (" + rootQueryString + ") that cannot be parsed into a JSONPath expression.", e);
		}
	}

	public static SourceReference createSourceReference( final Resource tm )
			throws RMLParserException
	{
		// Get the rml:logicalSource of the given triples map.
		final Resource ls;
		try {
			ls = ModelUtils.getSingleMandatoryResourceProperty( tm, RMLVocab.logicalSource );
		}
		catch ( final Exception e ) {
			throw new RMLParserException("There is a triples map (" + tm.toString() + ") that does not have a logical source.", e);
		}

		final Resource src;
		try {
			// As per the new version of RML, the object of every rml:source
			// triple must be a resource; it cannot be a literal anymore.
			src = ModelUtils.getSingleMandatoryResourceProperty( ls, RMLVocab.source );
		}
		catch ( final Exception e ) {
			throw new RMLParserException("There is a triples map (" + tm.toString() + ") whose logical source does not have an rm:source.", e);
		}

		return new MySourceReference(src);
	}

	/**
	 * This function implements lines 1-10 of Algorithm 2 of the paper.
	 *
	 * @param tm
	 * @return
	 */
	public static Set<String> extractQueries( final Resource tm )
	{
		final Set<String> queries = new HashSet<>();

		// lines 3-4 of Algorithm 2
		final StmtIterator itRef = tm.listProperties( RMLVocab.reference );
		while ( itRef.hasNext() ) {
			final RDFNode o = itRef.next().getObject();
			if ( o.isLiteral() ) {
				final String lex = o.asLiteral().getLexicalForm();
				queries.add(lex);
			}
		}

		// lines 5-6 of Algorithm 2
		final StmtIterator itTem = tm.listProperties( RMLVocab.template );
		while ( itTem.hasNext() ) {
			final RDFNode o = itTem.next().getObject();
			if ( o.isLiteral() ) {
				final String lex = o.asLiteral().getLexicalForm();
				extractQueriesFromTemplate(lex, queries);
			}
		}

		// lines 7 and 9-10 of Algorithm 2
		final StmtIterator itChl = tm.listProperties( RMLVocab.child );
		while ( itChl.hasNext() ) {
			final RDFNode o = itChl.next().getObject();
			if ( o.isLiteral() ) {
				final String lex = o.asLiteral().getLexicalForm();
				queries.add(lex);
			}
		}

		// lines 8 and 9-10 of Algorithm 2
		final StmtIterator itPTM = tm.getModel().listStatements( null,
		                                                        RMLVocab.parentTriplesMap,
		                                                        tm );
		while ( itPTM.hasNext() ) {
			final Resource om = itPTM.next().getSubject();
			final StmtIterator itJC = om.listProperties( RMLVocab.joinCondition );
			while ( itJC.hasNext() ) {
				final RDFNode jc = itJC.next().getObject();
				if ( jc.isResource() ) {
					final StmtIterator itPar = jc.asResource().listProperties( RMLVocab.parent );
					while ( itPar.hasNext() ) {
						final RDFNode par = itPar.next().getObject();
						if ( par.isLiteral() ) {
							final String lex = par.asLiteral().getLexicalForm();
							queries.add(lex);
						}
					}
				}
			}
		}

		return queries;
	}

	public static void extractQueriesFromTemplate( final String t,
	                                               final Set<String> queries ) {
		int fromIdx = 0;
		while ( fromIdx < t.length() ) {
			final int idx1 = t.indexOf('{', fromIdx);
			if ( idx1 == -1 ) return;

			final int idx2 = t.indexOf('}', idx1);
			if ( idx2 == -1 ) return;

			final String extractedQueryString = t.substring(idx1+1, idx2);
			queries.add(extractedQueryString);

			fromIdx = idx2 + 1;
		}
	}

	/**
	 * This function implements lines 11-13 of Algorithm 2 in a generic way;
	 * that is, the resulting map P contains the query string rather than
	 * concrete query objects.
	 *
	 * @param queries
	 * @return
	 */
	public static Map<String, String> createPwithStrings( final Set<String> queries ) {
		final Map<String, String> PwithStrings = new HashMap<>();
		int i = 1;
		for ( final String queryString : queries ) {
			final String a = "a" + i; // fresh attribute name
			PwithStrings.put(a, queryString);
			i++;
		}

		return PwithStrings;
	}

	/**
	 * Given a version of map P in which the queries are represented just as
	 * strings, this function creates a version of P in which the queries are
	 * represented as {@link JsonPathQuery} objects.
	 *
	 * @param PwithStrings
	 * @param tm - the triples maps for which the given queries have been
	 *             extracted; this is used only when an exception needs to
	 *             be thrown
	 * @return
	 * @throws RMLParserException
	 */
	public static Map<String, JsonPathQuery> createP( final Map<String, String> PwithStrings,
	                                                  final Resource tm )
			throws RMLParserException
	{
		final Map<String, JsonPathQuery> P = new HashMap<>();
		for ( final Map.Entry<String, String> e : PwithStrings.entrySet() ) {
			final String attr = e.getKey();
			final String qStr = e.getValue();

			final JsonPathQuery q;
			try {
				q = new JsonPathQuery(qStr);
			}
			catch ( final Exception ex ) {
				throw new RMLParserException("There is a triples map (" + tm.toString() + ") for which a query string has been extracted (" + qStr + ") that cannot be parsed into a JSONPath expression.", ex);
			}

			P.put(attr, q);
		}

		return P;
	}

	public static Map<String, String> createReverseP( final Map<String, String> PwithStrings ) {
		final Map<String, String> reverse = new HashMap<>();
		for ( final Map.Entry<String, String> e : PwithStrings.entrySet() ) {
			reverse.put( e.getValue(), e.getKey() );
		}

		return reverse;
	}

	/**
	 * This function implements Algorithm 3 of the paper.
	 *
	 * @param u
	 * @param baseIRI
	 * @param reverseP - maps the extracted query strings to the attributes
	 *                   to which these queries are assigned by P
	 * @return
	 */
	public static ExtendExpression createExtendExpression( final Resource u,
	                                                       final Node baseIRI,
	                                                       final Map<String, String> reverseP )
			throws RMLParserException
	{
		try {
			// line 1 of Algorithm 3
			final RDFNode c = ModelUtils.getSingleOptionalProperty( u, RMLVocab.constant );
			if ( c != null ) {
				return new ExtendExprConstant( c.asNode() );
			}

			// preparation for lines 3-12 of Algorithm 3
			final RDFNode r = ModelUtils.getSingleOptionalProperty( u, RMLVocab.reference );
			final RDFNode t = ModelUtils.getSingleOptionalProperty( u, RMLVocab.template );
			final ExtendExpression extExpr;

			// lines 3-12 of Algorithm 3
			if ( r != null && t == null ) {
				// lines 3-4 of Algorithm 3
				if ( ! r.isLiteral() )
					throw new RMLParserException( "The object (" + u.toString() + ") of an rml:reference statement is not a literal.");

				final String lex = r.asLiteral().getLexicalForm();
				final String attr = reverseP.get(lex);
				if ( attr == null )
					throw new IllegalArgumentException(); // this shouldn't happen

				extExpr = new ExtendExprAttribute(attr);
			}
			else if ( t != null && r == null ) {
				// lines 5-12 of Algorithm 3
				if ( ! t.isLiteral() )
					throw new RMLParserException( "The object (" + u.toString() + ") of an rml:template statement is not a literal.");

				final String lex = t.asLiteral().getLexicalForm();

				final List<ExtendExpression> subExpressions = split(lex, reverseP);
				if ( subExpressions.isEmpty() ) {
					final Node n = NodeFactory.createLiteralString("");
					extExpr = new ExtendExprConstant(n);
				}
				else if ( subExpressions.size() == 1 ) {
					extExpr = subExpressions.get(0);
				}
				else {
					extExpr = new ExtendExprFunction( ExtnFct_Concat.instance,
					                                  subExpressions );
				}
			}
			else {
				throw new RMLParserException( "One of the term maps (" + u.toString() + ") does not have an rml:reference or rml:template statement, or has both of them.");
			}

			// line 13 of Algorithm 3
			final RDFNode type = ModelUtils.getSingleOptionalProperty( u, RMLVocab.termType );
			if ( type != null && RMLVocab.BlankNode.equals(type) ) {
				return new ExtendExprFunction( ExtnFct_ToBNode.instance,
				                               extExpr );
			}

			// line 14 of Algorithm 3
			final RDFNode dt = ModelUtils.getSingleOptionalProperty( u, RMLVocab.datatype );
			if ( dt != null && dt.isURIResource() ) {
				final Node dtNode = dt.asNode();
				return new ExtendExprFunction( ExtnFct_ToLiteral.instance,
				                               extExpr,
				                               new ExtendExprConstant(dtNode) );
			}

			// line 15 of Algorithm 3
			if ( type != null && RMLVocab.Literal.equals(type) ) {
				final Node dtNode = XSD.xstring.asNode();
				return new ExtendExprFunction( ExtnFct_ToLiteral.instance,
				                               extExpr,
				                               new ExtendExprConstant(dtNode) );
			}

			// line 16 of Algorithm 3 (the first check, type==null, is not
			// in Algorithm 3 in the paper, which is a bug in that algorithm)
			if ( type == null && r != null && r.isLiteral() ) {
				final StmtIterator it = u.getModel().listStatements( null,
				                                                     RMLVocab.objectMap,
				                                                     u );
				if ( it.hasNext() ) {
					final Node dtNode = XSD.xstring.asNode();
					return new ExtendExprFunction( ExtnFct_ToLiteral.instance,
					                               extExpr,
					                               new ExtendExprConstant(dtNode) );
				}
			}

			// line 17 of Algorithm 3
			return new ExtendExprFunction( ExtnFct_ToIRI.instance,
			                               extExpr,
			                               new ExtendExprConstant(baseIRI) );
		}
		catch ( final IllegalArgumentException e ) {
			throw new RMLParserException( e.getMessage() );
		}
	}

	public static List<ExtendExpression> split( final String t,
	                                            final Map<String, String> reverseP )
			throws RMLParserException
	{
		final List<ExtendExpression> subExpressions = new ArrayList<>();

		int curIdx = 0;
		while ( curIdx < t.length() ) {
			final int idx1 = t.indexOf('{', curIdx);

			final int idxAfterCurrentNormalSubString;
			final int idx2;
			if ( idx1 == -1 ) {
				idxAfterCurrentNormalSubString = t.length();
				idx2 = -1;
			}
			else {
				idxAfterCurrentNormalSubString = idx1;
				idx2 = t.indexOf('}', idx1);
			}

			if ( curIdx < idxAfterCurrentNormalSubString ) {
				final String normalSubString = t.substring(curIdx,
				                                           idxAfterCurrentNormalSubString);
				final Node n = NodeFactory.createLiteralString(normalSubString);
				subExpressions.add( new ExtendExprConstant(n) );
			}

			if ( idx2 == -1 && idx1 != -1) {
				throw new RMLParserException( "The following rml:template contains a '{' without a corresponding '}': " + t);
			}
			else if ( idx2 != -1 ) {
				final String querySubString = t.substring(idx1+1, idx2);
				final String attr = reverseP.get(querySubString);
				if ( attr == null )
					throw new IllegalArgumentException(); // this shouldn't happen

				subExpressions.add( new ExtendExprAttribute(attr) );

				curIdx = idx2 + 1;
			}
			else {
				curIdx = t.length();
			}
		}

		return subExpressions;
	}

	protected static class MySourceReference implements SourceReference {
		public final Node node;
		public MySourceReference( final Node node ) { this.node = node; }
		public MySourceReference( final Resource r ) { this( r.asNode() ); }

		@Override
		public boolean equals( final Object other ) {
			if ( other == this ) return true;

			return (    other instanceof MySourceReference sr
			         && sr.node.equals(node) );
		}
	}

}
