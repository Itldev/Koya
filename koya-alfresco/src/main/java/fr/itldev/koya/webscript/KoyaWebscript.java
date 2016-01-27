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
package fr.itldev.koya.webscript;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 *
 */
public abstract class KoyaWebscript {

	private static final Logger LOGGER = Logger.getLogger(KoyaWebscript.class);

	/**
	 * Extracts JSON POST data.
	 *
	 * @param req
	 * @return
	 * @throws java.io.IOException
	 */
	public static Map<String, Object> getJsonMap(WebScriptRequest req) throws IOException {

		JSONParser parser = new JSONParser();
		// TODO improve json POST reading
		try {
			return (JSONObject) parser.parse(req.getContent().getContent());
		} catch (ParseException ex) {
			LOGGER.error(ex.getMessage(), ex);
		}

		return new HashMap<>();

	}

	/**
	 * Extracts URL paramters.
	 *
	 *
	 * @param req
	 * @return
	 */
	public static Map<String, String> getUrlParamsMap(WebScriptRequest req) {
		Map<String, String> params = new HashMap<>();
		params.putAll(req.getServiceMatch().getTemplateVars());
		for (String k : req.getParameterNames()) {
			String param;
			try {
				/**
				 * Decode double encoded url parameter : ex string with accent
				 * characters
				 *
				 * TODO charset permissive implementation
				 */
				param = new String(req.getParameter(k).getBytes("iso-8859-1"), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				param = req.getParameter(k);
				LOGGER.error(ex.getMessage(), ex);

			}

			params.put(k, param);
		}
		return params;
	}

	public static <T> T fromJSON(final TypeReference<T> type, final String jsonPacket) {
		T data = null;

		if (jsonPacket == null) {
			return null;
		}

		try {
			data = new ObjectMapper().readValue(jsonPacket, type);
		} catch (Exception e) {

		}
		return data;
	}

	public static String getObjectAsJson(Object o) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(o);
	}

}
