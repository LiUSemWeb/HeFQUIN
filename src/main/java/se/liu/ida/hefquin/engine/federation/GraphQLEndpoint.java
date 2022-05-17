package se.liu.ida.hefquin.engine.federation;

import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.engine.federation.access.GraphQLInterface;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLProperty;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public interface GraphQLEndpoint extends FederationMember
{
	@Override
	GraphQLInterface getInterface();

	/**
     * Verifies that @param className exists
     */
    public boolean containsClass(final String className);
	
	/**
     * Verifies that @param propertyName of @param className exists
     */    
    public boolean containsProperty(final String className, final String propertyName);

	/**
     * @return the GraphQLFieldType of @param propertyName of @param className ,
     * Otherwise return null if unable to find class or property
     */
    public GraphQLFieldType getPropertyFieldType(final String className, final String propertyName);

	/**
     * @return the value type for @param propertyName of @param className ,
     * Otherwise return null if unable to find class or property
     */
    public String getPropertyValueType(final String className, final String propertyName);

    /**
     * @return a set with the names of all the defined classes
     */
    public Set<String> getClasses();

	/**
	 * @return a map of property names to respective GraphQLProperty objects for the class @param className
	 * otherwise @return null if no such class exist.
	 */
	public Map<String,GraphQLProperty> getClassProperties(final String className);

	/**
     * @return a GraphQLEntrypoint for @param className where @param type is used to select an entrypoint
     * for the chosen class. Otherwise return null if the class doesn't exist or if the GraphQLEntrypoint of 
     * the given type isn't mapped to anything.
     */
    public GraphQLEntrypoint getEntrypoint(final String className, final GraphQLEntrypointType type);  
}
