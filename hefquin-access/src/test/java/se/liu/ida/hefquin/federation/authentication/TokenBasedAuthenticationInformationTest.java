package se.liu.ida.hefquin.federation.authentication;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TokenBasedAuthenticationInformationTest
{
	@Test
	public void addsAuthorizationBearerHeader() {
		final Map<String, String> headers = new HashMap<>();

		final AuthenticationInformation auth = new TokenBasedAuthenticationInformation("TOKEN12345");

		auth.applyTo( headers );

		assertEquals( "Bearer TOKEN12345", headers.get("Authorization") );
	}

	@Test
	public void addsCustomAuthorizationHeader() {
		final Map<String, String> headers = new HashMap<>();

		final AuthenticationInformation auth = new TokenBasedAuthenticationInformation( "Custom", "TOKEN12345");

		auth.applyTo( headers );

		assertEquals( "Custom TOKEN12345", headers.get("Authorization") );
	}
}
