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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData;

import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * 
 * Upload file Webscript
 * 
 * 
 * 
 */
public class Upload extends AbstractWebScript {

	private final Logger logger = Logger.getLogger(Upload.class);

	/* services */
	private KoyaContentService koyaContentService;
	private KoyaNodeService koyaNodeService;

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setKoyaContentService(KoyaContentService koyaContentService) {
		this.koyaContentService = koyaContentService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		Map<String, String> retMap = new HashMap<>();
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

		String format = urlParams.get("format");

		String fileName = null;
		Content content = null;
		String parentnoderef = null;

		FormData formData = (FormData) req.parseContent();
		FormData.FormField[] fields = formData.getFields();
		for (FormData.FormField field : fields) {
			if (field.getName().equals("parentnoderef")) {
				parentnoderef = field.getValue();
			}
			if (field.getName().equals("file") && field.getIsFile()) {
				fileName = field.getFilename();
				content = field.getContent();
			}
		}
		if (fileName == null || content == null) {
			retMap.put("error", "Uploaded file cannot be located in request");
			writeResponse(res, retMap, format);
			return;
		}
		try {
			NodeRef parent = koyaNodeService.getNodeRef(parentnoderef);
			retMap = koyaContentService.createContentNode(parent, fileName,
					content);
		} catch (KoyaServiceException ex) {
//			throw new WebScriptException("KoyaError : "
//					+ ex.getErrorCode().toString());
                        retMap.put("error", ex.getErrorCode().toString());
		}
		writeResponse(res, retMap, format);
	}

	private void writeResponse(WebScriptResponse res,
			Map<String, String> responseMap, String format) throws IOException {

		if (format != null && format.equals("text")) {
			res.setContentType("text/plain");
		} else {
			res.setContentType("application/json");
		}
		res.getWriter().write(KoyaWebscript.getObjectAsJson(responseMap));
	}

}
