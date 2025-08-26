package se.liu.ida.hefquin.federation.access.impl.reqproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalInterface;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;

public interface RequestProcessor<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
{
	DataRetrievalResponse<?> performRequest( ReqType req, MemberType fm ) throws FederationAccessException;

	/**
	 * Returns a User-Agent string constructed from the module version. This is
	 * loaded once from version.properties on the classpath.
	 */
	default String getUserAgent() {
		return UserAgentHolder.USER_AGENT;
	}

	/**
	 * Inner static class for lazy-loaded, cached User-Agent.
	 */
	class UserAgentHolder {
		static final String USER_AGENT;

		static {
			final Properties props = new Properties();
			String version = "unknown-version";

			try ( final InputStream in = DataRetrievalInterface.class.getResourceAsStream("/version.properties") ) {
				if ( in != null ) {
					props.load( in );
					version = props.getProperty("version", version);
				}
			} catch ( IOException e ) {
				// optionally log
			}

			USER_AGENT = "HeFQUIN/" + version + " (https://liusemweb.github.io/HeFQUIN/)";
		}
	}
}
