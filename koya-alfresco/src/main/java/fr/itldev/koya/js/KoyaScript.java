package fr.itldev.koya.js;

import fr.itldev.koya.alfservice.KoyaAclService;
import fr.itldev.koya.alfservice.KoyaNodeService;
import fr.itldev.koya.alfservice.KoyaShareService;
import fr.itldev.koya.alfservice.UserService;
import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.SecuredItem;
import fr.itldev.koya.model.impl.User;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.ServiceRegistry;

/**
 *
 *
 */
public class KoyaScript extends BaseProcessorExtension {

    private KoyaAclService koyaAclService;
    private KoyaNodeService koyaNodeService;
    private KoyaShareService koyaShareService;
    private UserService userService;
    private ServiceRegistry serviceRegistry;

    // <editor-fold defaultstate="collapsed" desc="getters/setters">
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setKoyaAclService(KoyaAclService koyaAclService) {
        this.koyaAclService = koyaAclService;
    }

    public void setKoyaNodeService(KoyaNodeService koyaNodeService) {
        this.koyaNodeService = koyaNodeService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setKoyaShareService(KoyaShareService koyaShareService) {
        this.koyaShareService = koyaShareService;
    }

    //</editor-fold>
    /**
     * List users who can access a node.
     *
     * @param n
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<ScriptNode> listUsersWhoCanAccesNode(ScriptNode n) throws KoyaServiceException {

        List<ScriptNode> users = new ArrayList<>();
        for (User u : koyaShareService.listUsersAccessShare(koyaNodeService.nodeRef2SecuredItem(n.getNodeRef()))) {
            users.add(new ScriptNode(u.getNodeRefasObject(), serviceRegistry));
        }

        return users;
    }

    /**
     *
     * @param n
     * @return
     */
    public List<ScriptNode> listNodesSharedWithUser(ScriptNode n) throws KoyaServiceException {
        List<ScriptNode> sharedElements = new ArrayList<>();
        for (SecuredItem s : koyaShareService.listItemsShared(userService.buildUser(n.getNodeRef()))) {
            sharedElements.add(new ScriptNode(s.getNodeRefasObject(), serviceRegistry));
        }
        return sharedElements;
    }

    /**
     * List all readable secured items in application for user in param.
     *
     * @param n
     * @return
     * @throws KoyaServiceException
     */
    public List<ScriptNode> listReadableSecuredItems(ScriptNode n) throws KoyaServiceException {
        List<ScriptNode> readableSecuredItems = new ArrayList<>();
        for (SecuredItem s : koyaShareService.getReadableSecuredItem(userService.buildUser(n.getNodeRef()), null)) {
            readableSecuredItems.add(new ScriptNode(s.getNodeRefasObject(), serviceRegistry));
        }
        return readableSecuredItems;
    }

}
