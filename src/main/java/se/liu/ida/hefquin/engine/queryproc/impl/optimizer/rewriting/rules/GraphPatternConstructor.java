package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;

import java.util.HashSet;
import java.util.Set;

public class GraphPatternConstructor {

    public static BGP createNewBGP( final LogicalOpRequest<?, ?> lop1, final LogicalOpRequest<?, ?> lop2 ) {
        final Set<TriplePattern> tps = getTriplePatternsOfReq(lop1);
        tps.addAll( getTriplePatternsOfReq(lop2) );

        return new BGPImpl(tps);
    }

    public static BGP createNewBGP( final LogicalOpTPAdd lopTPAdd, final LogicalOpRequest<?, ?> lopReq ) {
        final TriplePattern tp = lopTPAdd.getTP();

        final Set<TriplePattern> tps = getTriplePatternsOfReq(lopReq);
        tps.add(tp);

        return new BGPImpl(tps);
    }

    public static BGP createNewBGP( final LogicalOpBGPAdd lopBGPAdd, final LogicalOpRequest<?, ?> lopReq ) {
        final Set<TriplePattern> tps = new HashSet<>( lopBGPAdd.getBGP().getTriplePatterns() );

        tps.addAll( getTriplePatternsOfReq(lopReq) );

        return new BGPImpl(tps);
    }

    public static BGP createNewBGP( final LogicalOpBGPAdd lopBGPAdd1, final LogicalOpBGPAdd lopBGPAdd2 ) {
        final Set<TriplePattern> tps = new HashSet<>( lopBGPAdd1.getBGP().getTriplePatterns() );

        tps.addAll( lopBGPAdd2.getBGP().getTriplePatterns() );

        return new BGPImpl(tps);
    }

    public static BGP createNewBGP( final LogicalOpTPAdd lopTPAdd, final LogicalOpBGPAdd lopBGPAdd ) {
        final TriplePattern tp = lopTPAdd.getTP();

        final BGP bgp = lopBGPAdd.getBGP();
        final Set<TriplePattern> tps = new HashSet<>(bgp.getTriplePatterns());
        tps.add(tp);

        return new BGPImpl(tps);
    }

    public static SPARQLGraphPattern createNewGraphPatternWithAND( final LogicalOpTPAdd lopTPAdd, final LogicalOpRequest<?, ?> lopReq ) {
        final Triple tp = lopTPAdd.getTP().asJenaTriple();

        final SPARQLQuery graphPattern = ((SPARQLRequest) lopReq.getRequest()).getQuery();
        final Element element = graphPattern.asJenaQuery().getQueryPattern();

        ((ElementGroup) element).addTriplePattern(tp);

        return new SPARQLGraphPatternImpl(element);
    }

    public static SPARQLGraphPattern createNewGraphPatternWithAND(LogicalOpBGPAdd lopBGPAdd, LogicalOpRequest<?,?> lopReq) {
        final BGP bgpOfBGPAdd = lopBGPAdd.getBGP();
        final BasicPattern bgp = ( (OpBGP)bgpOfBGPAdd.asJenaOp() ).getPattern();
        final ElementTriplesBlock elementBGP = new ElementTriplesBlock( bgp );

        final SPARQLQuery queryOfReq = ((SPARQLRequest) lopReq.getRequest()).getQuery();
        final Element elementPattern = queryOfReq.asJenaQuery().getQueryPattern();

        ((ElementGroup) elementPattern).addElement(elementBGP);

        return new SPARQLGraphPatternImpl(elementPattern);
    }

    public static SPARQLGraphPattern createNewGraphPatternWithAND( final LogicalOpRequest<?, ?> lopReq1, final LogicalOpRequest<?, ?> lopReq2 ) {
        final SPARQLQuery graphPattern1 = ((SPARQLRequest) lopReq1.getRequest()).getQuery();
        final Element element1 = graphPattern1.asJenaQuery().getQueryPattern();

        final SPARQLQuery graphPattern2 = ((SPARQLRequest) lopReq2.getRequest()).getQuery();
        final Element element2 = graphPattern2.asJenaQuery().getQueryPattern();

        ((ElementGroup) element1).addElement(element2);

        return new SPARQLGraphPatternImpl(element1);
    }

    public static SPARQLGraphPattern createNewGraphPatternWithUnion( final LogicalOpRequest<?, ?> lopReq1, final LogicalOpRequest<?, ?> lopReq2 ) {
        final SPARQLQuery graphPattern1 = ((SPARQLRequest) lopReq1.getRequest()).getQuery();
        final Element element1 = graphPattern1.asJenaQuery().getQueryPattern();

        final SPARQLQuery graphPattern2 = ((SPARQLRequest) lopReq2.getRequest()).getQuery();
        final Element element2 = graphPattern2.asJenaQuery().getQueryPattern();

        final ElementUnion elementUnion = new ElementUnion(element1);
        elementUnion.addElement(element2);

        return new SPARQLGraphPatternImpl(elementUnion);
    }

    protected static Set<TriplePattern> getTriplePatternsOfReq( final LogicalOpRequest<?, ?> lop ) {
        final DataRetrievalRequest req = lop.getRequest();

        if ( req instanceof TriplePatternRequest) {
            final TriplePatternRequest tpReq = (TriplePatternRequest) lop.getRequest();
            final Set<TriplePattern> tps = new HashSet<>();
            tps.add( tpReq.getQueryPattern() );

            return tps;
        }
        else if ( req instanceof BGPRequest) {
            final BGPRequest bgpReq = (BGPRequest) lop.getRequest();
            final BGP bgp = bgpReq.getQueryPattern();

            if ( bgp.getTriplePatterns().size() == 0 ) {
                throw new IllegalArgumentException( "the BGP is empty" );
            }
            else {
                return new HashSet<>( bgp.getTriplePatterns() );
            }
        }
        else  {
            throw new IllegalArgumentException( "Cannot get triple patterns of the req" );
        }
    }

}
