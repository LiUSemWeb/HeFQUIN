package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;

import java.util.*;

public class SubQuery_Analyzer {
    private List<Node> subs, preds, objs;

    public SubQuery_Analyzer( final LogicalPlan lop ) {
        subs = new ArrayList<>();
        preds = new ArrayList<>();
        objs = new ArrayList<>();

        analyze(lop);
    }

    public void analyze( final LogicalPlan lop) {
        Set<TriplePattern> triples = new HashSet<>();
        if( lop.getRootOperator() instanceof LogicalOpRequest) {
            DataRetrievalRequest req = ((LogicalOpRequest<?, ?>) lop.getRootOperator()).getRequest();
            if ( req instanceof BGPRequest ) {
                triples = (Set<TriplePattern>) ((BGPRequest) req).getQueryPattern().getTriplePatterns();
            }
            else if ( req instanceof TriplePatternRequest) {
                triples.add(((TriplePatternRequest) req).getQueryPattern());
            }
        }
        else if ( lop.getRootOperator() instanceof LogicalOpUnion ) {
//            TODO
        }

        // analyze triples for variables
        for ( TriplePattern tp: triples ) {
            Node node;
            if ((node = tp.asJenaTriple().getSubject()).isVariable()) {
                subs.add(node);
            }
            if ((node = tp.asJenaTriple().getPredicate()).isVariable()) {
                preds.add(node);
            }
            if ((node = tp.asJenaTriple().getObject()).isVariable()) {
                objs.add(node);
            }
        }
    }

    public List<Node> getSubs() {
        return subs;
    }

    public List<Node> getPreds() { return preds; }

    public List<Node> getObjs() {
        return objs;
    }

}
