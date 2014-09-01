package fr.itldev.koya.webscript.meta;

import fr.itldev.koya.model.impl.MetaInfos;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 *
 */
public class GetInfos extends AbstractWebScript {

    private final Logger logger = Logger.getLogger(this.getClass());

    private ModuleService moduleService;

    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        MetaInfos mInfos = new MetaInfos();

        String response;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("META-INF/build.properties");
        Properties prop = new Properties();
        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ioe) {
            }
        }
        mInfos.setServerInfos(prop);
        for (ModuleDetails moduleDetails : moduleService.getAllModules()) {
            if (moduleDetails.getId().equals("koya-alfresco")) {
                mInfos.setKoyaInfos(moduleDetails.getProperties());
            } else {
                mInfos.getModules().add(moduleDetails.getProperties());
            }
        }
        response = KoyaWebscript.getObjectAsJson(mInfos);
        res.setContentType("application/json");
        res.getWriter().write(response);
    }

}
