package se.liu.ida.hefquin.engine.queryplan.utils;

public class TextBasedPlanPrinterBase {

    protected int indentLevel = 0;
    protected StringBuilder builder = new StringBuilder();

    protected String getString() {
        return builder.toString();
    }

    protected void addTabs() {
        for (int i = 0; i < indentLevel; i++)
            builder.append("  ");
    }
}
