package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;

import java.util.*;

public class Query_Analyzer {
    protected LogicalPlan plan;

    protected final List<Node> subs = new ArrayList<>();
    protected final List<Node> preds = new ArrayList<>();
    protected final List<Node> objs = new ArrayList<>();

    public Query_Analyzer( LogicalPlan plan ) {
        this.plan = plan;
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

        if( plan.getRootOperator() instanceof LogicalOpRequest) {
            return LogicalOpUtils.getTriplePatternsOfReq( (LogicalOpRequest<?, ?>) plan.getRootOperator());
        }
        else if ( plan.getRootOperator() instanceof LogicalOpMultiwayUnion || plan.getRootOperator() instanceof LogicalOpUnion ) {
            final int numOfSubPlans = plan.numberOfSubPlans();
            Set<TriplePattern> previousTPs = null;

            for ( int i = 0; i < numOfSubPlans; i++ ) {
                final LogicalPlan subPlan = plan.getSubPlan(i);
                if ( subPlan.getRootOperator() instanceof LogicalOpRequest ) {
                    final Set<TriplePattern> currentTPs = LogicalOpUtils.getTriplePatternsOfReq( (LogicalOpRequest<?, ?>) subPlan.getRootOperator());
                    if( !currentTPs.isEmpty() && previousTPs != null && !currentTPs.equals( previousTPs) ) {
                        throw new IllegalArgumentException("UNION is not added as a result of source selection");
                    }
                    previousTPs = currentTPs;
                }
                else
                    throw new IllegalArgumentException("Unsupported type of subquery under UNION (" + subPlan.getRootOperator().getClass().getName() + ")");
            }
            return previousTPs;
        }
        else
            throw new IllegalArgumentException("Unsupported type of root operator (" + plan.getRootOperator().getClass().getName() + ")");
    }

    public LogicalPlan getPlan() { return plan; }

    public List<Node> getSubs() { return subs; }

    public List<Node> getPreds() { return preds; }

    public List<Node> getObjs() { return objs; }

}
