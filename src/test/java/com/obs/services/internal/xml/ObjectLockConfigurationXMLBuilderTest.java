/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.obs.services.internal.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.obs.aitool.AIGenerated;
import com.obs.services.exception.ObsException;
import com.obs.services.model.objectlock.DefaultRetention;
import com.obs.services.model.objectlock.ObjectLockConfiguration;
import com.obs.services.model.objectlock.ObjectLockRule;

import org.junit.Test;

public class ObjectLockConfigurationXMLBuilderTest {

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML throws ObsException when config is null")
    @Test
    public void should_throw_obs_exception_when_config_is_null() {
        ObjectLockConfigurationXMLBuilder builder = new ObjectLockConfigurationXMLBuilder();
        try {
            builder.buildXML(null);
            fail("Expected ObsException when config is null");
        } catch (ObsException e) {
            assertTrue("Error message should mention null config",
                    e.getMessage().contains("ObjectLockConfiguration is null"));
        }
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML produces full XML when all fields are set")
    @Test
    public void should_build_full_xml_when_all_fields_set() {
        ObjectLockConfigurationXMLBuilder builder = new ObjectLockConfigurationXMLBuilder();
        String xml = builder.buildXML(createFullConfig());

        assertTrue("XML should contain ObjectLockEnabled element",
                xml.contains("<ObjectLockEnabled>"));
        assertTrue("XML should contain Rule element",
                xml.contains("<Rule>"));
        assertTrue("XML should contain DefaultRetention element",
                xml.contains("<DefaultRetention>"));
        assertTrue("XML should contain Mode element",
                xml.contains("<Mode>"));
        assertTrue("XML should contain Days element",
                xml.contains("<Days>"));
        assertTrue("XML should contain Years element",
                xml.contains("<Years>"));
        assertTrue("XML should contain COMPLIANCE mode",
                xml.contains("COMPLIANCE"));
        assertTrue("XML should contain 30 days",
                xml.contains("<Days>30</Days>"));
        assertTrue("XML should contain 1 year",
                xml.contains("<Years>1</Years>"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML omits ObjectLockEnabled when it is null")
    @Test
    public void should_build_xml_without_object_lock_enabled_when_it_is_null() {
        ObjectLockConfigurationXMLBuilder builder = new ObjectLockConfigurationXMLBuilder();
        String xml = builder.buildXML(createConfigWithoutObjectLockEnabled());

        assertFalse("XML should not contain ObjectLockEnabled element",
                xml.contains("<ObjectLockEnabled>"));
        assertTrue("XML should contain Rule element",
                xml.contains("<Rule>"));
        assertTrue("XML should contain DefaultRetention element",
                xml.contains("<DefaultRetention>"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML omits Rule when rule is null")
    @Test
    public void should_build_xml_without_rule_when_rule_is_null() {
        ObjectLockConfigurationXMLBuilder builder = new ObjectLockConfigurationXMLBuilder();
        String xml = builder.buildXML(createConfigWithoutRule());

        assertTrue("XML should contain ObjectLockEnabled element",
                xml.contains("<ObjectLockEnabled>"));
        assertFalse("XML should not contain Rule element",
                xml.contains("<Rule>"));
        assertFalse("XML should not contain DefaultRetention element",
                xml.contains("<DefaultRetention>"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML omits Rule when defaultRetention is null")
    @Test
    public void should_build_xml_without_rule_when_default_retention_is_null() {
        ObjectLockConfigurationXMLBuilder builder = new ObjectLockConfigurationXMLBuilder();
        String xml = builder.buildXML(createConfigWithoutDefaultRetention());

        assertTrue("XML should contain ObjectLockEnabled element",
                xml.contains("<ObjectLockEnabled>"));
        assertFalse("XML should not contain Rule element when defaultRetention is null",
                xml.contains("<Rule>"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML includes Years and omits Days when days is null")
    @Test
    public void should_build_xml_with_years_only_when_days_is_null() {
        ObjectLockConfigurationXMLBuilder builder = new ObjectLockConfigurationXMLBuilder();
        String xml = builder.buildXML(createConfigWithYearsOnly());

        assertTrue("XML should contain Mode element",
                xml.contains("<Mode>"));
        assertTrue("XML should contain Years element",
                xml.contains("<Years>"));
        assertFalse("XML should not contain Days element",
                xml.contains("<Days>"));
        assertTrue("XML should contain 2 years",
                xml.contains("<Years>2</Years>"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test that buildXML includes Days and omits Years when years is null")
    @Test
    public void should_build_xml_with_days_only_when_years_is_null() {
        ObjectLockConfigurationXMLBuilder builder = new ObjectLockConfigurationXMLBuilder();
        String xml = builder.buildXML(createConfigWithDaysOnly());

        assertTrue("XML should contain Mode element",
                xml.contains("<Mode>"));
        assertTrue("XML should contain Days element",
                xml.contains("<Days>"));
        assertFalse("XML should not contain Years element",
                xml.contains("<Years>"));
        assertTrue("XML should contain 90 days",
                xml.contains("<Days>90</Days>"));
    }

    private static ObjectLockConfiguration createFullConfig() {
        DefaultRetention retention = new DefaultRetention("COMPLIANCE", 30, 1);
        ObjectLockRule rule = new ObjectLockRule(retention);
        return new ObjectLockConfiguration("Enabled", rule);
    }

    private static ObjectLockConfiguration createConfigWithoutObjectLockEnabled() {
        DefaultRetention retention = new DefaultRetention("GOVERNANCE", 15, null);
        ObjectLockRule rule = new ObjectLockRule(retention);
        return new ObjectLockConfiguration(null, rule);
    }

    private static ObjectLockConfiguration createConfigWithoutRule() {
        return new ObjectLockConfiguration("Enabled", null);
    }

    private static ObjectLockConfiguration createConfigWithoutDefaultRetention() {
        ObjectLockRule rule = new ObjectLockRule(null);
        return new ObjectLockConfiguration("Enabled", rule);
    }

    private static ObjectLockConfiguration createConfigWithYearsOnly() {
        DefaultRetention retention = new DefaultRetention("COMPLIANCE", null, 2);
        ObjectLockRule rule = new ObjectLockRule(retention);
        return new ObjectLockConfiguration("Enabled", rule);
    }

    private static ObjectLockConfiguration createConfigWithDaysOnly() {
        DefaultRetention retention = new DefaultRetention("GOVERNANCE", 90, null);
        ObjectLockRule rule = new ObjectLockRule(retention);
        return new ObjectLockConfiguration("Enabled", rule);
    }
}
