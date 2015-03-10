package fr.itldev.koya.services.impl.util;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

public class NodeRefDeserializer extends JsonDeserializer<NodeRef> {

    @Override
    public NodeRef deserialize(JsonParser jp, DeserializationContext dc)
            throws IOException, JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jp);

        String storeRefProtocol = node.get("storeRef").get("protocol").asText();
        String storeRefIdentifier = node.get("storeRef").get("identifier")
                .asText();
        String id = node.get("id").asText();

        String nodeRefString = storeRefProtocol + "://" + storeRefIdentifier
                + "/" + id ;
        return new NodeRef(nodeRefString);
    }
}
