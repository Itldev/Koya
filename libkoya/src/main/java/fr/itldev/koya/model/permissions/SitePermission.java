package fr.itldev.koya.model.permissions;

import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.site.SiteModel;

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

    public static List<SitePermission> getAll() {

        List<SitePermission> all = new ArrayList<>();
        all.add(SitePermission.MANAGER);
        all.add(SitePermission.COLLABORATOR);
        all.add(SitePermission.CONSUMER);

        return all;

    }

    public static List<String> getAllAsString() {
        List<String> perms = new ArrayList<>();
        for (SitePermission p : getAll()) {
            perms.add(p.permissionName);
        }
        return perms;
    }

    public static SitePermission valueOf(String permissionName) {
        for (SitePermission p : getAll()) {
            if (p.equals(permissionName)) {
                return p;
            }
        }
        return null;
    }

}
