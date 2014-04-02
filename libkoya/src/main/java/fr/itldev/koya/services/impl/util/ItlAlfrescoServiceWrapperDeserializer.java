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

    @Override
    public ItlAlfrescoServiceWrapper deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        //
        JsonNode node = oc.readTree(jsonParser);

        List itemsList = new ArrayList();
        Integer nbItems = node.get("nbitems").getIntValue();

        if (nbItems > 0) {
            try {
                //def des items 
                ArrayNode items = (ArrayNode) node.get("items");

                for (JsonNode n : items) {
                    //Build Object with mapper type
                    itemsList.add(mapper.readValue(n, Class.forName(n.get("contentType").getTextValue())));
                }

            } catch (ClassNotFoundException ex) {
                throw new IOException("Erreur de désérialisation de la liste des elements", ex);
            }
        }
        //construction de l'objet
        ItlAlfrescoServiceWrapper wrapper = new ItlAlfrescoServiceWrapper();
        wrapper.setStatus(node.get("status").getTextValue());
        wrapper.setMessage(node.get("message").getTextValue());
        wrapper.setMessage(node.get("errorCode").getTextValue());
        wrapper.setNbitems(nbItems);
        wrapper.setItems(itemsList);

        return wrapper;

    }

}
