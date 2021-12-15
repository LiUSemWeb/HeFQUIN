package se.liu.ida.hefquin.engine.queryplan.utils;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpUnion;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWalker;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;


public class PhysicalPlanPrinterWithPattern extends PlanPrinter {

    protected final PhysicalPlanPrinterBeforeVisitor beforeVisitor = new PhysicalPlanPrinterBeforeVisitor();
    protected final PhysicalPlanPrinterAfterVisitor afterVisitor = new PhysicalPlanPrinterAfterVisitor();

    static public String printWithPattern( final PhysicalPlan plan ) {
        final PhysicalPlanPrinterWithPattern printer = new PhysicalPlanPrinterWithPattern();
        PhysicalPlanWalker.walk(plan, printer.beforeVisitor, printer.afterVisitor);
        return printer.getString();
    }

    private class PhysicalPlanPrinterBeforeVisitor implements PhysicalPlanVisitor {
        @Override
        public void visit( final PhysicalOpRequest<?, ?> op ) {
            addTabs();
            builder.append("> req ");
            builder.append("( ");

            final DataRetrievalRequest req = op.getLogicalOperator().getRequest();

            builder.append("{ ");
            if ( req instanceof SPARQLRequest ) {
                final SPARQLGraphPattern pattern = ((SPARQLRequest) req).getQueryPattern();
                if ( pattern instanceof TriplePattern ) {
                    builder.append( ((TriplePattern)pattern).asJenaTriple() );
                }
                else if ( pattern instanceof BGP ) {
                    builder.append( ((BGP) pattern).getTriplePatterns() );
                }
                else {
                    printTriplesOfGraphPattern( pattern.asJenaOp() );
                }
            }
            else {
                throw new UnsupportedOperationException( "Print graph pattern of the Request: " + req.getClass().getName()+" is an open TODO." );
            }
            builder.append(" }");

            builder.append(", ");
            printFm( op.getLogicalOperator().getFederationMember() );
            builder.append(" )");

            builder.append( System.lineSeparator() );
        }

        public void printTriplesOfGraphPattern( final Op op ) {
            if ( op instanceof OpBGP ) {
                builder.append( ((OpBGP) op).getPattern().getList() );
            }
            else if ( op instanceof OpJoin ) {
                printTriplesOfGraphPattern( (OpJoin) op );
            }
            else if ( op instanceof OpUnion) {
                printTriplesOfGraphPattern( (OpUnion) op );
            }
            else {
                throw new UnsupportedOperationException("Print triples of an arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + op.getClass().getName() + ").");
            }
        }

        public void printTriplesOfGraphPattern( final OpJoin op ) {
            printTriplesOfGraphPattern( op.getLeft());
            builder.append(" AND ");
            printTriplesOfGraphPattern( op.getRight() );
        }

        public void printTriplesOfGraphPattern( final OpUnion op ) {
            printTriplesOfGraphPattern( op.getLeft() );
            builder.append(" UNION ");
            printTriplesOfGraphPattern( op.getRight() );
        }

        @Override
        public void visit( final PhysicalOpBindJoin op ) {
            addTabs();

            final UnaryLogicalOp lop = op.getLogicalOperator();
            if ( lop instanceof LogicalOpTPAdd)  {
                builder.append( "> tpAdd_bindJoin ");
                printPatternAndFmOfTPAdd ( (LogicalOpTPAdd) lop );
            }
            else {
                throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
            }

            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpBindJoinWithVALUES op ) {
            addTabs();

            final UnaryLogicalOp lop = op.getLogicalOperator();
            if ( lop instanceof LogicalOpTPAdd)  {
                builder.append("> tpAdd_VALUESBindJoin ");
                printPatternAndFmOfTPAdd( (LogicalOpTPAdd) lop);
            }
            else if ( lop instanceof LogicalOpBGPAdd ) {
                builder.append("> bgpAdd_VALUESBindJoin ");
                printPatternAndFmOfBGPAdd((LogicalOpBGPAdd) lop);
            }
            else {
                throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
            }

            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpBindJoinWithFILTER op ) {
            addTabs();

            final UnaryLogicalOp lop = op.getLogicalOperator();
            if ( lop instanceof LogicalOpTPAdd)  {
                builder.append("> tpAdd_FILTERBindJoin ");
                printPatternAndFmOfTPAdd( (LogicalOpTPAdd) lop);
            }
            else if ( lop instanceof LogicalOpBGPAdd ) {
                builder.append("> bgpAdd_FILTERBindJoin ");
                printPatternAndFmOfBGPAdd((LogicalOpBGPAdd) lop);
            }
            else {
                throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
            }

            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpBindJoinWithUNION op ) {
            addTabs();

            final UnaryLogicalOp lop = op.getLogicalOperator();
            if ( lop instanceof LogicalOpTPAdd)  {
                builder.append("> tpAdd_UNIONBindJoin ");
                printPatternAndFmOfTPAdd((LogicalOpTPAdd) lop);
            }
            else if ( lop instanceof LogicalOpBGPAdd ) {
                builder.append("> bgpAdd_UNIONBindJoin ");
                printPatternAndFmOfBGPAdd((LogicalOpBGPAdd) lop);
            }
            else {
                throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
            }

            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpIndexNestedLoopsJoin op ) {
            addTabs();

            final UnaryLogicalOp lop = op.getLogicalOperator();
            if ( lop instanceof LogicalOpTPAdd )  {
                builder.append("> tpAdd_indexNestedLoop ");
                printPatternAndFmOfTPAdd((LogicalOpTPAdd) lop);
            }
            else if ( lop instanceof LogicalOpBGPAdd ) {
                builder.append("> bgpAdd_indexNestedLoop ");
                printPatternAndFmOfBGPAdd((LogicalOpBGPAdd) lop);
            }
            else {
                builder.append("> indexNestedLoop ");
            }

            builder.append(System.lineSeparator());
            indentLevel++;
        }

        private void printPatternAndFmOfBGPAdd( final LogicalOpBGPAdd lop ) {
            builder.append("( ");
            builder.append( lop.getBGP().getTriplePatterns() );
            builder.append(", ");
            printFm( lop.getFederationMember() );
            builder.append(" )");
        }

        private void printPatternAndFmOfTPAdd( final LogicalOpTPAdd lop ) {
            builder.append("( ");
            builder.append( lop.getTP().asJenaTriple() );
            builder.append(", ");
            printFm( lop.getFederationMember() );
            builder.append(" )");
        }

        private void printFm( final FederationMember federationMember ) {
            final DataRetrievalInterface intFace = federationMember.getInterface();
            if ( intFace instanceof SPARQLEndpointInterface){
                builder.append( ((SPARQLEndpointInterface) intFace).getURL() );
            }
            else {
                builder.append( " Print the Federation Member in the type of interface: " + intFace.getClass().getName() + "is an open TODO" );
            }
        }

        @Override
        public void visit( final PhysicalOpNaiveNestedLoopsJoin op ) {
            addTabs();
            builder.append("> naiveNestedLoop ");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpHashJoin op) {
            addTabs();
            builder.append("> hashJoin ");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpSymmetricHashJoin op ) {
            addTabs();
            builder.append("> symmetricHashJoin ");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpBinaryUnion op ) {
            addTabs();
            builder.append("> binaryUnion ");
            builder.append(System.lineSeparator());
            indentLevel++;
        }
    }

    private class PhysicalPlanPrinterAfterVisitor implements PhysicalPlanVisitor {
        @Override
        public void visit(final PhysicalOpRequest<?, ?> op) {
            // nothing to do here
        }

        @Override
        public void visit(final PhysicalOpBindJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpBindJoinWithVALUES op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpBindJoinWithFILTER op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpBindJoinWithUNION physicalOpBindJoinWithUNION) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpNaiveNestedLoopsJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpIndexNestedLoopsJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(PhysicalOpHashJoin physicalOpHashJoin) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpSymmetricHashJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpBinaryUnion op) {
            indentLevel--;
        }
    }

}
