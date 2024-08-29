package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.response.RecordsResponseImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.Neo4JException;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conn.Neo4jConnectionFactory;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conn.Neo4JConnectionException;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;

import java.util.Date;
import java.util.List;

public class Neo4jRequestProcessorImpl implements Neo4jRequestProcessor
{
    @Override
    public RecordsResponse performRequest( final Neo4jRequest req,
                                           final Neo4jServer fm )
            throws FederationAccessException
    {
        final Date requestStartTime = new Date();
        final Neo4jConnectionFactory.Neo4jConnection conn = Neo4jConnectionFactory.connect( fm.getInterface().getURL() );
        final List<TableRecord> result;
        try {
            result = conn.execute( req );
        }
        catch ( final Neo4JConnectionException ex ) {
            throw new FederationAccessException("Executing the given request at the Neo4j endpoint at '"
                    + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
        }
        catch ( final Neo4JException ex ) {
            throw new FederationAccessException("Executing the given request at the Neo4j endpoint at '"
                    + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
        }

        return new RecordsResponseImpl(result, fm, req, requestStartTime);
    }
}
