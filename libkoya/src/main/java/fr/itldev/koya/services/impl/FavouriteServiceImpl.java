package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.FavouriteService;
import fr.itldev.koya.services.UserService;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import static fr.itldev.koya.services.impl.AlfrescoRestService.fromJSON;
import java.util.List;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;

public class FavouriteServiceImpl extends AlfrescoRestService implements FavouriteService {

    private static final String REST_POST_TOGGLEFAVOURITE = "/s/fr/itldev/koya/global/togglefavourite";
    private static final String REST_GET_GETFAVOURITES = "/s/fr/itldev/koya/global/getfavourites";

    @Autowired
    UserService userService;

    @Override
    public List<SecuredItem> getFavourites(User user) throws AlfrescoServiceException {
        return fromJSON(new TypeReference<List<SecuredItem>>() {
        }, user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_GETFAVOURITES, String.class));
    }

    @Override
    public void setFavouriteValue(User user, SecuredItem item, Boolean favouriteValue) throws AlfrescoServiceException {
        item.setUserFavourite(favouriteValue);
        user.getRestTemplate().postForObject(
                getAlfrescoServerUrl() + REST_POST_TOGGLEFAVOURITE, item, String.class);
        //Automaticly reload user's preferences
        userService.loadPreferences(user);

    }

}
