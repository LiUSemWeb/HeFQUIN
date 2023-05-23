package se.liu.ida.hefquin.engine.data.mappings.impl;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.data.mappings.TermMapping;

import java.util.HashSet;
import java.util.Set;

public class TermMappingImpl implements TermMapping {
    protected final Node type;
    protected final Set<Node> translatedTerms = new HashSet<>();

    public TermMappingImpl( final Node type, final Node term ) {
        this.type = type;
        translatedTerms.add(term);
    }

    public TermMappingImpl( final Node type, final Set<Node> terms ) {
        this.type = type;
        translatedTerms.addAll(terms);
    }

    @Override
    public Node getTypeOfRule() {
        return type;
    }

    @Override
    public Set<Node> getTranslatedTerms() {
        return translatedTerms;
    }

}
