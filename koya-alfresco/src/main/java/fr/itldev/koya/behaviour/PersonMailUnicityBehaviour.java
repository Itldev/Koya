package fr.itldev.koya.behaviour;

import fr.itldev.koya.model.KoyaModel;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Check Mail unicity on creation
 *
 */
public class PersonMailUnicityBehaviour implements
        NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

    private final Logger logger = Logger.getLogger(this.getClass());

    private PolicyComponent policyComponent;
    private NodeService nodeService;

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void init() {
        // Create behaviours

        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef) {

        NodeRef person = childAssocRef.getChildRef();
        String mail = (String) nodeService.getProperty(person, ContentModel.PROP_EMAIL);

        //add koya:mailunique aspect on person --> execute mail unicity constraint
        Map<QName, Serializable> props = new HashMap<>();
        props.put(KoyaModel.QNAME_PROPASPECT_KOYA_MAIL, mail);
        nodeService.addAspect(person, KoyaModel.QNAME_KOYA_MAILUNIQUE, props);

    }

    @Override
    public void onUpdateProperties(final NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {


        final String mailAfterModif = (String) after.get(ContentModel.PROP_EMAIL);

        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork< Object>() {
            @Override
            public Object doWork() throws Exception {

                //Add MailUnique Aspect if not already present
                if (!nodeService.hasAspect(nodeRef, KoyaModel.QNAME_KOYA_MAILUNIQUE)) {
                    Map<QName, Serializable> props = new HashMap<>();
                    nodeService.addAspect(nodeRef, KoyaModel.QNAME_KOYA_MAILUNIQUE, props);
                }

                nodeService.setProperty(nodeRef, KoyaModel.QNAME_PROPASPECT_KOYA_MAIL, mailAfterModif);
                return null;
            }
        });

    }

}
