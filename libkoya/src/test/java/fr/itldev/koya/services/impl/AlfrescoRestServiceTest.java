package fr.itldev.koya.services.impl;

import java.util.List;

import junit.framework.TestCase;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.itldev.koya.model.impl.Activity;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:koya-services-tests.xml")
public class AlfrescoRestServiceTest extends TestCase {

    /**
     * Test of fromJSON method, of class AlfrescoRestService.
     */
    @Test
    public void testFromJSON() {
        System.out.println("fromJSON");

        //Test Notification JSON deserialization
        String notificationJsonStr
                = "["
                + "{"
                + "\"id\":21,"
                + "\"siteNetwork\":\"koya-company\","
                + "\"feedUserId\":\"admin\","
                + "\"postUserId\":\"admin\","
                + "\"postDate\":\"2014-09-10T16:33:48.000+02:00\","
                + "\"activitySummary\":\"{"
                + "\\\"parentNodeRef\\\":\\\"workspace://SpacesStore/821b6c54-5172-49e0-b2cf-5b8bafec5b16\\\","
                + "\\\"lastName\\\":\\\"\\\","
                + "\\\"nodeRefL\\\":\\\"workspace://SpacesStore/821b6c54-5172-49e0-b2cf-5b8bafec5b16\\\","
                + "\\\"name\\\":\\\"061.JPG\\\","
                + "\\\"typeQName\\\":\\\"content\\\","
                + "\\\"nodeRef\\\":\\\"workspace://SpacesStore/821b6c54-5172-49e0-b2cf-5b8bafec5b16\\\","
                + "\\\"displayPath\\\":\\\"\\\","
                + "\\\"firstName\\\":\\\"Administrator\\\""
                + "}\","
                + "\"activityType\":\"org.alfresco.documentlibrary.file-deleted\""
                + "},"
                + "{"
                + "\"id\":20,"
                + "\"siteNetwork\":\"koya-company\","
                + "\"feedUserId\":\"admin\","
                + "\"postUserId\":\"admin\","
                + "\"postDate\":\"2014-09-10T16:33:35.000+02:00\","
                + "\"activitySummary\":\"{"
                + "\\\"parentNodeRef\\\":\\\"workspace://SpacesStore/91700f66-58d0-481f-8f02-93770ce39f35\\\","
                + "\\\"lastName\\\":\\\"\\\","
                + "\\\"name\\\":\\\"061.JPG\\\","
                + "\\\"nodeRef\\\":\\\"workspace://SpacesStore/821b6c54-5172-49e0-b2cf-5b8bafec5b16\\\","
                + "\\\"firstName\\\":\\\"Administrator\\\""
                + "}\","
                + "\"activityType\":\"org.alfresco.documentlibrary.file-added\""
                + "}"
                + "]";
        Object expResult = null;

        Object result = AlfrescoRestService.fromJSON(new TypeReference<List<Activity>>() {
        }, notificationJsonStr);

    }

}
