/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.services.impl.util;

import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

public class ItlAlfrescoServiceWrapperDeserializer extends JsonDeserializer<ItlAlfrescoServiceWrapper> {

    public static final String ATTRIBUTE_CONTENT_TYPE = "contentType";
    public static final String ATTRIBUTE_ITEMS = "items";
    public static final String ATTRIBUTE_NBITEMS = "nbitems";
    public static final String ATTRIBUTE_STATUS = "status";
    public static final String ATTRIBUTE_MESSAGE = "message";
    public static final String ATTRIBUTE_ERRORCODE = "errorCode";

    @Override
    public ItlAlfrescoServiceWrapper deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        //
        JsonNode node = oc.readTree(jsonParser);

        List itemsList = new ArrayList();
        Integer nbItems = node.get(ATTRIBUTE_NBITEMS).getIntValue();

        if (nbItems > 0) {
            try {
                //def des items 
                ArrayNode items = (ArrayNode) node.get(ATTRIBUTE_ITEMS);

                for (JsonNode n : items) {
                    //Build Object with mapper type
                    itemsList.add(mapper.readValue(n, Class.forName(n.get(ATTRIBUTE_CONTENT_TYPE).getTextValue())));
                }

            } catch (ClassNotFoundException ex) {
                throw new IOException("Deserialization error in items list", ex);
            }
        }
        //construction de l'objet
        ItlAlfrescoServiceWrapper wrapper = new ItlAlfrescoServiceWrapper();
        wrapper.setStatus(node.get(ATTRIBUTE_STATUS).getTextValue());
        wrapper.setMessage(node.get(ATTRIBUTE_MESSAGE).getTextValue());
        wrapper.setErrorCode(node.get(ATTRIBUTE_ERRORCODE).asInt());
        wrapper.setNbitems(nbItems);
        wrapper.setItems(itemsList);

        return wrapper;

    }

}
