package fr.itldev.koya.services.exceptions;

/**
 * Webscripts Error codes definitions
 *
 */
public interface KoyaErrorCodes {

    //
    public static final Integer UNHANDLED = 0;

    //100 -> companies errors
    public static final Integer COMPANY_EMPTY_TITLE = 100;

    //200 -> Spaces errors
    public static final Integer SPACE_EMPTY_NAME = 200;
    public static final Integer SPACE_INVALID_PARENT = 201;
    public static final Integer SPACE_DOCLIB_NODE_NOT_FOUND = 202;

    //300 -> Dossiers errors
    public static final Integer DOSSIER_EMPTY_NAME = 300;
    public static final Integer DOSSIER_NOT_IN_SPACE = 301;
    public static final Integer DOSSIER_NAME_EXISTS = 302;

    //400 -> Content errors
    public static final Integer CONTENT_INVALID_HIERACHY = 400;

}
