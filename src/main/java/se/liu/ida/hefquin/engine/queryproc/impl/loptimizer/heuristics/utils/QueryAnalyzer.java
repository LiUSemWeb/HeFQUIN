package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;

import java.util.*;

public class QueryAnalyzer {
    protected final LogicalPlan plan;

    protected List<Node> subs = new ArrayList<>();
    protected List<Node> preds = new ArrayList<>();
    protected List<Node> objs = new ArrayList<>();

    public QueryAnalyzer( final LogicalPlan plan ) {
        this.plan = plan;

        if( plan == null ) {
            return;
        }
        final Set<TriplePattern> tps = extractTriplePatterns();

        for ( final TriplePattern tp: tps ) {
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

    protected Set<TriplePattern> extractTriplePatterns() {
        final LogicalOperator lop = plan.getRootOperator();

        if( lop instanceof LogicalOpRequest) {
            return LogicalOpUtils.getTriplePatternsOfReq( (LogicalOpRequest<?, ?>) lop);
        }
        else if ( lop instanceof LogicalOpMultiwayUnion || plan.getRootOperator() instanceof LogicalOpUnion ) {
            final int numOfSubPlans = plan.numberOfSubPlans();
            Set<TriplePattern> previousTPs = null;

            for ( int i = 0; i < numOfSubPlans; i++ ) {
                final LogicalOperator subLop = plan.getSubPlan(i).getRootOperator();

                if ( subLop instanceof LogicalOpRequest ) {
                    final Set<TriplePattern> currentTPs = LogicalOpUtils.getTriplePatternsOfReq( (LogicalOpRequest<?, ?>) subLop);
                    if( !currentTPs.isEmpty() && previousTPs != null && !currentTPs.equals( previousTPs) ) {
                        throw new IllegalArgumentException("UNION is not added as a result of source selection");
                    }
                    previousTPs = currentTPs;
                }
                else
                    throw new IllegalArgumentException("Unsupported type of subquery under UNION (" + subLop.getClass().getName() + ")");
            }
            return previousTPs;
        }
        else
            throw new IllegalArgumentException("Unsupported type of root operator (" + lop.getClass().getName() + ")");
    }

    public LogicalPlan getPlan() { return plan; }

    public List<Node> getSubs() { return subs; }

    public List<Node> getPreds() { return preds; }

    public List<Node> getObjs() { return objs; }

    public void setSubs( final List<Node> subs ) {
        this.subs = subs;
    }

    public void setObjs( final List<Node> objs ) {
        this.objs = objs;
    }

    public void setPreds( final List<Node> preds ) {
        this.preds = preds;
    }

}
