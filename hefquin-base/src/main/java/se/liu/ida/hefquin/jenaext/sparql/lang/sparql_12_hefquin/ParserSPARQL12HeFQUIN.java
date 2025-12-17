package se.liu.ida.hefquin.jenaext.sparql.lang.sparql_12_hefquin;

import java.io.Reader;
import java.io.StringReader;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.lang.SPARQLParser;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;

import se.liu.ida.hefquin.jenaext.query.SyntaxForHeFQUIN;
import se.liu.ida.hefquin.jenaext.sparql.lang.sparql_12_hefquin.javacc.SPARQLParser12ForHeFQUIN;

public class ParserSPARQL12HeFQUIN extends SPARQLParser
{
	// The code in this file is copied from Apache Jena (specifically, from
	// the class org.apache.jena.sparql.lang.sparql_12.ParserSPARQL12), and
	// adapted to use SPARQLParser12ForHeFQUIN and syntaxSPARQL_12_HeFQUIN.
	//     -Olaf

    private interface Action { void exec(SPARQLParser12ForHeFQUIN parser) throws Exception; }

    @Override
    protected Query parse$(final Query query, String queryString) {
        query.setSyntax(SyntaxForHeFQUIN.syntaxSPARQL_12_HeFQUIN);
        Action action = (SPARQLParser12ForHeFQUIN parser) -> parser.QueryUnit();
        perform(query, queryString, action);
        return query;
    }

    public static Element parseElement(String string) {
        final Query query = new Query();
        Action action = (SPARQLParser12ForHeFQUIN parser) -> {
            Element el = parser.GroupGraphPattern();
            query.setQueryPattern(el);
        };
        perform(query, string, action);
        return query.getQueryPattern();
    }

    public static Template parseTemplate(String string) {
        final Query query = new Query();
        Action action = (SPARQLParser12ForHeFQUIN parser) -> {
            Template t = parser.ConstructTemplate();
            query.setConstructTemplate(t);
        };
        perform(query, string, action);
        return query.getConstructTemplate();
    }

    // All throwable handling.
    private static void perform(Query query, String string, Action action) {
        Reader in = new StringReader(string);
        SPARQLParser12ForHeFQUIN parser = new SPARQLParser12ForHeFQUIN(in);

        try {
            query.setStrict(true);
            parser.setQuery(query);
            action.exec(parser);
        } catch (se.liu.ida.hefquin.jenaext.sparql.lang.sparql_12_hefquin.javacc.ParseException ex) {
            throw new QueryParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn);
        } catch (se.liu.ida.hefquin.jenaext.sparql.lang.sparql_12_hefquin.javacc.TokenMgrError tErr) {
            // Last valid token : not the same as token error message - but this
            // should not happen
            int col = parser.token.endColumn;
            int line = parser.token.endLine;
            throw new QueryParseException(tErr.getMessage(), line, col);
        } catch (QueryException ex) {
            throw ex;
        } catch (JenaException ex) {
            throw new QueryException(ex.getMessage(), ex);
        } catch (Error err) {
            // The token stream can throw errors.
            throw new QueryParseException(err.getMessage(), err, -1, -1);
        } catch (Throwable th) {
            Log.warn(SPARQLParser12ForHeFQUIN.class, "Unexpected throwable: ", th);
            throw new QueryException(th.getMessage(), th);
        }
    }

}
