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
