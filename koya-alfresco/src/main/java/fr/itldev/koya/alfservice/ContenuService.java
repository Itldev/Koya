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

import fr.itldev.koya.model.Contenu;
import fr.itldev.koya.model.impl.Document;
import fr.itldev.koya.model.impl.Repertoire;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Service gérant les éléments de contenu Répertoires et Documents
 */
public class ContenuService {

    private final Logger logger = Logger.getLogger(this.getClass());

    private NodeService nodeService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public Repertoire creerRepertoire(String nom, NodeRef parent) {

        //TODO  le parent doit etre un rep ou un dossier a verifier 
        //TODO verifier qu'il n'existe pas déja un répertoire ayant ce nom OU laisser remonter l'exception ??
        //construction des propriétes du noeud.
        final Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, nom);
        //TODO autres propriétés

        ChildAssociationRef car = nodeService.createNode(parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nom),
                ContentModel.TYPE_FOLDER,
                properties);

        return nodeRepertoireBuilder(car.getChildRef());
    }

    public Contenu deplacer(NodeRef toMove, NodeRef dest) {

        //TODO vérification de sécurité pour valider le déplacement (1 rep doit etre fils d'un dossier etc...)
        //impl de regles de fct ???
        String name = (String) nodeService.getProperty(toMove, ContentModel.PROP_NAME);

        nodeService.moveNode(toMove, dest, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));

        if (nodeService.getType(toMove).equals(ContentModel.TYPE_FOLDER)) {//TODO folder ou element héritant de folder !!! cf méthode pyramides dictionnary
            return nodeRepertoireBuilder(toMove);
        } else {
            return nodeDocumentBuilder(toMove);
        }
    }

    /**
     * Liste récursive des contenus d'un parent donné
     *
     * //TODO limter le nb de retours ou la profondeurs, etc
     *
     * //limiter au niveau courant etc
     *
     * @param parent
     * @return
     */
    public List<Contenu> lister(NodeRef parent) {

        List<Contenu> contenus = new ArrayList<>();

        for (ChildAssociationRef car : nodeService.getChildAssocs(parent)) {
            NodeRef childNr = car.getChildRef();
            //
            if (nodeService.getType(childNr).equals(ContentModel.TYPE_FOLDER)) {//TODO folder ou element héritant de folder !!! cf méthode pyramides dictionnary
                contenus.add(nodeRepertoireBuilder(childNr));
                contenus.addAll(lister(childNr));
            } else {
                contenus.add(nodeDocumentBuilder(childNr));
            }

        }
        return contenus;
    }

    /**
     *
     * @param repNodeRef
     * @return
     */
    private Repertoire nodeRepertoireBuilder(NodeRef repNodeRef) {
        Repertoire r = new Repertoire();
        r.setNodeRef(repNodeRef.toString());
        r.setNom((String) nodeService.getProperty(repNodeRef, ContentModel.PROP_NAME));
        r.setParentNodeRefasObject(nodeService.getPrimaryParent(repNodeRef).getParentRef());
        return r;
    }

    /**
     *
     * @param docNodeRef
     * @return
     */
    private Document nodeDocumentBuilder(NodeRef docNodeRef) {
        Document d = new Document();
        d.setNodeRef(docNodeRef.toString());
        d.setNom((String) nodeService.getProperty(docNodeRef, ContentModel.PROP_NAME));
        d.setParentNodeRefasObject(nodeService.getPrimaryParent(docNodeRef).getParentRef());
        return d;
    }

}
