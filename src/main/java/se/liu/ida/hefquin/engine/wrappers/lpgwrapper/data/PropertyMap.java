package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data;

/**
 * Represents the set of property/value pairs that an LPG node or edge can have
 */
public interface PropertyMap {

    /**
     * Retrieves the value of a given property, or null if the property is not present
     * @param key the name of the property required
     */
    Value getValueFor(final String key);

    /**
     * Obtain the collection of property names on the map
     */
    Iterable<String> getPropertyNames();

    /**
     * Obtain all the values stored on the map
     */
    Iterable<Value> getAllValues();

}
