package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.formula;

import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils.QueryAnalyzer;

import java.util.List;

public interface FormulaForComputingSelectivity {

    double estimate( final List<QueryAnalyzer> selectedPlans, final QueryAnalyzer subPlan );

}
