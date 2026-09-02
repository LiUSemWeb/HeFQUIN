package se.liu.ida.hefquin.federation.authentication;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class BasicAuthenticationInformationTest
{
	@Test
	public void basicAuthenticationAddsAuthorizationHeader() {
		final Map<String, String> headers = new HashMap<>();

		final AuthenticationInformation auth = new BasicAuthenticationInformation("admin","root");

		auth.applyTo( headers );

		final String credentials = "admin:root";
		assertEquals( "Basic " + Base64.getEncoder().encodeToString( credentials.getBytes(StandardCharsets.UTF_8) ), headers.get("Authorization") );
	}
}
