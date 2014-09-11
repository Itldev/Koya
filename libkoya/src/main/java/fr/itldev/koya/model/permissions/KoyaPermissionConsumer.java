package fr.itldev.koya.model.permissions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class KoyaPermissionConsumer extends KoyaPermission {

    public static final KoyaPermissionConsumer CLIENT = new KoyaPermissionConsumer("KoyaClient");
    public static final KoyaPermissionConsumer PARTNER = new KoyaPermissionConsumer("KoyaPartner");
    public static final KoyaPermissionConsumer CLIENTCONTRIBUTOR = new KoyaPermissionConsumer("KoyaClientContributor");

    private KoyaPermissionConsumer(String permissionName) {
        super(permissionName);
    }

    public static List<KoyaPermission> getAll() {
        List<KoyaPermission> all = new ArrayList<>();
        all.add(KoyaPermissionConsumer.CLIENT);
        all.add(KoyaPermissionConsumer.PARTNER);
        all.add(KoyaPermissionConsumer.CLIENTCONTRIBUTOR);

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
