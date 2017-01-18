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

import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.Dossier;
import fr.itldev.koya.model.impl.Space;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.namespace.QName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Model definition and constants.
 * 
 */
public class KoyaModel {

	public static final String KOYA_URI = "http://www.itldev.fr/koyamodel";
	public static final String NAMESPACE_KOYA_CONTENT_MODEL = "http://www.itldev.fr/koyamodel/content/1.0";
	public static final String NAMESPACE_ALFRESCO_ROOT = "http://www.alfresco.org/model/site/1.0";
	// ============= Types ===============
	public static final QName TYPE_COMPANY = SiteModel.TYPE_SITE;
	//
	public static final QName TYPE_SPACE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "space");
	//
	public static final QName TYPE_DOSSIER = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "dossier");
	
	public static final QName TYPE_DOSSIERCLASSIFYFOLDER = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "dossierClassifyFolder");

	public static final QName TYPE_CONTACTITEM = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "contactItem");

	public static final QName TYPE_CONTACT = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "contact");

	public static final QName TYPE_COMPANYPROPERTIES = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "companyProperties");

	// ================ Aspects ==========================

	public static final QName ASPECT_COMPANYSITE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "companySite");

	public static final QName ASPECT_ACTIVABLE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "activable");

/*
	public static final QName ASPECT_MAILUNIQUE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "mailunique");
*/

	public static final QName ASPECT_CIVILTITLED = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "civilTitled");

	public static final QName ASPECT_LASTMODIFIED = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "lastModified");

	public static final QName ASPECT_CONFIDENTIAL = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "confidential");

	public static final QName ASPECT_BPMSTATUS = QName.createQName(
				NAMESPACE_KOYA_CONTENT_MODEL, "bpmStatus");
	
	public static final QName ASPECT_TEMPFILE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "temp");

	// ================= Aspects Properties

	public static final QName PROP_COMPANYHOME = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "companyHome");

	public static final QName PROP_ISACTIVE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "isActive");

	public static final QName PROP_MAIL = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "mail");

	public static final QName PROP_CIVILTITLE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "civilTitle");

	public static final QName PROP_LASTMODIFICATIONDATE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "lastModificationDate");

	public static final QName PROP_NOTIFIED = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "notified");
	
	public static final QName PROP_BPMCURRENTSTATUS = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "currentBpmStatus");

	// ================= Types Properties =========================
	public static final QName PROP_CONTACTITEM_VALUE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "contactItemValue");

	public static final QName PROP_CONTACTITEM_TYPE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "contactItemType");

	public static final QName PROP_ADDRESS = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "address");

	public static final QName PROP_ADDRESS2 = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "address2");

	public static final QName PROP_ZIPCODE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "zipcode");

	public static final QName PROP_CITY = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "city");

	public static final QName PROP_DESCRIPTION = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "description");

	public static final QName PROP_LEGALINFOS = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "legalInformations");

	public static final QName PROP_MAILHEADERTEXT = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "mailHeaderText");

	public static final QName PROP_REFERENCE = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "reference");

	public static final QName PROP_ACTIVITIIDS = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "activitiIds");
	
	public static final QName PROP_DOSSIERREF = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "dossierRef");

	// ================= Type associations
	// ===========================================
	public static final QName ASSOC_CONTACT_USER = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "contactUserAssoc");

	public static final QName ASSOC_LOGO_COMPANY = QName.createQName(
			NAMESPACE_KOYA_CONTENT_MODEL, "companyLogo");

	/**
	 * Used because koya namespace is not registered
	 * 
	 * TODO register koya namespace
	 */
	public static final Map<QName, String> TYPES_SHORT_PREFIX = Collections
			.unmodifiableMap(new HashMap<QName, String>() {
				private static final long serialVersionUID = 1L;

				{
					put(TYPE_COMPANY, "st:site");
					put(TYPE_SPACE, "koya:space");
					put(TYPE_DOSSIER, "koya:dossier");
				}
			});

	public static final Map<Class<? extends KoyaNode>, QName> CLASS_TO_QNAME = Collections
			.unmodifiableMap(new HashMap<Class<? extends KoyaNode>, QName>() {
				private static final long serialVersionUID = 1L;

				{
					put(Company.class, TYPE_COMPANY);
					put(Space.class, TYPE_SPACE);
					put(Dossier.class, TYPE_DOSSIER);
				}
			});

	// ========= Constraints values

	public static class BpmStatusValues {
		public static String RUNNING = "RUNNING";
		public static String CANCELED = "CANCELED";
		public static String FINISHED = "FINISHED";
		public static String UNKNOWN = "UNKNOWN";

	}

}
