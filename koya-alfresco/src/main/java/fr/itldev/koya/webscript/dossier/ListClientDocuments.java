package fr.itldev.koya.webscript.dossier;

import java.io.IOException;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.itldev.koya.alfservice.DossierService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.webscript.KoyaWebscript;

/**
 * List Client Documents in hidden folder for specified dossier
 * 
 */
public class ListClientDocuments extends AbstractWebScript {

	private DossierService dossierService;
	private KoyaNodeService koyaNodeService;

	public void setDossierService(DossierService dossierService) {
		this.dossierService = dossierService;
	}

	public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
		this.koyaNodeService = koyaNodeService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {
		Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

		String response = "[]";
		try {

			Dossier d = koyaNodeService
					.getKoyaNode(koyaNodeService.getNodeRef((String) urlParams
							.get(KoyaWebscript.WSCONST_NODEREF)), Dossier.class);
		
			response = KoyaWebscript.getObjectAsJson(dossierService
					.listKoyaClientDocuments(d));

		} catch (KoyaServiceException ex) {
			throw new WebScriptException("KoyaError : "
					+ ex.getErrorCode().toString());
		}

		res.setContentEncoding("UTF-8");
		res.setContentType("application/json;charset=UTF-8");

		res.getWriter().write(response);

	}
}
