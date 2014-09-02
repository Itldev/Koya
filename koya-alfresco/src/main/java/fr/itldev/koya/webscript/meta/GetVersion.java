package fr.itldev.koya.webscript.meta;

import fr.itldev.koya.services.impl.util.KoyaUtil;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Returns libKoya version
 *
 *
 */
public class GetVersion extends AbstractWebScript {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        res.setContentType("application/json");
        res.getWriter().write(KoyaUtil.getLibKoyaVersion());
    }

}
