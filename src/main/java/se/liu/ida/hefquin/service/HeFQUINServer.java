package se.liu.ida.hefquin.service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.liu.ida.hefquin.engine.HeFQUINEngine;

@SpringBootApplication
@RestController
public class HeFQUINServer
{	
	private static HeFQUINEngine engine;

	public static void main( String[] args ) {
		SpringApplication.run(HeFQUINServer.class, args);
	}
	
	public HeFQUINServer( @Value("${SERVICE_FED_CONF}") String fedConfFilename,
	                      @Value("${SERVICE_ENGINE_CONF}") String engineConfFilename ) {
		engine = HeFQUINServerUtils.getEngine(fedConfFilename, engineConfFilename);
	}

	@PostMapping( value = "/sparql",
	              consumes = "application/sparql-query",
	              produces = { "application/sparql-results+xml",
	                           "application/sparql-results+json",
	                           "text/csv",
	                           "text/tsv" } )
	public String handleSparqlQuery( @RequestBody String query,
    	                             @RequestHeader Map<String, String> headers ) {
		String accept = headers.get("Accept");
		return execute(query, accept);
	}

	@PostMapping( value = "/sparql",
	              consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
	             produces = { "application/sparql-results+xml",
	                          "application/sparql-results+json",
	                          "text/csv",
	                          "text/tsv" } )
	public String executePostQueryUrlEncoded( @RequestParam Map<String, String> params,
		                                      @RequestHeader Map<String, String> headers ) {
		String query = params.get("query");
		String accept = headers.get("Accept");
		return execute(query, accept);
	}

	@GetMapping( value = "sparql",
	             produces = { "application/sparql-results+xml",
	                          "application/sparql-results+json",
	                          "text/csv",
	                          "text/tsv" } )
	public String executeGetQuery( @RequestParam(name = "query") String query,
	                               @RequestHeader Map<String, String> headers ) {
		String accept = headers.get("Accept");
		return execute(query, accept);
	}

	private String execute( String queryString, String mimeType ) {
		final Query query = QueryFactory.create(queryString);
		final ResultsFormat resultsFormat = HeFQUINServerUtils.convert(mimeType);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
		engine.executeQuery(query, resultsFormat, ps);
		ps.flush();
		ps.close();
		return baos.toString();
	}
}