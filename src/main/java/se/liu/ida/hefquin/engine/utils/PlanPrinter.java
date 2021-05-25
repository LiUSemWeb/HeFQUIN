package se.liu.ida.hefquin.engine.utils;

public class PlanPrinter {

    protected int indentLevel = 0;
    protected StringBuilder builder = new StringBuilder();

    protected void addTabs() {
        for (int i = 0; i < indentLevel; i++)
            builder.append("  ");
    }
}
