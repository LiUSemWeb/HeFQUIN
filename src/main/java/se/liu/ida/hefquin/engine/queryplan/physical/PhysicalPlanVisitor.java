package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public interface PhysicalPlanVisitor {
    void visit(final PhysicalOpRequest<?,?> op);
    void visit(final PhysicalOpRequestWithTranslation<?,?> op);

    void visit(final PhysicalOpBindJoin op);
    void visit(final PhysicalOpBindJoinWithVALUES op);
    void visit(final PhysicalOpBindJoinWithFILTER op);
    void visit(final PhysicalOpBindJoinWithUNION op);
    void visit(final PhysicalOpNaiveNestedLoopsJoin op);
    void visit(final PhysicalOpIndexNestedLoopsJoin op);

    void visit(final PhysicalOpHashJoin op);
    void visit(final PhysicalOpSymmetricHashJoin op);

    void visit(final PhysicalOpBinaryUnion op);

    void visit(final PhysicalOpFilter op);
}
