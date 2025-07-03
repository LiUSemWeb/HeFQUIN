package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.Collections;
import java.util.Set;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BGPRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;

public class LogicalOpUtils
{
    /**
     * Return a set of triple patterns, which are extracted from a given Request (support TriplePatternRequest and BGPRequest)
     */
    public static Set<TriplePattern> getTriplePatternsOfReq( final LogicalOpRequest<?, ?> lop ) {
        final DataRetrievalRequest req = lop.getRequest();

        if ( req instanceof TriplePatternRequest tpReq ) {
            return Collections.singleton( tpReq.getQueryPattern() );
        }
        else if ( req instanceof BGPRequest bgpReq ) {
            final BGP bgp = bgpReq.getQueryPattern();

            if ( bgp.getTriplePatterns().size() == 0 ) {
                throw new IllegalArgumentException( "the BGP is empty" );
            }
            else {
                return bgp.getTriplePatterns();
            }
        }
        else if( req instanceof SPARQLRequest sparqlReq ) {
            return sparqlReq.getQueryPattern().getAllMentionedTPs();
        }
        else  {
            throw new IllegalArgumentException( "Cannot get triple patterns of the given request operator (type: " + req.getClass().getName() + ")." );
        }
    }

    public static UnaryLogicalOp createLogicalAddOpFromPhysicalReqOp( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

        if ( ! (lop instanceof LogicalOpRequest) ) {
            throw new IllegalArgumentException( "unsupported type of logical operator: " + lop.getClass().getName() );
        }

        return createLogicalAddOpFromLogicalReqOp( (LogicalOpRequest<?, ?>) lop );
    }

    public static UnaryLogicalOp createLogicalAddOpFromLogicalReqOp( final LogicalOpRequest<?, ?> reqOp ) {
        final DataRetrievalRequest req = reqOp.getRequest();
        final FederationMember fm = reqOp.getFederationMember();

        if( req instanceof SPARQLRequest sparqlReq ) {
            final SPARQLGraphPattern pattern = sparqlReq.getQueryPattern();
            return new LogicalOpGPAdd( fm, pattern );
        }
        else {
            throw new IllegalArgumentException( "unsupported type of request: " + req.getClass().getName() );
        }
    }

    public static UnaryLogicalOp createLogicalOptAddOpFromPhysicalReqOp( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();

        if ( ! (lop instanceof LogicalOpRequest) ) {
            throw new IllegalArgumentException( "unsupported type of logical operator: " + lop.getClass().getName() );
        }

        final LogicalOpRequest<?, ?> reqOp = (LogicalOpRequest<?, ?>) lop;
        final DataRetrievalRequest req = reqOp.getRequest();
        final FederationMember fm = reqOp.getFederationMember();

        if( req instanceof SPARQLRequest sparqlReq ) {
            final SPARQLGraphPattern pattern = sparqlReq.getQueryPattern();
            return new LogicalOpGPOptAdd( fm, pattern );
        }
        else {
            throw new IllegalArgumentException( "unsupported type of request: " + req.getClass().getName() );
        }
    }

}
