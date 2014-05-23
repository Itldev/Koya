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

    //500 -> User errors
    public static final Integer UNKNOWN_USER = 500;
    public static final Integer NO_SUCH_USER_IDENTIFIED_BY_AUTHKEY = 501;
    public static final Integer MANY_USERS_IDENTIFIED_BY_AUTHKEY = 502;
    public static final Integer LOGIN_ALREADY_EXISTS = 503;
    public static final Integer CANT_MODIFY_USER_PASSWORD = 504;

}
