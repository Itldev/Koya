package fr.itldev.koya.model.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.util.collections.CollectionUtils;

/**
 *
 *
 */
public class SitePermission extends AlfrescoPermission {

    public static final SitePermission MANAGER = new SitePermission(SiteModel.SITE_MANAGER);
    public static final SitePermission COLLABORATOR = new SitePermission(SiteModel.SITE_COLLABORATOR);
    public static final SitePermission CONTRIBUTOR = new SitePermission(SiteModel.SITE_CONTRIBUTOR);
    public static final SitePermission CONSUMER = new SitePermission(SiteModel.SITE_CONSUMER);

    public SitePermission(String permissionName) {
        super(permissionName);
    }

    private static final Map<String, SitePermission> sitePermissionCache = new HashMap<String, SitePermission>() {
        {
            for (SitePermission sp : getAll()) {
                put(sp.permissionName, sp);
            }
        }
    };

    public static List<SitePermission> getAll() {

        List<SitePermission> all = new ArrayList<>();
        all.add(SitePermission.MANAGER);
        all.add(SitePermission.COLLABORATOR);
        all.add(SitePermission.CONSUMER);

        return all;

    }

    public static List<String> getAllAsString() {
        return CollectionUtils.toListOfStrings(getAll());
    }

    public static SitePermission valueOf(String permissionName) {
        return sitePermissionCache.get(permissionName);
    }
}
