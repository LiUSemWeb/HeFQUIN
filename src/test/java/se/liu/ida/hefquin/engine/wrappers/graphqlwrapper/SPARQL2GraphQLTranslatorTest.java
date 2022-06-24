package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Map.entry;

import org.apache.jena.atlas.json.JsonNull;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
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
    protected static final Node uri1 = NodeFactory.createURI(config.mapFieldToProperty("Author", "id"));
    protected static final Node uri2 = NodeFactory.createURI(config.mapFieldToProperty("Author", "name"));
    protected static final Node uri3 = NodeFactory.createURI(config.mapFieldToProperty("Author", "age"));
    protected static final Node uri4 = NodeFactory.createURI(config.mapFieldToProperty("Author", "books"));
    protected static final Node uri5 = NodeFactory.createURI(config.mapFieldToProperty("Book", "id"));
    protected static final Node uri6 = NodeFactory.createURI(config.mapFieldToProperty("Book", "title"));
    protected static final Node uri7 = NodeFactory.createURI(config.mapFieldToProperty("Book", "nr_pages"));
    protected static final Node uri8 = NodeFactory.createURI(config.mapFieldToProperty("Book", "genre"));
    protected static final Node uri9 = NodeFactory.createURI(config.mapFieldToProperty("Book", "authors"));
    protected static final Node uri10 = NodeFactory.createURI(config.getClassMembershipURI());

    protected static final Node uri11 = NodeFactory.createURI(config.mapTypeToClass("Author"));
    protected static final Node uri12 = NodeFactory.createURI(config.mapTypeToClass("Book"));

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
        final Set<QueryEntrypointInfo> expectedQueryInfo = new HashSet<>();

        // Entrypoint 1
        final Set<String> fp = new HashSet<>();
        fp.add("/id_Author:id");
        fp.add("/object_books_of_Author:books/id_Book:id");
        fp.add("/object_books_of_Author:books/scalar_title_of_Book:title");
        fp.add("/scalar_id_of_Author:id");
        fp.add("/scalar_name_of_Author:name");
        final Map<String,JsonValue> argValues = Map.ofEntries(
            entry("id", new JsonString("auth3"))
        );
        final Map<String,String> argDefs = Map.ofEntries(
            entry("id", "ID!")
        );
        expectedQueryInfo.add(new QueryEntrypointInfo(fp, GraphQLEntrypointType.SINGLE, argValues, argDefs));

        assert(verifyQueryInformation(expectedQueryInfo, translatedQuery));
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

        // Expected result
        final Set<QueryEntrypointInfo> expectedQueryInfo = new HashSet<>();

        // Entrypoint 1
        final Set<String> fp1 = new HashSet<>();
        fp1.add("/id_Book:id");
        fp1.add("/object_authors_of_Book:authors/id_Author:id");
        fp1.add("/scalar_genre_of_Book:genre");
        fp1.add("/scalar_id_of_Book:id");
        fp1.add("/scalar_nr_pages_of_Book:nr_pages");
        fp1.add("/scalar_title_of_Book:title");
        final Map<String,JsonValue> argValues1 = new HashMap<>();
        final Map<String,String> argDefs1 = new HashMap<>();
        expectedQueryInfo.add(new QueryEntrypointInfo(fp1, GraphQLEntrypointType.FULL, argValues1, argDefs1));

        // Entrypoint 2
        final Set<String> fp2 = new HashSet<>();
        fp2.add("/id_Author:id");
        fp2.add("/object_books_of_Author:books/id_Book:id");
        fp2.add("/scalar_age_of_Author:age");
        fp2.add("/scalar_id_of_Author:id");
        fp2.add("/scalar_name_of_Author:name");
        final Map<String,JsonValue> argValues2 = new HashMap<>();
        final Map<String,String> argDefs2 = new HashMap<>();
        expectedQueryInfo.add(new QueryEntrypointInfo(fp2, GraphQLEntrypointType.FULL, argValues2, argDefs2));

        assert(verifyQueryInformation(expectedQueryInfo, translatedQuery));
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
        final Set<QueryEntrypointInfo> expectedQueryInfo = new HashSet<>();

        // Entrypoint 1
        final Set<String> fp = new HashSet<>();
        fp.add("/id_Author:id");
        fp.add("/object_books_of_Author:books/id_Book:id");
        fp.add("/object_books_of_Author:books/object_authors_of_Book:authors/id_Author:id");
        fp.add("/object_books_of_Author:books/scalar_genre_of_Book:genre");
        fp.add("/object_books_of_Author:books/scalar_id_of_Book:id");
        fp.add("/object_books_of_Author:books/scalar_nr_pages_of_Book:nr_pages");
        fp.add("/object_books_of_Author:books/scalar_title_of_Book:title");
        fp.add("/scalar_id_of_Author:id");
        final Map<String,JsonValue> argValues = Map.ofEntries(
            entry("id", new JsonString("auth3"))
        );
        final Map<String,String> argDefs = Map.ofEntries(
            entry("id", "ID!")
        );
        expectedQueryInfo.add(new QueryEntrypointInfo(fp, GraphQLEntrypointType.SINGLE, argValues, argDefs));

        assert(verifyQueryInformation(expectedQueryInfo, translatedQuery));
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
        final Set<QueryEntrypointInfo> expectedQueryInfo = new HashSet<>();

        // Entrypoint 1
        final Set<String> fp = new HashSet<>();
        fp.add("/id_Author:id");
        fp.add("/object_books_of_Author:books/id_Book:id");
        fp.add("/scalar_age_of_Author:age");
        fp.add("/scalar_id_of_Author:id");
        fp.add("/scalar_name_of_Author:name");
        final Map<String,JsonValue> argValues = new HashMap<>();
        final Map<String,String> argDefs = new HashMap<>();
        expectedQueryInfo.add(new QueryEntrypointInfo(fp, GraphQLEntrypointType.FULL, argValues, argDefs));

        assert(verifyQueryInformation(expectedQueryInfo, translatedQuery));
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
        final Set<QueryEntrypointInfo> expectedQueryInfo = new HashSet<>();

        // Entrypoint 1
        final Set<String> fp = new HashSet<>();
        fp.add("/id_Author:id");
        fp.add("/object_books_of_Author:books/id_Book:id");
        fp.add("/object_books_of_Author:books/object_authors_of_Book:authors/id_Author:id");
        fp.add("/object_books_of_Author:books/scalar_nr_pages_of_Book:nr_pages");
        fp.add("/scalar_name_of_Author:name");
        final Map<String,JsonValue> argValues = Map.ofEntries(
            entry("age", JsonNull.instance),
            entry("name", new JsonString("William Shakespeare"))
        );
        final Map<String,String> argDefs = Map.ofEntries(
            entry("age", "Int"),
            entry("name", "String")
        );

        expectedQueryInfo.add(new QueryEntrypointInfo(fp, GraphQLEntrypointType.FILTERED, argValues, argDefs));

        assert(verifyQueryInformation(expectedQueryInfo, translatedQuery));

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
        final Set<QueryEntrypointInfo> expectedQueryInfo = new HashSet<>();

        // Entrypoint 1
        final Set<String> fp = new HashSet<>();
        fp.add("/id_Book:id");
        fp.add("/scalar_genre_of_Book:genre");
        fp.add("/scalar_title_of_Book:title");
        final Map<String,JsonValue> argValues = Map.ofEntries(
            entry("genre", new JsonString("MYSTERY")),
            entry("nr_pages", JsonNull.instance),
            entry("title", JsonNull.instance)
        );
        final Map<String,String> argDefs = Map.ofEntries(
            entry("genre", "Genre"),
            entry("nr_pages", "Int"),
            entry("title", "String")
        );
        expectedQueryInfo.add(new QueryEntrypointInfo(fp, GraphQLEntrypointType.FILTERED, argValues, argDefs));

        assert(verifyQueryInformation(expectedQueryInfo, translatedQuery));
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
        final Set<QueryEntrypointInfo> expectedQueryInfo = new HashSet<>();

        // Entrypoint 1
        final Set<String> fp1 = new HashSet<>();
        fp1.add("/id_Author:id");
        fp1.add("/object_books_of_Author:books/id_Book:id");
        fp1.add("/object_books_of_Author:books/scalar_title_of_Book:title");
        fp1.add("/scalar_id_of_Author:id");
        final Map<String,JsonValue> argValues1 = Map.ofEntries(
            entry("id",new JsonString("auth3"))
        );
        final Map<String,String> argDefs1 = Map.ofEntries(
            entry("id", "ID!")
        );
        expectedQueryInfo.add(new QueryEntrypointInfo(fp1, GraphQLEntrypointType.SINGLE, argValues1, argDefs1));

        // Entrypoint 2
        final Set<String> fp2 = new HashSet<>();
        fp2.add("/id_Author:id");
        fp2.add("/object_books_of_Author:books/id_Book:id");
        fp2.add("/object_books_of_Author:books/scalar_title_of_Book:title");
        fp2.add("/scalar_id_of_Author:id");
        final Map<String,JsonValue> argValues2 = Map.ofEntries(
            entry("id",new JsonString("auth4"))
        );
        final Map<String,String> argDefs2 = Map.ofEntries(
            entry("id", "ID!")
        );
        expectedQueryInfo.add(new QueryEntrypointInfo(fp2, GraphQLEntrypointType.SINGLE, argValues2, argDefs2));

        assert(verifyQueryInformation(expectedQueryInfo, translatedQuery));
    }


    /**
     * Function used to verfify a Set of expected query information @param expected to the actual
     * translated query @param translatedQuery. 
     */
    private boolean verifyQueryInformation(final Set<QueryEntrypointInfo> expected, final GraphQLQuery translatedQuery){

        final Set<String> actualFieldPaths = translatedQuery.getFieldPaths();

        // Maps the entrypoint of an actual fieldPath to a set of all trimmed fieldPaths using that entrypoint
        final Map<String,Set<String>> seperatedFieldPaths = new HashMap<>();

        for(final String fieldPath : actualFieldPaths){

            // Seperate entrypoint from fieldPath
            final int splitIndex = fieldPath.indexOf('/');
            final String entrypointPath = fieldPath.substring(0, splitIndex);
            final String trimmedFieldPath = fieldPath.substring(splitIndex);
            final Set<String> trimmedFieldPaths;

            if(seperatedFieldPaths.containsKey(entrypointPath)){
                trimmedFieldPaths = seperatedFieldPaths.get(entrypointPath);
            }
            else{
                trimmedFieldPaths = new HashSet<>();
                seperatedFieldPaths.put(entrypointPath, trimmedFieldPaths);
            }

            trimmedFieldPaths.add(trimmedFieldPath);
        }

        // Create QueryEntrypointInfo objects from the actual query information
        final JsonObject actualArgValues = translatedQuery.getArgumentValues();
        final Map<String,String> actualArgDefinitions = translatedQuery.getArgumentDefinitions();
        final Set<QueryEntrypointInfo> actual = new HashSet<>();

        for(final String entrypointPath : seperatedFieldPaths.keySet()){
            // Retrieve the alias for the entrypoint
            final int aliasIndex = entrypointPath.indexOf(':');
            final String epAlias = entrypointPath.substring(0, aliasIndex).toLowerCase();
            final GraphQLEntrypointType epType;

            // Decide on the epType
            if(epAlias.contains(GraphQLEntrypointType.SINGLE.toString().toLowerCase())){
                epType = GraphQLEntrypointType.SINGLE;
            }
            else if(epAlias.contains(GraphQLEntrypointType.FILTERED.toString().toLowerCase())){
                epType = GraphQLEntrypointType.FILTERED;
            }
            else if(epAlias.contains(GraphQLEntrypointType.FULL.toString().toLowerCase())){
                epType = GraphQLEntrypointType.FULL;
            }
            else{
                return false;
            }

            final Map<String,JsonValue> entrypointArgValues = new HashMap<>();
            final Map<String,String> entrypointArgDefinitions = new HashMap<>();

            final int argIndexStart = entrypointPath.indexOf('(');
            final int argIndexEnd = entrypointPath.indexOf(')');

            // If the entrypointPath has no arguments
            if(argIndexStart < 0 || argIndexEnd < 0){
                actual.add(new QueryEntrypointInfo(seperatedFieldPaths.get(entrypointPath), epType, entrypointArgValues, entrypointArgDefinitions));
                continue;
            }

            // Parse arguments of the entrypointPath
            final String argsPath = entrypointPath.substring(argIndexStart + 1, argIndexEnd).replaceAll("\\s", "");
            for(final String arg : argsPath.split(",")){
                final int i = arg.indexOf(':');
                final String argName = arg.substring(0, i);
                final String varName = arg.substring(i+2);
                final String argDefinition = actualArgDefinitions.get(varName);
                final JsonValue argValue = actualArgValues.get(varName);

                entrypointArgValues.put(argName, argValue);
                entrypointArgDefinitions.put(argName,argDefinition);
            }

            actual.add(new QueryEntrypointInfo(seperatedFieldPaths.get(entrypointPath), epType, entrypointArgValues, entrypointArgDefinitions));
        }

        return expected.equals(actual);
    }

    /**
     * Helper class used to verify expected results against actual query results.
     * Contains known information about a certain entrypoint in the query.
     * (Unknown informations are entrypoint and variable names)
     */
    private class QueryEntrypointInfo {
        final protected Set<String> trimmedFieldPaths;
        final protected GraphQLEntrypointType epType;
        final protected Map<String,JsonValue> argValues;
        final protected Map<String,String> argDefinitions;

        public QueryEntrypointInfo (final Set<String> trimmedFieldPaths,
                                    final GraphQLEntrypointType epType,
                                    final Map<String,JsonValue> argValues,
                                    final Map<String,String> argDefinitions){
            this.trimmedFieldPaths = trimmedFieldPaths;
            this.epType = epType;
            this.argValues = argValues;
            this.argDefinitions = argDefinitions;
        }

        @Override
        public boolean equals(final Object o){
            if(this == o){
                return true;
            }
    
            if(!(o instanceof QueryEntrypointInfo)){
                return false;
            }
    
            final QueryEntrypointInfo that = (QueryEntrypointInfo) o;

            return  trimmedFieldPaths.equals(that.trimmedFieldPaths) &&
                    epType.equals(that.epType) &&
                    argValues.equals(that.argValues) &&
                    argDefinitions.equals(that.argDefinitions);
        }

        @Override
        public int hashCode(){
            return Objects.hash(trimmedFieldPaths,epType,argValues,argDefinitions);
        }
    }
}
