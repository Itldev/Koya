package fr.itldev.koya.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * This Object aims to carry Alfresco server meta informations
 *
 */
public class MetaInfos {

    Properties serverInfos;
    Properties koyaInfos;
    List<Properties> modules = new ArrayList<>();

    public Properties getServerInfos() {
        return serverInfos;
    }

    public void setServerInfos(Properties serverInfos) {
        this.serverInfos = serverInfos;
    }

    public Properties getKoyaInfos() {
        return koyaInfos;
    }

    public void setKoyaInfos(Properties koyaInfos) {
        this.koyaInfos = koyaInfos;
    }

    public List<Properties> getModules() {
        return modules;
    }

    public void setModules(List<Properties> modules) {
        this.modules = modules;
    }

    public MetaInfos() {
    }

}
