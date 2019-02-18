/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Copyright © 2017-2018 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.spike.service;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EchoServiceTest {

	@InjectMocks
	EchoService echoService;

	@Mock
	HttpHeaders headers;

	@Mock
	HttpServletRequest req;

	@Test
	public void pingTest() {
		Mockito.when(req.getMethod()).thenReturn("GET");
		Mockito.when(req.getRequestURL()).thenReturn(new StringBuffer("url"));
		Mockito.when(req.getRemoteHost()).thenReturn("localhost");
		Assert.assertNotNull(echoService.ping(headers, req));
	}
}
