package at.ac.webster.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author ginccc
 */

@NoArgsConstructor
@ApplicationScoped
public final class JsonSerialization implements IJsonSerialization {
    @Inject
    ObjectMapper objectMapper;

    @Override
    public String serialize(Object model) throws IOException {
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, model);
        return writer.toString();
    }

    @Override
    public <T> T deserialize(String json, Class<T> type) throws IOException {
        return objectMapper.readerFor(type).readValue(json);
    }
}
