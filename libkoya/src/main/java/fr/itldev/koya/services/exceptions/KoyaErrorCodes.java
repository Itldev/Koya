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
package fr.itldev.koya.services.exceptions;

/**
 * Webscripts Error codes definitions
 *
 */
public interface KoyaErrorCodes {

    // 000 -> global errors
    public static final Integer UNHANDLED = 0;
    public static final Integer INVALID_NODEREF = 1;
    public static final Integer INVALID_SECUREDITEM_NODEREF = 2;
    public static final Integer INVALID_NODE_HIERACHY = 3;
    public static final Integer DUPLICATE_CHILD_RENAME = 4;
    public static final Integer INVALID_LUCENE_PATH = 5;
    public static final Integer INVALID_XPATH_NODE = 6;
    public static final Integer CANNOT_FIND_XPATH_NODE = 7;

    //100 -> companies errors
    public static final Integer COMPANY_EMPTY_TITLE = 100;
    public static final Integer COMPANY_SITE_NOT_FOUND = 101;

    //200 -> Spaces errors
    public static final Integer SPACE_EMPTY_NAME = 200;
    public static final Integer SPACE_INVALID_PARENT = 201;
    public static final Integer SPACE_DOCLIB_NODE_NOT_FOUND = 202;
    public static final Integer SPACE_TEMPLATE_NOT_FOUND = 203;

    //300 -> Dossiers errors
    public static final Integer DOSSIER_EMPTY_NAME = 300;
    public static final Integer DOSSIER_NOT_IN_SPACE = 301;
    public static final Integer DOSSIER_NAME_EXISTS = 302;

    //400 -> Content errors
    public static final Integer CONTENT_CREATION_INVALID_TYPE = 400;
    public static final Integer DIR_CREATION_INVALID_PARENT_TYPE = 401;
    public static final Integer DIR_CREATION_NAME_EXISTS = 402;
    public static final Integer MOVE_DESTINATION_NAME_ALREADY_EXISTS = 403;
    public static final Integer MOVE_SOURCE_NOT_FOUND = 404;
    public static final Integer CONTENT_IS_NOT_ZIP = 405;
    public static final Integer ZIP_EXTRACTION_PROCESS_ERROR = 406;
    public static final Integer FILE_DELETE_ERROR = 407;
        public static final Integer FILE_UPLOAD_NAME_EXISTS = 408;


    //500 -> User errors
    public static final Integer UNKNOWN_USER = 500;
    public static final Integer NO_SUCH_USER_IDENTIFIED_BY_AUTHKEY = 501;
    public static final Integer MANY_USERS_IDENTIFIED_BY_AUTHKEY = 502;
    public static final Integer LOGIN_ALREADY_EXISTS = 503;
    public static final Integer CANT_MODIFY_USER_PASSWORD = 504;
    public static final Integer CANNOT_LOGOUT_USER = 505;
    
    //550 -> invitation process errors
    public static final Integer INVALID_INVITATION_TICKET = 550;
    public static final Integer INVALID_INVITATION_ID = 551;
    public static final Integer INVITATION_PROCESS_USER_MODIFICATION_ERROR = 552;
    public static final Integer INVITATION_PROCESS_ACCEPT_ERROR = 553;
    public static final Integer INVITATION_ALREADY_COMPLETED = 554;
    public static final Integer INVITATION_PROCESS_POST_ACTIVITY_ERROR = 555;

    //600 -> Sales Offer
    public static final Integer SALES_OFFER_LACK_MANDATORY_PROPERTY = 600;

    //800 -> Security
    public static final Integer SECU_UNSHARABLE_TYPE = 800;
    public static final Integer SECU_UNSETTABLE_PERMISSION = 801;
    public static final Integer SECU_USER_MUSTBE_COLLABORATOR_OR_ADMIN_TO_APPLY_PERMISSION = 802;
    public static final Integer SECU_USER_MUSTBE_CONSUMER_TO_APPLY_PERMISSION = 803;
    public static final Integer SECU_USER_MUSTBE_COMPANY_MEMBER_TO_CHANGE_COMPANYROLE = 804;

    //900 -> Koya mail
    public static final Integer KOYAMAIL_INVALID_SUBJECT_PROPERTIES_PATH = 900;
    public static final Integer KOYAMAIL_CANNOT_FIND_TEMPLATE = 901;
    public static final Integer KOYAMAIL_UNSUPPORTED_TEMPLATE_LOCATION_TYPE = 902;
    public static final Integer KOYAMAIL_SUBJECT_KEY_NOT_EXISTS_IN_PROPERTIES = 903;

}
