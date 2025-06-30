package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.wrappers.lpg.Neo4jException;
import se.liu.ida.hefquin.engine.wrappers.lpg.conn.Neo4jConnectionException;
import se.liu.ida.hefquin.engine.wrappers.lpg.conn.Neo4jConnectionFactory;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.federation.Neo4jServer;
import se.liu.ida.hefquin.federation.access.*;
import se.liu.ida.hefquin.federation.access.impl.response.RecordsResponseImpl;

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
            result = conn.execute( req.getCypherQuery() );
        }
        catch ( final Neo4jConnectionException ex ) {
            throw new FederationAccessException("Executing the given request at the Neo4j endpoint at '"
                    + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
        }
        catch ( final Neo4jException ex ) {
            throw new FederationAccessException("Executing the given request at the Neo4j endpoint at '"
                    + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
        }

        return new RecordsResponseImpl(result, fm, req, requestStartTime);
    }
}
