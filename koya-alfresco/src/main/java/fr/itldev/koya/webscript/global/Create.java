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

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaContentService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Directory;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import fr.itldev.koya.services.exceptions.KoyaErrorCodes;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * Creates Space, Dossier or Directory
 * 
 * 
 */
public class Create extends AbstractWebScript {
	private Logger logger = Logger.getLogger(this.getClass());

	private SpaceService spaceService;
	private DossierService dossierService;
	private KoyaNodeService koyaNodeService;
	private KoyaContentService koyaContentService;

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setDossierService(DossierService dossierService) {
		this.dossierService = dossierService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	public void setKoyaContentService(KoyaContentService koyaContentService) {
		this.koyaContentService = koyaContentService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		Map<String, Object> jsonPostMap = KoyaWebscript.getJsonMap(req);
		Map<String, String> urlParamsMap = KoyaWebscript.getUrlParamsMap(req);
		//String name = (String) jsonPostMap.get(KoyaWebscript.WSCONST_NAME);
		String title = (String) jsonPostMap.get(KoyaWebscript.WSCONST_TITLE);
		String ktype = (String) jsonPostMap.get("ktype");
		String response = "";

		try {
			NodeRef parent = koyaNodeService.getNodeRef((String) urlParamsMap
					.get(KoyaWebscript.WSCONST_PARENTNODEREF));

			/**
			 * Attention chaine de car recue : name ou title ?? title pour les
			 * dossiers
			 * 
			 * TODO 1 seul service en backend !
			 * 
			 * TODO deserialisation complete de l'espace en argument
			 */

			
			
			if (ktype.equals(Dossier.class.getSimpleName())) {
				response = KoyaWebscript.getObjectAsJson(dossierService.create(
						title, parent, null));
			} else if (ktype.equals(Space.class.getSimpleName())) {
				response = KoyaWebscript.getObjectAsJson(spaceService.create(
						title, parent, null));

			} else if (ktype.equals(Directory.class.getSimpleName())) {
				response = KoyaWebscript.getObjectAsJson(koyaContentService
						.createDir(title, parent));
			} else {
				throw new KoyaServiceException(KoyaErrorCodes.INVALID_TYPE_KOYANODECREATION);
			}

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);
	}
}
