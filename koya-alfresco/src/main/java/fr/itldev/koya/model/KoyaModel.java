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
package fr.itldev.koya.model;

import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.namespace.QName;

/**
 * Model definition and constants.
 *
 */
public class KoyaModel {

    public static final String NAMESPACE_KOYA_CONTENT_MODEL = "http://www.itldev.fr/koyamodel/content/1.0";
    public static final String NAMESPACE_ALFRESCO_ROOT = "http://www.alfresco.org/model/site/1.0";
    // ============= Types ===============
    public static final String TYPE_KOYA_COMPANY = "site";
    public static final QName QNAME_KOYA_COMPANY = SiteModel.TYPE_SITE;
    public static final String TYPESHORTPREFIX_KOYA_COMPANY = "st:site";
    //    
    public static final String TYPE_KOYA_SPACE = "space";
    public static final QName QNAME_KOYA_SPACE = QName.createQName(NAMESPACE_KOYA_CONTENT_MODEL, TYPE_KOYA_SPACE);
    public static final String TYPESHORTPREFIX_KOYA_SPACE = "koya:space";
    //
    public static final String TYPE_KOYA_DOSSIER = "dossier";
    public static final QName QNAME_KOYA_DOSSIER = QName.createQName(NAMESPACE_KOYA_CONTENT_MODEL, TYPE_KOYA_DOSSIER);
    public static final String TYPESHORTPREFIX_KOYA_DOSSIER = "koya:dossier";
    //================ Aspects ==========================
    public static final String ASPECT_KOYA_ACTIVABLE = "activable";
    public static final QName QNAME_KOYA_ACTIVABLE = QName.createQName(NAMESPACE_KOYA_CONTENT_MODEL, ASPECT_KOYA_ACTIVABLE);
    public static final String ASPECTSHORTPREFIX_KOYA_ACTIVABLE = "koya:activable";

    public static final String ASPECT_KOYA_MAILUNIQUE = "mailunique";
    public static final QName QNAME_KOYA_MAILUNIQUE = QName.createQName(NAMESPACE_KOYA_CONTENT_MODEL, ASPECT_KOYA_MAILUNIQUE);
    public static final String ASPECTSHORTPREFIX_KOYA_MAILUNIQUE = "koya:mailunique";
    //================= Propriétés sur les aspects
    public static final String PROPASPECT_KOYA_ISACTIVE = "isActive";
    public static final QName QNAME_PROPASPECT_KOYA_ISACTIVE = QName.createQName(NAMESPACE_KOYA_CONTENT_MODEL, PROPASPECT_KOYA_ISACTIVE);
    public static final String PROPASPECTSHORTPREFIX_KOYA_ISACTIVE = "koya:isActive";

    public static final String PROPASPECT_KOYA_MAIL = "mail";
    public static final QName QNAME_PROPASPECT_KOYA_MAIL = QName.createQName(NAMESPACE_KOYA_CONTENT_MODEL, PROPASPECT_KOYA_MAIL);
    public static final String PROPASPECTSHORTPREFIX_KOYA_MAIL = "koya:mail";

}
