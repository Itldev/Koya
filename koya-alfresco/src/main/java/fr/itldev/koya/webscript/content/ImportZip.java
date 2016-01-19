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
package fr.itldev.koya.webscript.content;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.action.UnzipActionExecuter;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.exceptions.KoyaErrorCodes;
import fr.itldev.koya.model.json.RestConstants;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * 
 * Import Zip Webscript
 * 
 * 
 * 
 */
public class ImportZip extends AbstractWebScript {

	private final Logger logger = Logger.getLogger(ImportZip.class);

	protected NodeService nodeService;
	protected ActionService actionService;

	/* services */
	private KoyaNodeService koyaNodeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> urlParamsMap = KoyaWebscript.getUrlParamsMap(req);

		try {
			NodeRef zipNodeRef = koyaNodeService.getNodeRef((String) urlParamsMap.get(RestConstants.WSCONST_NODEREF));

			importZip(zipNodeRef);
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString(), ex);
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write("");
	}

	private void importZip(NodeRef zipFile) throws KoyaServiceException {
		try {
			Map<String, Serializable> paramsImport = new HashMap<>();
			paramsImport.put(ImporterActionExecuter.PARAM_DESTINATION_FOLDER,
					nodeService.getPrimaryParent(zipFile).getParentRef());
			Action importZip = actionService.createAction(UnzipActionExecuter.NAME, paramsImport);
			// /**
			// * Process must be executed synchronously in order to delete
			// * original zip
			// *
			// * We could also have written a new action that extracts AND
			// delete
			// * zip.
			// */
			importZip.setExecuteAsynchronously(false);
			actionService.executeAction(importZip, zipFile);
		} catch (KoyaServiceException kse) {
			throw kse;
		} catch (Exception ex) {
			throw new KoyaServiceException(KoyaErrorCodes.ZIP_EXTRACTION_PROCESS_ERROR, ex);
		}

	}
}
