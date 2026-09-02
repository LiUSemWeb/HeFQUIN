package se.liu.ida.hefquin.federation.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RiotNotFoundException;
import org.junit.Test;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.authentication.AuthenticationInformation;
import se.liu.ida.hefquin.federation.authentication.BasicAuthenticationInformation;
import se.liu.ida.hefquin.federation.authentication.BearerAuthenticationInformation;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

public class FederationDescriptionReaderTest
{
	@Test
	public void twoFMs() {
		final String turtle =
				  "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:     <http://example.org/>\n"
				+ "\n"
				+ "ex:dbpediaSPARQL a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://dbpedia.org/sparql\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                    fd:FixedEndpointInterface ;\n"
				+ "                     fd:supportedProtocol fd:SPARQLProtocol ;\n"
				+ "                     fd:endpointAddress   \"http://dbpedia.org/sparql\"^^xsd:anyURI ] .\n"
				+ "\n"
				+ "ex:dbpediaTPF a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://fragments.dbpedia.org/2016-04/en\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                         fd:FragmentInterface ;\n"
				+ "                     fd:supportedProtocol      fd:TPFProtocol ;\n"
				+ "                     fd:exampleFragmentAddress \"http://fragments.dbpedia.org/2016-04/en\"^^xsd:anyURI ] .";

		final Model fd = ModelFactory.createDefaultModel();

		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
		b.parse(fd);

		final FederationCatalog cat = FederationDescriptionReader.readFromModel(fd);

		assertEquals( 2, cat.getAllFederationMembers().size() );

		final FederationMember fm1 = cat.getFederationMemberByURI("http://dbpedia.org/sparql");
		assertTrue( fm1 instanceof SPARQLEndpoint );
		assertNull( ((SPARQLEndpoint) fm1).getVocabularyMapping() );
		assertEquals( "http://dbpedia.org/sparql", ((SPARQLEndpoint) fm1).getURL() );

		final FederationMember fm2 = cat.getFederationMemberByURI("http://fragments.dbpedia.org/2016-04/en");
		assertTrue( fm2 instanceof TPFServer );
		assertNull( ((TPFServer) fm2).getVocabularyMapping() );
	}

	@Test
	public void sparqlFMWithTwoEndpoints() {
		final String turtle =
				  "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:     <http://example.org/>\n"
				+ "\n"
				+ "ex:dbpediaSPARQL a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://dbpedia.org/sparql\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                         fd:FixedEndpointInterface ;\n"
				+ "                     fd:supportedProtocol      fd:SPARQLProtocol ;\n"
				+ "                     fd:endpointAddress        \"http://dbpedia.org/sparql\"^^xsd:anyURI, \"http://localhost:7474/db/neo4j/tx\"^^xsd:anyURI ] ;\n"
				+ "                     fd:vocabularyMappingsFile \"dbpedia/vocabularyMappings.nt\" .";

		final Model fd = ModelFactory.createDefaultModel();
		FederationDescriptionReader.readFromModel(fd);
		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
		b.parse(fd);

		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			FederationDescriptionReader.readFromModel(fd);
		});
		// Test that the error message is correct
		final String expectedErrorMessage = "More than one SPARQL endpointAddress!";
		final String actualErrorMessage = exception.getMessage();
		assertEquals(expectedErrorMessage, actualErrorMessage);
	}

	@Test
	public void sparqlFMWithoutRequiredProperty() {
		final String turtle =
				  "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:     <http://example.org/>\n"
				+ "\n"
				+ "ex:dbpediaSPARQL a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://dbpedia.org/sparql\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                         fd:FixedEndpointInterface ;\n"
				+ "                     fd:supportedProtocol      fd:SPARQLProtocol ;\n"
				+ "                     fd:exampleFragmentAddress \"http://dbpedia.org/sparql\"^^xsd:anyURI, \"http://localhost:7474/db/neo4j/tx\"^^xsd:anyURI ] .";

		final Model fd = ModelFactory.createDefaultModel();

		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
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
				  "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:     <http://example.org/>\n"
				+ "\n"
				+ "ex:dbpediaTPF a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://fragments.dbpedia.org/2016-04/en\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                         fd:FragmentInterface ;\n"
				+ "                     fd:supportedProtocol      fd:TPFProtocol ;\n"
				+ "                     fd:exampleFragmentAddress \"http://fragments.dbpedia.org/2016-04/en\"^^xsd:anyURI, \"http://localhost:7474/db/neo4j/tx\"^^xsd:anyURI ] .";

		final Model fd = ModelFactory.createDefaultModel();

		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
		b.parse(fd);

		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			FederationDescriptionReader.readFromModel(fd);
		});
		// Test that the error message is correct
		final String expectedErrorMessage = "More than one TPF exampleFragmentAddress!";
		final String actualErrorMessage = exception.getMessage();
		assertEquals(expectedErrorMessage, actualErrorMessage);
	}

	@Test
	public void tpfFMWithoutRequiredProperty() {
		final String turtle =
				  "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:     <http://example.org/>\n"
				+ "\n"
				+ "ex:dbpediaTPF a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://fragments.dbpedia.org/2016-04/en\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                    fd:FragmentInterface ;\n"
				+ "                     fd:supportedProtocol fd:TPFProtocol ;\n"
				+ "                     fd:endpointAddress   \"http://fragments.dbpedia.org/2016-04/en\"^^xsd:anyURI, \"http://localhost:7474/db/neo4j/tx\"^^xsd:anyURI ] .";

		final Model fd = ModelFactory.createDefaultModel();

		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
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
				  "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:     <http://example.org/>\n"
				+ "\n"
				+ "ex:dbpediaSPARQL a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://dbpedia.org/sparql\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                         fd:FixedEndpointInterface ;\n"
				+ "                     fd:supportedProtocol      fd:SPARQLProtocol ;\n"
				+ "                     fd:endpointAddress        \"http://dbpedia.org/sparql\"^^xsd:anyURI ] ;\n"
				+ "                     fd:vocabularyMappingsFile \"dbpedia/vocabularyMappings.nt\" .";

		final Model fd = ModelFactory.createDefaultModel();
		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
		b.parse( fd );

		final FederationCatalog cat = FederationDescriptionReader.readFromModel( fd );
		final FederationMember fm = cat.getFederationMemberByURI( "http://dbpedia.org/sparql" );
		assertTrue( fm instanceof SPARQLEndpoint );
		assertNotNull( ((SPARQLEndpoint) fm).getVocabularyMapping() );
	}

	@Test
	public void vocabularyMappingFileIsLoadedFromLocalPath() {
		final String turtle =
				  "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:     <http://example.org/>\n"
				+ "\n"
				+ "ex:dbpediaSPARQL a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://dbpedia.org/sparql\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                         fd:FixedEndpointInterface ;\n"
				+ "                     fd:supportedProtocol      fd:SPARQLProtocol ;\n"
				+ "                     fd:endpointAddress        \"http://dbpedia.org/sparql\"^^xsd:anyURI ] ;\n"
				+ "                     fd:vocabularyMappingsFile \"src/test/resources/dbpedia/vocabularyMappings.nt\" .";

		final Model fd = ModelFactory.createDefaultModel();
		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
		b.parse( fd );

		final FederationCatalog cat = FederationDescriptionReader.readFromModel( fd );
		final FederationMember fm = cat.getFederationMemberByURI( "http://dbpedia.org/sparql" );
		assertTrue( fm instanceof SPARQLEndpoint );
		assertNotNull( ((SPARQLEndpoint) fm).getVocabularyMapping() );
	}

	@Test
	public void missingVocabularyMappingFileThrowsExpectedException() {
		final String turtle =
				  "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX fd:     <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:     <http://example.org/>\n"
				+ "\n"
				+ "ex:dbpediaSPARQL a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://dbpedia.org/sparql\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                         fd:FixedEndpointInterface ;\n"
				+ "                     fd:supportedProtocol      fd:SPARQLProtocol ;\n"
				+ "                     fd:endpointAddress        \"http://dbpedia.org/sparql\"^^xsd:anyURI ] ;\n"
				+ "                     fd:vocabularyMappingsFile \"dummy/vocab.nt\" .";

		final Model fd = ModelFactory.createDefaultModel();
		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
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

	@Test
	public void tokenEnvVarIsParsed() {
		final String turtle =
				  "PREFIX xsd:        <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX fd:         <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:         <http://example.org/>\n"
				+ "PREFIX td:         <https://www.w3.org/2019/wot/td#>\n"
				+ "PREFIX wotsec:     <https://www.w3.org/2019/wot/security#>\n"
				+ "\n"
				+ "ex:dbpediaSPARQL a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://dbpedia.org/sparql\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                         fd:FixedEndpointInterface ;\n"
				+ "                     fd:supportedProtocol      fd:SPARQLProtocol ;\n"
				+ "                     fd:endpointAddress        \"http://dbpedia.org/sparql\"^^xsd:anyURI ;\n"
				+ "\n"
				+ "                     td:hasSecurityConfiguration [ a                wotsec:BearerSecurityScheme ;\n"
				+ "                                                   wotsec:in        \"header\" ;\n"
				+ "                                                   fd:envarForToken \"ENV_VAR_FOR_TOKEN\" ; ] ] ." ;

		final Model fd = ModelFactory.createDefaultModel();
		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
		b.parse( fd );

		final FederationDescriptionReader reader = new FederationDescriptionReader() {
			@Override
			protected String getRequiredEnvironmentVariable( final String name ) {
				return "ENV_VAR_FOR_TOKEN".equals(name)
						? "TOKEN12345"
						: null;
			}
		};

		final FederationCatalog cat = reader.parseFedDescr( fd );
		final FederationMember fm = cat.getFederationMemberByURI( "http://dbpedia.org/sparql" );
		final AuthenticationInformation auth = ( ( SPARQLEndpoint ) fm ).getAuthenticationInformation();

		assertTrue( auth instanceof BearerAuthenticationInformation );
		assertEquals( "TOKEN12345", ( ( BearerAuthenticationInformation ) auth ).getToken() );
	}

	@Test
	public void usernameAndPasswordEnvVarsAreParsed() {
		final String turtle =
				  "PREFIX xsd:        <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX fd:         <http://w3id.org/hefquin/feddesc#>\n"
				+ "PREFIX ex:         <http://example.org/>\n"
				+ "PREFIX td:         <https://www.w3.org/2019/wot/td#>\n"
				+ "PREFIX wotsec:     <https://www.w3.org/2019/wot/security#>\n"
				+ "\n"
				+ "ex:dbpediaSPARQL a fd:RDFBasedFederationMember ;\n"
				+ "      fd:serviceURI \"http://dbpedia.org/sparql\"^^xsd:anyURI ;\n"
				+ "      fd:interface [ a                         fd:FixedEndpointInterface ;\n"
				+ "                     fd:supportedProtocol      fd:SPARQLProtocol ;\n"
				+ "                     fd:endpointAddress        \"http://dbpedia.org/sparql\"^^xsd:anyURI ;\n"
				+ "\n"
				+ "                     td:hasSecurityConfiguration [ a                wotsec:BasicSecurityScheme ;\n"
				+ "                                                   wotsec:in        \"header\" ;\n"
				+ "                                                   fd:envarForUsername \"ENV_VAR_FOR_USERNAME\" ;\n"
				+ "                                                   fd:envarForPassword \"ENV_VAR_FOR_PASSWORD\" ; ] ] ." ;

		final Model fd = ModelFactory.createDefaultModel();
		final RDFParserBuilder b = RDFParser.fromString( turtle, Lang.TURTLE );
		b.parse( fd );

		final FederationDescriptionReader reader = new FederationDescriptionReader() {
			@Override
			protected String getRequiredEnvironmentVariable( final String name ) {
				if ( "ENV_VAR_FOR_USERNAME".equals(name) )
					return "admin";
				else if ( "ENV_VAR_FOR_PASSWORD".equals(name) )
					return "root";
				else return null;
			}
		};

		final FederationCatalog cat = reader.parseFedDescr( fd );
		final FederationMember fm = cat.getFederationMemberByURI( "http://dbpedia.org/sparql" );
		final AuthenticationInformation auth = ( ( SPARQLEndpoint ) fm ).getAuthenticationInformation();

		assertTrue( auth instanceof BasicAuthenticationInformation );
		assertEquals( "admin", ( ( BasicAuthenticationInformation ) auth ).getUsername() );
		assertEquals( "root", ( ( BasicAuthenticationInformation ) auth ).getPassword() );
	}
}
