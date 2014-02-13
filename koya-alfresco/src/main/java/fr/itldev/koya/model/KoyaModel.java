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

package fr.itldev.koya.model;

import org.alfresco.service.namespace.QName;

/**
 * Défnitions de modèle de données et constantes relatives au module
 * koya
 *
 */
public class KoyaModel {

    public static final String NAMESPACE_ITLSD_CONTENT_MODEL = "http://www.itldev.fr/koyamodel/content/1.0";
    public static final String NAMESPACE_ALFRESCO_RACINE = "http://www.alfresco.org/model/site/1.0";
    // ============= Types ===============
    public static final String TYPE_ITL_SOCIETE = "site";
    public static final QName QNAME_ITL_SOCIETE = QName.createQName(NAMESPACE_ALFRESCO_RACINE, TYPE_ITL_SOCIETE);
    public static final String TYPESHORTPREFIX_ITL_SOCIETE = "st:site";
    //    
    public static final String TYPE_ITL_ESPACE = "espace";
    public static final QName QNAME_ITL_ESPACE = QName.createQName(NAMESPACE_ITLSD_CONTENT_MODEL, TYPE_ITL_ESPACE);
    public static final String TYPESHORTPREFIX_ITL_ESPACE = "itlsd:espace";
    //
    public static final String TYPE_ITL_DOSSIER = "dossier";
    public static final QName QNAME_ITL_DOSSIER = QName.createQName(NAMESPACE_ITLSD_CONTENT_MODEL, TYPE_ITL_DOSSIER);
    public static final String TYPESHORTPREFIX_ITL_DOSSIER = "itlsd:dossier";
    //================ Aspects ==========================
    public static final String ASPECT_ITL_SDCONTENEURACTIF = "activable";
    public static final QName QNAME_ITL_SDCONTENEURACTIF = QName.createQName(NAMESPACE_ITLSD_CONTENT_MODEL, ASPECT_ITL_SDCONTENEURACTIF);
    public static final String ASPECTSHORTPREFIX_ITL_DOSSIER = "itlsd:activable";
    //================= Propriétés sur les aspects
    public static final String PROPASPECT_ITL_ISACTIF = "isActif";
    public static final QName QNAME_PROPASPECT_ITL_ISACTIF = QName.createQName(NAMESPACE_ITLSD_CONTENT_MODEL, PROPASPECT_ITL_ISACTIF);
    public static final String PROPASPECTSHORTPREFIX_ITL_ISACTIF = "itlsd:isActif";

    //Chemins d'accès aux modèles de noeuds
}
