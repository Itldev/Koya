package fr.itldev.koya.model.json;

import fr.itldev.koya.model.SecuredItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * This object wraps a list of securedItems to be shared to a list of users mail
 * adresses.Itl is useful to be json serialized.
 *
 *
 */
public class SharingWrapper {

    private List<String> sharedNodeRefs = new ArrayList<>();
    private List<String> sharingUsersMails;

    public List<String> getSharedNodeRefs() {
        return sharedNodeRefs;
    }

    public void setSharedNodeRefs(List<String> sharedNodeRefs) {
        this.sharedNodeRefs = sharedNodeRefs;
    }

    public List<String> getSharingUsersMails() {
        return sharingUsersMails;
    }

    public void setSharingUsersMails(List<String> sharingUsersMails) {
        this.sharingUsersMails = sharingUsersMails;
    }

    public SharingWrapper(List<SecuredItem> sharedItems, List<String> usersMails) {

        for (SecuredItem s : sharedItems) {
            sharedNodeRefs.add(s.getNodeRef());
        }
        sharingUsersMails = usersMails;
    }

    public SharingWrapper() {
    }

}
