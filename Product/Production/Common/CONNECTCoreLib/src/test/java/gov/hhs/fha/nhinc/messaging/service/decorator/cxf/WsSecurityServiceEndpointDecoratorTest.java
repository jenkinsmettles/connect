/*
 * Copyright (c) 2009-2019, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package gov.hhs.fha.nhinc.messaging.service.decorator.cxf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gov.hhs.fha.nhinc.messaging.client.CONNECTClient;
import gov.hhs.fha.nhinc.messaging.client.CONNECTTestClient;
import gov.hhs.fha.nhinc.messaging.service.ServiceEndpoint;
import gov.hhs.fha.nhinc.messaging.service.port.TestServicePortDescriptor;
import gov.hhs.fha.nhinc.messaging.service.port.TestServicePortType;
import java.util.Map;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.junit.Test;

/**
 * @author akong
 *
 */
public class WsSecurityServiceEndpointDecoratorTest {

    /**
     * This test ensures that the interceptor count is the same no matter how many times the decorator is called on the
     * constructor.
     */
    @Test
    public void ensureInterceptorCountIsConstant() {
        CONNECTClient<TestServicePortType> client = createClient();

        Client cxfClient = ClientProxy.getClient(client.getPort());
        int numOutInterceptors = cxfClient.getOutInterceptors().size();

        createClient();
        createClient();
        CONNECTClient<TestServicePortType> client2 = createClient();

        Client cxfClient2 = ClientProxy.getClient(client2.getPort());
        assertEquals(numOutInterceptors, cxfClient2.getOutInterceptors().size());
    }

    /**
     * This test ensures that the ws-security on the client is configured properly.
     */
    @Test
    public void testWsSecurityProperties() {
        CONNECTClient<TestServicePortType> client = createClient();

        verifyWsSecurityProperties(client);
    }

    /**
     * This method verifies that the passed in client is configured for Ws-Security properly.
     *
     * @param client
     */
    public void verifyWsSecurityProperties(CONNECTClient<?> client) {
        Client clientProxy = ClientProxy.getClient(client.getPort());

        WSS4JOutInterceptor wss4jInterceptor = null;
        for (Interceptor<? extends Message> interceptor : clientProxy.getOutInterceptors()) {
            if (interceptor instanceof WSS4JOutInterceptor) {
                wss4jInterceptor = (WSS4JOutInterceptor) interceptor;
                break;
            }
        }

        assertNotNull(wss4jInterceptor);
       // assertTrue(wss4jInterceptor.isAllowMTOM());

        Map<String, Object> properties = wss4jInterceptor.getProperties();
        new WsSecurityConfigFactoryTest().verifyWsSecurityProperties(properties);
    }

    private CONNECTClient<TestServicePortType> createClient() {
        CONNECTTestClient<TestServicePortType> testClient = new CONNECTTestClient<>(
            new TestServicePortDescriptor());

        ServiceEndpoint<TestServicePortType> serviceEndpoint = testClient.getServiceEndpoint();

        serviceEndpoint = new WsSecurityServiceEndpointDecorator<>(serviceEndpoint, (String) null, null);
        serviceEndpoint.configure();

        return testClient;
    }

}
