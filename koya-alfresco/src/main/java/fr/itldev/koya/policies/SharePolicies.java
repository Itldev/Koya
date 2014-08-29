/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.itldev.koya.policies;
import fr.itldev.koya.model.KoyaModel;
import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


public interface SharePolicies {

    public interface BeforeSharePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(KoyaModel.KOYA_URI, "beforeShare");
        /**
         * Called before an item is shared.
         * 
         * @param nodeRef the reference to the item about to be shared
         * @param username username the item is about to be shared whith
         */
        public void beforeShareItem(NodeRef nodeRef, String username);
    }
    
    public interface AfterSharePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(KoyaModel.KOYA_URI, "afterShare");
        /**
         * Called after an item has been shared.
         * 
         * @param nodeRef the reference to the item has been shared
         * @param username username the item has been shared whith
         */
        public void afterShareItem(NodeRef nodeRef, String username);
    }
    
    public interface BeforeUnsharePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(KoyaModel.KOYA_URI, "beforeUnshare");
        /**
         * Called before an item is unshared.
         * 
         * @param nodeRef the reference to the item about to be unshared
         * @param username username the item is about to be unshared from
         */
        public void beforeUnshareItem(NodeRef nodeRef, String username);
    }
    
    public interface AfterUnsharePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(KoyaModel.KOYA_URI, "afterUnshare");
        /**
         * Called after an item has been unshared.
         * 
         * @param nodeRef the reference to the item has been unshared
         * @param username username the item is about has been unshared from
         */
        public void afterUnshareItem(NodeRef nodeRef, String username);
    }
}
