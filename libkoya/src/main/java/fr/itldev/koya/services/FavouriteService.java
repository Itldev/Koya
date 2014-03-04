package fr.itldev.koya.services;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.exceptions.AlfrescoServiceException;
import java.util.List;

public interface FavouriteService {

    public List<SecuredItem> getFavourites(User user) throws AlfrescoServiceException;

    public void setFavouriteValue(User user, SecuredItem item, Boolean favouriteValue) throws AlfrescoServiceException;

}
