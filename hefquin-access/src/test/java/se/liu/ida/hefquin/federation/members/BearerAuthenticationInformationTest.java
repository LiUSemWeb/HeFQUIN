package se.liu.ida.hefquin.federation.members;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class BearerAuthenticationInformationTest
{
	@Test
	public void bearerAuthenticationAddsAuthorizationHeader() {
		final Map<String, String> headers = new HashMap<>();

		final AuthenticationInformation auth = new BearerAuthenticationInformation("TOKEN12345");

		auth.applyTo( headers );

		assertEquals( "Bearer TOKEN12345", headers.get("Authorization") );
	}
}
