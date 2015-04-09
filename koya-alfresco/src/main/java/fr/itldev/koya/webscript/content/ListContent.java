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

import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import magick.FilterType;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * 
 * Content Listing Webscript
 * 
 * 
 * 
 */
public class ListContent extends AbstractWebScript {

	private static final Integer DEFAULT_MAX_DEPTH = 50;

	private KoyaContentService koyaContentService;
	private KoyaNodeService koyaNodeService;
	private NodeService nodeService;

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setKoyaContentService(KoyaContentService koyaContentService) {
		this.koyaContentService = koyaContentService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	private static final String MODE_RECURSIVE = "recursive";
	private static final String MODE_PAGINATED = "paginated";
	private static final List<String> AllowedModes = new ArrayList<String>() {
		private static final long serialVersionUID = 7029599703520077938L;

		{
			add(MODE_RECURSIVE);
			add(MODE_PAGINATED);
		}
	};
	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		Map<String, String> urlParamsMap = KoyaWebscript.getUrlParamsMap(req);
		String response = "";

		try {
			NodeRef parent = koyaNodeService.getNodeRef((String) urlParamsMap
					.get(KoyaWebscript.WSCONST_NODEREF));

			String mode = urlParamsMap.get("mode");			
			String filterExpr = processPatternFilter(urlParamsMap.get("filterExpr"));
			
			/**
			 * 
			 * TODO parameters not processed
			 * 
			 */
			String sortExpr = urlParamsMap.get("sortExpr");
			String typeFilter = urlParamsMap.get("typeFilter");

			/**
			 * Check mode
			 */
			if (!AllowedModes.contains(mode)) {
				throw new KoyaServiceException(
						KoyaErrorCodes.CONTENT_UNKNOWN_WEBSCRIPT_LISTING_MODE);
			}
			Boolean onlyFolders = ((String) urlParamsMap
					.get(KoyaWebscript.WSCONST_ONLYFOLDERS)).equals("true");

			Integer maxItems = null;
			Integer skipCount = null;
			Integer depth = null;
			if (mode.equals(MODE_RECURSIVE)) {

				if (urlParamsMap.containsKey(KoyaWebscript.WSCONST_MAXDEPTH)) {
					depth = new Integer(
							(String) urlParamsMap
									.get(KoyaWebscript.WSCONST_MAXDEPTH));
				} else {
					depth = DEFAULT_MAX_DEPTH;
				}

			} else if (mode.equals(MODE_PAGINATED)) {
				maxItems = Integer.valueOf(urlParamsMap.get("maxItems"));
				skipCount = Integer.valueOf(urlParamsMap.get("skipCount"));

			}

			logger.error("skipcount=" + skipCount + ";maxItems=" + maxItems
					+ ";depth=" + depth + ";onlyFolders=" + onlyFolders
					+ ";filterExpr=" + filterExpr + ";sortExpr=" + sortExpr
					+ ";typeFilter=" + typeFilter);

			response = KoyaWebscript.getObjectAsJson(koyaNodeService
					.listChildrenPaginated(parent, skipCount, maxItems, depth,
							onlyFolders,filterExpr));
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());
		}
		res.setContentType("application/json");
		res.getWriter().write(response);
	}
	
	private String processPatternFilter(String paramFilterExpr){		
		if(paramFilterExpr == null || paramFilterExpr.isEmpty()){
			return null;
		}		
		if(!paramFilterExpr.endsWith("*")){
			paramFilterExpr+="*";
		}		
		return paramFilterExpr;		
	}

}
