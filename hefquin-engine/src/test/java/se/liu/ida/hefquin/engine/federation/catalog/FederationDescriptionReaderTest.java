package se.liu.ida.hefquin.engine.federation.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RiotNotFoundException;
import org.junit.Ignore;
import org.junit.Test;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.engine.federation.access.TPFInterface;

public class FederationDescriptionReaderTest
{
	@Test
	public void twoFMs() {
		final String turtle =
				  "PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:     <http://example.org/>\n"
				+ "\n"
				+ "ex:dbpediaSPARQL\n"
				+ "      a            fd:FederationMember ;\n"
				+ "      fd:interface [ a                  fd:SPARQLEndpointInterface ;\n"
				+ "                     fd:endpointAddress <http://dbpedia.org/sparql> ] .\n"
				+ "\n"
				+ "ex:dbpediaTPF\n"
				+ "      a            fd:FederationMember ;\n"
				+ "      fd:interface [ a                         fd:TPFInterface ;\n"
				+ "                     fd:exampleFragmentAddress <http://fragments.dbpedia.org/2016-04/en> ] .";

		final Model fd = ModelFactory.createDefaultModel();

		final RDFParserBuilder b = RDFParser.fromString(turtle);
		b.lang( Lang.TURTLE );
		b.parse(fd);

		final FederationCatalog cat = FederationDescriptionReader.readFromModel(fd);

		assertEquals( 2, cat.getAllFederationMembers().size() );

		final FederationMember fm1 = cat.getFederationMemberByURI("http://dbpedia.org/sparql");
		assertTrue( fm1.getVocabularyMapping() == null );
		assertTrue( fm1 instanceof SPARQLEndpoint );
		assertTrue( fm1.getInterface() instanceof SPARQLEndpointInterface );
		assertEquals( "http://dbpedia.org/sparql", ((SPARQLEndpointInterface) fm1.getInterface()).getURL() );

		final FederationMember fm2 = cat.getFederationMemberByURI("http://fragments.dbpedia.org/2016-04/en");
		assertTrue( fm2.getVocabularyMapping() == null );
		assertTrue( fm2 instanceof TPFServer );
		assertTrue( fm2.getInterface() instanceof TPFInterface );
	}

	@Test
	public void sparqlFMWithTwoEndpoints() {
		final String turtle =
				"PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
						+ "PREFIX ex:     <http://example.org/>\n"
						+ "\n"
						+ "ex:dbpediaSPARQL\n"
						+ "      a            fd:FederationMember ;\n"
						+ "      fd:interface [ a                  fd:SPARQLEndpointInterface ;\n"
						+ "                     fd:endpointAddress <http://dbpedia.org/sparql>, <http://localhost:7474/db/neo4j/tx> ];\n"
						+ "                     fd:vocabularyMappingsFile \"dbpedia/vocabularyMappings.nt\".";

		final Model fd = ModelFactory.createDefaultModel();
		FederationDescriptionReader.readFromModel(fd);
		final RDFParserBuilder b = RDFParser.fromString(turtle);
		b.lang( Lang.TURTLE );
		b.parse(fd);

		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			FederationDescriptionReader.readFromModel(fd);
		});
		// Test that the error message is correct
		final String expectedErrorMessage = "More Than One SPARQL endpointAddress!";
		final String actualErrorMessage = exception.getMessage();
		assertEquals(expectedErrorMessage, actualErrorMessage);
	}

	@Test
	public void sparqlFMWithoutRequiredProperty() {
		final String turtle =
				"PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
						+ "PREFIX ex:     <http://example.org/>\n"
						+ "\n"
						+ "ex:dbpediaSPARQL\n"
						+ "      a            fd:FederationMember ;\n"
						+ "      fd:interface [ a                  fd:SPARQLEndpointInterface ;\n"
						+ "                     fd:exampleFragmentAddress <http://dbpedia.org/sparql>, <http://localhost:7474/db/neo4j/tx> ].\n";

		final Model fd = ModelFactory.createDefaultModel();

		final RDFParserBuilder b = RDFParser.fromString(turtle);
		b.lang( Lang.TURTLE );
		b.parse(fd);

		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			FederationDescriptionReader.readFromModel(fd);
		});
		// Test that the error message is correct
		final String expectedErrorMessage = "SPARQL endpointAddress is required!";
		final String actualErrorMessage = exception.getMessage();
		assertEquals(expectedErrorMessage, actualErrorMessage);
	}

	@Test
	public void tpfFMWithTwoEndpoints() {
		final String turtle =
				"PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
						+ "PREFIX ex:     <http://example.org/>\n"
						+ "\n"
						+ "ex:dbpediaTPF\n"
						+ "      a            fd:FederationMember ;\n"
						+ "      fd:interface [ a                  fd:TPFInterface ;\n"
						+ "                     fd:exampleFragmentAddress <http://fragments.dbpedia.org/2016-04/en>, <http://localhost:7474/db/neo4j/tx> ].\n";

		final Model fd = ModelFactory.createDefaultModel();

		final RDFParserBuilder b = RDFParser.fromString(turtle);
		b.lang( Lang.TURTLE );
		b.parse(fd);

		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			FederationDescriptionReader.readFromModel(fd);
		});
		// Test that the error message is correct
		final String expectedErrorMessage = "More Than One TPF exampleFragmentAddress!";
		final String actualErrorMessage = exception.getMessage();
		assertEquals(expectedErrorMessage, actualErrorMessage);
	}

	@Test
	public void tpfFMWithoutRequiredProperty() {
		final String turtle =
				"PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
						+ "PREFIX ex:     <http://example.org/>\n"
						+ "\n"
						+ "ex:dbpediaTPF\n"
						+ "      a            fd:FederationMember ;\n"
						+ "      fd:interface [ a                  fd:TPFInterface ;\n"
						+ "                     fd:endpointAddress <http://fragments.dbpedia.org/2016-04/en>, <http://localhost:7474/db/neo4j/tx> ].\n";

		final Model fd = ModelFactory.createDefaultModel();

		final RDFParserBuilder b = RDFParser.fromString(turtle);
		b.lang( Lang.TURTLE );
		b.parse(fd);

		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			FederationDescriptionReader.readFromModel(fd);
		});
		// Test that the error message is correct
		final String expectedErrorMessage = "TPF exampleFragmentAddress is required!";
		final String actualErrorMessage = exception.getMessage();
		assertEquals(expectedErrorMessage, actualErrorMessage);
	}

	@Test
	public void vocabularyMappingFileIsLoadedFromClasspathResource() {
		final String turtle =
				"PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
						+ "PREFIX ex:     <http://example.org/>\n"
						+ "\n"
						+ "ex:dbpediaSPARQL\n"
						+ "      a            fd:FederationMember ;\n"
						+ "      fd:interface [ a                  fd:SPARQLEndpointInterface ;\n"
						+ "                     fd:endpointAddress <http://dbpedia.org/sparql> ];\n"
						+ "                     fd:vocabularyMappingsFile \"dbpedia/vocabularyMappings.nt\".";

		final Model fd = ModelFactory.createDefaultModel();
		final RDFParserBuilder b = RDFParser.fromString( turtle );
		b.lang( Lang.TURTLE );
		b.parse( fd );

		final FederationCatalog cat = FederationDescriptionReader.readFromModel( fd );
		final FederationMember fm = cat.getFederationMemberByURI( "http://dbpedia.org/sparql" );
		assertNotNull( fm.getVocabularyMapping() );
	}

	@Test
	public void vocabularyMappingFileIsLoadedFromLocalPath() {
		final String turtle =
				"PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
						+ "PREFIX ex:     <http://example.org/>\n"
						+ "\n"
						+ "ex:dbpediaSPARQL\n"
						+ "      a            fd:FederationMember ;\n"
						+ "      fd:interface [ a                  fd:SPARQLEndpointInterface ;\n"
						+ "                     fd:endpointAddress <http://dbpedia.org/sparql> ];\n"
						+ "                     fd:vocabularyMappingsFile \"src/test/resources/dbpedia/vocabularyMappings.nt\".";

		final Model fd = ModelFactory.createDefaultModel();
		final RDFParserBuilder b = RDFParser.fromString( turtle );
		b.lang( Lang.TURTLE );
		b.parse( fd );

		final FederationCatalog cat = FederationDescriptionReader.readFromModel( fd );
		final FederationMember fm = cat.getFederationMemberByURI( "http://dbpedia.org/sparql" );
		assertNotNull( fm.getVocabularyMapping() );
	}

	@Test
	public void missingVocabularyMappingFileThrowsExpectedException() {
		final String turtle =
				"PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
						+ "PREFIX ex:     <http://example.org/>\n"
						+ "\n"
						+ "ex:dbpediaSPARQL\n"
						+ "      a            fd:FederationMember ;\n"
						+ "      fd:interface [ a                  fd:SPARQLEndpointInterface ;\n"
						+ "                     fd:endpointAddress <http://dbpedia.org/sparql> ];\n"
						+ "                     fd:vocabularyMappingsFile \"dummy/vocab.nt\".";

		final Model fd = ModelFactory.createDefaultModel();
		final RDFParserBuilder b = RDFParser.fromString( turtle );
		b.lang( Lang.TURTLE );
		b.parse( fd );

		final Exception exception = assertThrows( IllegalArgumentException.class, () -> {
			FederationDescriptionReader.readFromModel( fd );
		} );


		assertTrue( "Expected cause to be RiotNotFoundException, but was: " + exception.getCause(),
			exception.getCause() instanceof RiotNotFoundException );

		// Test that the error message is correct
		final String expectedErrorMessage = "Not found: dummy/vocab.nt";
		final String actualErrorMessage = exception.getCause().getMessage();
		assertEquals( expectedErrorMessage, actualErrorMessage );
	}

}
