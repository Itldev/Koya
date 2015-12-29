package fr.itldev.koya.webscript.content;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * @author nico
 * 
 */
public class Size extends AbstractWebScript {
	private KoyaNodeService koyaNodeService;

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.extensions.webscripts.WebScript#execute(org.
	 * springframework.extensions.webscripts.WebScriptRequest,
	 * org.springframework.extensions.webscripts.WebScriptResponse)
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		String response;
		try {

			Map<String, String> urlParamsMap = KoyaWebscript
					.getUrlParamsMap(req);
			NodeRef nodeRef = koyaNodeService.getNodeRef((String) urlParamsMap
					.get(KoyaWebscript.WSCONST_NODEREF));
			response = KoyaWebscript.getObjectAsJson(koyaNodeService.getByteSize(nodeRef));
		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());
		}
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().write(response);

	}

}
