package fr.itldev.koya.services.impl.util;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import fr.itldev.koya.model.impl.ContactItem;

public class ContactItemListDeserializer extends
        JsonDeserializer<List<ContactItem>> {

    @Override
    public List<ContactItem> deserialize(JsonParser jp,
            DeserializationContext dc) throws IOException,
            JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jp);
        List<ContactItem> contactItems = mapper.convertValue(node,
                new TypeReference<List<ContactItem>>() {
                });
        return contactItems;
    }
}
