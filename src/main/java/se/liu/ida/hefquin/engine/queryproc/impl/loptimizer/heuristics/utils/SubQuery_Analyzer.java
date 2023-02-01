package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;

import java.util.*;

public class SubQuery_Analyzer {
    protected final List<Node> subs, preds, objs;

    public SubQuery_Analyzer( final LogicalPlan lop ) {
        subs = new ArrayList<>();
        preds = new ArrayList<>();
        objs = new ArrayList<>();

        analyze(lop);
    }

    public void analyze( final LogicalPlan lop) {
        Set<TriplePattern> triples;

        if( lop.getRootOperator() instanceof LogicalOpRequest) {
            triples = LogicalOpUtils.getTriplePatternsOfReq( (LogicalOpRequest<?, ?>) lop.getRootOperator());
        }
        else if ( lop.getRootOperator() instanceof LogicalOpMultiwayUnion || lop.getRootOperator() instanceof LogicalOpUnion ) {
            final int numOfSubPlans = lop.numberOfSubPlans();
            Set<TriplePattern> previousTriples = new HashSet<>();

            for ( int i = 0; i < numOfSubPlans; i++ ) {
                final LogicalPlan subPlan = lop.getSubPlan(i);
                if ( subPlan.getRootOperator() instanceof LogicalOpRequest ) {
                    final Set<TriplePattern> currentTriples = LogicalOpUtils.getTriplePatternsOfReq( (LogicalOpRequest<?, ?>) subPlan.getRootOperator());
                    if( !currentTriples.isEmpty() && !previousTriples.isEmpty() && !currentTriples.equals( previousTriples) ) {
                        throw new IllegalArgumentException("UNION is not added s a result of source selection");
                    }
                    previousTriples = new HashSet<>(currentTriples);
                }
                else
                    throw new IllegalArgumentException("Unsupported type of subquery under UNION (" + subPlan.getRootOperator().getClass().getName() + ")");
            }
            triples = previousTriples;
        }
        else
            throw new IllegalArgumentException("Unsupported type of root operator (" + lop.getRootOperator().getClass().getName() + ")");

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

    public List<Node> getSubs() { return subs; }

    public List<Node> getPreds() { return preds; }

    public List<Node> getObjs() { return objs; }

}
