package fr.itldev.koya.constraint;

import org.alfresco.repo.dictionary.constraint.AbstractConstraint;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.log4j.Logger;

/**
 * Constraint thats check if user mail attribute value already exists in
 * Alfresco instance.
 *
 */
public class UniqueMailConstraint extends AbstractConstraint {

    private Logger logger = Logger.getLogger(this.getClass());
    //Search Service Staticly defined in order to be injected by bean declaration 
    //but used by pojo instancied Objects
    private static SearchService searchService;

    public void setSearchService(SearchService searchService) {
        UniqueMailConstraint.searchService = searchService;
    }

    @Override
    protected void evaluateSingleValue(final Object value) {
        final String luceneRequest = "TYPE:\"cm:person\" AND @cm\\:email:\"" + value.toString() + "\" ";

        ConstraintException contraintEx = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< ConstraintException>() {
            @Override
            public ConstraintException doWork() throws Exception {
                ResultSet rs = null;

                ConstraintException contraintEx;
                try {
                    rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, luceneRequest);
                    //mail @ should appear only once (ie in user currently created attribute)            
                    if (rs.getNumberFound() > 1) {
                        return new ConstraintException("user email already exists", value);
                    }

                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
                return null;
            }

        });
        

        if (contraintEx != null) {
            throw contraintEx;
        }

    }

}
