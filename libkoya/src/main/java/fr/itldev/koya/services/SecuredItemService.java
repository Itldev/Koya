package fr.itldev.koya.services;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

/**
 * Secured Items Generic methods
 *
 */
public interface SecuredItemService extends AlfrescoService {

    /**
     * Deletes item.
     *
     * @param user
     * @param securedItem
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    void delete(User user, SecuredItem securedItem) throws AlfrescoServiceException;

    /**
     * Renames item.
     *
     * @param user
     * @param securedItem
     * @param newName
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    void rename(User user, SecuredItem securedItem, String newName) throws AlfrescoServiceException;

    /**
     *
     * Returns Secured Item Parent if exists.
     *
     * @param user
     * @param securedItem
     * @return
     * @throws AlfrescoServiceException
     */
    SecuredItem getParent(User user, SecuredItem securedItem) throws AlfrescoServiceException;

    /**
     * Returns SecuredItems ancestors list.
     *
     * @param user
     * @param securedItem
     * @return
     * @throws AlfrescoServiceException
     */
    List<SecuredItem> getParents(User user, SecuredItem securedItem) throws AlfrescoServiceException;

}
