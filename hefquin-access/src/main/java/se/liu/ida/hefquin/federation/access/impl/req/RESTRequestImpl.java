package se.liu.ida.hefquin.federation.access.impl.req;

import java.net.URI;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpLib;
import org.apache.jena.sparql.exec.http.Params;

import se.liu.ida.hefquin.federation.access.RESTRequest;

public class RESTRequestImpl implements RESTRequest
{
	protected final URI uri;

	/**
	 * @param uri - the URI to be requested
	 */
	public RESTRequestImpl( final URI uri ) {
		assert uri != null;
		this.uri = uri;
	}

	/**
	 * @param uri - the URI to be requested (in the form of a string)
	 * @throws IllegalArgumentException if the given string cannot be turned
	 *                                  into a suitable {@link URI} object
	 */
	public RESTRequestImpl( final String uri ) throws IllegalArgumentException {
		this( toRequestURI(uri) );
	}

	/**
	 * This constructor forms the URI by concatenating the given URL with
	 * a string of query parameters that is created from the given parameters.
	 *
	 * @param url
	 * @param params
	 * @throws IllegalArgumentException if the given URL, together with the
	 *                                  given parameters, cannot be turned
	 *                                  into a suitable {@link URI} object
	 */
	public RESTRequestImpl( final String url, final Params params ) throws IllegalArgumentException {
		this( HttpLib.requestURL(url, params.httpString()) );
	}

	protected static URI toRequestURI( final String uri ) throws IllegalArgumentException {
		try {
			return HttpLib.toRequestURI(uri);
		}
		catch ( final HttpException e ) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	@Override
	public URI getURI() {
		return uri;
	}

}
