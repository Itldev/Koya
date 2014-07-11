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
package fr.itldev.koya.webscript.company;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Preferences;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.webscript.KoyaWebscript;
import fr.itldev.koya.webscript.content.AddContent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Commit company preferences.
 *
 *
 */
public class CommitPreferences extends AbstractWebScript {

    private final Logger logger = Logger.getLogger(AddContent.class);
    private CompanyService companyService;

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
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

    /**
     *
     * @param req
     * @param res
     * @throws IOException
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String companyName = (String) getUrlParamsMap(req).get(KoyaWebscript.WSCONST_COMPANYNAME);
        ItlAlfrescoServiceWrapper wrapper = new ItlAlfrescoServiceWrapper();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Preferences p = mapper.readValue(req.getContent().getReader(), Preferences.class);
            companyService.commitPreferences(companyName, p);
            wrapper.setStatusOK();
        } catch (KoyaServiceException ex) {
            wrapper.setStatusFail(ex.getMessage());
            wrapper.setErrorCode(ex.getErrorCode());
        } catch (IOException ex) {
            wrapper.setStatusFail(ex.toString());
            wrapper.setErrorCode(KoyaErrorCodes.CONTENT_CREATION_INVALID_TYPE);
        } catch (Exception ex) {
            wrapper.setStatusFail(ex.toString());
            wrapper.setErrorCode(KoyaErrorCodes.UNHANDLED);
        }

        res.setContentType("application/json");

        res.getWriter().write(wrapper.getAsJSON());
        logger.trace(wrapper.getAsJSON());
    }
}
