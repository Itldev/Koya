package fr.itldev.koya.webscript.meta;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 *
 *
 */
public class GetInfos extends AbstractWebScript {

    private Logger logger = Logger.getLogger(this.getClass());

    private ModuleService moduleService;

    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res)
            throws IOException {

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("META-INF/build.properties");
        Properties prop = new Properties();
        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ioe) {
            }
        }
        Map m = new HashMap();
        m.put("build", prop);
        for (ModuleDetails moduleDetails : moduleService.getAllModules()) {
            m.put("module-" + moduleDetails.getId(), moduleDetails);
        }
        ObjectMapper mapper = new ObjectMapper();
        res.getWriter().write(mapper.writeValueAsString(m));

    }

}
