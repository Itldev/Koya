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
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class PreferencesSerializer extends JsonSerializer<Preferences> {

    @Override
    public void serialize(Preferences t, JsonGenerator jgen, SerializerProvider sp) throws IOException, JsonProcessingException {

        //put values in a hierachical map
        Map hierchicalPrefs = new HashMap();
        for (String k : t.keySet()) {
            recursiveUnstack(k, t.get(k), hierchicalPrefs);
        }

        //serialization
        jgen.writeStartObject();
        for (Object k : hierchicalPrefs.keySet()) {
            jgen.writeObjectField(k.toString(), hierchicalPrefs.get(k));

        }
        jgen.writeObjectField("contentType", t.getContentType());
        jgen.writeEndObject();
    }

    private void recursiveUnstack(String key, Object value, Map container) {
        String[] kparts = key.split("\\.");
        if (key.equals(kparts[0])) {
            container.put(key, value);
        } else {

            Map inner = (Map) container.get(kparts[0]);
            if (inner == null) {
                inner = new HashMap();
                container.put(kparts[0], inner);
            }
            recursiveUnstack(key.substring(key.indexOf('.') + 1), value, inner);
        }
    }

}
