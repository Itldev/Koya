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
package fr.itldev.koya.webscript.global;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.json.MailWrapper;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Send Email.
 *
 */
public class SendMail extends AbstractWebScript {

    private final Logger logger = Logger.getLogger(this.getClass());

    protected ActionService actionService;
    protected AuthenticationService authenticationService;
    protected SearchService searchService;

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        try {

            ObjectMapper mapper = new ObjectMapper();
            MailWrapper mw = mapper.readValue(req.getContent().getReader(), MailWrapper.class);
            logger.error(mw.toString());

            //TODO get authentication info (guest or not --> snder or not )
            try {
                /**
                 * Create 1 action per recipient as they must be registered user
                 * https://forums.alfresco.com/forum/developer-discussions/repository-services/mailactionexecuterparamtomany-parameter-not-working
                 *
                 * TODO find multi user mail sending way
                 */

                for (String recipient : mw.getTo()) {

                    /**
                     * Params Setting
                     */
                    Map<String, Serializable> paramsMail = new HashMap<>();
                    // String currentUserName = authenticationService.getCurrentUserName();
                    paramsMail.put(MailActionExecuter.PARAM_TO, recipient);
                    if (mw.getFrom() != null) {
                        paramsMail.put(MailActionExecuter.PARAM_FROM, mw.getFrom());
                    }
                    paramsMail.put(MailActionExecuter.PARAM_SUBJECT, mw.getSubject());
                    paramsMail.put(MailActionExecuter.PARAM_TEXT, mw.getContent());

                    if (mw.getTemplatePath() != null) {
                        ResultSet resultSet = searchService.query(
                                new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"),
                                SearchService.LANGUAGE_LUCENE, "PATH:\"" + mw.getTemplatePath() + "\"");
                        if (resultSet.length() == 0) {
                            logger.error("Template " + mw.getTemplatePath() + " not found.");
                            return;
                        } else {
                            NodeRef template = resultSet.getNodeRef(0);
                            paramsMail.put(MailActionExecuter.PARAM_TEMPLATE, template);

                            Map<String, Serializable> templateModel = new HashMap<>();
                            templateModel.put("args", (Serializable) mw.getTemplateParams());
                            paramsMail.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);
                        }
                        //paramsMail.put(MailActionExecuter.PARAM_SUBJECT_PARAMS, "");
                    }
                    /**
                     * Action execution
                     */
                    actionService.executeAction(actionService.createAction(
                            MailActionExecuter.NAME, paramsMail), null);
                }

            } catch (Exception ex) {
                logger.error(ex.toString());
                throw new KoyaServiceException(0);
            }

        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }

        res.setContentType("application/json");

        res.getWriter().write("");
    }

}
