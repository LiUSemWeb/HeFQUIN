package se.liu.ida.hefquin.federation.access.impl.req;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RESTRequestImplTest {

	@Test
	public void testConstructorWithURI() {
		// given
		final URI uri = URI.create("http://example.com/api");
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(uri);
		
		// then
		assertEquals(uri, request.getURI());
	}

	@Test
	public void testConstructorWithString() {
		// given
		final String uriString = "http://example.com/api";
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(uriString);
		
		// then
		assertEquals(URI.create(uriString), request.getURI());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithInvalidString() {
		// given
		final String invalidUri = "invalid uri with spaces";
		
		// when
		new RESTRequestImpl(invalidUri);
		
		// then - exception expected
	}

	@Test
	public void testConstructorWithTemplateAndEmptyBindings() {
		// given
		final String urlTemplate = "http://example.com/api";
		final Map<String, String> bindings = new HashMap<>();
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		assertEquals(URI.create("http://example.com/api"), request.getURI());
	}

	@Test
	public void testConstructorWithPathVariableBinding() {
		// given
		final String urlTemplate = "http://example.com/api/{id}/details";
		final Map<String, String> bindings = new HashMap<>();
		bindings.put("id", "123");
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		assertEquals(URI.create("http://example.com/api/123/details"), request.getURI());
	}

	@Test
	public void testConstructorWithMultiplePathVariables() {
		// given
		final String urlTemplate = "http://example.com/api/{category}/{id}";
		final Map<String, String> bindings = new HashMap<>();
		bindings.put("category", "products");
		bindings.put("id", "456");
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		assertEquals(URI.create("http://example.com/api/products/456"), request.getURI());
	}

	@Test
	public void testConstructorWithQueryParameters() {
		// given
		final String urlTemplate = "http://example.com/api{?param1,param2}";
		final Map<String, String> bindings = new HashMap<>();
		bindings.put("param1", "value1");
		bindings.put("param2", "value2");
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		final URI expectedUri = URI.create("http://example.com/api?param1=value1&param2=value2");
		assertEquals(expectedUri, request.getURI());
	}

	@Test
	public void testConstructorWithPartialQueryParameters() {
		// given
		final String urlTemplate = "http://example.com/api{?param1,param2,param3}";
		final Map<String, String> bindings = new HashMap<>();
		bindings.put("param1", "value1");
		bindings.put("param3", "value3");
		// param2 is not provided
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		final URI expectedUri = URI.create("http://example.com/api?param1=value1&param3=value3");
		assertEquals(expectedUri, request.getURI());
	}

	@Test
	public void testConstructorWithEmptyQueryParameterValues() {
		// given
		final String urlTemplate = "http://example.com/api{?param1,param2}";
		final Map<String, String> bindings = new HashMap<>();
		bindings.put("param1", "value1");
		bindings.put("param2", ""); // empty value
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		final URI expectedUri = URI.create("http://example.com/api?param1=value1");
		assertEquals(expectedUri, request.getURI());
	}

	@Test
	public void testConstructorWithNullQueryParameterValues() {
		// given
		final String urlTemplate = "http://example.com/api{?param1,param2}";
		final Map<String, String> bindings = new HashMap<>();
		bindings.put("param1", "value1");
		bindings.put("param2", null); // null value
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		final URI expectedUri = URI.create("http://example.com/api?param1=value1");
		assertEquals(expectedUri, request.getURI());
	}

	@Test
	public void testConstructorWithPathAndQueryParameters() {
		// given
		final String urlTemplate = "http://example.com/api/{category}{?limit,offset}";
		final Map<String, String> bindings = new HashMap<>();
		bindings.put("category", "products");
		bindings.put("limit", "10");
		bindings.put("offset", "20");
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		final URI expectedUri = URI.create("http://example.com/api/products?limit=10&offset=20");
		assertEquals(expectedUri, request.getURI());
	}

	@Test
	public void testConstructorWithUrlEncodingRequired() {
		// given
		final String urlTemplate = "http://example.com/api/{param}";
		final Map<String, String> bindings = new HashMap<>();
		bindings.put("param", "value with spaces");
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		// URLEncoder produces + for spaces (form encoding), which is acceptable for URLs
		final URI expectedUri = URI.create("http://example.com/api/value+with+spaces");
		assertEquals(expectedUri, request.getURI());
	}

	@Test
	public void testConstructorWithSpecialCharacters() {
		// given
		final String urlTemplate = "http://example.com/api{?search}";
		final Map<String, String> bindings = new HashMap<>();
		bindings.put("search", "query&with=special&chars");
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		final URI result = request.getURI();
		assertTrue(result.toString().contains("query%26with%3Dspecial%26chars"));
	}

	@Test
	public void testConstructorWithNoQueryParametersInTemplate() {
		// given
		final String urlTemplate = "http://example.com/api/fixed-endpoint";
		final Map<String, String> bindings = new HashMap<>();
		bindings.put("unused", "value"); // this binding won't be used
		
		// when
		final RESTRequestImpl request = new RESTRequestImpl(urlTemplate, bindings);
		
		// then
		final URI expectedUri = URI.create("http://example.com/api/fixed-endpoint");
		assertEquals(expectedUri, request.getURI());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithMissingPathParameter() {
		// given
		final String urlTemplate = "http://example.com/api/{id}/details";
		final Map<String, String> bindings = new HashMap<>();
		// id path parameter is missing from bindings
		
		// when
		new RESTRequestImpl(urlTemplate, bindings);
		
		// then - exception expected because {id} cannot be resolved
	}

}


