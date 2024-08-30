package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLSchemaImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.DefaultGraphQL2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.JSON2SolutionGraphConverterImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.SPARQL2GraphQLTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl.GraphQLSolutionGraphSolverImpl;

/**
 * Collection of useful data structures for when testing GraphQL wrapper
 * modules.
 */
public class GraphQLWrapperTestBase {
    // Author test fields
    protected static final GraphQLField a1 = new GraphQLFieldImpl("id", "ID!", GraphQLFieldType.SCALAR);
    protected static final GraphQLField a2 = new GraphQLFieldImpl("name", "String!", GraphQLFieldType.SCALAR);
    protected static final GraphQLField a3 = new GraphQLFieldImpl("age", "Int!", GraphQLFieldType.SCALAR);
    protected static final GraphQLField a4 = new GraphQLFieldImpl("books", "Book", GraphQLFieldType.OBJECT);

    // Book test fields
    protected static final GraphQLField b1 = new GraphQLFieldImpl("id", "ID!", GraphQLFieldType.SCALAR);
    protected static final GraphQLField b2 = new GraphQLFieldImpl("title", "String!", GraphQLFieldType.SCALAR);
    protected static final GraphQLField b3 = new GraphQLFieldImpl("nr_pages", "Int!", GraphQLFieldType.SCALAR);
    protected static final GraphQLField b4 = new GraphQLFieldImpl("genre", "Genre", GraphQLFieldType.SCALAR);
    protected static final GraphQLField b5 = new GraphQLFieldImpl("authors", "Author", GraphQLFieldType.OBJECT);

    // Argument definitions for entrypoints
    protected static final Map<String, String> argDefs1 = Map.ofEntries(entry("id", "ID!"));

    protected static final Map<String, String> argDefs2 = Map.ofEntries(entry("name", "String"), entry("age", "Int"));

    protected static final Map<String, String> argDefs3 = Map.ofEntries(entry("title", "String"),
            entry("nr_pages", "Int"), entry("genre", "Genre"));

    // Query entrypoints (query type fields)
    protected static final GraphQLEntrypoint e1 = new GraphQLEntrypointImpl("author", argDefs1, "Author",
            GraphQLEntrypointType.SINGLE);
    protected static final GraphQLEntrypoint e2 = new GraphQLEntrypointImpl("authors", argDefs2, "Author",
            GraphQLEntrypointType.FILTERED);
    protected static final GraphQLEntrypoint e3 = new GraphQLEntrypointImpl("allAuthors", new HashMap<>(), "Author",
            GraphQLEntrypointType.FULL);
    protected static final GraphQLEntrypoint e4 = new GraphQLEntrypointImpl("book", argDefs1, "Book",
            GraphQLEntrypointType.SINGLE);
    protected static final GraphQLEntrypoint e5 = new GraphQLEntrypointImpl("books", argDefs3, "Book",
            GraphQLEntrypointType.FILTERED);
    protected static final GraphQLEntrypoint e6 = new GraphQLEntrypointImpl("allBooks", new HashMap<>(), "Book",
            GraphQLEntrypointType.FULL);

    // Translator, config and schema
    protected static final String classPrefix = "http://example.org/c/";
    protected static final String propertyPrefix = "http://example.org/p/";
    protected static final SPARQL2GraphQLTranslator translator = new SPARQL2GraphQLTranslatorImpl();
    protected static final GraphQL2RDFConfiguration config = new DefaultGraphQL2RDFConfiguration(classPrefix,
            propertyPrefix);
    protected static final GraphQLSchema schema = initializeGraphQLTestSchema();
    protected static final JSON2SolutionGraphConverter jsonTranslator = new JSON2SolutionGraphConverterImpl(config, schema);
    protected static final GraphQLSolutionGraphSolver solutionGraphTranslator = new GraphQLSolutionGraphSolverImpl();

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
    protected static GraphQLSchema initializeGraphQLTestSchema(){
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
