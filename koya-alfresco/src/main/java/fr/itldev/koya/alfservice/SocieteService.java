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

package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.impl.OffreCommerciale;
import fr.itldev.koya.model.impl.Societe;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 *
 * Les Société sont les sites ayant l'aspect itlsd:activable
 *
 */
public class SocieteService {

    private static final String SITE_PRESET = "site-dashboard";
    private static final String DESC = "Société Suivi Dossier";
    private final Logger logger = Logger.getLogger(SocieteService.class);

    private SiteService siteService;
    private NodeService nodeService;
    private KoyaNodeService koyaNodeService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

   

    // </editor-fold>
    public Societe creer(String titre, OffreCommerciale oc) throws KoyaServiceException {

        if (titre == null || titre.isEmpty()) {
            throw new KoyaServiceException();//TODO préciser l'exc
        }

        String shortName = getShortNameFromTitle(titre);
        SiteInfo sInfo = siteService.createSite(SITE_PRESET, shortName, titre, DESC + " - " + shortName, SiteVisibility.PUBLIC);
        Societe created = new Societe(sInfo);
        koyaNodeService.setActifStatus(sInfo.getNodeRef(), Boolean.TRUE);

        //TODO copie des elements depuis un template
        /**
         * Créaton du skelette minimal de société
         */
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, "documentLibrary");
        ChildAssociationRef car = nodeService.createNode(created.getNodeRefasObject(),
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "documentLibrary"),
                ContentModel.TYPE_FOLDER,
                properties);

        /**
         *
         */
        //TODO appel vers le service Share pour créer la partie spécifique ou fct inverse : ie appel seulement depuis share ...
        return created;
    }

    public List<Societe> lister() {
        //TODO précision pour lister les site dont un utilisateur est membre. --> userSerice ??
        //TODO switch actif ?
        List<Societe> socs = new ArrayList<>();

        for (SiteInfo s : siteService.listSites("", SITE_PRESET)) {
            if (koyaNodeService.isSdSite(s.getNodeRef())) {
                socs.add(new Societe(s));
            }
        }
        return socs;
    }

    public List<OffreCommerciale> listerOffresCommerciales() {
        return null;

    }

    public OffreCommerciale getOffreCommerciale(String nomOffre) {
        return null;

    }

    /**
     * Méthode de normalisation du nom de site : Suppression des espaces, etc...
     *
     * @return
     */
    private String getShortNameFromTitle(String title) {
        //TODO normaliser avec une regex, suppr les accents ou 
        String shortTitle = title.replace(" ", "-");

        /**
         * On assure l'unicité du shortTitle meme sir le titre existe déja.
         */
        String buildTitle = shortTitle;
        int incr = 1;
        while (siteService.getSite(buildTitle) != null) {
            buildTitle = shortTitle + "-" + incr;
            incr++;
        }
        return buildTitle;
    }

}
