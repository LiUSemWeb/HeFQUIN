package se.liu.ida.hefquin.engine.data.mappings.impl;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.data.mappings.TermMapping;

import java.util.HashSet;
import java.util.Set;

public class TermMappingImpl implements TermMapping {
    protected final Node type;
    protected final Set<Node> translatedTerms = new HashSet<>();

    public TermMappingImpl( final Node type ) {
        this.type = type;
    }

    public void addTranslatedTerm( final Node term ) {
        translatedTerms.add(term);
    }

    public void addTranslatedTerm( final Set<Node> term ) {
        translatedTerms.addAll(term);
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
