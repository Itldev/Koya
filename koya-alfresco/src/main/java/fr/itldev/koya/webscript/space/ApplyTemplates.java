package fr.itldev.koya.webscript.space;

import fr.itldev.koya.alfservice.ModelService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.util.Map;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class ApplyTemplates extends AbstractWebScript {

    ModelService modelService;

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        Map<String, String> urlParams = KoyaWebscript.getUrlParamsMap(req);

        //TODO use standard name
        String shortname = (String) urlParams.get(KoyaWebscript.WSCONST_SHORTNAME);
        String templatename = (String) urlParams.get(KoyaWebscript.WSCONST_TEMPLATENAME);

        try {
            modelService.companyInitTemplate(shortname, templatename);
        } catch (KoyaServiceException ex) {
            throw new WebScriptException("KoyaError : " + ex.getErrorCode().toString());
        }
        res.setContentType("application/json");
        res.getWriter().write("");
    }

}
