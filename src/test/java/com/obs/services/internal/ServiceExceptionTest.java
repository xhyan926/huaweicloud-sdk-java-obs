/**
 * Copyright 2019 Huawei Technologies Co.,Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.obs.services.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.obs.aitool.AIGenerated;

import org.junit.Test;

public class ServiceExceptionTest {

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException parses JSON fields when content type is application/json")
    @Test
    public void should_parse_json_fields_when_content_type_is_application_json() {
        String jsonBody = "{\"code\":\"TestCode\",\"message\":\"TestMessage\",\"request_id\":\"req123\"}";
        ServiceException exception = new ServiceException("Error", jsonBody, "application/json");

        assertEquals("TestCode", exception.getErrorCode());
        assertEquals("TestMessage", exception.getErrorMessage());
        assertEquals("req123", exception.getErrorRequestId());
        assertEquals(jsonBody.replaceAll("\n", "").trim(), exception.getXmlMessage());
        assertNull(exception.getErrorHostId());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException parses JSON fields when content type contains charset suffix")
    @Test
    public void should_parse_json_fields_when_content_type_contains_charset() {
        String jsonBody = "{\"code\":\"Code2\",\"message\":\"Message2\"}";
        ServiceException exception = new ServiceException("Error", jsonBody, "application/json;charset=utf-8");

        assertEquals("Code2", exception.getErrorCode());
        assertEquals("Message2", exception.getErrorMessage());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException parses XML fields when content type is application/xml")
    @Test
    public void should_parse_xml_fields_when_content_type_is_application_xml() {
        String xmlBody = "<Error><Code>AccessDenied</Code><Message>Access denied</Message>"
                + "<RequestId>req456</RequestId><HostId>host789</HostId></Error>";
        ServiceException exception = new ServiceException("Error", xmlBody, "application/xml");

        assertEquals("AccessDenied", exception.getErrorCode());
        assertEquals("Access denied", exception.getErrorMessage());
        assertEquals("req456", exception.getErrorRequestId());
        assertEquals("host789", exception.getErrorHostId());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException falls back to XML parsing when content type is null")
    @Test
    public void should_parse_xml_fields_when_content_type_is_null() {
        String xmlBody = "<Error><Code>InvalidRequest</Code><Message>Invalid</Message></Error>";
        ServiceException exception = new ServiceException("Error", xmlBody, (String) null, null);

        assertEquals("InvalidRequest", exception.getErrorCode());
        assertEquals("Invalid", exception.getErrorMessage());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException skips parsing when response body is null")
    @Test
    public void should_skip_parsing_when_response_body_is_null() {
        ServiceException exception = new ServiceException("Error", null, "application/json");

        assertNull(exception.getErrorCode());
        assertNull(exception.getErrorMessage());
        assertNull(exception.getXmlMessage());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException skips parsing when response body is empty")
    @Test
    public void should_skip_parsing_when_response_body_is_empty() {
        ServiceException exception = new ServiceException("Error", "", "application/json");

        assertNull(exception.getErrorCode());
        assertNull(exception.getErrorMessage());
        assertNull(exception.getXmlMessage());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException appends Details element to error message when present in XML")
    @Test
    public void should_append_details_to_error_message_when_xml_contains_details() {
        String xmlBody = "<Error><Code>TestCode</Code><Message>Base message</Message>"
                + "<Details>Additional details</Details></Error>";
        ServiceException exception = new ServiceException("Error", xmlBody, "application/xml");

        assertEquals("TestCode", exception.getErrorCode());
        assertEquals("Base message Additional details", exception.getErrorMessage());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException extracts EncodedAuthorizationMessage from XML error response")
    @Test
    public void should_extract_encoded_authorization_message_when_present() {
        String xmlBody = "<Error><Code>EncodedAuth</Code><Message>Auth required</Message>"
                + "<EncodedAuthorizationMessage>encoded:auth:message</EncodedAuthorizationMessage></Error>";
        ServiceException exception = new ServiceException("Error", xmlBody, "application/xml");

        assertEquals("EncodedAuth", exception.getErrorCode());
        assertEquals("encoded:auth:message", exception.getEncodedAuthorizationMessage());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException returns null for missing JSON fields instead of throwing")
    @Test
    public void should_return_null_for_missing_fields_when_json_lacks_them() {
        String jsonBody = "{\"code\":\"OnlyCode\"}";
        ServiceException exception = new ServiceException("Error", jsonBody, "application/json");

        assertEquals("OnlyCode", exception.getErrorCode());
        assertNull(exception.getErrorMessage());
        assertNull(exception.getErrorRequestId());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException toString includes all context fields when all are set")
    @Test
    public void should_include_all_context_fields_in_toString_when_all_set() {
        ServiceException exception = new ServiceException("Base error");
        exception.setRequestVerb("PUT");
        exception.setRequestPath("/bucket/object");
        exception.setRequestHost("obs.example.com");
        exception.setResponseDate("Fri, 25 Apr 2026 10:00:00 GMT");
        exception.setResponseCode(403);
        exception.setResponseStatus("Forbidden");

        String result = exception.toString();
        assertTrue(result.contains("PUT"));
        assertTrue(result.contains("/bucket/object"));
        assertTrue(result.contains("obs.example.com"));
        assertTrue(result.contains("Fri, 25 Apr 2026 10:00:00 GMT"));
        assertTrue(result.contains("ResponseCode: 403"));
        assertTrue(result.contains("ResponseStatus: Forbidden"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException toString appends requestId parsed from JSON error")
    @Test
    public void should_append_request_id_in_toString_when_parsed_from_json() {
        ServiceException exception = new ServiceException("JSON error");
        exception.setRequestVerb("GET");
        exception.setResponseCode(404);
        exception.setErrorRequestId("json-req-id");
        exception.setErrorHostId("json-host-id");

        String result = exception.toString();
        assertTrue(result.contains("RequestId: json-req-id"));
        assertTrue(result.contains("HostId: json-host-id"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException toString skips request context section when verb is null")
    @Test
    public void should_skip_request_context_in_toString_when_verb_is_null() {
        ServiceException exception = new ServiceException("No verb error");
        exception.setRequestPath("/some/path");
        exception.setRequestHost("example.com");
        exception.setResponseCode(500);

        String result = exception.toString();
        assertTrue(result.contains("ResponseCode: 500"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException toString skips ResponseCode when code is -1 (default)")
    @Test
    public void should_skip_response_code_in_toString_when_code_is_minus_one() {
        ServiceException exception = new ServiceException("No code error");
        exception.setRequestVerb("POST");

        String result = exception.toString();
        assertTrue(result.contains("POST"));
        assertTrue("Should not contain ResponseCode when responseCode is -1",
                !result.contains("ResponseCode: -1"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify ServiceException toString skips RequestId when error request id is null")
    @Test
    public void should_skip_request_id_in_toString_when_id_is_null() {
        ServiceException exception = new ServiceException("No request id error");
        exception.setRequestVerb("DELETE");
        exception.setResponseCode(204);
        exception.setErrorRequestId(null);

        String result = exception.toString();
        assertTrue("Should not contain RequestId when null",
                !result.contains("RequestId:"));
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Verify three-arg constructor delegates to four-arg constructor and parses XML body")
    @Test
    public void should_delegate_to_four_arg_constructor_when_using_three_arg_constructor() {
        String xmlBody = "<Error><Code>TestCode</Code><Message>Test</Message></Error>";
        Throwable cause = new RuntimeException("Root cause");
        ServiceException exception = new ServiceException("Error", xmlBody, cause);

        assertEquals("TestCode", exception.getErrorCode());
        assertEquals("Test", exception.getErrorMessage());
        assertEquals(cause, exception.getCause());
    }
}
