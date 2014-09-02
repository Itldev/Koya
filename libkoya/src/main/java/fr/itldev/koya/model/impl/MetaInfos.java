/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 */

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
