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
import fr.itldev.koya.model.impl.Espace;
import fr.itldev.koya.model.KoyaModel;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Service de gestion des espaces
 */
public class EspaceService {

    private static final String DOCLIB_NAME = "documentLibrary";

    private Logger logger = Logger.getLogger(this.getClass());

    private NodeService nodeService;
    private SearchService searchService;
    private KoyaNodeService koyaNodeService;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    // </editor-fold>
    /**
     * Méthode de création d'un espace dans un conteneur valide : Société ou
     * Espace.
     *
     * @param nom
     * @param parent
     * @param prop
     * @return
     * @throws KoyaServiceException
     */
    public Espace creer(String nom, NodeRef parent, Map<String, String> prop) throws KoyaServiceException {

        //le dossier doit avoir un nom
        if (nom == null || nom.isEmpty()) {
            throw new KoyaServiceException();
        }

        NodeRef nrParent = null;

        if (nodeService.getType(parent).equals(KoyaModel.QNAME_ITL_ESPACE)) {
            //si le parent est un espace on séléctionne son noeud
            nrParent = parent;
        } else if (nodeService.getType(parent).equals(KoyaModel.QNAME_ITL_SOCIETE)) {
            //si c'est une société, on séléction documentLibrary
            nrParent = getDocLibNodeRef(parent);
        } else {
            throw new KoyaServiceException();//TODO type de parent invalide !!
        }

        //TODO verifier qu'il n'existe pas déja un dossier ayant ce nom 
        //construction des propriétes du noeud.
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, nom);
        //TODO autres propriétés

        ChildAssociationRef car = nodeService.createNode(nrParent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nom),
                KoyaModel.QNAME_ITL_ESPACE,
                properties);
        koyaNodeService.setActifStatus(car.getChildRef(), Boolean.TRUE);

        return nodeEspaceBuilder(car.getChildRef());
    }

    /**
     * Retourne la liste plate de tous les espaces d'une société.
     *
     * @param shortNameSociete
     * @return
     */
    public List<Espace> lister(String shortNameSociete) {
        List<Espace> espaces = new ArrayList<>();

        String listEspacesQuery = "PATH:\"/app:company_home/st:sites/cm:" + shortNameSociete + "/cm:documentLibrary//*\"";
        listEspacesQuery += " AND TYPE:\"" + KoyaModel.TYPESHORTPREFIX_ITL_ESPACE + "\"";

        ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, listEspacesQuery);

        for (ResultSetRow r : rs) {
            espaces.add(nodeEspaceBuilder(r.getNodeRef()));
        }
        return espaces;
    }

    /**
     * Supression d'un espace
     *
     * @param e
     */
    public void supprimer(Espace e) {
        NodeRef n = new NodeRef(e.getNodeRef());
        //TODO appel méthode de suppr générale 
    }

    /**
     * Déplacement d'un espace
     *
     * @param e
     */
    public void deplacer(Espace e) {
        //TODO appel méthode de depl générale 
    }

    /**
     *
     * =============== Méthodes privées =================
     *
     */
    /**
     *
     * @param espaceNodeRef
     * @return
     */
    private Espace nodeEspaceBuilder(NodeRef espaceNodeRef) {
        Espace e = new Espace();
        e.setNodeRef(espaceNodeRef.toString());
        e.setNom((String) nodeService.getProperty(espaceNodeRef, ContentModel.PROP_NAME));

        //défintion du noderef parent.
        NodeRef directParent = nodeService.getPrimaryParent(espaceNodeRef).getParentRef();
        NodeRef realParent = null;

        if (nodeService.getType(directParent).equals(KoyaModel.QNAME_ITL_ESPACE)) {
            realParent = directParent;
        } else if (nodeService.getProperty(directParent, ContentModel.PROP_NAME).equals(DOCLIB_NAME)) {
            //parent du parent
            realParent = nodeService.getPrimaryParent(directParent).getParentRef();
        } else {
            logger.warn("Erreur de hiérachie des parent d'espace");
            //TODO lever un exception 
        }

        //status activité
        e.setActive(koyaNodeService.isActif(espaceNodeRef));

        e.setParentNodeRefasObject(realParent);
        return e;
    }

    /**
     * Méthode qui rends le nodeRef "documentLibrary" d'une société = parent des
     * espaces racine.
     *
     * @param s
     * @return
     * @throws KoyaServiceException
     */
    private NodeRef getDocLibNodeRef(NodeRef nodeSociete) throws KoyaServiceException {
        //TODO mettre en cache les nodeRef/société

        for (ChildAssociationRef car : nodeService.getChildAssocs(nodeSociete)) {
            if (nodeService.getProperty(car.getChildRef(), ContentModel.PROP_NAME).equals(DOCLIB_NAME)) {
                return car.getChildRef();
            }
        }

        throw new KoyaServiceException();//TODO erreur aucun doc lib
    }
}
