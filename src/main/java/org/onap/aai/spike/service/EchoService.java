/**
 * ﻿============LICENSE_START=======================================================
 * Spike
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property.
 * Copyright © 2017 Amdocs
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 */
package org.onap.aai.spike.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;
import org.onap.aai.cl.api.LogFields;
import org.onap.aai.cl.api.LogLine;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.cl.eelf.LoggerFactory;
import org.onap.aai.cl.mdc.MdcContext;
import org.onap.aai.spike.logging.SpikeMsgs;
import org.onap.aai.spike.util.SpikeConstants;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/services/spike/v1/echo-service")
public class EchoService {

    private static Logger logger = LoggerFactory.getInstance().getLogger(EchoService.class.getName());
    private static Logger auditLogger = LoggerFactory.getInstance().getAuditLogger(EchoService.class.getName());

    @GetMapping("/echo")
    public ResponseEntity<String> ping(@RequestHeader HttpHeaders headers, HttpServletRequest req) {

        String fromIp = req.getRemoteAddr();
        String fromAppId = "";
        String transId = null;

        if (headers.getFirst("X-FromAppId") != null) {
            fromAppId = headers.getFirst("X-FromAppId");
        }

        if ((headers.getFirst("X-TransactionId") == null) || headers.getFirst("X-TransactionId").isEmpty()) {
            transId = java.util.UUID.randomUUID().toString();
        } else {
            transId = headers.getFirst("X-TransactionId");
        }

        MdcContext.initialize(transId, SpikeConstants.SPIKE_SERVICE_NAME, "", fromAppId, fromIp);

        // Generate error log
        logger.info(SpikeMsgs.PROCESS_REST_REQUEST, req.getMethod(), req.getRequestURL().toString(),
                req.getRemoteHost(), Status.OK.toString());

        // Generate audit log.
        auditLogger.info(SpikeMsgs.PROCESS_REST_REQUEST,
                new LogFields().setField(LogLine.DefinedFields.RESPONSE_CODE, Status.OK.toString())
                        .setField(LogLine.DefinedFields.RESPONSE_DESCRIPTION, Status.OK.toString()),
                req.getMethod(), req.getRequestURL().toString(), req.getRemoteHost(), Status.OK.toString());
        MDC.clear();

        return new ResponseEntity<>("Alive", HttpStatus.OK);
    }
}
