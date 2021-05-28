package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.*;

public interface ExecutablePlanVisitor {

    void visit(final ExecOpRequestBRTPF op);
    void visit(final ExecOpRequestSPARQL op);
    void visit(final ExecOpRequestTPFatBRTPFServer op);
    void visit(final ExecOpRequestTPFatTPFServer op);

    void visit(final ExecOpBindJoinBRTPF op);
    void visit(final ExecOpBindJoinSPARQLwithFILTER op);
    void visit(final ExecOpBindJoinSPARQLwithUNION op);
    void visit(final ExecOpBindJoinSPARQLwithVALUES op);

    void visit(final ExecOpNaiveNestedLoopsJoin op);
    void visit(final ExecOpIndexNestedLoopsJoinTPF op);
    void visit(final ExecOpIndexNestedLoopsJoinBRTPF op);
    void visit(final ExecOpIndexNestedLoopsJoinSPARQL op);
    void visit(final ExecOpHashJoin op);
    void visit(final ExecOpSymmetricHashJoin op);

    void visit(final ExecOpBinaryUnion op);

}
