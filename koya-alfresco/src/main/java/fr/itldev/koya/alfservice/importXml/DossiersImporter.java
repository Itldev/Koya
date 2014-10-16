package fr.itldev.koya.alfservice.importXml;

import fr.itldev.koya.alfservice.importXml.model.DossierXml;
import fr.itldev.koya.alfservice.importXml.model.DossiersXmlWrapper;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;

public class DossiersImporter {

    private FileFolderService fileFolderService;

    public int Import(NodeRef nodeRef) {
        int nbDossiersImport = 0;
        try {
            JAXBContext context = JAXBContext.newInstance(DossiersXmlWrapper.class, DossierXml.class);
            List<DossierXml> l = ((DossiersXmlWrapper)context.createUnmarshaller().unmarshal(fileFolderService.getReader(nodeRef).getContentInputStream())).getDossiers();

            nbDossiersImport = l.size();
            
        } catch (JAXBException ex) {
            Logger.getLogger(DossiersImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nbDossiersImport;
    }
}
