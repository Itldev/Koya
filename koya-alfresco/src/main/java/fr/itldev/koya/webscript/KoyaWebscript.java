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

package fr.itldev.koya.webscript;

import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 */
public abstract class KoyaWebscript extends AbstractWebScript {

    /**
     * Extracts JSON POST data.
     *
     * @param req
     * @return
     * @throws java.io.IOException
     */
    protected Map<String, Object> getJsonMap(WebScriptRequest req) throws IOException {

        JSONParser parser = new JSONParser();
        Reader reader = req.getContent().getReader();
        JSONObject jsonConteneur = null;
        if (reader.ready()) {
            try {
                return (JSONObject) parser.parse(req.getContent().getReader());
            } catch (ParseException ex) {
            }
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
    protected Map<String, String> getUrlParamsMap(WebScriptRequest req) {
        Map<String, String> params = new HashMap<>();
        params.putAll(req.getServiceMatch().getTemplateVars());
        for (String k : req.getParameterNames()) {
            params.put(k, req.getParameter(k));
        }
        return params;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        ItlAlfrescoServiceWrapper wrapper = new ItlAlfrescoServiceWrapper();

        //TODO url params et url template
        try {
            wrapper = koyaExecute(wrapper, getUrlParamsMap(req), getJsonMap(req));
            wrapper.setStatusOK();
        } catch (Exception ex) {
            wrapper.setStatusFail(ex.toString());
        }

        res.setContentType("application/json");
        res.getWriter().write(wrapper.getAsJSON());
    }

    public abstract ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception;

}
