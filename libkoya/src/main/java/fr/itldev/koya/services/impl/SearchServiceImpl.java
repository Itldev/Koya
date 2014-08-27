package fr.itldev.koya.services.impl;

import fr.itldev.koya.model.Container;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import fr.itldev.koya.services.SearchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;

/**
 * Search service.
 *
 */
public class SearchServiceImpl extends AlfrescoRestService implements SearchService {

    private static final String REST_GET_SEARCH = "/s/slingshot/search?"
            + "term={term}&maxResults={maxResults}"
            + "&sort=&repo=true&rootNode={rootNode}&query={query}";

    private static final String SEARCHRESULTKEY_TOTALRECORDS = "totalRecords";
    private static final String SEARCHRESULTKEY_TOTALRECORDSUPPER = "totalRecordsUpper";
    private static final String SEARCHRESULTKEY_STARTINDEX = "startIndex";
    private static final String SEARCHRESULTKEY_ITEMS = "items";
    //item attributes
    private static final String SEARCHRESULTKEY_NODEREF = "nodeRef";
    private static final String SEARCHRESULTKEY_NAME = "name";
    private static final String SEARCHRESULTKEY_DISPLAYNAME = "displayName";

    /*
     * 
     */
    private static final Integer DEFAULT_MAXITEMS = 50;

    /**
     * Execute basic search for all securedItems from securedItem defined as
     * base.
     *
     * @param user
     * @param base
     * @param searchexpr
     * @return
     */
    @Override
    public List<SecuredItem> search(User user, Container base, String searchexpr) {

        String baseNodeRef = "";

        if (base != null) {
            baseNodeRef = base.getNodeRef();
        }

        List<SecuredItem> itemsFound = new ArrayList<>();

        Map result = user.getRestTemplate().getForObject(
                getAlfrescoServerUrl() + REST_GET_SEARCH, Map.class, processSearchExpr(searchexpr),
                DEFAULT_MAXITEMS, baseNodeRef, "");

        for (Map itemMap : (List<Map>) result.get(SEARCHRESULTKEY_ITEMS)) {

            try {
                String nodeRef = itemMap.get(SEARCHRESULTKEY_NODEREF).toString();
                SecuredItem item = getSecuredItem(user, nodeRef);
                if (item != null) {
                    itemsFound.add(item);
                }
            } catch (RestClientException ex) {
            }
        }

        return itemsFound;
    }

    /**
     * TODO complex methods that :
     *
     * - allow paginator and max results.
     *
     * - limit results type
     */
    /**
     * Simple input search processing
     *
     * TODO options : starts with, ends with, contain
     *
     * TODO do treat expressions between quotes
     *
     * @param inSearch
     * @return
     */
    private String processSearchExpr(String inSearch) {
        /* ==== format search expression ====
        
         Find all elements that starts with single words defined in searhexpr.
        
         if many, results should contains each single words (AND separate in search)
         
         */

        if (inSearch == null || inSearch.isEmpty()) {
            return "*";
        } else {

            //remove AND terms
            inSearch = inSearch.replaceAll(" AND ", " ");
            //deletes starting, finishing and multispaces
            inSearch = inSearch.replaceAll("^[\\s]+", "");
            inSearch = inSearch.replaceAll("\\s+$", "");
            inSearch = inSearch.replaceAll("\\s+", " ");

            String searchExpr = "";
            String sep = "";
            for (String term : inSearch.split(" ")) {
                searchExpr += sep + " " + term + "*";
                sep = " AND ";
            }
            return searchExpr;
        }

    }
}
