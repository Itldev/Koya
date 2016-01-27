package fr.itldev.koya.model.permissions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class KoyaPermission extends AlfrescoPermission {

    protected KoyaPermission(String permissionName) {
        super(permissionName);
    }

    public static List<KoyaPermission> getAll() {

        List<KoyaPermission> all = new ArrayList<>();
        all.add(KoyaPermissionCollaborator.RESPONSIBLE);
        all.add(KoyaPermissionCollaborator.MEMBER);

        all.add(KoyaPermissionConsumer.CLIENT);
        all.add(KoyaPermissionConsumer.PARTNER);

        return all;

    }

    public static List<String> getAllAsString() {
        List<String> perms = new ArrayList<>();
        for (KoyaPermission p : getAll()) {
            perms.add(p.permissionName);
        }
        return perms;
    }

    public static KoyaPermission valueOf(String permissionName) {
        for (KoyaPermission p : getAll()) {
            if (p.equals(permissionName)) {
                return p;
            }
        }
        return null;
    }
}
