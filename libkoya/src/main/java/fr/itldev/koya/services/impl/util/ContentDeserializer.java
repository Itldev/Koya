package fr.itldev.koya.services.impl.util;

import fr.itldev.koya.model.Content;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

public class ContentDeserializer extends JsonDeserializer<Content> {
    
    @Override
    public Content deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        
        Content content = null;
        try {
            ObjectCodec oc = jp.getCodec();
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
            //
            JsonNode node = oc.readTree(jp);
            
            String contentType = node.get("contentType").getTextValue();
            content = (Content) Class.forName(contentType).newInstance();
            
            content.setName(node.get("name").getTextValue());
            content.setNodeRef(node.get("nodeRef").getTextValue());
            content.setPath(node.get("path").getTextValue());
            content.setParentNodeRef(node.get("parentNodeRef").getTextValue());
            content.setUserFavourite(node.get("userFavourite").getBooleanValue());
            
            if (Directory.class.isAssignableFrom(content.getClass())) {

                /**
                 * Builds recursivly childrens list (content)
                 */
                List<Content> childList = new ArrayList<>();
                ArrayNode items = (ArrayNode) node.get("children");
                for (JsonNode n : items) {
                    childList.add(mapper.readValue(n, Content.class));
                }
                ((Directory) content).setChildren(childList);
                
            } else {
                ((Document) content).setByteSize(node.get("byteSize").getLongValue());
            }
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            
        }
        
        return content;
    }
    
}
