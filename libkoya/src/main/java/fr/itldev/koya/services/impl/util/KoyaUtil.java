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

package fr.itldev.koya.services.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 *
 *
 */
public class KoyaUtil {

    public static final String getLibKoyaVersion() {

        Enumeration resEnum;
        try {
            /*
             Loads all build.properties resources and 
             */
            resEnum = Thread.currentThread().getContextClassLoader().getResources("META-INF/build.properties");
            while (resEnum.hasMoreElements()) {
                try {
                    URL url = (URL) resEnum.nextElement();
                    InputStream is = url.openStream();
                    Properties prop = new Properties();
                    if (is != null) {
                        try {
                            prop.load(is);
                        } catch (IOException ioe) {
                        }
                    }
                    if (prop.get("build.artifactId").equals("libkoya")) {
                        return prop.get("build.version.full").toString();
                    }

                } catch (Exception e) {
                }
            }
        } catch (Exception e) {

        }
        return "";
    }

}
