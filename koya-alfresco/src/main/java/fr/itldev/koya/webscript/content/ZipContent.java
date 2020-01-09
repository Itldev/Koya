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

import fr.itldev.koya.action.ZipContentActionExecuter;
import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.exceptions.KoyaErrorCodes;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.webscript.KoyaWebscript;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.simple.JSONArray;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Mostly comming from atolcd ZipContents
 * http://github.com/atolcd/alfresco-zip-and-download.git
 * 
 * http://www.atolcd.com/
 */
public class ZipContent extends AbstractWebScript {

	private static final String ARG_NODEREFS = "nodeRefs";
	private static final String ARG_PDF = "pdf";
	private static final String ARG_ZIPNAME = "zipname";
	private static final String ARG_ASYNC = "async";

	private ActionService actionService;
	private KoyaNodeService koyaNodeService;
	private CompanyService companyService;

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String response = null;

		try {
			Map<String, Object> jsonPostMap = KoyaWebscript.getJsonMap(req);

			ArrayList<NodeRef> nodeRefs = new ArrayList<>();
			JSONArray jsonArray = (JSONArray) jsonPostMap.get(ARG_NODEREFS);
			Boolean pdf = (Boolean) jsonPostMap.get(ARG_PDF);
			String zipname = (String) jsonPostMap.get(ARG_ZIPNAME);
			Boolean async;
			try {
				async = (Boolean) jsonPostMap.get(ARG_ASYNC);
			} catch (Exception e) {
				async = false;
			}

			if (pdf == null) {
				pdf = Boolean.FALSE;
			}

			if(zipname != null) {
				zipname = zipname.replaceAll("'", "_");
			}

			int len = jsonArray.size();
			for (int i = 0; i < len; i++) {
				NodeRef n = koyaNodeService.getNodeRef(jsonArray.get(i).toString());
				nodeRefs.add(n);
			}

			Company c = (Company) koyaNodeService.getFirstParentOfType(nodeRefs.get(0),
					Company.class);
			NodeRef companyTmpZipDir = companyService.getTmpZipDir(c);

			try {
				Map<String, Serializable> paramsZipContent = new HashMap<>();

				paramsZipContent.put(ZipContentActionExecuter.PARAM_NODEREFS, nodeRefs);
				paramsZipContent.put(ZipContentActionExecuter.PARAM_ZIPNAME, zipname);
				paramsZipContent.put(ZipContentActionExecuter.PARAM_PDF, pdf);
				paramsZipContent.put(ZipContentActionExecuter.PARAM_COMPANYTMPZIPDIR,
						companyTmpZipDir);
				paramsZipContent.put(ZipContentActionExecuter.PARAM_ASYNC, async);

				Action zipContent = actionService.createAction(ZipContentActionExecuter.NAME,
						paramsZipContent);

				zipContent.setExecuteAsynchronously(async);
				actionService.executeAction(zipContent, null);

				if (!async) {
					response = KoyaWebscript
							.getObjectAsJson(koyaNodeService.getKoyaNode((NodeRef) zipContent
									.getParameterValue(ZipContentActionExecuter.PARAM_RESULT)));
				}else{
					//TODO return processing message
				}

			} catch (KoyaServiceException kse) {
				throw kse;
			} catch (Exception ex) {
				throw new KoyaServiceException(KoyaErrorCodes.ZIP_EXTRACTION_PROCESS_ERROR, ex);
			}
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
		} catch (RuntimeException e) {
			/**
			 * TODO koya specific exception
			 */
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST,
					"Erreur lors de la génération de l'archive.", e);
		}
		res.setContentEncoding("UTF-8");
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}

}
