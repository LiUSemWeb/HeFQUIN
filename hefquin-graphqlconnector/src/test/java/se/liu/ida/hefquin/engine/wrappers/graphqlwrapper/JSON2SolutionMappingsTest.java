package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonException;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;
import org.apache.jena.datatypes.xsd.impl.XSDPlainType;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.query.impl.SPARQLQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLArgument;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLArgumentImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl.GraphQLQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.QueryExecutionException;

/**
 * Unit tests for JSON2SolutionGraph and SolutionGraph2SolutionMappings
 */
public class JSON2SolutionMappingsTest extends GraphQLWrapperTestBase
{
    @Test
    public void test1() throws ParseException, JsonException, QueryExecutionException {

        // The original SPARQL query
        final String queryString = 
        "prefix p: <http://example.org/p/> " +
        "prefix c: <http://example.org/c/> " +
        "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
        "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +

        "SELECT ?name ?title ?genre ?nr WHERE {" +
        "   ?author p:id_of_Author      \"auth3\"^^xsd:string ;" +
        "           p:name_of_Author    ?name ;" +
        "           p:books_of_Author   ?book ." +
        "   ?book   p:title_of_Book     ?title ;" +
        "           p:genre_of_Book     ?genre ;" +
        "           p:nr_pages_of_Book  ?nr ." +
        "}";
        final Query jenaQuery = QueryFactory.create(queryString.toString());
        final SPARQLQuery sparqlQuery = new SPARQLQueryImpl(jenaQuery);

        // How the GraphQL Query will look like:
        final Set<String> fieldPaths = new HashSet<>();
        fieldPaths.add("ep_single0:author(id:$var0)/id_Author:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books:books/id_Book:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books:books/scalar_genre:genre");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books:books/scalar_nr_pages:nr_pages");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books:books/scalar_title:title");
        fieldPaths.add("ep_single0:author(id:$var0)/scalar_id:id");
        fieldPaths.add("ep_single0:author(id:$var0)/scalar_name:name");
        final Set<GraphQLArgument> queryArgs = new HashSet<>();
        queryArgs.add(new GraphQLArgumentImpl("var0", "id", new JsonString("auth3"), "ID!"));
        final GraphQLQuery graphqlQuery = new GraphQLQueryImpl(fieldPaths, queryArgs);

        /* How the JSON response can look like
        ----------------------------------------------
        {
            "data": {
                "ep_single0": {
                    "id_Author": "auth3",
                    "object_books": [
                        {
                            "id_Book": "book1",
                            "scalar_genre": "FANTASY",
                            "scalar_nr_pages": 423,
                            "scalar_title": "The Fellowship of the Ring"
                        },
                        {
                            "id_Book": "book2",
                            "scalar_genre": "null",
                            "scalar_nr_pages": 352,
                            "scalar_title": "The Two Towers"
                        },
                        {
                            "id_Book": "book3",
                            "scalar_genre": "FANTASY",
                            "scalar_nr_pages": 416,
                            "scalar_title": "The Return of the King"
                        }
                    ],
                    "scalar_id": "auth3",
                    "scalar_name": "J.R.R. Tolkien"
                }
            }
        }
        */
        final String jsonString = 
            "{\"data\": {\"ep_single0\": {\"id_Author\": \"auth3\",\"object_books\": [{\"id_Book\": \"book1\","+
            "\"scalar_genre\": \"FANTASY\",\"scalar_nr_pages\": 423,\"scalar_title\": \"The Fellowship of the Ring\"},"+
            "{\"id_Book\": \"book2\",\"scalar_genre\": null,\"scalar_nr_pages\": 352,\"scalar_title\": "+
            "\"The Two Towers\"},{\"id_Book\": \"book3\",\"scalar_genre\": \"FANTASY\",\"scalar_nr_pages\": 416,"+
            "\"scalar_title\": \"The Return of the King\"}],\"scalar_id\": \"auth3\",\"scalar_name\": \"J.R.R. Tolkien\"}}}";

        final JsonObject jsonObject = JSON.parse(jsonString);
        
        // Translated JSON to solution mappings
        final Model solutionGraph = jsonTranslator.translateJSON(jsonObject);
        final Set<SolutionMapping> actualSolutionMappings = new HashSet<>(
            solutionGraphTranslator.execSelectQuery(solutionGraph, sparqlQuery)
        );

        // The expected solution mappings
        final Set<SolutionMapping> expectedSolutionMappings = new HashSet<>();
        
        final Var v1 = Var.alloc("name");
        final Var v2 = Var.alloc("title");
        final Var v3 = Var.alloc("genre");
        final Var v4 = Var.alloc("nr");

        final BindingBuilder bindingBuilder = BindingBuilder.create();
        final Binding b1 = bindingBuilder.add(v1, NodeFactory.createLiteral("J.R.R. Tolkien", XSDPlainType.XSDstring))
                                        .add(v2,NodeFactory.createLiteral("The Return of the King", XSDPlainType.XSDstring))
                                        .add(v3,NodeFactory.createLiteral("FANTASY", XSDPlainType.XSDstring))
                                        .add(v4,NodeFactory.createLiteral("416", XSDPlainType.XSDint)).build();
        bindingBuilder.reset();
        final Binding b2 = bindingBuilder.add(v1, NodeFactory.createLiteral("J.R.R. Tolkien", XSDPlainType.XSDstring))
                                        .add(v2,NodeFactory.createLiteral("The Fellowship of the Ring", XSDPlainType.XSDstring))
                                        .add(v3,NodeFactory.createLiteral("FANTASY", XSDPlainType.XSDstring))
                                        .add(v4,NodeFactory.createLiteral("423", XSDPlainType.XSDint)).build();

        expectedSolutionMappings.add(new SolutionMappingImpl(b1));
        expectedSolutionMappings.add(new SolutionMappingImpl(b2));

        assertEquals(expectedSolutionMappings, actualSolutionMappings);
    }

    @Test
    public void test2() throws ParseException, JsonException, QueryExecutionException {

        // The original SPARQL query
        final String queryString = 
        "prefix p: <http://example.org/p/> " +
        "prefix c: <http://example.org/c/> " +
        "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
        "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +

        "SELECT ?title ?nr WHERE {" +
        "   ?author1 p:id_of_Author      \"auth6\"^^xsd:string ;" +
        "            p:books_of_Author   ?book ." +
        "   ?author2 p:id_of_Author      \"auth7\"^^xsd:string ;" +
        "            p:books_of_Author   ?book ." +
        "   ?book    p:title_of_Book     ?title ;" +
        "            p:nr_pages_of_Book  ?nr ." +
        "}";
        final Query jenaQuery = QueryFactory.create(queryString.toString());
        final SPARQLQuery sparqlQuery = new SPARQLQueryImpl(jenaQuery);

        // How the GraphQL Query can look like:
        final Set<String> fieldPaths = new HashSet<>();
        fieldPaths.add("ep_single0:author(id:$var0)/id_Author:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books:books/id_Book:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books:books/scalar_nr_pages:nr_pages");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books:books/scalar_title:title");
        fieldPaths.add("ep_single0:author(id:$var0)/scalar_id:id");
        fieldPaths.add("ep_single1:author(id:$var1)/id_Author:id");
        fieldPaths.add("ep_single1:author(id:$var1)/object_books:books/id_Book:id");
        fieldPaths.add("ep_single1:author(id:$var1)/object_books:books/scalar_nr_pages:nr_pages");
        fieldPaths.add("ep_single1:author(id:$var1)/object_books:books/scalar_title:title");
        fieldPaths.add("ep_single1:author(id:$var1)/scalar_id:id");
        final Set<GraphQLArgument> queryArgs = new HashSet<>();
        queryArgs.add(new GraphQLArgumentImpl("var0", "id", new JsonString("auth6"), "ID!"));
        queryArgs.add(new GraphQLArgumentImpl("var1", "id", new JsonString("auth7"), "ID!"));
        final GraphQLQuery graphqlQuery = new GraphQLQueryImpl(fieldPaths, queryArgs);

        /* How the JSON response can look like
        ----------------------------------------------
        {
            "data": {
                "ep_single0": {
                    "id_Author": "auth6",
                    "object_books": [
                        {
                            "id_Book": "book9",
                            "scalar_nr_pages": 978,
                            "scalar_title": "The imaginary book for this example"
                        },
                        {
                            "id_Book": "book10",
                            "scalar_nr_pages": 486,
                            "scalar_title": "Another funny book"
                        }
                    ],
                    "scalar_id": "auth6"
                },
                "ep_single1": {
                    "id_Author": "auth7",
                    "object_books": [
                        {
                            "id_Book": "book9",
                            "scalar_nr_pages": 978,
                            "scalar_title": "The imaginary book for this example"
                        }
                    ],
                    "scalar_id": "auth7"
                }
            }
        }
        */
        final String jsonString = 
            "{\"data\": {\"ep_single0\": {\"id_Author\": \"auth6\",\"object_books\": [{\"id_Book\": \"book9\"," +
            "\"scalar_nr_pages\": 978,\"scalar_title\": \"The imaginary book for this example\"},{" +
            "\"id_Book\": \"book10\",\"scalar_nr_pages\": 486,\"scalar_title\": \"Another funny book\"}]," +
            "\"scalar_id\": \"auth6\"},\"ep_single1\": {\"id_Author\": \"auth7\",\"object_books\": [{" +
            "\"id_Book\": \"book9\",\"scalar_nr_pages\": 978,\"scalar_title\": \"The imaginary book for this example\"" +
            "}],\"scalar_id\": \"auth7\"}}}";

        final JsonObject jsonObject = JSON.parse(jsonString);
        
        // Translated JSON to solution mappings
        final Model solutionGraph = jsonTranslator.translateJSON(jsonObject);
        final Set<SolutionMapping> actualSolutionMappings = new HashSet<>(
            solutionGraphTranslator.execSelectQuery(solutionGraph, sparqlQuery)
        );
        
        // The expected solution mappings
        final Set<SolutionMapping> expectedSolutionMappings = new HashSet<>();
        
        final Var v1 = Var.alloc("title");
        final Var v2 = Var.alloc("nr");

        final BindingBuilder bindingBuilder = BindingBuilder.create();
        final Binding b1 = bindingBuilder.add(v1, NodeFactory.createLiteral("The imaginary book for this example", XSDPlainType.XSDstring))
                                        .add(v2,NodeFactory.createLiteral("978", XSDPlainType.XSDint)).build();

        expectedSolutionMappings.add(new SolutionMappingImpl(b1));

        assertEquals(expectedSolutionMappings, actualSolutionMappings);
    }
}
