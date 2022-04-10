package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;

public class GraphQLQueryImpl implements GraphQLQuery
{
    protected TreeSet<String> fieldPaths;
    protected JsonObject parameterValues;
    protected Map<String,String> parameterDefinitions;

    public GraphQLQueryImpl(){
        this.fieldPaths = new TreeSet<>();
        this.parameterValues = new JsonObject();
        this.parameterDefinitions = new HashMap<>();
    }

    /**
     * Example: 
     *      addFieldPath("books/title")
     *      addFieldPath("books/author/name")
     */
    public void addFieldPath(String fieldPath){
        fieldPaths.add(fieldPath.replaceAll(" ", ""));
    }

    public void addParameter(String parameterName, JsonValue parameterValue, String graphQLType){
        parameterValues.put(parameterName, parameterValue);
        parameterDefinitions.put(parameterName, graphQLType);
    }

    public String getURL(){
        String url = "?query=";
        if(!parameterDefinitions.isEmpty()){
            url += "query(";
            for(String parameterName : parameterDefinitions.keySet()){
                url += "$" + parameterName + ":" + parameterDefinitions.get(parameterName)+",";
            }
            url += ")";
        }
        url += queryToString();
        url += "&variables="+getParameterString();
        url += "&raw";

        return url;
    }
    
    /**
     * Helper function to create the url string
     */
    protected String queryToString(){
        String urlQuery = "";
        String path = "";
        int depth = 0;

        for(String currentPath : fieldPaths){

            int splitIndex = currentPath.lastIndexOf("/")+1;
            String domain = currentPath.substring(0, splitIndex);
            String field = currentPath.substring(splitIndex);

            // Parse out if domain of currentPath starts differently than actual path
            while(!domain.startsWith(path)){
                urlQuery += "},";
                int i = path.lastIndexOf("/",path.length()-2);
                path = (i>0) ? path.substring(0, i+1) : "";
                --depth;
            }

            // Parse in if domain and path are different, keep adding parts from domain to path until they are the same
            int begin=path.length();
            while(!path.equals(domain)){
                int i = domain.indexOf("/",begin);
                String domainPart = (i>0) ? domain.substring(begin,i) : domain.substring(begin);
                urlQuery += domainPart + "{";
                path += domainPart + "/";
                begin=i+1;
                ++depth;
            }

            urlQuery += field + ",";
        }

        // Parse out completely after final field is added
        while(depth > 0){
            urlQuery += "}";
            --depth;
        }
            
        return "{"+urlQuery+"}";
    }

    protected String getParameterString(){
        return parameterValues.toString().replaceAll(" ", "");
    }
}
