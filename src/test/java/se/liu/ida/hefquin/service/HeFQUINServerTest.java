package se.liu.ida.hefquin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URLEncoder;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class HeFQUINServerTest extends AbstractHeFQUINServerTest
{
	@Override
	@Before
	public void setUp() {
		super.setUp();
	}

	@Test
	public void postRequestWithXMLFormat1() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content("query=" + URLEncoder.encode("SELECT (1 AS ?x) WHERE {}", "UTF-8"))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept("application/sparql-results+xml"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("application/sparql-results+xml"));
	}

	@Test
	public void postRequestWithJSONFormat1() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content("query=" + URLEncoder.encode("SELECT (1 AS ?x) WHERE {}", "UTF-8"))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept("application/sparql-results+json"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("application/sparql-results+json"));
	}

	@Test
	public void postRequestWithCSVFormat1() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content("query=" + URLEncoder.encode("SELECT (1 AS ?x) WHERE {}", "UTF-8"))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept("text/csv"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("text/csv"));
	}

	@Test
	public void postRequestWithTSVFormat1() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content("query=" + URLEncoder.encode("SELECT (1 AS ?x) WHERE {}", "UTF-8"))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept("text/tsv"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("text/tsv"));
	}

	@Test
	public void postRequestWithXMLFormat2() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content("SELECT (1 AS ?x) WHERE {}")
				.contentType("application/sparql-query")
				.accept("application/sparql-results+xml"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("application/sparql-results+xml"));
	}

	@Test
	public void postRequestWithJSONFormat2() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content("SELECT (1 AS ?x) WHERE {}")
				.contentType("application/sparql-query")
				.accept("application/sparql-results+json"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("application/sparql-results+json"));
	}

	@Test
	public void postRequestWithCSVFormat3() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content("SELECT (1 AS ?x) WHERE {}")
				.contentType("application/sparql-query")
				.accept("text/csv"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("text/csv"));
	}

	@Test
	public void postRequestWithTSVFormat4() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content("SELECT (1 AS ?x) WHERE {}")
				.contentType("application/sparql-query")
				.accept("text/tsv"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("text/tsv"));
	}

	@Test
	public void postRequestWithInvalidContentType() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content("SELECT (1 AS ?x) WHERE {}")
				.contentType("application/invalid")
				.accept("text/tsv"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(415, status);
	}

	@Test
	public void postRequestWithInvalidAccept() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
				.content("SELECT (1 AS ?x) WHERE {}")
				.contentType("application/sparql-query")
				.accept("application/invalid"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(406, status);
	}

	@Test
	public void getRequestWithXMLFormat() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
				.param("query", "SELECT (1 AS ?x) WHERE {}")
				.accept("application/sparql-results+xml"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("application/sparql-results+xml"));
	}

	@Test
	public void getRequestWithJSONFormat() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
				.param("query", "SELECT (1 AS ?x) WHERE {}")
				.accept("application/sparql-results+json"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("application/sparql-results+json"));
	}

	@Test
	public void getRequestWithCSVFormat() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
				.param("query", "SELECT (1 AS ?x) WHERE {}")
				.accept("text/csv"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("text/csv"));
	}

	@Test
	public void getRequestWithTSVFormat() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
				.param("query", "SELECT (1 AS ?x) WHERE {}")
				.accept("text/tsv"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(200, status);
		final String contentType = mvcResult.getResponse().getContentType();
		assertTrue(contentType.contains("text/tsv"));
	}

	@Test
	public void getRequestWithInvalidAccept() throws Exception {
		String uri = "/sparql";
		MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
				.param("query", "SELECT (1 AS ?x) WHERE {}")
				.accept("application/invalid"))
				.andReturn();

		final int status = mvcResult.getResponse().getStatus();
		assertEquals(406, status);
	}
}
