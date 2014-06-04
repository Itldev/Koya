package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.model.json.SharingWrapper;
import fr.itldev.koya.services.ShareService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

/**
 *
 *
 */
public class ShareServiceImpl extends AlfrescoRestService implements ShareService {

    protected static final String REST_POST_SHAREITEMS = "/s/fr/itldev/koya/share/shareitems";
    protected static final String REST_GET_SHAREDUSERS = "/s/fr/itldev/koya/share/sharedusers/{noderef}";

    /**
     * Shared SecuredItems to a list of users (pre created or not)
     *
     * @param user
     * @param sharedItems
     * @param usersMails
     */
    @Override
    public void shareItems(User user, List<SecuredItem> sharedItems, List<String> usersMails, String serverPath, String acceptUrl, String rejectUrl) {

        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_SHAREITEMS,
                new SharingWrapper(sharedItems, usersMails, serverPath, acceptUrl, rejectUrl), ItlAlfrescoServiceWrapper.class);

        int n = ret.getItems().size();

        //TODO analyse return
    }

    /**
     * Undo shares to sepcified users.
     *
     * @param user
     * @param sharedItems
     * @param usersMails
     */
    @Override
    public void unShareItems(User user, List<SecuredItem> sharedItems, List<String> usersMails) {

        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_SHAREITEMS,
                new SharingWrapper(sharedItems, usersMails, Boolean.TRUE), ItlAlfrescoServiceWrapper.class);

        int n = ret.getItems().size();

        //TODO analyse return
    }

    /**
     * Show Users who can publicly access to given element.
     *
     * @param user
     * @param item
     * @return
     * @throws fr.itldev.koya.services.exceptions.AlfrescoServiceException
     */
    @Override
    public List<User> sharedUsers(User user, SecuredItem item) throws AlfrescoServiceException {

        ItlAlfrescoServiceWrapper ret = user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_SHAREDUSERS, ItlAlfrescoServiceWrapper.class, item.getNodeRef());
        if (ret.getStatus().equals(ItlAlfrescoServiceWrapper.STATUS_OK)) {
            return ret.getItems();
        } else {
            throw new AlfrescoServiceException(ret.getMessage());
        }

    }

}
