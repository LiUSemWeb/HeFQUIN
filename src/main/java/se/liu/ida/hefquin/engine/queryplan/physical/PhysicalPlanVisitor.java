package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public interface PhysicalPlanVisitor {
    void visit(final PhysicalOpRequest<?,?> op);

    void visit(final PhysicalOpBindJoin op);
    void visit(final PhysicalOpBindJoinWithVALUES op);
    void visit(final PhysicalOpBindJoinWithFILTER op);
<<<<<<< HEAD
    void visit(final PhysicalOpBindJoinWithUNION physicalOpBindJoinWithUNION);
    void visit(final PhysicalOpNaiveNestedLoopsJoin op);
    void visit(final PhysicalOpIndexNestedLoopsJoin op);

    void visit(final PhysicalOpHashJoin physicalOpHashJoin);
=======
    void visit(final PhysicalOpNaiveNestedLoopsJoin op);
    void visit(final PhysicalOpIndexNestedLoopsJoin op);

>>>>>>> 6e96eb9d74b6839c61a31117a25ad65797e00edd
    void visit(final PhysicalOpSymmetricHashJoin op);

    void visit(final PhysicalOpBinaryUnion op);
}
