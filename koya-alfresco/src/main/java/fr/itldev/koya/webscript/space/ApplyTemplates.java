/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.itldev.koya.webscript.space;

import fr.itldev.koya.alfservice.CompanyService;
import fr.itldev.koya.alfservice.ModelService;
import fr.itldev.koya.alfservice.SpaceService;
import fr.itldev.koya.model.json.ItlAlfrescoServiceWrapper;
import fr.itldev.koya.webscript.KoyaWebscript;
import java.util.Map;

/**
 *
 * @author nico
 */
public class ApplyTemplates extends KoyaWebscript {
    ModelService modelService;

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    @Override
    public ItlAlfrescoServiceWrapper koyaExecute(ItlAlfrescoServiceWrapper wrapper, Map<String, String> urlParams, Map<String, Object> jsonPostMap) throws Exception {
        wrapper.addItem(modelService.companyInitTemplate(urlParams.get("shortname"), urlParams.get("templatename")));
        
        return wrapper;
    }
    
    
    
}
