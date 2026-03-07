package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.*;

import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.*;

import se.liu.ida.hefquin.base.query.*;
import se.liu.ida.hefquin.base.query.impl.*;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
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
     * Creates a BGP by merging two sets of triple patterns,
     * which are extracted from two given Requests.
     */
    public static BGP createNewBGP( final LogicalOpRequest<?, ?> lop1, final LogicalOpRequest<?, ?> lop2 ) {
        final Set<TriplePattern> tps = getTriplePatternsOfReq(lop1);
        tps.addAll( getTriplePatternsOfReq(lop2) );

        return new BGPImpl(tps);
    }

    /**
     * Creates a BGP by adding a triple pattern to a set of triple patterns,
     * where the triple pattern is extracted from a given tpAdd operator,
     * and the set of triple patterns are extracted from the given Request.
     */
    public static BGP createNewBGP( final LogicalOpTPAdd lopTPAdd, final LogicalOpRequest<?, ?> lopReq ) {
        final TriplePattern tp = lopTPAdd.getTP();

        final Set<TriplePattern> tps = getTriplePatternsOfReq(lopReq);
        tps.add(tp);

        return new BGPImpl(tps);
    }

    /**
     * Creates a BGP by merging two sets of triple patterns,
     * where one of them is extracted from a given bgpAdd operator,
     * and another one is extracted from a given Request.
     */
    public static BGP createNewBGP( final LogicalOpBGPAdd lopBGPAdd, final LogicalOpRequest<?, ?> lopReq ) {
        final Set<TriplePattern> tps = new HashSet<>( lopBGPAdd.getBGP().getTriplePatterns() );

        tps.addAll( getTriplePatternsOfReq(lopReq) );

        return new BGPImpl(tps);
    }

    /**
     * Creates a BGP by merging two sets of triple patterns,
     * which are extracted from two given bgpAdd operators.
     */
    public static BGP createNewBGP( final LogicalOpBGPAdd lopBGPAdd1, final LogicalOpBGPAdd lopBGPAdd2 ) {
        final Set<TriplePattern> tps = new HashSet<>( lopBGPAdd1.getBGP().getTriplePatterns() );

        tps.addAll( lopBGPAdd2.getBGP().getTriplePatterns() );

        return new BGPImpl(tps);
    }

    /**
     * Creates a BGP by adding a triple pattern to a set of triple patterns,
     * where the triple pattern is extracted from a given tpAdd operator,
     * and the set of triple patterns are extracted from a given bgpAdd operator.
     */
    public static BGP createNewBGP( final LogicalOpTPAdd lopTPAdd, final LogicalOpBGPAdd lopBGPAdd ) {
        final TriplePattern tp = lopTPAdd.getTP();

        final Set<TriplePattern> tps = new HashSet<>(lopBGPAdd.getBGP().getTriplePatterns());
        tps.add(tp);

        return new BGPImpl(tps);
    }

    /**
     * Creates a new graph pattern by adding a triple pattern to the graph pattern of a given SPARQLRequest,
     * where the triple pattern is extracted from a given tpAdd operator.
     */
    public static SPARQLGraphPattern createNewGraphPatternWithAND(final LogicalOpTPAdd lopTPAdd, final LogicalOpRequest<?, ?> lopReq ) {
        final ElementGroup elementGroup = new ElementGroup();
        elementGroup.addElement( getPatternOfRequest(lopReq) );
        elementGroup.addTriplePattern( lopTPAdd.getTP().asJenaTriple() );

        return new GenericSPARQLGraphPatternImpl1(elementGroup);
    }

    /**
     * Creates a new graph pattern by adding a BGP to the graph pattern of a given SPARQLRequest,
     * where the BGP is extracted from a given bgpAdd operator.
     */
    public static SPARQLGraphPattern createNewGraphPatternWithAND( final LogicalOpBGPAdd lopBGPAdd, final LogicalOpRequest<?,?> lopReq ) {
        final BasicPattern bgp = new BasicPattern();
        for ( TriplePattern tp: lopBGPAdd.getBGP().getTriplePatterns() ){
            bgp.add( tp.asJenaTriple() );
        }

        final ElementGroup elementGroup = new ElementGroup();
        elementGroup.addElement( new ElementTriplesBlock( bgp ) );
        elementGroup.addElement( getPatternOfRequest(lopReq) );
        return new GenericSPARQLGraphPatternImpl1(elementGroup);
    }

    /**
     * Creates a new graph pattern using a conjunction of two graph patterns,
     * which are extracted from two given SPARQLRequests.
     */
    public static SPARQLGraphPattern createNewGraphPatternWithAND( final LogicalOpRequest<?, ?> lopReq1, final LogicalOpRequest<?, ?> lopReq2 ) {
        final ElementGroup elementGroup = new ElementGroup();
        elementGroup.addElement( getPatternOfRequest(lopReq1) );
        elementGroup.addElement( getPatternOfRequest(lopReq2) );

        return new GenericSPARQLGraphPatternImpl1(elementGroup);
    }

    /**
     * Creates a new graph pattern using a union of two graph patterns,
     * which are extracted from two given SPARQLRequests.
     */
    public static SPARQLGraphPattern createNewGraphPatternWithUnion( final LogicalOpRequest<?, ?> lopReq1, final LogicalOpRequest<?, ?> lopReq2 ) {
        final ElementUnion elementUnion = new ElementUnion();
        elementUnion.addElement( getPatternOfRequest(lopReq1) );
        elementUnion.addElement( getPatternOfRequest(lopReq2) );

        return new GenericSPARQLGraphPatternImpl1(elementUnion);
    }

    public static Element getPatternOfRequest( final LogicalOpRequest<?, ?> lopReq ){
        final DataRetrievalRequest req = lopReq.getRequest();
        if ( req instanceof SPARQLRequest ) {
            final SPARQLQuery graphPattern = ((SPARQLRequest) req).getQuery();
            return graphPattern.asJenaQuery().getQueryPattern();
        }
        else  {
            throw new IllegalArgumentException( "Unsupported type of request: " + req.getClass().getName() );
        }

    }

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

        if ( req instanceof BGPRequest) {
            return createBGPAddLopFromRequest( (BGPRequest) req, fm );
        }
        else if( req instanceof TriplePatternRequest ) {
            return createTPAddLopFromRequest( (TriplePatternRequest) req, fm );
        }
        else if( req instanceof SPARQLRequest ) {
            return createGPAddLopFromRequest( (SPARQLRequest) req, fm );
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

        if ( req instanceof BGPRequest) {
            return createBGPOptAddLopFromRequest( (BGPRequest) req, fm );
        }
        else if( req instanceof TriplePatternRequest ) {
            return createTPOptAddLopFromRequest( (TriplePatternRequest) req, fm );
        }
        else if( req instanceof SPARQLRequest ) {
            return createGPOptAddLopFromRequest( (SPARQLRequest) req, fm );
        }
        else {
            throw new IllegalArgumentException( "unsupported type of request: " + req.getClass().getName() );
        }
    }

    /**
     * Creates a logical bgpAdd operator that uses the BGP of the
     * given request, together with the given federation member.
     */
    public static LogicalOpBGPAdd createBGPAddLopFromRequest( final BGPRequest req,
                                                              final FederationMember fm ) {
        final BGP bgp = req.getQueryPattern();
        return new LogicalOpBGPAdd( fm, bgp );
    }

    /**
     * Creates a logical bgpOptAdd operator that uses the BGP of the
     * given request, together with the given federation member.
     */
    public static LogicalOpBGPOptAdd createBGPOptAddLopFromRequest( final BGPRequest req,
                                                                    final FederationMember fm ) {
        final BGP bgp = req.getQueryPattern();
        return new LogicalOpBGPOptAdd( fm, bgp );
    }

    /**
     * Creates a logical tpAdd operator that uses the triple pattern of
     * the given request, together with the given federation member.
     */
    public static LogicalOpTPAdd createTPAddLopFromRequest( final TriplePatternRequest req,
                                                            final FederationMember fm ) {
        final TriplePattern tp = req.getQueryPattern();
        return new LogicalOpTPAdd( fm, tp );
    }

    /**
     * Creates a logical tpOptAdd operator that uses the triple pattern
     * of the given request, together with the given federation member.
     */
    public static LogicalOpTPOptAdd createTPOptAddLopFromRequest( final TriplePatternRequest req,
                                                                  final FederationMember fm ) {
        final TriplePattern tp = req.getQueryPattern();
        return new LogicalOpTPOptAdd( fm, tp );
    }

    /**
     * Creates a logical gpAdd operator that uses the graph pattern of
     * the given request, together with the given federation member.
     */
    public static LogicalOpGPAdd createGPAddLopFromRequest( final SPARQLRequest req,
                                                            final FederationMember fm ) {
        final SPARQLGraphPattern pattern = req.getQueryPattern();
        return new LogicalOpGPAdd( fm, pattern );
    }

    /**
     * Creates a logical gpOptAdd operator that uses the graph pattern
     * of the given request, together with the given federation member.
     */
    public static LogicalOpGPOptAdd createGPOptAddLopFromRequest( final SPARQLRequest req,
                                                                  final FederationMember fm ) {
        final SPARQLGraphPattern pattern = req.getQueryPattern();
        return new LogicalOpGPOptAdd( fm, pattern );
    }

}
