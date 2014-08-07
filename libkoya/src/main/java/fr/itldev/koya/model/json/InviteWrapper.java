package fr.itldev.koya.model.json;

import fr.itldev.koya.model.SecuredItem;
import java.util.List;

/**
 *
 * This object wraps invitation url that are transmitted to client.
 *
 *
 */
public class InviteWrapper {

    private String companyName;
    private String email;
    private String roleName;

    private String acceptUrl;
    private String rejectUrl;
    private String serverPath;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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

    public InviteWrapper(String serverPath, String acceptUrl, String rejectUrl) {

        this.acceptUrl = acceptUrl;
        this.rejectUrl = rejectUrl;
        this.serverPath = serverPath;
    }

    public InviteWrapper() {
    }

}
