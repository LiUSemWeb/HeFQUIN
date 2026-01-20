package se.liu.ida.hefquin.federation.access.impl.req;

import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpLib;

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
	public RESTRequestImpl( final String urlTemplate, final Map<String,String> bindings ) throws IllegalArgumentException {
		this( toRequestURI(expandTemplate(urlTemplate, bindings)) );
	}

	private static String expandTemplate( final String urlTemplate, final Map<String,String> bindings ) {
        String result = urlTemplate;
        
        // First, expand path variables {var} - these are mandatory
        for ( final Map.Entry<String,String> e : bindings.entrySet() ) {
            final String placeholder = "{" + e.getKey() + "}";
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                result = result.replace( placeholder, urlEncode(e.getValue()) );
            }
        }
        
        // Handle query parameters {?arg1,arg2,...}
        Pattern queryPattern = Pattern.compile("\\{\\?([^}]+)\\}");
        Matcher queryMatcher = queryPattern.matcher(result);
        
        if (queryMatcher.find()) {
            String queryVars = queryMatcher.group(1);
            String[] varNames = queryVars.split(",");
            
            StringBuilder queryString = new StringBuilder();
            boolean first = true;
            
            for (String varName : varNames) {
                varName = varName.trim();
                String value = bindings.get(varName);
                
                if (value != null && !value.isEmpty()) {
                    if (first) {
                        queryString.append("?");
                        first = false;
                    } else {
                        queryString.append("&");
                    }
                    queryString.append(varName).append("=").append(urlEncode(value));
                }
            }
            
            // Replace the {?...} pattern with the built query string
            result = queryMatcher.replaceAll(queryString.toString());
        }
        
        return result;
    }
    
    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
