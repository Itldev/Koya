package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.Company;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.model.json.SharingWrapper;
import fr.itldev.koya.services.ShareService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import static fr.itldev.koya.services.impl.AlfrescoRestService.fromJSON;
import java.util.List;
import org.codehaus.jackson.type.TypeReference;

/**
 *
 *
 */
public class ShareServiceImpl extends AlfrescoRestService implements ShareService {

    protected static final String REST_POST_SHAREITEMS = "/s/fr/itldev/koya/share/shareitems";
    protected static final String REST_GET_SHAREDUSERS = "/s/fr/itldev/koya/share/sharedusers/{noderef}";
    protected static final String REST_GET_SHAREDITEMS = "/s/fr/itldev/koya/share/listusershares/{userName}/{companyName}";

    /**
     * Shares SecuredItems to a list of users (pre created or not)
     *
     * @param user
     * @param sharedItems
     * @param usersMails
     * @param serverPath
     * @param acceptUrl
     * @param rejectUrl
     */
    @Override
    public void shareItems(User user, List<SecuredItem> sharedItems, List<String> usersMails, String serverPath, String acceptUrl, String rejectUrl) {

        user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_SHAREITEMS,
                new SharingWrapper(sharedItems, usersMails,
                        serverPath, acceptUrl, rejectUrl), String.class);

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

        user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_SHAREITEMS,
                new SharingWrapper(sharedItems, usersMails, Boolean.TRUE), String.class);

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

        return fromJSON(new TypeReference<List<User>>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_SHAREDUSERS, String.class, item.getNodeRef()));

    }

    /**
     * Get all securedItems shared for specified user on a company.
     *
     * @param userLogged
     * @param userToGetShares
     * @param c
     * @return
     * @throws AlfrescoServiceException
     */
    @Override
    public List<SecuredItem> sharedItems(User userLogged, User userToGetShares, Company c) throws AlfrescoServiceException {

        return fromJSON(new TypeReference<List<SecuredItem>>() {
        }, userLogged.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_SHAREDITEMS,
                String.class, userToGetShares.getUserName(), c.getName()));
    }

}
