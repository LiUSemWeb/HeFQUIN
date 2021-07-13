package se.liu.ida.hefquin.engine.utils.lpg;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SPARQLStar2CypherTranslator {

    final static private String MATCH_PATTERN = "MATCH (%s)-[:%s]->(%s)";
    final static private String NESTED_MATCH_PATTERN = "MATCH (%s)-[%s:%s {%s : '%s'}]->(%s)";
    final static private String RETURN_PATTERN = "%s AS %s";
    final static private String RETURN_LITERAL_PATTERN = "%s.%s AS %s";
    final static private String EXISTS_CONDITION = "EXISTS(%s.%s)";

    public static String translate( final BGP bgp ) {
        return translate(bgp, new DefaultConfiguration());
    }

    public static String translate( final BGP bgp, final Configuration configuration ) {
        final Set<? extends TriplePattern> triples = bgp.getTriplePatterns();
        if (triples.size() == 1) {
            return translateTriple((TriplePattern) triples.toArray()[0], configuration);
        }
        return translateBGP(bgp);
    }

    private static String translateBGP( final BGP bgp ) {
        final Set<String> matches = new HashSet<>();
        final Set<? extends TriplePattern> triples = bgp.getTriplePatterns();
        final Set<String> returns = new HashSet<>();
        final Set<String> conditions = new HashSet<>();
        return null;
    }

    private static String translateTriple(final TriplePattern pattern, Configuration configuration) {
        final Triple b = pattern.asJenaTriple();
        final Node s = b.getSubject();
        final Node p = b.getPredicate();
        final Node o = b.getObject();
        if (pattern.numberOfVars() == 1){
            if (s.isVariable()) {
                if (p.isURI() && o.isLiteral() && configuration.mapsToProperty(p.getURI())) {
                    return Translations.getVarPropertyLiteral(s, p, o, configuration);
                }
                else if (p.isURI() && (o.isURI() || o.isBlank())) {
                    if (configuration.isLabelIRI(p.getURI()) && configuration.mapsToLabel(o.getURI())) {
                        return Translations.getVarLabelClass(s, p, o, configuration);
                    }
                    else if (configuration.mapsToRelationship(p.getURI()) &&
                            (o.isBlank() || configuration.mapsToNode(o.getURI()))){
                        return Translations.getVarRelationshipURI(s, p, o, configuration);
                    }
                    else {
                        throw new IllegalArgumentException("Predicate must be the label URI or the mapping of a " +
                                "Relationship. Object must be a literal or a class mapping");
                    }
                }
                else {
                    throw new IllegalArgumentException("Predicate must be an URI. Object must be a literal or an URI");
                }
            }
            else if (o.isVariable()) {
                if (p.isURI() && configuration.isLabelIRI(p.getURI())
                        && (s.isBlank() || (s.isURI() && configuration.mapsToNode(s.getURI())))) {
                    return Translations.getNodeLabelVar(s, p, o, configuration);
                } else if (p.isURI() && configuration.mapsToProperty(p.getURI())
                        && (s.isBlank() || (s.isURI() && configuration.mapsToNode(s.getURI())))) {
                    return Translations.getNodePropertyVar(s, p, o, configuration);
                } else if (p.isURI() && configuration.mapsToRelationship(p.getURI())
                        && (s.isBlank() || (s.isURI() && configuration.mapsToNode(s.getURI())))) {
                    return Translations.getNodeRelationshipVar(s, p, o, configuration);
                } else {
                    throw new IllegalArgumentException("Illegal values for subject and predicate");
                }
            }
            else if (p.isVariable()) {
                if ((s.isBlank() || (s.isURI() && configuration.mapsToNode(s.getURI())))
                        && (o.isBlank() || (o.isURI() && configuration.mapsToNode(o.getURI())))) {
                    return Translations.getNodeVarNode(s, p, o, configuration);
                } else if ((s.isBlank() || (s.isURI() && configuration.mapsToNode(s.getURI()))) && o.isLiteral()) {
                    return Translations.getNodeVarLiteral(s, p, o, configuration);
                } else if ((s.isBlank() || (s.isURI() && configuration.mapsToNode(s.getURI())))
                        && o.isURI() && configuration.mapsToLabel(o.getURI())) {
                    return Translations.getNodeVarLabel(s, p, o, configuration);
                } else {
                    throw new IllegalArgumentException("Illegal values for subject and object");
                }
            }
        } else if (pattern.numberOfVars() == 2) {
            if (s.isVariable() && o.isVariable()) {
                if (p.isURI() && configuration.isLabelIRI(p.getURI())) {
                    return Translations.getVarLabelVar(s, p, o, configuration);
                } else if (p.isURI() && configuration.mapsToRelationship(p.getURI())) {
                    return Translations.getVarRelationshipVar(s, p, o, configuration);
                } else if (p.isURI() && configuration.mapsToProperty(p.getURI())) {
                    return Translations.getVarPropertyVar(s, p, o, configuration);
                } else {
                    throw new IllegalArgumentException("Predicate must be a mapping of a property or a relationship or " +
                            "the label URI");
                }
            } else if (s.isVariable() && p.isVariable()) {
                if (o.isURI() && configuration.mapsToLabel(o.getURI())) {
                    return Translations.getVarVarLabel(s, p, o, configuration);
                } else if (o.isBlank() || (o.isURI() && configuration.mapsToNode(o.getURI()))) {
                    return Translations.getVarVarNode(s, p, o, configuration);
                } else if (o.isLiteral()) {
                    return Translations.getVarVarLiteral(s, p, o, configuration);
                }
            } else {
                if (s.isBlank() || (s.isURI() && configuration.mapsToNode(s.getURI()))) {
                    return Translations.getNodeVarVar(s, p, o, configuration);
                }
            }
        } else {
            return Translations.getVarVarVar(s, p, o, configuration);
        }
        return null;
    }

    private static class Translations {

        final protected static String varPropLit    = "MATCH (cpvar1) WHERE cpvar1.%s = '%s' " +
                "RETURN nm(cpvar1) AS r1, '' AS r2, '' AS r3 " +
                "UNION MATCH (cpvar2)-[cpvar3]->(cpvar4) WHERE cpvar2.%s = '%s' " +
                "RETURN nm(cpvar2) AS r1, elm(cpvar3) AS r2, nm(cpvar4) AS r3";
        final protected static String varLabelClass = "MATCH (cpvar1) WHERE cpvar1:%s RETURN nm(cpvar1) AS %s";
        final protected static String varRelURI     = "MATCH (cpvar1)-[:%s]->(cpvar2) WHERE ID(cpvar2) = %s RETURN nm(cpvar1) AS %s";
        final protected static String nodeLabelVar  = "MATCH (cpvar1) WHERE ID(cpvar1)=%s RETURN labels(cpvar1) AS %s";
        final protected static String nodePropVar   = "MATCH (cpvar1) WHERE ID(cpvar1)=%s AND EXISTS(cpvar1.%s) RETURN cpvar1.%s AS %s";
        final protected static String nodeRelVar    = "MATCH (cpvar1)-[:%s]->(cpvar2) WHERE ID(cpvar1)=%s RETURN nm(cpvar2) AS %s";
        final protected static String nodeVarNode   = "MATCH (cpvar1)-[cpvar2]->(cpvar3) WHERE ID(cpvar1)=%s AND ID(cpvar3)=%s RETURN elm(cpvar2) AS %s";
        final protected static String nodeVarLit    = "MATCH (cpvar1) WHERE ID(cpvar1)=%s RETURN [k in KEYS(cpvar1) WHERE cpvar1[k]='%s' | pm(k)] AS %s";
        final protected static String nodeVarLabel  = "RETURN %s AS %s";
        final protected static String varLabelVar   = "MATCH (cpvar1) RETURN nm(cpvar1) AS %s, labels(cpvar1) AS %s";
        final protected static String varRelVar     = "MATCH (cpvar1)-[:%s]->(cpvar2) RETURN nm(cpvar1) AS %s, nm(cpvar2) AS %s";
        final protected static String varPropVar    = "MATCH (cpvar1) WHERE EXISTS(cpvar1.%s) " +
                "RETURN nm(cpvar1) AS r1, '' AS r2, '' AS r3, cpvar1.%s AS %s UNION " +
                "MATCH (cpvar2)-[cpvar3]->(cpvar4) WHERE EXISTS(cpvar3.%s) " +
                "RETURN nm(cpvar2) AS r1, elm(cpvar3) AS r2, nm(cpvar4) AS r3, cpvar3.%s AS %s";
        final protected static String varVarLabel   = "MATCH (cpvar1) WHERE cpvar1:%s RETURN nm(cpvar1) AS %s, %s AS %s";
        final protected static String varVarNode    = "MATCH (cpvar1)-[cpvar2]->(cpvar3) WHERE ID(cpvar3)=%s RETURN nm(cpvar1) AS %s, elm(cpvar2) AS %s";
        final protected static String varVarLit     = "MATCH (cpvar1)-[cpvar2]->(cpvar3) " +
                "RETURN nm(cpvar1) AS r1, elm(cpvar2) AS r2, nm(cpvar3) AS r3, " +
                "[k IN KEYS(cpvar2) WHERE cpvar2[k]='%s' | pm(k)] AS %s UNION " +
                "MATCH (cpvar4) RETURN nm(cpvar4) AS r1, '' AS r2, '' AS r3, " +
                "[k IN KEYS(cpvar4) WHERE cpvar4[k]='%s' | pm(k)] AS %s";
        final protected static String nodeVarVar    = "MATCH (cpvar1) WHERE ID(cpvar1)=%s " +
                "RETURN %s AS %s, labels(cpvar1) AS %s UNION " +
                "MATCH (cpvar2) WHERE ID(cpvar2)=%s " +
                "RETURN [k IN KEYS(cpvar2) | pm(k)] AS %s, " +
                "[k in KEYS(cpvar2) | cpvar2[k]] AS %s UNION " +
                "MATCH (cpvar3)-[cpvar4]->(cpvar5) WHERE ID(cpvar3)=%s " +
                "RETURN elm(cpvar4) AS %s, nm(cpvar5) AS %s";
        final protected static String varVarVar     = "MATCH (cpvar1)-[cpvar2]->(cpvar3) " +
                "RETURN nm(cpvar1) AS %s, elm(cpvar2) AS %s, nm(cpvar3) AS %s UNION " +
                "MATCH (cpvar4) RETURN nm(cpvar4) AS %s, %s AS %s, labels(cpvar4) AS %s; " +
                "MATCH (cpvar5) RETURN cpvar5 AS s UNION MATCH ()-[cpvar6]->() RETURN cpvar6 AS s";

        public static String getVarPropertyLiteral(final Node s, final Node p, final Node o,
                                                   final Configuration configuration) {
            final String property = configuration.unmapProperty(p.getURI());
            final String literal = o.getLiteralValue().toString();
            return String.format(varPropLit, property, literal, property, literal);
        }

        public static String getVarLabelClass(final Node s, final Node p, final Node o,
                                              final Configuration configuration) {
            final String clazz = configuration.unmapLabel(o.getURI());
            return String.format(varLabelClass, clazz, s.getName());
        }

        public static String getVarRelationshipURI(final Node s, final Node p, final Node o,
                                                   final Configuration configuration) {
            final String relationship = configuration.unmapRelationship(p.getURI());
            final String nodeID = configuration.unmapNode(o.getURI());
            return String.format(varRelURI, relationship, nodeID, s.getName());
        }

        public static String getNodeLabelVar(final Node s, final Node p, final Node o,
                                             final Configuration configuration) {
            return String.format(nodeLabelVar, configuration.unmapNode(s.getURI()), o.getName());
        }

        public static String getNodePropertyVar(final Node s, final Node p, final Node o,
                                                final Configuration configuration) {
            final String property = configuration.unmapProperty(p.getURI());
            return String.format(nodePropVar, configuration.unmapNode(s.getURI()), property,
                    property, o.getName());
        }

        public static String getNodeRelationshipVar(final Node s, final Node p, final Node o,
                                                    final Configuration configuration) {
            return String.format(nodeRelVar, configuration.unmapRelationship(p.getURI()),
                    configuration.unmapNode(s.getURI()), o.getName());
        }

        public static String getNodeVarNode(final Node s, final Node p, final Node o,
                                            final Configuration configuration) {
            return String.format(nodeVarNode, configuration.unmapNode(s.getURI()),
                    configuration.unmapNode(o.getURI()), p.getName());
        }

        public static String getNodeVarLiteral(final Node s, final Node p, final Node o,
                                               final Configuration configuration) {
            return String.format(nodeVarLit, configuration.unmapNode(s.getURI()),
                    o.getLiteralValue(), p.getName());
        }

        public static String getNodeVarLabel(final Node s, final Node p, final Node o,
                                             final Configuration configuration) {
            return String.format(nodeVarLabel, configuration.getLabelIRI(), p.getName());
        }

        public static String getVarLabelVar(final Node s, final Node p, final Node o,
                                            final Configuration configuration) {
            return String.format(varLabelVar, s.getName(), o.getName());
        }

        public static String getVarRelationshipVar(final Node s, final Node p, final Node o,
                                                   final Configuration configuration) {
            final String relationship = configuration.unmapRelationship(p.getURI());
            return String.format(varRelVar, relationship, s.getName(), o.getName());
        }

        public static String getVarPropertyVar(final Node s, final Node p, final Node o,
                                               final Configuration configuration) {
            final String property = configuration.unmapProperty(p.getURI());
            final String objectVar = o.getName();
            return String.format(varPropVar, property, property, objectVar, property, property, objectVar);
        }

        public static String getVarVarLabel(final Node s, final Node p, final Node o,
                                            final Configuration configuration) {
            return String.format(varVarLabel, configuration.unmapLabel(o.getURI()), s.getName(),
                    configuration.getLabelIRI(), p.getName());
        }

        public static String getVarVarNode(final Node s, final Node p, final Node o,
                                           final Configuration configuration) {
            return String.format(varVarNode, configuration.unmapNode(o.getURI()), s.getName(), p.getName());
        }

        public static String getVarVarLiteral(final Node s, final Node p, final Node o,
                                              final Configuration configuration) {
            return String.format(varVarLit, o.getLiteralValue(), p.getName(), o.getLiteralValue(), p.getName());
        }

        public static String getNodeVarVar(final Node s, final Node p, final Node o,
                                           final Configuration configuration) {
            String nodeID = configuration.unmapNode(s.getURI());
            return String.format(nodeVarVar, nodeID, configuration.getLabelIRI(), p.getName(), o.getName(),
                    nodeID, p.getName(), o.getName(), nodeID, p.getName(), o.getName());
        }

        public static String getVarVarVar(final Node s, final Node p, final Node o,
                                          final Configuration configuration) {
            return String.format(varVarVar, s.getName(), p.getName(), o.getName(),
                    s.getName(), configuration.getLabelIRI(), p.getName(), o.getName());
        }

    }

    protected static class CypherVarGenerator {

        protected static final Map<String, String> varmap = new HashMap<>();
        protected static final String VAR_PATTERN = "cpvar%d";
        protected static int current = 1;

        public static String getVarFor( final String var ) {
            if (varmap.containsKey(var))
                return varmap.get(var);
            varmap.put(var, String.format(VAR_PATTERN, current));
            current++;
            return varmap.get(var);
        }

        public static String getAnonVar() {
            String var = String.format(VAR_PATTERN, current);
            current++;
            return var;
        }
    }
}
