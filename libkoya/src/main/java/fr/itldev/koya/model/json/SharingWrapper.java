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
    private Boolean resetSharings;
    private String acceptUrl;
    private String rejectUrl;
    private String serverPath;

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

    public Boolean isResetSharings() {
        return resetSharings;
    }

    public void setResetSharings(Boolean resetSharings) {
        this.resetSharings = resetSharings;
    }

    public String getAcceptUrl() {
        return acceptUrl;
    }

    public void setAcceptUrl(String acceptUrl) {
        this.acceptUrl = acceptUrl;
    }

    public String getRejectUrl() {
        return rejectUrl;
    }

    public void setRejectUrl(String rejectUrl) {
        this.rejectUrl = rejectUrl;
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    public SharingWrapper(List<SecuredItem> sharedItems, List<String> usersMails) {
        this(sharedItems, usersMails, Boolean.FALSE, null, null, null);
    }

    public SharingWrapper(List<SecuredItem> sharedItems, List<String> usersMails, Boolean resetSharings) {
        this(sharedItems, usersMails, resetSharings, null, null, null);
    }

    public SharingWrapper(List<SecuredItem> sharedItems, List<String> usersMails, String serverPath, String acceptUrl, String rejectUrl) {
        this(sharedItems, usersMails, Boolean.FALSE, serverPath, acceptUrl, rejectUrl);
    }

    public SharingWrapper(List<SecuredItem> sharedItems, List<String> usersMails, Boolean resetSharings, String serverPath, String acceptUrl, String rejectUrl) {

        for (SecuredItem s : sharedItems) {
            sharedNodeRefs.add(s.getNodeRef());
        }
        sharingUsersMails = usersMails;

        this.resetSharings = resetSharings;
        this.acceptUrl = acceptUrl;
        this.rejectUrl = rejectUrl;
        this.serverPath = serverPath;
    }

    public SharingWrapper() {
    }

}
