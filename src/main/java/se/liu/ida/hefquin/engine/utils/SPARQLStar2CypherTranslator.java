package se.liu.ida.hefquin.engine.utils;

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
        final Set<? extends TriplePattern> triples = bgp.getTriplePatterns();
        if (triples.size() == 1) {
            return translateTriple((TriplePattern) triples.toArray()[0]);
        }
        return translateBGP(bgp);
    }

    private static String translateBGP( final BGP bgp ) {
        final Set<String> matches = new HashSet<>();
        final Set<? extends TriplePattern> triples = bgp.getTriplePatterns();
        final Set<String> returns = new HashSet<>();
        final Set<String> conditions = new HashSet<>();
        findRelationships(triples, matches, returns, conditions);
        for (TriplePattern tp : triples) {

        }
        return null;
    }

    private static void findRelationships(final Set<? extends TriplePattern> triples,
                                          final Set<String> matches,
                                          final Set<String> returns,
                                          final Set<String> conditions) {
        Set<TriplePattern> toRemove = new HashSet<>();
        for (final TriplePattern tp : triples) {
            Triple triple = tp.asJenaTriple();
            if (triple.getSubject().isNodeTriple()) {
                Triple nestedTriple = triple.getSubject().getTriple();
                if (triple.getPredicate().isURI()
                        && triple.getPredicate().getURI().startsWith(Configurations.PROPERTY_MAPPING)) {
                    if (nestedTriple.getSubject().isVariable() && nestedTriple.getObject().isVariable()) {
                        if (nestedTriple.getPredicate().isURI()
                                && (nestedTriple.getObject().isLiteral() || nestedTriple.getObject().isVariable())
                                && nestedTriple.getPredicate().getURI().startsWith(Configurations.RELATIONSHIP_MAPPING)) {
                            final String subjectVar = nestedTriple.getSubject().getName();
                            final String objectVar = nestedTriple.getObject().getName();
                            final String cypherEdgeVar = CypherVarGenerator.getAnonVar();
                            matches.add(
                                    String.format(NESTED_MATCH_PATTERN, subjectVar,
                                            Configurations.unmapRelationship(nestedTriple.getPredicate().getURI()),
                                            cypherEdgeVar,
                                            Configurations.unmapProperty(triple.getPredicate().getURI()),
                                            triple.getObject().getLiteral().toString(),
                                            objectVar)
                            );
                            returns.add(String.format(RETURN_PATTERN,
                                    CypherVarGenerator.getVarFor(subjectVar),
                                    subjectVar));
                            returns.add(String.format(RETURN_PATTERN,
                                    CypherVarGenerator.getVarFor(objectVar),
                                    objectVar));
                            if (nestedTriple.getObject().isVariable()){
                                returns.add(String.format(RETURN_LITERAL_PATTERN, cypherEdgeVar,
                                        Configurations.unmapProperty(triple.getPredicate().getURI()),
                                        nestedTriple.getObject().getName()));
                                conditions.add(String.format(EXISTS_CONDITION, cypherEdgeVar,
                                        Configurations.unmapProperty(triple.getPredicate().getURI())));
                            }
                            toRemove.add(tp);
                        } else {
                            throw new IllegalArgumentException("Nested triple can only query for relationships");
                        }
                    } else {
                        throw new IllegalArgumentException("Nested triples must have variables in subject and object");
                    }
                } else {
                    throw new IllegalArgumentException("Nested triples only permit property querying");
                }
            }
            if (triple.getSubject().isVariable() && triple.getObject().isVariable()){
                if (triple.getPredicate().isURI()
                        && triple.getPredicate().getURI().startsWith(Configurations.RELATIONSHIP_MAPPING)){
                    final String subjectVar = triple.getSubject().getName();
                    final String objectVar = triple.getObject().getName();
                    matches.add(
                            String.format(MATCH_PATTERN, subjectVar,
                                    Configurations.unmapRelationship(triple.getPredicate().getURI()),
                                    objectVar)
                    );
                    returns.add(String.format(RETURN_PATTERN,
                            CypherVarGenerator.getVarFor(subjectVar),
                            subjectVar));
                    returns.add(String.format(RETURN_PATTERN,
                            CypherVarGenerator.getVarFor(objectVar),
                            objectVar));
                    toRemove.add(tp);
                }
            }
        }
        for (TriplePattern tp : toRemove) {
            triples.remove(tp);
        }
    }

    private static String translateTriple( final TriplePattern pattern ) {
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
                    } else {
                        throw new IllegalArgumentException("Predicate must be the label URI or the mapping of a Relationship." +
                                "Object must be a literal or a class mapping");
                    }
                } else {
                    throw new IllegalArgumentException("Predicate must be an URI. Object must be a literal or an URI");
                }
            } else {
                throw new IllegalArgumentException("Variables in the predicate are not permitted");
            }
        } else if (pattern.numberOfVars() == 2) {
            if (s.isVariable() && o.isVariable()) {
                if (p.isURI() && p.getURI().equals(Configurations.LABEL_URI)) {
                    return Translations.getVarLabelVar(s, p, o);
                } else if (p.isURI() && p.getURI().startsWith(Configurations.RELATIONSHIP_MAPPING)) {
                    return Translations.getVarRelationshipVar(s, p, o);
                } else if (p.isURI() && p.getURI().startsWith(Configurations.PROPERTY_MAPPING)) {
                    return Translations.getVarPropertyVar(s, p, o);
                } else {
                    throw new IllegalArgumentException("Predicate must be a mapping of a property or a relationship or the label URI");
                }
            } else {
                throw new IllegalArgumentException("Variables in the predicate are not permitted");
            }
        } else {
            return Translations.getVarVarVar(s, p, o);
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
        final protected static String varLabelVar   = "MATCH (cpvar1) RETURN nm(cpvar1) AS %s, labels(cpvar1) AS %s";
        final protected static String varRelVar     = "MATCH (cpvar1)-[:%s]->(cpvar2) RETURN nm(cpvar1) AS %s, nm(cpvar2) AS %s";
        final protected static String varPropVar    = "MATCH (cpvar1) WHERE EXISTS(cpvar1.%s) " +
                "RETURN nm(cpvar1) AS r1, '' AS r2, '' AS r3, cpvar1.%s AS %s UNION " +
                "MATCH (cpvar2)-[cpvar3]->(cpvar4) WHERE EXISTS(cpvar3.%s) " +
                "RETURN nm(cpvar2) AS r1, elm(cpvar3) AS r2, nm(cpvar4) AS r3, cpvar3.%s AS %s";
        final protected static String varVarVar     = "MATCH (cpvar1)-[cpvar2]->(cpvar3) " +
                "RETURN nm(cpvar1) AS %s, elm(cpvar2) AS %s, nm(cpvar3) AS %s UNION " +
                "MATCH (cpvar4) RETURN nm(cpvar4) AS %s, %s AS %s, labels(cpvar4) AS %s; " +
                "MATCH (cpvar5) RETURN cpvar5 AS s UNION MATCH ()-[cpvar6]->() RETURN cpvar6 AS s";

        public static String getVarPropertyLiteral( final Node s, final Node p, final Node o ) {
            final String property = Configurations.unmapProperty(p.getURI());
            final String literal = o.getLiteralValue().toString();
            return String.format(varPropLit, property, literal, property, literal);
        }

        public static String getVarLabelClass( final Node s, final Node p, final Node o ) {
            final String clazz = Configurations.unmapClass(o.getURI());
            return String.format(varLabelClass, clazz, s.getName());
        }

        public static String getVarRelationshipURI( final Node s, final Node p, final Node o ) {
            final String relationship = Configurations.unmapRelationship(p.getURI());
            final int nodeID = Configurations.unmapNode(o.getURI());
            return String.format(varRelURI, relationship, nodeID, s.getName());
        }

        public static String getVarLabelVar( final Node s, final Node p, final Node o ) {
            return String.format(varLabelVar, s.getName(), o.getName());
        }

        public static String getVarRelationshipVar( final Node s, final Node p, final Node o ) {
            final String relationship = Configurations.unmapRelationship(p.getURI());
            return String.format(varRelVar, relationship, s.getName(), o.getName());
        }

        public static String getVarPropertyVar( final Node s, final Node p, final Node o ) {
            final String property = Configurations.unmapProperty(p.getURI());
            final String objectVar = o.getName();
            return String.format(varPropVar, property, property, objectVar, property, property, objectVar);
        }

        public static String getVarVarVar( final Node s, final Node p, final Node o ) {
            return String.format(varVarVar, s.getName(), p.getName(), o.getName(),
                    s.getName(), Configurations.LABEL_URI, p.getName(), o.getName());
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
