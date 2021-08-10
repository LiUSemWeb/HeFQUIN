package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.response.StringResponseImpl;

import java.util.Date;

public class Neo4jRequestProcessorImpl implements Neo4jRequestProcessor
{
    @Override
    public StringResponse performRequest( final Neo4jRequest req,
                                                   final Neo4jServer fm )
            throws FederationAccessException
    {
        final Date requestStartTime = new Date();
        final Neo4jConnectionFactory.Neo4jConnection conn = Neo4jConnectionFactory.connect( fm.getInterface().getURL() );
        final String result;
        try {
            result = conn.execute( req );
        }
        catch ( final Neo4JConnectionException ex ) {
            throw new FederationAccessException("Executing the given request at the Neo4j endpoint at '" + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
        }
        return new StringResponseImpl(result, fm, req, requestStartTime);
    }
}
