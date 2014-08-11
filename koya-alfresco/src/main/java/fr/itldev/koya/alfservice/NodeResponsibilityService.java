package fr.itldev.koya.alfservice;

import fr.itldev.koya.exception.KoyaServiceException;
import fr.itldev.koya.model.KoyaModel;
import fr.itldev.koya.model.impl.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

/**
 * Service that manages Nodes responsabilty concept. A node can have many
 * responsibles users.
 *
 * Only used for Dossiers for the moment.
 *
 *
 */
public class NodeResponsibilityService {

    private NodeService nodeService;

    private UserService userService;
    private OwnableService ownableService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }

    private static final QNamePattern QNAMEPATTERN_ASSOCRESP = new QNamePattern() {
        @Override
        public boolean isMatch(QName qname) {
            return qname.equals(KoyaModel.ASSOC_RESPONSIBLES);
        }
    };

    /**
     * List all dossiers responsibles.
     *
     * @param dossierNodeRef
     * @return
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public List<User> listResponsibles(NodeRef dossierNodeRef) throws KoyaServiceException {
        List<User> responsibles = new ArrayList<>();
        setResponsabilityAspectIfNeeded(dossierNodeRef);

        for (AssociationRef ar : nodeService.getTargetAssocs(dossierNodeRef, QNAMEPATTERN_ASSOCRESP)) {
            responsibles.add(userService.buildUser(ar.getTargetRef()));
        }
        return responsibles;
    }

    /**
     * Adds new responsibles for selected nodeRef.
     *
     * @param dossierNodeRef
     * @param responsibles
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public void addResponsible(NodeRef dossierNodeRef, List<String> responsibles) throws KoyaServiceException {
        setResponsabilityAspectIfNeeded(dossierNodeRef);
        List<NodeRef> resps = getResponsiblesNodeRefs(dossierNodeRef);

        for (String r : responsibles) {
            NodeRef nRespAdd = userService.getUserByUsername(r).getNodeRefasObject();
            if (!resps.contains(nRespAdd)) {
                resps.add(nRespAdd);
            }
        }
        nodeService.setAssociations(dossierNodeRef, KoyaModel.ASSOC_RESPONSIBLES, resps);
    }

    /**
     *
     * @param dossierNodeRef
     * @param responsibles
     * @throws fr.itldev.koya.exception.KoyaServiceException
     */
    public void delResponsible(NodeRef dossierNodeRef, List<String> responsibles) throws KoyaServiceException {
        setResponsabilityAspectIfNeeded(dossierNodeRef);
        List<NodeRef> resps = getResponsiblesNodeRefs(dossierNodeRef);
        for (String r : responsibles) {
            NodeRef nRespDel = userService.getUserByUsername(r).getNodeRefasObject();
            if (resps.contains(nRespDel)) {
                resps.remove(nRespDel);
            }
        }
        nodeService.setAssociations(dossierNodeRef, KoyaModel.ASSOC_RESPONSIBLES, resps);
    }

    /**
     * Set responsability aspect, owner is automaticly set as responsible.
     *
     * @param n
     */
    private void setResponsabilityAspectIfNeeded(NodeRef n) {
        if (!nodeService.hasAspect(n, KoyaModel.ASPECT_RESPONSABILTY)) {
            nodeService.addAspect(n, KoyaModel.ASPECT_RESPONSABILTY, null);
            User owner = userService.getUserByUsername(ownableService.getOwner(n));
            nodeService.setAssociations(n, KoyaModel.ASSOC_RESPONSIBLES, new ArrayList<>(Arrays.asList(owner.getNodeRefasObject())));
        }
    }

    /**
     * Get Node's Responsibles noderefs.
     *
     * @param n
     * @return
     */
    private List<NodeRef> getResponsiblesNodeRefs(NodeRef n) {
        List<NodeRef> responsibles = new ArrayList<>();
        for (AssociationRef ar : nodeService.getTargetAssocs(n, QNAMEPATTERN_ASSOCRESP)) {
            responsibles.add(ar.getTargetRef());
        }
        return responsibles;
    }
}
