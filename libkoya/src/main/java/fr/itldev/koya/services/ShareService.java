package fr.itldev.koya.services;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

/**
 * Sharing Secured items.
 *
 */
public interface ShareService extends AlfrescoService {

    /**
     * Shares SecuredItems to a list of users (pre created or not)
     *
     * @param user
     * @param sharedItems
     * @param usersMails
     */
    void shareItems(User user, List<SecuredItem> sharedItems, List<String> usersMails, String serverPath, String acceptUrl, String rejectUrl);
    
    
     /**
     * Revoke Shares SecuredItems to a list of users
     *
     * @param user
     * @param sharedItems
     * @param usersMails
     */
    void unShareItems(User user, List<SecuredItem> sharedItems, List<String> usersMails);

    /**
     * Show Users who can publicly access to given element.
     *
     * @param user
     * @param item
     * @return
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    List<User> sharedUsers(User user, SecuredItem item) throws AlfrescoServiceException;
}
