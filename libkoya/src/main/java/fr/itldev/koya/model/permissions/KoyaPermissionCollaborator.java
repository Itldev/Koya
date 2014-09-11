package fr.itldev.koya.model.permissions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class KoyaPermissionCollaborator extends KoyaPermission {

    public static final KoyaPermissionCollaborator MEMBER
            = new KoyaPermissionCollaborator("KoyaMember");
    public static final KoyaPermissionCollaborator RESPONSIBLE
            = new KoyaPermissionCollaborator("KoyaResponsible");

    private KoyaPermissionCollaborator(String permissionName) {
        super(permissionName);
    }

    public static List<KoyaPermission> getAll() {

        List<KoyaPermission> all = new ArrayList<>();
        all.add(KoyaPermissionCollaborator.RESPONSIBLE);
        all.add(KoyaPermissionCollaborator.MEMBER);

        return all;

    }

    public static List<String> getAllAsString() {
        List<String> perms = new ArrayList<>();
        for (KoyaPermission p : getAll()) {
            perms.add(p.permissionName);
        }
        return perms;
    }


}
