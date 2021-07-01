package se.liu.ida.hefquin.engine.utils;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;

import java.util.Set;

public class SPARQLStar2CypherTranslator {

    public static String translate( final BGP bgp ) {
        final Set<? extends TriplePattern> triples = bgp.getTriplePatterns();
        if (triples.size() == 1) {
            return translateTriple((TriplePattern) triples.toArray()[0]);
        }
        return null;
    }

    private static String translateTriple(final TriplePattern pattern) {
        final Triple b = pattern.asJenaTriple();
        final Node s = b.getSubject();
        final Node p = b.getPredicate();
        final Node o = b.getObject();
        if (pattern.numberOfVars() == 1){
            if (s.isVariable()) {
                if (p.isURI() && o.isLiteral()) {
                    if (p.getURI().startsWith(Configurations.PROPERTY_MAPPING)) {
                        return Translations.getVarPropertyLiteral(s, p, o);
                    }
                } else if (p.isURI() && o.isURI()) {
                    if (p.getURI().equals(Configurations.LABEL_URI)
                            && o.getURI().startsWith(Configurations.CLASS_MAPPING)) {
                        return Translations.getVarLabelClass(s, p, o);
                    } else if (p.getURI().startsWith(Configurations.RELATIONSHIP_MAPPING)
                            && o.getURI().startsWith(Configurations.NODE_MAPPING)){
                        return Translations.getVarRelationshipURI(s, p, o);
                    }
                }
            }
        }
        return null;
    }

    private static class Translations {

        final protected static String varPropLit = "MATCH (cpvar1) WHERE cpvar1.%s = '%s' RETURN nm(cpvar1) AS %s " +
                                  "UNION MATCH ()-[cpvar2]->() WHERE cpvar2.%s = '%s' RETURN elm(cpvar2) AS %s";
        final protected static String varLabelClass = "MATCH (cpvar1) WHERE cpvar1:%s RETURN nm(cpvar1) AS %s";
        final protected static String varRelURI = "MATCH (cpvar1)-[:%s]->(cpvar2) " +
                                  "WHERE ID(cpvar2) = %s RETURN nm(cpvar1) AS %s";

        public static String getVarPropertyLiteral( final Node s, final Node p, final Node o ) {
            final String property = Configurations.unmapProperty(p.getURI());
            final String literal = o.getLiteralValue().toString();
            final String varName = s.getName();
            return String.format(varPropLit, property, literal, varName, property, literal, varName);
        }

        public static String getVarLabelClass( final Node s, final Node p, final Node o ) {
            final String clazz = Configurations.unmapClass(o.getURI());
            return String.format(varLabelClass, clazz, s.getName());
        }

        public static String getVarRelationshipURI( final Node s, final Node p, final Node o) {
            final String relationship = Configurations.unmapRelationship(p.getURI());
            final int nodeID = Configurations.unmapNode(o.getURI());
            return String.format(varRelURI, relationship, nodeID, s.getName());
        }
    }
}
