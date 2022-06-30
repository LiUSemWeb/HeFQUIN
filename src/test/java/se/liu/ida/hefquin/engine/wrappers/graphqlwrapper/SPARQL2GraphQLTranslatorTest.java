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
import org.junit.Test;

import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;

/**
 * Verifies functionality of SPARQL2GraphQLTranslatorImpl
 */
public class SPARQL2GraphQLTranslatorTest extends GraphQLWrapperTestBase {

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
        fp.add("/object_books:books/id_Book:id");
        fp.add("/object_books:books/scalar_title:title");
        fp.add("/scalar_id:id");
        fp.add("/scalar_name:name");
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
        fp1.add("/object_authors:authors/id_Author:id");
        fp1.add("/scalar_genre:genre");
        fp1.add("/scalar_id:id");
        fp1.add("/scalar_nr_pages:nr_pages");
        fp1.add("/scalar_title:title");
        final Map<String,JsonValue> argValues1 = new HashMap<>();
        final Map<String,String> argDefs1 = new HashMap<>();
        expectedQueryInfo.add(new QueryEntrypointInfo(fp1, GraphQLEntrypointType.FULL, argValues1, argDefs1));

        // Entrypoint 2
        final Set<String> fp2 = new HashSet<>();
        fp2.add("/id_Author:id");
        fp2.add("/object_books:books/id_Book:id");
        fp2.add("/scalar_age:age");
        fp2.add("/scalar_id:id");
        fp2.add("/scalar_name:name");
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
        fp.add("/object_books:books/id_Book:id");
        fp.add("/object_books:books/object_authors:authors/id_Author:id");
        fp.add("/object_books:books/scalar_genre:genre");
        fp.add("/object_books:books/scalar_id:id");
        fp.add("/object_books:books/scalar_nr_pages:nr_pages");
        fp.add("/object_books:books/scalar_title:title");
        fp.add("/scalar_id:id");
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
        fp.add("/object_books:books/id_Book:id");
        fp.add("/scalar_age:age");
        fp.add("/scalar_id:id");
        fp.add("/scalar_name:name");
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
        fp.add("/object_books:books/id_Book:id");
        fp.add("/object_books:books/object_authors:authors/id_Author:id");
        fp.add("/object_books:books/scalar_nr_pages:nr_pages");
        fp.add("/scalar_name:name");
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
        fp.add("/scalar_genre:genre");
        fp.add("/scalar_title:title");
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
        fp1.add("/object_books:books/id_Book:id");
        fp1.add("/object_books:books/scalar_title:title");
        fp1.add("/scalar_id:id");
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
        fp2.add("/object_books:books/id_Book:id");
        fp2.add("/object_books:books/scalar_title:title");
        fp2.add("/scalar_id:id");
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
