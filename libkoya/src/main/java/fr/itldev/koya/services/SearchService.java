package fr.itldev.koya.services;

import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import java.util.List;
import java.util.Set;

public interface SearchService {

    /**
     *
     * @param user
     * @param filters
     * @return
     */
    List<SecuredItem> search(User user, Set filters);//cf alfresco search services options (paging infos etc)

}
