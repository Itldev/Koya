package fr.itldev.koya.services;

import fr.itldev.koya.model.Container;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import java.util.List;

public interface SearchService {

    /**
     * Execute basic search for all securedItems from securedItem defined as base.
     *
     * @param user
     * @param base
     * @param searchexpr
     * @return
     */
    List<SecuredItem> search(User user, Container base, String searchexpr);

}
