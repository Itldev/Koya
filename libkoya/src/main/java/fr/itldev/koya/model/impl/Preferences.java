/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

package fr.itldev.koya.model.impl;

import java.util.HashMap;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import fr.itldev.koya.services.impl.util.PreferencesDeserializer;
import fr.itldev.koya.services.impl.util.PreferencesSerializer;

@JsonDeserialize(using = PreferencesDeserializer.class)
@JsonSerialize(using = PreferencesSerializer.class)
public final class Preferences extends HashMap<String, Object> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public String toString() {
        StringBuilder ts = new StringBuilder(" ==== Preferences ===== \n");

        for (String k : this.keySet()) {
            ts.append(k).append(" = ").append(this.get(k).toString()).append("\n");
        }

        return ts.toString();
    }
}
