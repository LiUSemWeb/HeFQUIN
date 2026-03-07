package se.liu.ida.hefquin.engine.wrappers.graphql;

import static java.util.Map.entry;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sys.JenaSystem;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLEntrypointImpl;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLFieldImpl;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.impl.GraphQLSchemaImpl;
import se.liu.ida.hefquin.engine.wrappers.graphql.impl.DefaultGraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphql.impl.GraphQLSolutionGraphSolverImpl;
import se.liu.ida.hefquin.engine.wrappers.graphql.impl.JSON2SolutionGraphConverterImpl;
import se.liu.ida.hefquin.engine.wrappers.graphql.impl.SPARQL2GraphQLTranslatorImpl;

/**
 * Collection of useful data structures for when testing GraphQL wrapper
 * modules.
 */
public class GraphQLWrapperTestBase {

    static { JenaSystem.init(); }

    // Author test fields
    protected final GraphQLField a1 = new GraphQLFieldImpl("id", "ID!", GraphQLFieldType.SCALAR);
    protected final GraphQLField a2 = new GraphQLFieldImpl("name", "String!", GraphQLFieldType.SCALAR);
    protected final GraphQLField a3 = new GraphQLFieldImpl("age", "Int!", GraphQLFieldType.SCALAR);
    protected final GraphQLField a4 = new GraphQLFieldImpl("books", "Book", GraphQLFieldType.OBJECT);

    // Book test fields
    protected final GraphQLField b1 = new GraphQLFieldImpl("id", "ID!", GraphQLFieldType.SCALAR);
    protected final GraphQLField b2 = new GraphQLFieldImpl("title", "String!", GraphQLFieldType.SCALAR);
    protected final GraphQLField b3 = new GraphQLFieldImpl("nr_pages", "Int!", GraphQLFieldType.SCALAR);
    protected final GraphQLField b4 = new GraphQLFieldImpl("genre", "Genre", GraphQLFieldType.SCALAR);
    protected final GraphQLField b5 = new GraphQLFieldImpl("authors", "Author", GraphQLFieldType.OBJECT);

    // Argument definitions for entrypoints
    protected final Map<String, String> argDefs1 = Map.ofEntries(entry("id", "ID!"));

    protected final Map<String, String> argDefs2 = Map.ofEntries(entry("name", "String"), entry("age", "Int"));

    protected final Map<String, String> argDefs3 = Map.ofEntries(entry("title", "String"),
            entry("nr_pages", "Int"), entry("genre", "Genre"));

    // Query entrypoints (query type fields)
    protected final GraphQLEntrypoint e1 = new GraphQLEntrypointImpl("author", argDefs1, "Author",
            GraphQLEntrypointType.SINGLE);
    protected final GraphQLEntrypoint e2 = new GraphQLEntrypointImpl("authors", argDefs2, "Author",
            GraphQLEntrypointType.FILTERED);
    protected final GraphQLEntrypoint e3 = new GraphQLEntrypointImpl("allAuthors", new HashMap<>(), "Author",
            GraphQLEntrypointType.FULL);
    protected final GraphQLEntrypoint e4 = new GraphQLEntrypointImpl("book", argDefs1, "Book",
            GraphQLEntrypointType.SINGLE);
    protected final GraphQLEntrypoint e5 = new GraphQLEntrypointImpl("books", argDefs3, "Book",
            GraphQLEntrypointType.FILTERED);
    protected final GraphQLEntrypoint e6 = new GraphQLEntrypointImpl("allBooks", new HashMap<>(), "Book",
            GraphQLEntrypointType.FULL);

    // Translator, config and schema
    protected final String classPrefix = "http://example.org/c/";
    protected final String propertyPrefix = "http://example.org/p/";
    protected final SPARQL2GraphQLTranslator translator = new SPARQL2GraphQLTranslatorImpl();
    protected final GraphQL2RDFConfiguration config = new DefaultGraphQL2RDFConfiguration(classPrefix,
            propertyPrefix);
    protected final GraphQLSchema schema = initializeGraphQLTestSchema();
    protected final JSON2SolutionGraphConverter jsonTranslator = new JSON2SolutionGraphConverterImpl(config, schema);
    protected final GraphQLSolutionGraphSolver solutionGraphTranslator = new GraphQLSolutionGraphSolverImpl();

    // Variables nodes
    protected final Node var1 = NodeFactory.createVariable("author");
    protected final Node var2 = NodeFactory.createVariable("book");
    protected final Node var3 = NodeFactory.createVariable("name");
    protected final Node var4 = NodeFactory.createVariable("title");
    protected final Node var5 = NodeFactory.createVariable("id");
    protected final Node var6 = NodeFactory.createVariable("s");
    protected final Node var7 = NodeFactory.createVariable("p");
    protected final Node var8 = NodeFactory.createVariable("o");
    protected final Node var9 = NodeFactory.createVariable("author2");

    // URI nodes
    protected final Node uri1 = NodeFactory.createURI(config.mapFieldToProperty("Author", "id"));
    protected final Node uri2 = NodeFactory.createURI(config.mapFieldToProperty("Author", "name"));
    protected final Node uri3 = NodeFactory.createURI(config.mapFieldToProperty("Author", "age"));
    protected final Node uri4 = NodeFactory.createURI(config.mapFieldToProperty("Author", "books"));
    protected final Node uri5 = NodeFactory.createURI(config.mapFieldToProperty("Book", "id"));
    protected final Node uri6 = NodeFactory.createURI(config.mapFieldToProperty("Book", "title"));
    protected final Node uri7 = NodeFactory.createURI(config.mapFieldToProperty("Book", "nr_pages"));
    protected final Node uri8 = NodeFactory.createURI(config.mapFieldToProperty("Book", "genre"));
    protected final Node uri9 = NodeFactory.createURI(config.mapFieldToProperty("Book", "authors"));
    protected final Node uri10 = NodeFactory.createURI(config.getClassMembershipURI());

    protected final Node uri11 = NodeFactory.createURI(config.mapTypeToClass("Author"));
    protected final Node uri12 = NodeFactory.createURI(config.mapTypeToClass("Book"));

    // Literal nodes
    protected final Node lit1 = NodeFactory.createLiteralDT("auth3", XSDBaseStringType.XSDstring);
    protected final Node lit2 = NodeFactory.createLiteralDT("book5", XSDBaseStringType.XSDstring);
    protected final Node lit3 = NodeFactory.createLiteralDT("39", XSDBaseNumericType.XSDunsignedInt);
    protected final Node lit4 = NodeFactory.createLiteralDT("William Shakespeare", XSDBaseStringType.XSDstring);
    protected final Node lit5 = NodeFactory.createLiteralDT("MYSTERY", XSDBaseStringType.XSDstring);
    protected final Node lit6 = NodeFactory.createLiteralDT("auth4", XSDBaseStringType.XSDstring);

    /**
     * Initializes a GraphQL endpoint for the tests
     */
    protected GraphQLSchema initializeGraphQLTestSchema(){
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

        return new GraphQLSchemaImpl(objectTypeToFields, objectTypeToEntrypoint);
    }
}
