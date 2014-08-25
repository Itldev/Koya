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

import fr.itldev.koya.model.impl.Preferences;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

public class PreferencesDeserializer extends JsonDeserializer<Preferences> {
    
    private Logger logger = Logger.getLogger(this.getClass());
    
    @Override
    public Preferences deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        Preferences pref = new Preferences();
        recursiveDeserialize(jp.readValueAsTree(), "", "", pref);
        
        return pref;
    }
    
    private void recursiveDeserialize(JsonNode n, String key, String keySep, Preferences pref) {
        
        if (n.isContainerNode()) {
            Iterator<Entry<String, JsonNode>> it = n.getFields();
            while (it.hasNext()) {
                Entry<String, JsonNode> e = it.next();
                recursiveDeserialize(e.getValue(), key + keySep + e.getKey(), ".", pref);
            }
            
        } else {
            //exclude contentType attribute
            if (!key.equals(ItlAlfrescoServiceWrapperDeserializer.ATTRIBUTE_CONTENT_TYPE)) {               
                if (n.isBoolean()) {
                    pref.put(key, n.getBooleanValue());
                } else if (n.isTextual()) {
                    pref.put(key, n.getTextValue());
                } else {
                    logger.error("unhandled type for deserialization : " + key);
                }
            }
        }
        
    }
    
}
