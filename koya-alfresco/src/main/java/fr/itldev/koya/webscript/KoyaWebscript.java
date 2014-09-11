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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 *
 */
public abstract class KoyaWebscript {

    public static final String WSCONST_NODEREF = "nodeRef";
    public static final String WSCONST_NAME = "name";
    public static final String WSCONST_SHORTNAME = "shortname";
    public static final String WSCONST_TEMPLATENAME = "templatename";
    public static final String WSCONST_NBANCESTOR = "nbAncestor";
    public static final String WSCONST_PARENTNODEREF = "parentNodeRef";
    public static final String WSCONST_USERNAME = "userName";
    public static final String WSCONST_COMPANYNAME = "companyName";
    public static final String WSCONST_ROLENAME = "roleName";
    public static final String WSCONST_ONLYFOLDERS = "onlyFolders";
    public static final String WSCONST_QUERY = "query";
    public static final String WSCONST_ROLEFILTER = "roleFilter";
    public static final String WSCONST_MAXRESULTS = "maxResults";
    public static final String WSCONST_EMAIL = "email";
    public static final String WSCONST_COMPANIESFILTER = "companiesFilter";
    public static final String WSCONST_PREFKEY = "preferenceKey";
    public static final String WSCONST_ENABLE = "enable";
    public static final String WSCONST_MAXDEPTH = "maxdepth";
    public static final String WSCONST_USERFAVOURITE = "userFavourite";
    public static final String WSCONST_NEWNAME = "newName";
    public static final String WSCONST_ACTIVE = "active";

    /**
     * Extracts JSON POST data.
     *
     * @param req
     * @return
     * @throws java.io.IOException
     */
    public static Map<String, Object> getJsonMap(WebScriptRequest req) throws IOException {

        JSONParser parser = new JSONParser();
        //TODO improve json POST reading
        try {
            return (JSONObject) parser.parse(req.getContent().getReader());
        } catch (ParseException ex) {
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
            params.put(k, req.getParameter(k));
        }
        return params;
    }

    /**
     * TODO change this implementation because it's based on static definition
     * of attributes : make it dynamic.
     */
    public final static String[] ESCAPED_FIELDS_NAMES = {"name", "title", "companyName", "companyAddress", "description", "legalInformations"};

    /**
     * escapes identified fields after json serialization
     *
     * @param jsonWrapper
     * @return
     */
    private static String escapeWrapper(String jsonWrapper) {

        for (String fieldName : ESCAPED_FIELDS_NAMES) {
            //match on name attribute
            Pattern p = Pattern.compile("\\\"" + fieldName + "\\\":\\\"([^\\\"]*)\\\"");
            Matcher m = p.matcher(jsonWrapper);
            while (m.find()) {
                jsonWrapper = jsonWrapper.replace(m.group(1), StringEscapeUtils.escapeJava(m.group(1)));
            }
        }

        return jsonWrapper;
    }

    public static String getObjectAsJson(Object o) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return escapeWrapper(mapper.writeValueAsString(o));
    }

}
