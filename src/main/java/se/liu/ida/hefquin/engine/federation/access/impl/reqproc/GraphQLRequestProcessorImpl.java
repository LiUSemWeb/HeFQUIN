package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonParseException;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JSON;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.GraphQLRequest;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.response.JSONResponseImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;

public class GraphQLRequestProcessorImpl implements GraphQLRequestProcessor {
	protected final int connectionTimeout;
	protected final int readTimeout;

	/**
	 * The given timeouts are specified in milliseconds. Any value {@literal <=} 0
	 * means no timeout.
	 */
	public GraphQLRequestProcessorImpl(final int connectionTimeout, final int readTimeout) {
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
	}

	public GraphQLRequestProcessorImpl() {
		this(-1, -1);
	}

	@Override
	public JSONResponse performRequest(final GraphQLRequest req,
			final GraphQLEndpoint fm)
			throws FederationAccessException {

		final Date startTime = new Date();
		final GraphQLQuery query = req.getGraphQLQuery();
		final String url = fm.getInterface().getURL();

		HttpURLConnection con = null;
		OutputStreamWriter outWriter = null;
		BufferedReader bufferReader = null;
		String responseBody = "{}";
		try {
			// Setup the connection
			final URL endpointURL = new URL(url);
			con = (HttpURLConnection) endpointURL.openConnection();
			con.setRequestMethod("POST");
			con.setConnectTimeout(connectionTimeout);
			con.setReadTimeout(readTimeout);
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.connect();

			// Sending post body
			JsonObject postBody = new JsonObject();
			postBody.put("query", query.toString());
			postBody.put("variables", query.getArgumentValues());
			postBody.put("raw", true);
			outWriter = new OutputStreamWriter(con.getOutputStream());
			outWriter.write(postBody.toString());
			outWriter.close();

			// Fetch the input stream stream
			InputStream iStream;
			final int status = con.getResponseCode();
			if (status >= 200 && status < 300) {
				iStream = con.getInputStream();
			} else {
				con.disconnect();
				throw new FederationAccessException(
						"Couldn't establish a connection to endpoint. Response code: " + status, req, fm);
			}

			// Components used to read the message
			bufferReader = new BufferedReader(new InputStreamReader(iStream));
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
		catch (final IOException e) {
			if(con != null){
				con.disconnect();
			}
			try {
				if(outWriter != null){
					outWriter.close();
				}
				if(bufferReader != null){
					bufferReader.close();
				}
			}
			catch(final IOException e2){
				throw new FederationAccessException(req, fm);
			}
			throw new FederationAccessException(req, fm);
		}

		// Parse JSON responseBody into a json object
		JsonObject jsonObj;
		try {
			jsonObj = JSON.parse(responseBody);
		} 
		catch (final JsonParseException e) {
			throw new FederationAccessException("Unable to parse the retrieved JSON", req, fm);
		}

		return new JSONResponseImpl(jsonObj, fm, req, startTime);
	}
}
