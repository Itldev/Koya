/**
 * Koya is an alfresco module that provides a corporate orientated dataroom.
 *
 * Copyright (C) Itl Developpement 2014
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see `<http://www.gnu.org/licenses/>`.
 */
package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.impl.MetaInfos;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.model.json.MailWrapper;
import fr.itldev.koya.services.AlfrescoService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.client.RestTemplate;

public class AlfrescoRestService implements AlfrescoService {

    protected static final String DESERIALISATION_ERROR = "Object deserialisation error";
    private static final String REST_GET_SERVERINFOS = "/s/fr/itldev/koya/meta/infos";
    private static final String REST_POST_MAIL = "/s/fr/itldev/koya/global/mail";

    private String alfrescoServerUrl;

    private RestTemplate template;

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public String getAlfrescoServerUrl() {
        return alfrescoServerUrl;
    }

    public void setAlfrescoServerUrl(String alfrescoServerUrl) {
        this.alfrescoServerUrl = alfrescoServerUrl;
    }

    public RestTemplate getTemplate() {
        return template;
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }

    // </editor-fold>
    /**
     * Get Informations about server and modules.
     *
     * @param user
     * @return
     */
    @Override
    public MetaInfos getServerInfos(User user) throws AlfrescoServiceException {

        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().getForObject(
                alfrescoServerUrl + REST_GET_SERVERINFOS, ItlAlfrescoServiceWrapper.class);

        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ((MetaInfos) ret.getItems().get(0));
        } else {
            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }

    }

    /**
     *
     * @param user
     * @param wrapper
     * @throws AlfrescoServiceException
     */
    @Override
    public void sendMail(User user, MailWrapper wrapper) throws AlfrescoServiceException {
        ItlAlfrescoServiceWrapper ret = null;

        if (user == null) {
            ret = getTemplate().postForObject(
                    alfrescoServerUrl + REST_POST_MAIL + "?guest=true", wrapper, ItlAlfrescoServiceWrapper.class);
        } else {
            ret = user.getRestTemplate().postForObject(
                    alfrescoServerUrl + REST_POST_MAIL, wrapper, ItlAlfrescoServiceWrapper.class);
        }

        if (!ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {

            throw new AlfrescoServiceException(ret.getMessage(), ret.getErrorCode());
        }
    }

    /*
     * ================ Utils methods ==================
     */
    /**
     * Extracts noderef parts.
     *
     * @param nodeRef
     * @return
     */
    protected Map<String, String> explodeNodeRef(String nodeRef) {

        Map exploded = new HashMap<>();

        Pattern p = Pattern.compile("(\\w+):\\/\\/(\\w+)\\/(.*)");
        Matcher m = p.matcher(nodeRef);

        if (m.find()) {
            exploded.put("store_type", m.group(1));
            exploded.put("store_id", m.group(2));
            exploded.put("id", m.group(3));

        }

        return exploded;
    }

    /**
     * Converts byte datasize to human readable
     *
     * @param bytes
     * @param si
     * @return
     */
    protected String getHumanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " b";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
                + (si ? "" : "i");
        return String.format("%.1f %so", bytes / Math.pow(unit, exp), pre);
    }

}
