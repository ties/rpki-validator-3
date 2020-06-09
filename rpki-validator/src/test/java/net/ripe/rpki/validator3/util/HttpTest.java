/**
 * The BSD License
 *
 * Copyright (c) 2010-2018 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ripe.rpki.validator3.util;

import net.ripe.rpki.validator3.IntegrationTest;
import net.ripe.rpki.validator3.rrdp.Notification;
import net.ripe.rpki.validator3.rrdp.RrdpParser;
import net.ripe.rpki.validator3.util.http.HttpStreaming;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@IntegrationTest
class HttpTest {
    @Autowired
    private HttpClient client;
    private final RrdpParser rrdpParser = new RrdpParser();

    @Test()
    void fetchRipeRRDPWithHEB() throws Exception {
        Assertions.assertDoesNotThrow(() -> {
            Notification notification = HttpStreaming.readStream(() -> {
                final Request request = client.newRequest("https://rrdp.ripe.net/notification.xml");
                request.header(HttpHeader.USER_AGENT, null);
                request.header(HttpHeader.USER_AGENT, UUID.randomUUID().toString());
                return request;
            }, rrdpParser::notification);
            assertNotNull(notification);
            assertNotNull(notification.serial);
        });
    }

    @Test()
    void fetchNLNetlabRRDPWithHEB() throws Exception {
        Assertions.assertDoesNotThrow(() -> {
            Notification notification = HttpStreaming.readStream(() -> {
                final Request request = client.newRequest("https://rrdp.rpki.nlnetlabs.nl/rrdp/notification.xml");
                request.header(HttpHeader.USER_AGENT, null);
                request.header(HttpHeader.USER_AGENT, "RIPE NCC RPKI Validator/test");
                return request;
            }, rrdpParser::notification);
            assertNotNull(notification);
            assertNotNull(notification.serial);
        });
    }

}