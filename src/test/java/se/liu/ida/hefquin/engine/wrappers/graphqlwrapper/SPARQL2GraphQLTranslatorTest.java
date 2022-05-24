package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;
import static org.junit.Assert.assertEquals;

import org.apache.jena.atlas.json.JsonNull;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.GraphQLInterface;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.GraphQLInterfaceImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.DefaultGraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.GraphQLEndpointImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.SPARQL2GraphQLTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl.GraphQLQueryImpl;

/**
 * Verifies functionality of SPARQL2GraphQLTranslatorImpl
 */
public class SPARQL2GraphQLTranslatorTest {
    
    // Author test fields
    protected static final GraphQLField a1 = new GraphQLFieldImpl("id","ID!",GraphQLFieldType.SCALAR);
    protected static final GraphQLField a2 = new GraphQLFieldImpl("name","String!",GraphQLFieldType.SCALAR);
    protected static final GraphQLField a3 = new GraphQLFieldImpl("age","Int!",GraphQLFieldType.SCALAR);
    protected static final GraphQLField a4 = new GraphQLFieldImpl("books","Book",GraphQLFieldType.OBJECT);

    // Book test fields
    protected static final GraphQLField b1 = new GraphQLFieldImpl("id","ID!",GraphQLFieldType.SCALAR);
    protected static final GraphQLField b2 = new GraphQLFieldImpl("title","String!",GraphQLFieldType.SCALAR);
    protected static final GraphQLField b3 = new GraphQLFieldImpl("nr_pages","Int!",GraphQLFieldType.SCALAR);
    protected static final GraphQLField b4 = new GraphQLFieldImpl("genre","String",GraphQLFieldType.SCALAR);
    protected static final GraphQLField b5 = new GraphQLFieldImpl("authors","Author",GraphQLFieldType.OBJECT);

    // Argument definitions for entrypoints
    protected static final Map<String,String> argDefs1 = Map.ofEntries(
        entry("id", "ID!")
    );

    protected static final Map<String,String> argDefs2 = Map.ofEntries(
        entry("name","String"),
        entry("age","Int")
    );

    protected static final Map<String,String> argDefs3 = Map.ofEntries(
        entry("title","String"),
        entry("nr_pages","Int"),
        entry("genre","Genre")
    );

    // Query entrypoints (query type fields)
    protected static final GraphQLEntrypoint e1 = new GraphQLEntrypointImpl("author", argDefs1, "Author",GraphQLEntrypointType.SINGLE);
    protected static final GraphQLEntrypoint e2 = new GraphQLEntrypointImpl("authors", argDefs2, "Author",GraphQLEntrypointType.FILTERED);
    protected static final GraphQLEntrypoint e3 = new GraphQLEntrypointImpl("allAuthors", new HashMap<>(), "Author",GraphQLEntrypointType.FULL);
    protected static final GraphQLEntrypoint e4 = new GraphQLEntrypointImpl("book", argDefs1, "Book",GraphQLEntrypointType.SINGLE);
    protected static final GraphQLEntrypoint e5 = new GraphQLEntrypointImpl("books", argDefs3, "Book",GraphQLEntrypointType.FILTERED);
    protected static final GraphQLEntrypoint e6 = new GraphQLEntrypointImpl("allBooks", new HashMap<>(), "Book",GraphQLEntrypointType.FULL);

    // Translator, config and endpoint
    protected static final String classPrefix = "http://example.org/c/";
    protected static final String propertyPrefix = "http://example.org/p/";
    protected static final SPARQL2GraphQLTranslator translator = new SPARQL2GraphQLTranslatorImpl();
    protected static final GraphQL2RDFConfiguration config = new DefaultGraphQL2RDFConfiguration(classPrefix, propertyPrefix);
    protected static final GraphQLEndpoint endpoint = initializeGraphQLTestEndpoint();

    // Variables nodes
    protected static final Node var1 = NodeFactory.createVariable("author");
    protected static final Node var2 = NodeFactory.createVariable("book");
    protected static final Node var3 = NodeFactory.createVariable("name");
    protected static final Node var4 = NodeFactory.createVariable("title");
    protected static final Node var5 = NodeFactory.createVariable("id");
    protected static final Node var6 = NodeFactory.createVariable("s");
    protected static final Node var7 = NodeFactory.createVariable("p");
    protected static final Node var8 = NodeFactory.createVariable("o");
    protected static final Node var9 = NodeFactory.createVariable("author2");

    // URI nodes
    protected static final Node uri1 = NodeFactory.createURI(propertyPrefix + "id_of_Author");
    protected static final Node uri2 = NodeFactory.createURI(propertyPrefix +"name_of_Author");
    protected static final Node uri3 = NodeFactory.createURI(propertyPrefix +"age_of_Author");
    protected static final Node uri4 = NodeFactory.createURI(propertyPrefix +"books_of_Author");
    protected static final Node uri5 = NodeFactory.createURI(propertyPrefix +"id_of_Book");
    protected static final Node uri6 = NodeFactory.createURI(propertyPrefix +"title_of_Book");
    protected static final Node uri7 = NodeFactory.createURI(propertyPrefix +"nr_pages_of_Book");
    protected static final Node uri8 = NodeFactory.createURI(propertyPrefix +"genre_of_Book");
    protected static final Node uri9 = NodeFactory.createURI(propertyPrefix +"authors_of_Book");
    protected static final Node uri10 = NodeFactory.createURI(RDF.type.getURI());
    protected static final Node uri11 = NodeFactory.createURI(classPrefix + "Author");
    protected static final Node uri12 = NodeFactory.createURI(classPrefix + "Book");

    // Literal nodes
    protected static final Node lit1 = NodeFactory.createLiteral("auth3", XSDBaseStringType.XSDstring);
    protected static final Node lit2 = NodeFactory.createLiteral("book5", XSDBaseStringType.XSDstring);
    protected static final Node lit3 = NodeFactory.createLiteral("39", XSDBaseNumericType.XSDunsignedInt);
    protected static final Node lit4 = NodeFactory.createLiteral("William Shakespeare", XSDBaseStringType.XSDstring);
    protected static final Node lit5 = NodeFactory.createLiteral("MYSTERY", XSDBaseStringType.XSDstring);
    protected static final Node lit6 = NodeFactory.createLiteral("auth4", XSDBaseStringType.XSDstring);

    /**
     * Initializes a GraphQL endpoint for the tests
     */
    protected static GraphQLEndpoint initializeGraphQLTestEndpoint(){
        Map<String,GraphQLField> authorFields = new HashMap<>();
        Map<String,GraphQLField> bookFields = new HashMap<>();
        Map<String,Map<String,GraphQLField>> objectTypeToFields = new HashMap<>();
        Map<GraphQLEntrypointType,GraphQLEntrypoint> authorEntrypoints = new HashMap<>();
        Map<GraphQLEntrypointType,GraphQLEntrypoint> bookEntrypoints = new HashMap<>();
        Map<String,Map<GraphQLEntrypointType,GraphQLEntrypoint>> objectTypeToEntrypoint = new HashMap<>();
        authorFields.put("id",a1);
        authorFields.put("name",a2);
        authorFields.put("age",a3);
        authorFields.put("books",a4);
        bookFields.put("id",b1);
        bookFields.put("title",b2);
        bookFields.put("nr_pages",b3);
        bookFields.put("genre",b4);
        bookFields.put("authors",b5);

        objectTypeToFields.put("Author",authorFields);
        objectTypeToFields.put("Book",bookFields);

        authorEntrypoints.put(GraphQLEntrypointType.SINGLE,e1);
        authorEntrypoints.put(GraphQLEntrypointType.FILTERED,e2);
        authorEntrypoints.put(GraphQLEntrypointType.FULL,e3);

        bookEntrypoints.put(GraphQLEntrypointType.SINGLE,e4);
        bookEntrypoints.put(GraphQLEntrypointType.FILTERED,e5);
        bookEntrypoints.put(GraphQLEntrypointType.FULL,e6);

        objectTypeToEntrypoint.put("Author", authorEntrypoints);
        objectTypeToEntrypoint.put("Book", bookEntrypoints);
        final GraphQLInterface graphqlInterface = new GraphQLInterfaceImpl("");
        return new GraphQLEndpointImpl(objectTypeToFields, objectTypeToEntrypoint, graphqlInterface);
    }

    @Test
    public void test1(){
        /**
         * ?author p:id_of_Author    "auth3" .
         * ?author p:name_of_Author  ?name .
         * ?author p:books_of_Author ?book .
         * ?book   p:title_of_Book   ?title
         */
        Set<TriplePattern> tps = new HashSet<>();
        tps.add(new TriplePatternImpl(var1, uri1, lit1));
        tps.add(new TriplePatternImpl(var1, uri2, var3));
        tps.add(new TriplePatternImpl(var1, uri4, var2));
        tps.add(new TriplePatternImpl(var2, uri6, var4));
        final BGP bgp = new BGPImpl(tps);
        final GraphQLQuery translatedQuery = translator.translateBGP(bgp, config, endpoint);

        // Expected result
        final Set<String> fieldPaths = new HashSet<>();
        fieldPaths.add("ep_single0:author(id:$var0)/id_Author:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books_of_Author:books/id_Book:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books_of_Author:books/scalar_title_of_Book:title");
        fieldPaths.add("ep_single0:author(id:$var0)/scalar_id_of_Author:id");
        fieldPaths.add("ep_single0:author(id:$var0)/scalar_name_of_Author:name");
        final JsonObject argValues = new JsonObject();
        argValues.put("var0", "auth3");
        final Map<String,String> argDefinitions = new HashMap<>();
        argDefinitions.put("var0","ID!");
        final GraphQLQuery expectedQuery = new GraphQLQueryImpl(fieldPaths, argValues, argDefinitions);

        // Verify that translated and expected query are identical
        assertEquals(expectedQuery.getFieldPaths(), translatedQuery.getFieldPaths());
        assertEquals(expectedQuery.getArgumentValues(), translatedQuery.getArgumentValues());
        assertEquals(expectedQuery.getArgumentDefinitions(), translatedQuery.getArgumentDefinitions());
    }

    @Test
    public void test2(){
        /**
         * ?s ?p ?o .
         */
        final Set<TriplePattern> tps = new HashSet<>();
        tps.add(new TriplePatternImpl(var6, var7, var8));
        final BGP bgp = new BGPImpl(tps);
        final GraphQLQuery translatedQuery = translator.translateBGP(bgp, config, endpoint);

        // Expected Result
        final Set<String> fieldPaths = new HashSet<>();
        fieldPaths.add("ep_full0:allBooks/id_Book:id");
        fieldPaths.add("ep_full0:allBooks/object_authors_of_Book:authors/id_Author:id");
        fieldPaths.add("ep_full0:allBooks/scalar_genre_of_Book:genre");
        fieldPaths.add("ep_full0:allBooks/scalar_id_of_Book:id");
        fieldPaths.add("ep_full0:allBooks/scalar_nr_pages_of_Book:nr_pages");
        fieldPaths.add("ep_full0:allBooks/scalar_title_of_Book:title");
        fieldPaths.add("ep_full1:allAuthors/id_Author:id");
        fieldPaths.add("ep_full1:allAuthors/object_books_of_Author:books/id_Book:id");
        fieldPaths.add("ep_full1:allAuthors/scalar_age_of_Author:age");
        fieldPaths.add("ep_full1:allAuthors/scalar_id_of_Author:id");
        fieldPaths.add("ep_full1:allAuthors/scalar_name_of_Author:name");
        final JsonObject argValues = new JsonObject();
        final Map<String,String> argDefinitions = new HashMap<>();
        final GraphQLQuery expectedQuery = new GraphQLQueryImpl(fieldPaths, argValues, argDefinitions);

        // Verify that translated and expected query are identical
        assertEquals(expectedQuery.getFieldPaths(), translatedQuery.getFieldPaths());
        assertEquals(expectedQuery.getArgumentValues(), translatedQuery.getArgumentValues());
        assertEquals(expectedQuery.getArgumentDefinitions(), translatedQuery.getArgumentDefinitions());
    }

    @Test
    public void test3(){
        /**
         * ?author p:id_of_Author    "auth3" .
         * ?author p:books_of_Author ?book .
         * ?book   ?p                ?o .
         */
        final Set<TriplePattern> tps = new HashSet<>();
        tps.add(new TriplePatternImpl(var1, uri1, lit1));
        tps.add(new TriplePatternImpl(var1, uri4, var2));
        tps.add(new TriplePatternImpl(var2, var7, var8));
        final BGP bgp = new BGPImpl(tps);
        final GraphQLQuery translatedQuery = translator.translateBGP(bgp, config, endpoint);

        // Expected result
        final Set<String> fieldPaths = new HashSet<>();
        fieldPaths.add("ep_single0:author(id:$var0)/id_Author:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books_of_Author:books/id_Book:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books_of_Author:books/object_authors_of_Book:authors/id_Author:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books_of_Author:books/scalar_genre_of_Book:genre");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books_of_Author:books/scalar_id_of_Book:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books_of_Author:books/scalar_nr_pages_of_Book:nr_pages");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books_of_Author:books/scalar_title_of_Book:title");
        fieldPaths.add("ep_single0:author(id:$var0)/scalar_id_of_Author:id");
        final JsonObject argValues = new JsonObject();
        argValues.put("var0", "auth3");
        final Map<String,String> argDefinitions = new HashMap<>();
        argDefinitions.put("var0","ID!");
        final GraphQLQuery expectedQuery = new GraphQLQueryImpl(fieldPaths, argValues, argDefinitions);
        
        // Verify that translated and expected query are identical
        assertEquals(expectedQuery.getFieldPaths(), translatedQuery.getFieldPaths());
        assertEquals(expectedQuery.getArgumentValues(), translatedQuery.getArgumentValues());
        assertEquals(expectedQuery.getArgumentDefinitions(), translatedQuery.getArgumentDefinitions());
    }

    @Test
    public void test4(){
        /**
         * ?s rdf:type c:Author .
         * ?s ?p       ?o .
         */
        final Set<TriplePattern> tps = new HashSet<>();
        tps.add(new TriplePatternImpl(var6, uri10, uri11));
        tps.add(new TriplePatternImpl(var6, var7, var8));
        final BGP bgp = new BGPImpl(tps);
        final GraphQLQuery translatedQuery = translator.translateBGP(bgp, config, endpoint);

        // Expected result
        final Set<String> fieldPaths = new HashSet<>();
        fieldPaths.add("ep_full0:allAuthors/id_Author:id");
        fieldPaths.add("ep_full0:allAuthors/object_books_of_Author:books/id_Book:id");
        fieldPaths.add("ep_full0:allAuthors/scalar_age_of_Author:age");
        fieldPaths.add("ep_full0:allAuthors/scalar_id_of_Author:id");
        fieldPaths.add("ep_full0:allAuthors/scalar_name_of_Author:name");
        final JsonObject argValues = new JsonObject();
        final Map<String,String> argDefinitions = new HashMap<>();
        final GraphQLQuery expectedQuery = new GraphQLQueryImpl(fieldPaths, argValues, argDefinitions);
        
        // Verify that translated and expected query are identical
        assertEquals(expectedQuery.getFieldPaths(), translatedQuery.getFieldPaths());
        assertEquals(expectedQuery.getArgumentValues(), translatedQuery.getArgumentValues());
        assertEquals(expectedQuery.getArgumentDefinitions(), translatedQuery.getArgumentDefinitions());
    }

    @Test
    public void test5(){
        /**
         * ?author p:books_of_Author  ?book .
         * ?author p:name_of_Author   "William Shakespeare" .
         * ?book   p:nr_pages_of_Book 39 .
         * ?book   p:authors_of_Book  ?author .
         */
        final Set<TriplePattern> tps = new HashSet<>();
        tps.add(new TriplePatternImpl(var1, uri4, var2));
        tps.add(new TriplePatternImpl(var1, uri2, lit4));
        tps.add(new TriplePatternImpl(var2, uri7, lit3));
        tps.add(new TriplePatternImpl(var2, uri9, var1));
        final BGP bgp = new BGPImpl(tps);
        final GraphQLQuery translatedQuery = translator.translateBGP(bgp, config, endpoint);

        // Expected result
        final Set<String> fieldPaths = new HashSet<>();
        fieldPaths.add("ep_filtered0:authors(age:$var0,name:$var1)/id_Author:id");
        fieldPaths.add("ep_filtered0:authors(age:$var0,name:$var1)/object_books_of_Author:books/id_Book:id");
        fieldPaths.add("ep_filtered0:authors(age:$var0,name:$var1)/object_books_of_Author:books/object_authors_of_Book:authors/id_Author:id");
        fieldPaths.add("ep_filtered0:authors(age:$var0,name:$var1)/object_books_of_Author:books/scalar_nr_pages_of_Book:nr_pages");
        fieldPaths.add("ep_filtered0:authors(age:$var0,name:$var1)/scalar_name_of_Author:name");
        final JsonObject argValues = new JsonObject();
        argValues.put("var0", JsonNull.instance);
        argValues.put("var1", "William Shakespeare");
        final Map<String,String> argDefinitions = new HashMap<>();
        argDefinitions.put("var0", "Int");
        argDefinitions.put("var1", "String");
        final GraphQLQuery expectedQuery = new GraphQLQueryImpl(fieldPaths, argValues, argDefinitions);
        
        // Verify that translated and expected query are identical
        assertEquals(expectedQuery.getFieldPaths(), translatedQuery.getFieldPaths());
        assertEquals(expectedQuery.getArgumentValues(), translatedQuery.getArgumentValues());
        assertEquals(expectedQuery.getArgumentDefinitions(), translatedQuery.getArgumentDefinitions());
    }

    @Test
    public void test6(){
        /**
         * ?book p:genre_of_Book "MYSTERY" .
         * ?book p:title_of_Book ?title .
         */
        final Set<TriplePattern> tps = new HashSet<>();
        tps.add(new TriplePatternImpl(var2, uri8, lit5));
        tps.add(new TriplePatternImpl(var2, uri6, var4));
        final BGP bgp = new BGPImpl(tps);
        final GraphQLQuery translatedQuery = translator.translateBGP(bgp, config, endpoint);

        // Expected result
        final Set<String> fieldPaths = new HashSet<>();
        fieldPaths.add("ep_filtered0:books(genre:$var0,nr_pages:$var1,title:$var2)/id_Book:id");
        fieldPaths.add("ep_filtered0:books(genre:$var0,nr_pages:$var1,title:$var2)/scalar_genre_of_Book:genre");
        fieldPaths.add("ep_filtered0:books(genre:$var0,nr_pages:$var1,title:$var2)/scalar_title_of_Book:title");
        final JsonObject argValues = new JsonObject();
        argValues.put("var0", "MYSTERY");
        argValues.put("var1", JsonNull.instance);
        argValues.put("var2", JsonNull.instance);
        final Map<String,String> argDefinitions = new HashMap<>();
        argDefinitions.put("var0", "Genre");
        argDefinitions.put("var1", "Int");
        argDefinitions.put("var2", "String");
        final GraphQLQuery expectedQuery = new GraphQLQueryImpl(fieldPaths, argValues, argDefinitions);
        
        // Verify that translated and expected query are identical
        assertEquals(expectedQuery.getFieldPaths(), translatedQuery.getFieldPaths());
        assertEquals(expectedQuery.getArgumentValues(), translatedQuery.getArgumentValues());
        assertEquals(expectedQuery.getArgumentDefinitions(), translatedQuery.getArgumentDefinitions());
    }

    @Test
    public void test7(){
        /**
         * ?author  p:id_of_Author      "auth3";
         *          p:books_of_Author   ?book .
         * ?author2 p:id_of_Author      "auth4" ;
         *          p:books_of_Author   ?book .
         * ?book    p:title_of_Book     ?title .
         */

        final Set<TriplePattern> tps = new HashSet<>();
        tps.add(new TriplePatternImpl(var1,uri1,lit1));
        tps.add(new TriplePatternImpl(var1,uri4,var2));
        tps.add(new TriplePatternImpl(var9,uri1,lit6));
        tps.add(new TriplePatternImpl(var9,uri4,var2));
        tps.add(new TriplePatternImpl(var2,uri6,var4));
        final BGP bgp = new BGPImpl(tps);
        final GraphQLQuery translatedQuery = translator.translateBGP(bgp, config, endpoint);

        // Expected result
        final Set<String> fieldPaths = new HashSet<>();
        fieldPaths.add("ep_single0:author(id:$var0)/id_Author:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books_of_Author:books/id_Book:id");
        fieldPaths.add("ep_single0:author(id:$var0)/object_books_of_Author:books/scalar_title_of_Book:title");
        fieldPaths.add("ep_single0:author(id:$var0)/scalar_id_of_Author:id");
        fieldPaths.add("ep_single1:author(id:$var1)/id_Author:id");
        fieldPaths.add("ep_single1:author(id:$var1)/object_books_of_Author:books/id_Book:id");
        fieldPaths.add("ep_single1:author(id:$var1)/object_books_of_Author:books/scalar_title_of_Book:title");
        fieldPaths.add("ep_single1:author(id:$var1)/scalar_id_of_Author:id");
        final JsonObject argValues = new JsonObject();
        argValues.put("var0", "auth3");
        argValues.put("var1", "auth4");
        final Map<String,String> argDefinitions = new HashMap<>();
        argDefinitions.put("var0", "ID!");
        argDefinitions.put("var1", "ID!");
        final GraphQLQuery expectedQuery = new GraphQLQueryImpl(fieldPaths, argValues, argDefinitions);

        // Verify that translated and expected query are identical
        assertEquals(expectedQuery.getFieldPaths(), translatedQuery.getFieldPaths());
        assertEquals(expectedQuery.getArgumentValues(), translatedQuery.getArgumentValues());
        assertEquals(expectedQuery.getArgumentDefinitions(), translatedQuery.getArgumentDefinitions());
    }
}
