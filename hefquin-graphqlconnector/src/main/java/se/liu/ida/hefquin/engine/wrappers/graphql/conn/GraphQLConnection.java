package se.liu.ida.hefquin.engine.wrappers.graphql.conn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonParseException;

import se.liu.ida.hefquin.base.utils.BuildInfo;
import se.liu.ida.hefquin.engine.wrappers.graphql.query.GraphQLQuery;

import org.apache.jena.atlas.json.JSON;

public class GraphQLConnection
{
	public static JsonObject performRequest( final GraphQLQuery query,
	                                         final String url,
	                                         final int connectionTimeout,
	                                         final int readTimeout )
			throws GraphQLConnectionException {

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
			con.setRequestProperty("User-Agent", BuildInfo.getUserAgent());
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
				throw new GraphQLConnectionException(
						"Couldn't establish a connection to endpoint. Response code: " + status);
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
				throw new GraphQLConnectionException(e2);
			}
			throw new GraphQLConnectionException(e);
		}

		// Parse JSON responseBody into a json object
		JsonObject jsonObj;
		try {
			jsonObj = JSON.parse(responseBody);
		} 
		catch (final JsonParseException e) {
			throw new GraphQLConnectionException("Unable to parse the retrieved JSON", e);
		}

		return jsonObj;
	}

}
