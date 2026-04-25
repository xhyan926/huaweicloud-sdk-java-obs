/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.obs.services.model.objectlock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.obs.aitool.AIGenerated;

import org.junit.Test;

public class ObjectLockModelTest {

    // ------------------------------ DefaultRetention 测试 ------------------------------

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test DefaultRetention constructor sets all fields correctly")
    @Test
    public void should_set_all_fields_when_default_retention_constructed_with_params() {
        DefaultRetention retention = new DefaultRetention("GOVERNANCE", 30, null);

        assertEquals("GOVERNANCE", retention.getMode());
        assertEquals(Integer.valueOf(30), retention.getDays());
        assertNull(retention.getYears());

        String toString = retention.toString();
        assertNotNull(toString);
        assertEquals("DefaultRetention [mode=GOVERNANCE, days=30, years=null]", toString);
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test DefaultRetention default constructor and setters set fields correctly")
    @Test
    public void should_set_fields_via_setters_when_default_retention_uses_default_ctor() {
        DefaultRetention retention = new DefaultRetention();
        assertNull(retention.getMode());
        assertNull(retention.getDays());
        assertNull(retention.getYears());

        retention.setMode("COMPLIANCE");
        retention.setDays(365);
        retention.setYears(null);

        assertEquals("COMPLIANCE", retention.getMode());
        assertEquals(Integer.valueOf(365), retention.getDays());
        assertNull(retention.getYears());
    }

    // ------------------------------ ObjectLockRule 测试 ------------------------------

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test ObjectLockRule setter sets defaultRetention correctly")
    @Test
    public void should_set_default_retention_when_object_lock_rule_uses_setter() {
        DefaultRetention defaultRetention = new DefaultRetention("GOVERNANCE", 30, null);
        ObjectLockRule rule = new ObjectLockRule();
        rule.setDefaultRetention(defaultRetention);

        assertEquals(defaultRetention, rule.getDefaultRetention());

        String toString = rule.toString();
        assertNotNull(toString);
        assertEquals("ObjectLockRule [defaultRetention=" + defaultRetention + "]", toString);
    }

    // ------------------------------ ObjectLockConfiguration 测试 ------------------------------

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test ObjectLockConfiguration constructor sets all fields correctly")
    @Test
    public void should_set_all_fields_when_object_lock_config_constructed_with_params() {
        DefaultRetention defaultRetention = new DefaultRetention("COMPLIANCE", null, 1);
        ObjectLockRule rule = new ObjectLockRule(defaultRetention);
        ObjectLockConfiguration config = new ObjectLockConfiguration("Enabled", rule);

        assertEquals("Enabled", config.getObjectLockEnabled());
        assertEquals(rule, config.getRule());

        String toString = config.toString();
        assertNotNull(toString);
        assertEquals("ObjectLockConfiguration [objectLockEnabled=Enabled, rule=" + rule + "]", toString);
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test ObjectLockConfiguration default constructor and setters set fields correctly")
    @Test
    public void should_set_fields_via_setters_when_object_lock_config_uses_default_ctor() {
        ObjectLockConfiguration config = new ObjectLockConfiguration();
        assertNull(config.getObjectLockEnabled());
        assertNull(config.getRule());

        DefaultRetention defaultRetention = new DefaultRetention("GOVERNANCE", 30, null);
        ObjectLockRule rule = new ObjectLockRule(defaultRetention);

        config.setObjectLockEnabled("Enabled");
        config.setRule(rule);

        assertEquals("Enabled", config.getObjectLockEnabled());
        assertEquals(rule, config.getRule());
    }

    // ------------------------------ ObjectRetention 测试 ------------------------------

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test ObjectRetention constructor sets all fields correctly")
    @Test
    public void should_set_all_fields_when_object_retention_constructed_with_params() {
        Long retainUntilDate = System.currentTimeMillis() + 86400000L;
        ObjectRetention retention = new ObjectRetention("COMPLIANCE", retainUntilDate);

        assertEquals("COMPLIANCE", retention.getMode());
        assertEquals(retainUntilDate, retention.getRetainUntilDate());

        String toString = retention.toString();
        assertNotNull(toString);
        assertEquals("ObjectRetention [mode=COMPLIANCE, retainUntilDate=" + retainUntilDate + "]", toString);
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test ObjectRetention default constructor and setters set fields correctly")
    @Test
    public void should_set_fields_via_setters_when_object_retention_uses_default_ctor() {
        ObjectRetention retention = new ObjectRetention();
        assertNull(retention.getMode());
        assertNull(retention.getRetainUntilDate());

        Long retainUntilDate = System.currentTimeMillis() + 86400000L;
        retention.setMode("GOVERNANCE");
        retention.setRetainUntilDate(retainUntilDate);

        assertEquals("GOVERNANCE", retention.getMode());
        assertEquals(retainUntilDate, retention.getRetainUntilDate());
    }

    // ------------------------------ SetObjectRetentionRequest 测试 ------------------------------

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test SetObjectRetentionRequest three-param constructor sets bucket, key and retention")
    @Test
    public void should_set_bucket_key_and_retention_when_using_three_param_constructor() {
        ObjectRetention retention = new ObjectRetention("COMPLIANCE", System.currentTimeMillis() + 86400000L);
        SetObjectRetentionRequest request = new SetObjectRetentionRequest("test-bucket", "test-object", retention);

        assertEquals("test-bucket", request.getBucketName());
        assertEquals("test-object", request.getObjectKey());
        assertEquals(retention, request.getRetention());
        assertNull(request.getVersionId());

        String toString = request.toString();
        assertNotNull(toString);
        assertEquals("SetObjectRetentionRequest [bucketName=test-bucket, objectKey=test-object, versionId=null, retention=" + retention + "]", toString);
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test SetObjectRetentionRequest four-param constructor sets all fields including versionId")
    @Test
    public void should_set_all_fields_including_version_id_when_using_four_param_constructor() {
        ObjectRetention retention = new ObjectRetention("GOVERNANCE", System.currentTimeMillis() + 86400000L);
        SetObjectRetentionRequest request = new SetObjectRetentionRequest("test-bucket", "test-object", retention, "version-123");

        assertEquals("test-bucket", request.getBucketName());
        assertEquals("test-object", request.getObjectKey());
        assertEquals(retention, request.getRetention());
        assertEquals("version-123", request.getVersionId());

        String toString = request.toString();
        assertNotNull(toString);
        assertEquals("SetObjectRetentionRequest [bucketName=test-bucket, objectKey=test-object, versionId=version-123, retention=" + retention + "]", toString);
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test SetObjectRetentionRequest default constructor and setters set all fields correctly")
    @Test
    public void should_set_all_fields_via_setters_when_set_retention_request_uses_default_ctor() {
        SetObjectRetentionRequest request = new SetObjectRetentionRequest();
        assertNull(request.getBucketName());
        assertNull(request.getObjectKey());
        assertNull(request.getRetention());
        assertNull(request.getVersionId());

        ObjectRetention retention = new ObjectRetention("COMPLIANCE", System.currentTimeMillis() + 86400000L);
        request.setBucketName("test-bucket");
        request.setObjectKey("test-object");
        request.setRetention(retention);
        request.setVersionId("version-456");

        assertEquals("test-bucket", request.getBucketName());
        assertEquals("test-object", request.getObjectKey());
        assertEquals(retention, request.getRetention());
        assertEquals("version-456", request.getVersionId());
    }
}
