/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.obs.services.internal.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.obs.aitool.AIGenerated;
import com.obs.services.exception.ObsException;
import com.obs.services.model.objectlock.ObjectRetention;

import org.junit.Test;

public class ObjectRetentionXMLBuilderTest {

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML throws ObsException when retention is null")
    @Test
    public void should_throw_obs_exception_when_retention_is_null() {
        ObjectRetentionXMLBuilder builder = new ObjectRetentionXMLBuilder();
        try {
            builder.buildXML(null);
            fail("Expected ObsException when retention is null");
        } catch (ObsException e) {
            assertTrue("Error message should mention null retention",
                    e.getMessage().contains("ObjectRetention is null"));
        }
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML produces full XML when mode and retainUntilDate are set")
    @Test
    public void should_build_full_xml_when_mode_and_date_set() {
        ObjectRetentionXMLBuilder builder = new ObjectRetentionXMLBuilder();
        String xml = builder.buildXML(createFullRetention());

        assertTrue("XML should contain Retention element",
                xml.contains("<Retention>"));
        assertTrue("XML should contain Mode element",
                xml.contains("<Mode>"));
        assertTrue("XML should contain RetainUntilDate element",
                xml.contains("<RetainUntilDate>"));
        assertTrue("XML should contain COMPLIANCE mode",
                xml.contains("COMPLIANCE"));
        assertTrue("XML should contain retain until date value",
                xml.contains("<RetainUntilDate>" + 1234567890123L + "</RetainUntilDate>"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML omits Mode element when mode is null")
    @Test
    public void should_build_xml_without_mode_when_mode_is_null() {
        ObjectRetentionXMLBuilder builder = new ObjectRetentionXMLBuilder();
        String xml = builder.buildXML(createRetentionWithoutMode());

        assertTrue("XML should contain Retention element",
                xml.contains("<Retention>"));
        assertFalse("XML should not contain Mode element when mode is null",
                xml.contains("<Mode>"));
        assertTrue("XML should contain RetainUntilDate element",
                xml.contains("<RetainUntilDate>"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML omits RetainUntilDate element when retainUntilDate is null")
    @Test
    public void should_build_xml_without_retain_until_date_when_date_is_null() {
        ObjectRetentionXMLBuilder builder = new ObjectRetentionXMLBuilder();
        String xml = builder.buildXML(createRetentionWithoutRetainUntilDate());

        assertTrue("XML should contain Retention element",
                xml.contains("<Retention>"));
        assertTrue("XML should contain Mode element",
                xml.contains("<Mode>"));
        assertFalse("XML should not contain RetainUntilDate element when retainUntilDate is null",
                xml.contains("<RetainUntilDate>"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML produces empty Retention element when all fields are null")
    @Test
    public void should_build_empty_retention_xml_when_all_fields_null() {
        ObjectRetentionXMLBuilder builder = new ObjectRetentionXMLBuilder();
        String xml = builder.buildXML(createRetentionWithNoFields());

        assertTrue("XML should contain Retention element",
                xml.contains("<Retention>"));
        assertFalse("XML should not contain Mode element when mode is null",
                xml.contains("<Mode>"));
        assertFalse("XML should not contain RetainUntilDate element when retainUntilDate is null",
                xml.contains("<RetainUntilDate>"));
        assertTrue("XML should contain only empty Retention element",
                xml.equals("<Retention></Retention>"));
    }

    private static ObjectRetention createFullRetention() {
        return new ObjectRetention("COMPLIANCE", 1234567890123L);
    }

    private static ObjectRetention createRetentionWithoutMode() {
        return new ObjectRetention(null, 9876543210987L);
    }

    private static ObjectRetention createRetentionWithoutRetainUntilDate() {
        return new ObjectRetention("GOVERNANCE", null);
    }

    private static ObjectRetention createRetentionWithNoFields() {
        return new ObjectRetention(null, null);
    }
}
