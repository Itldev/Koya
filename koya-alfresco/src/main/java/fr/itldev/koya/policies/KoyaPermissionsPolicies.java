package fr.itldev.koya.policies;

import fr.itldev.koya.model.permissions.KoyaPermission;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.interfaces.SubSpace;
import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.namespace.QName;

/**
 *This policies appens on CONSUMER users role attirbution
 *
 */
public interface KoyaPermissionsPolicies {

    public interface AfterGrantKoyaPermissionPolicy extends ClassPolicy {

        public static final String NAMESPACE = KoyaModel.KOYA_URI;

        public static final QName QNAME = QName.createQName(KoyaModel.KOYA_URI, "afterGrantKoyaPermission");

        public void afterGrantKoyaPermission(SubSpace subSpace, String authority, KoyaPermission permission);
    }

    public interface BeforeRevokeKoyaPermissionPolicy extends ClassPolicy {

        public static final String NAMESPACE = KoyaModel.KOYA_URI;

        public static final QName QNAME = QName.createQName(KoyaModel.KOYA_URI, "beforeRevokeKoyaPermission");

        public void beforeRevokeKoyaPermission(SubSpace subSpace, String authority, KoyaPermission permission);
    }
}
