
package fr.itldev.koya.action.importXml.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "contents")
@XmlAccessorType(XmlAccessType.NONE)
public class ContentsXmlWrapper {
    @XmlElementRef
    private List<ContentXml> contentXmls;

    public List<ContentXml> getContentXmls() {
        return contentXmls;
    }

    public void setContentXmls(List<ContentXml> contentXmls) {
        this.contentXmls = contentXmls;
    }
}
