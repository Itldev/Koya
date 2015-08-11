package fr.itldev.koya.workflows.realestatesale;

import org.alfresco.service.namespace.QName;

/**
 *
 *
 */
public interface RealEstateSaleModel {

    public static final String KOYARES_MODEL_1_0_URI = "http://www.itldev.fr/model/workflow/realEstateSale/1.0";

    
    //TASKS 
    public static final QName TYPE_START_TASK = QName.createQName(KOYARES_MODEL_1_0_URI, "startTask");
    public static final QName TYPE_RESET_PENDING_TASK = QName.createQName(KOYARES_MODEL_1_0_URI, "pendingValidationTask");
    
    
    //PROPERTIES
    
    //START TASK
    public static final QName PROP_SELLER = QName.createQName(KOYARES_MODEL_1_0_URI, "seller");
    public static final QName PROP_BUYER = QName.createQName(KOYARES_MODEL_1_0_URI, "buyer");
    
    //UP DOC
    public static final QName PROP_UPLOAD_DOC = QName.createQName(KOYARES_MODEL_1_0_URI, "upDoc");


    
    //WORKFLOW PROPERTIES ?????
    public static final String wfVarUser = "bpm_assignee";
    public static final String wfVarSeller = "koyares_seller";
    public static final String wfVarBuyer = "koyares_buyer";
}
