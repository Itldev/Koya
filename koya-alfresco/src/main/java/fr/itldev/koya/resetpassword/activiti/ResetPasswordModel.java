package fr.itldev.koya.resetpassword.activiti;

import org.alfresco.service.namespace.QName;

/**
 *
 *
 */
public interface ResetPasswordModel {

    public static final String KOYARP_MODEL_1_0_URI = "http://www.itldev.fr/model/workflow/resetPassword/1.0";

    public static final QName TYPE_RESET_REQUEST_TASK = QName.createQName(KOYARP_MODEL_1_0_URI, "resetRequestTask");
    public static final QName PROP_RESETURL = QName.createQName(KOYARP_MODEL_1_0_URI, "resetUrl");
    public static final QName PROP_RESETTICKET = QName.createQName(KOYARP_MODEL_1_0_URI, "resetTicket");

    public static final QName TYPE_RESET_PENDING_TASK = QName.createQName(KOYARP_MODEL_1_0_URI, "resetPendingTask");
    public static final QName PROP_RESET_NEWPASSWORD = QName.createQName(KOYARP_MODEL_1_0_URI, "newPassword");
    
    public static final String wfVarUser = "bpm_assignee";
    public static final String wfVarResetUrl = "koyarpwf_resetUrl";
    public static final String wfVarResetTicket = "koyarpwf_resetTicket";
    public static final String wfVarNewPassword = "koyarpwf_newPassword";

}
