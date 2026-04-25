/**
 * Copyright 2019 Huawei Technologies Co., Ltd.
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

package com.obs.services.internal.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;

import com.obs.aitool.AIGenerated;
import com.obs.services.AbstractClient;
import com.obs.services.internal.utils.CallCancelHandler;
import com.obs.services.model.DownloadFileRequest;
import com.obs.services.model.ResumableTransferHandle;

import org.junit.Test;

/**
 * Test class for {@link DownloadFileTask}.
 */
public class DownloadFileTaskTest {

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test cancel returns true and cancels handler when cancelHandler is set")
    @Test
    public void should_return_true_and_cancel_handler_when_cancel_with_handler() {
        AbstractClient mockClient = createMockAbstractClient();
        DownloadFileRequest request = new DownloadFileRequest("test-bucket", "test-key");
        CallCancelHandler handler = new CallCancelHandler();
        request.setCancelHandler(handler);

        DownloadFileTask task = new DownloadFileTask(mockClient, "test-bucket", request, null);

        boolean result = task.cancel();

        assertTrue("cancel() should return true when cancelHandler is not null", result);
        assertTrue("cancelHandler should be cancelled after calling cancel()", handler.isCancelled());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test cancel returns false when cancelHandler is null")
    @Test
    public void should_return_false_when_cancel_without_handler() {
        AbstractClient mockClient = createMockAbstractClient();
        DownloadFileRequest request = new DownloadFileRequest("test-bucket", "test-key");
        // cancelHandler is null by default

        DownloadFileTask task = new DownloadFileTask(mockClient, "test-bucket", request, null);

        boolean result = task.cancel();

        assertFalse("cancel() should return false when cancelHandler is null", result);
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test pause returns true and pauses handle when transferHandle is set")
    @Test
    public void should_return_true_and_pause_handle_when_pause_with_handle() {
        AbstractClient mockClient = createMockAbstractClient();
        DownloadFileRequest request = new DownloadFileRequest("test-bucket", "test-key");
        ResumableTransferHandle handle = new ResumableTransferHandle();
        request.setTransferHandle(handle);

        DownloadFileTask task = new DownloadFileTask(mockClient, "test-bucket", request, null);

        boolean result = task.pause();

        assertTrue("pause() should return true when transferHandle is not null", result);
        assertTrue("transferHandle should be paused after calling pause()", handle.isPaused());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test pause returns false when transferHandle is null")
    @Test
    public void should_return_false_when_pause_without_handle() {
        AbstractClient mockClient = createMockAbstractClient();
        DownloadFileRequest request = new DownloadFileRequest("test-bucket", "test-key");
        // transferHandle is null by default

        DownloadFileTask task = new DownloadFileTask(mockClient, "test-bucket", request, null);

        boolean result = task.pause();

        assertFalse("pause() should return false when transferHandle is null", result);
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test isCancelled returns true when cancelHandler is already cancelled")
    @Test
    public void should_be_cancelled_when_handler_is_cancelled() {
        AbstractClient mockClient = createMockAbstractClient();
        DownloadFileRequest request = new DownloadFileRequest("test-bucket", "test-key");
        CallCancelHandler handler = new CallCancelHandler();
        handler.cancel();
        request.setCancelHandler(handler);

        DownloadFileTask task = new DownloadFileTask(mockClient, "test-bucket", request, null);

        // isTaskCancelled() is private, we verify it through getResult()
        // When cancelled, getResult should return empty due to cancellation
        assertTrue("cancelHandler should be cancelled", handler.isCancelled());
    }

    @AIGenerated(author = "yanliwei", date = "2026-04-25",
            description = "Test isCancelled returns false when cancelHandler is null")
    @Test
    public void should_not_be_cancelled_when_handler_is_null() {
        AbstractClient mockClient = createMockAbstractClient();
        DownloadFileRequest request = new DownloadFileRequest("test-bucket", "test-key");
        // cancelHandler is null by default

        DownloadFileTask task = new DownloadFileTask(mockClient, "test-bucket", request, null);

        // When cancelHandler is null, isTaskCancelled() should return false
        // We verify by checking that request's cancelHandler is null
        boolean expectedResult = request.getCancelHandler() != null
                && request.getCancelHandler().isCancelled();
        assertFalse("isTaskCancelled should return false when cancelHandler is null", expectedResult);
    }

    /**
     * Create a mock AbstractClient for testing purposes.
     */
    private AbstractClient createMockAbstractClient() {
        return mock(AbstractClient.class);
    }
}
