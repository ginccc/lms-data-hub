package at.ac.webster.serialization;

import java.io.IOException;

/**
 * @author ginccc
 */
public interface IJsonSerialization {
    String serialize(Object model) throws IOException;

    <T> T deserialize(String json, Class<T> type) throws IOException;
}
