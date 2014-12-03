package fr.itldev.koya.action.importXml.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dossier")
@XmlAccessorType(XmlAccessType.NONE)
public class DossierXml {

    @XmlElement(name = "reference")
    private String reference;
    @XmlElement(name = "name")
    private String name;
    @XmlElementWrapper(name = "responsibles")
    @XmlElement(name = "responsible")
    private List<String> responsibles = new ArrayList<>();
    @XmlElementWrapper(name = "members")
    @XmlElement(name = "member")
    private List<String> members = new ArrayList<>();
    @XmlElement(name = "space", nillable = true)
    private String space;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getResponsibles() {
        return responsibles;
    }

    public void setResponsibles(List<String> responsable) {
        this.responsibles = responsable;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

}
