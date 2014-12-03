package fr.itldev.koya.action.importXml.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dossiers")
@XmlAccessorType(XmlAccessType.NONE)
public class DossiersXmlWrapper {
    @XmlElementRef
    private List<DossierXml> dossiers;

    public List<DossierXml> getDossiers() {
        return dossiers;
    }

    public void setDossiers(List<DossierXml> dossiers) {
        this.dossiers = dossiers;
    }
    
    
}
