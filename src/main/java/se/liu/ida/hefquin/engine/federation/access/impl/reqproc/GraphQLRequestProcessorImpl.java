package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonParseException;
import org.apache.jena.atlas.json.JSON;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.GraphQLRequest;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.response.JSONResponseImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;


public class GraphQLRequestProcessorImpl implements GraphQLRequestProcessor
{
	protected final int connectionTimeout;
	protected final int readTimeout;

	/**
	 * The given timeouts are specified in milliseconds. Any value {@literal <=} 0 means no timeout.
	 */
	public GraphQLRequestProcessorImpl( final int connectionTimeout, final int readTimeout ) {
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
	}

	public GraphQLRequestProcessorImpl() {
		this(-1, -1);
	}

	@Override
	public JSONResponse performRequest( final GraphQLRequest req,
	                                    final GraphQLEndpoint fm )
	                                    		throws FederationAccessException {

		Date startTime = new Date();
		GraphQLQuery query = req.getGraphQLQuery();
		String url = fm.getInterface().getURL();
		url += query.getURL();
		
		String responseBody = "{}";
		try {
			// Setup the connection
			URL endpointURL = new URL(url);
			HttpURLConnection con = (HttpURLConnection) endpointURL.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeout);
            con.setRequestProperty("Accept", "application/json");
			con.connect();

            // Fetch the input stream stream
			InputStream iStream;
            int status = con.getResponseCode();
            if(status >= 200 && status < 300){
                iStream = con.getInputStream();
            }
            else {
                throw new FederationAccessException("Couldn't establish a connection to endpoint. Response code: " + status,req, fm);
            }

            // Components used to read the message
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder stringBuilder = new StringBuilder();

            // Read from the buffer
            String lineStr;
            while ((lineStr = bufferReader.readLine()) != null) {
                stringBuilder.append(lineStr);
            }

            responseBody = stringBuilder.toString();

			// Disconnect
            bufferReader.close();
            con.disconnect();
		}
		catch(IOException e) {
			throw new FederationAccessException(req, fm);
		}

		// Parse JSON responseBody into a json object
		JsonObject jsonObj;
		try {
			jsonObj = JSON.parse(responseBody);
		} catch (JsonParseException e) {
			throw new FederationAccessException("Unable to parse the retrieved JSON", req, fm);
		}

		return new JSONResponseImpl(jsonObj,fm,req,startTime);
	}
}
