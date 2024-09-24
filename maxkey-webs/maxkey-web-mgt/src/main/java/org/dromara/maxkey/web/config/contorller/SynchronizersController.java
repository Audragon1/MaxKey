/*
 * Copyright [2021] [MaxKey of copyright http://www.maxkey.top]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.dromara.maxkey.web.config.contorller;

import org.dromara.maxkey.authn.annotation.CurrentUser;
import org.dromara.maxkey.crypto.password.PasswordReciprocal;
import org.dromara.maxkey.entity.*;
import org.dromara.maxkey.persistence.service.SynchronizersService;
import org.dromara.maxkey.synchronizer.ISynchronizerService;
import org.dromara.maxkey.synchronizer.service.SyncJobConfigFieldService;
import org.dromara.maxkey.util.StringUtils;
import org.dromara.maxkey.web.WebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = {"/config/synchronizers"})
public class SynchronizersController {
    static final Logger logger = LoggerFactory.getLogger(SynchronizersController.class);

    @Autowired
    SynchronizersService synchronizersService;

    @Autowired
    SyncJobConfigFieldService syncJobConfigFieldService;

    @RequestMapping(value = {"/fetch"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<?> fetch(Synchronizers synchronizers, @CurrentUser UserInfo currentUser) {
        logger.debug("fetch {}", synchronizers);
        synchronizers.setInstId(currentUser.getInstId());
        return new Message<>(
                synchronizersService.fetchPageResults(synchronizers)).buildResponse();
    }

    @RequestMapping(value = {"/get/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> get(@PathVariable("id") String id) {
        Synchronizers synchronizers = synchronizersService.get(id);
        synchronizers.setCredentials(PasswordReciprocal.getInstance().decoder(synchronizers.getCredentials()));
        return new Message<>(synchronizers).buildResponse();
    }

    @ResponseBody
    @RequestMapping(value = {"/add"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> add(@RequestBody Synchronizers synchronizers, @CurrentUser UserInfo currentUser) {
        logger.debug("-add  : {}", synchronizers);
        synchronizers.setInstId(currentUser.getInstId());
        if (StringUtils.isNotBlank(synchronizers.getCredentials())) {
            synchronizers.setCredentials(PasswordReciprocal.getInstance().encode(synchronizers.getCredentials()));
        }
        if (synchronizersService.insert(synchronizers)) {
            return new Message<Synchronizers>(Message.SUCCESS).buildResponse();
        } else {
            return new Message<Synchronizers>(Message.FAIL).buildResponse();
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/update"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> update(@RequestBody Synchronizers synchronizers, @CurrentUser UserInfo currentUser) {
        logger.debug("-update  : {}", synchronizers);
        synchronizers.setInstId(currentUser.getInstId());
        synchronizers.setCredentials(PasswordReciprocal.getInstance().encode(synchronizers.getCredentials()));
        if (synchronizersService.update(synchronizers)) {
            return new Message<Synchronizers>(Message.SUCCESS).buildResponse();
        } else {
            return new Message<Synchronizers>(Message.FAIL).buildResponse();
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/delete"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> delete(@RequestParam("ids") String ids) {
        logger.debug("-delete  ids : {} ", ids);
        if (synchronizersService.deleteBatch(ids)) {
            return new Message<Connectors>(Message.SUCCESS).buildResponse();
        } else {
            return new Message<Connectors>(Message.FAIL).buildResponse();
        }
    }

    @ResponseBody
    @RequestMapping(value = {"/synchr"})
    public ResponseEntity<?> synchr(@RequestParam("id") String id) {
        logger.debug("-sync ids : {}", id);

        List<String> ids = StringUtils.string2List(id, ",");
        try {
            for (String sysId : ids) {
                Synchronizers synchronizer = synchronizersService.get(sysId);
                synchronizer.setCredentials(PasswordReciprocal.getInstance().decoder(synchronizer.getCredentials()));
                logger.debug("synchronizer {}", synchronizer);
                ISynchronizerService synchronizerService = WebContext.getBean(synchronizer.getService(), ISynchronizerService.class);
                if (synchronizerService != null) {
                    synchronizerService.setSynchronizer(synchronizer);
                    synchronizerService.sync();
                } else {
                    logger.info("synchronizer {} not exist .", synchronizer.getService());
                }
            }
        } catch (Exception e) {
            logger.error("synchronizer Exception ", e);
            return new Message<Synchronizers>(Message.FAIL).buildResponse();

        }
        return new Message<Synchronizers>(Message.SUCCESS).buildResponse();
    }


    @RequestMapping(value = {"/mapping-list/{jobId}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<?> mapping(@PathVariable Long jobId) {
        logger.debug("mapping {}", jobId);
        List<SyncJobConfigField> syncJobConfigFields = syncJobConfigFieldService.findByJobId(jobId);
        return new Message<>(syncJobConfigFields).buildResponse();
    }

    @RequestMapping(value = {"/mapping-get/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<?> mappingGet(@PathVariable Long id) {
        logger.debug("mapping get {}", id);
        SyncJobConfigField syncJobConfigFields = syncJobConfigFieldService.get(String.valueOf(id));
        return new Message<>(syncJobConfigFields).buildResponse();
    }

    @RequestMapping(value = {"/mapping-delete/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<?> mappingDelete(@PathVariable Long id) {
        logger.debug("mappingDelete {}", id);
        syncJobConfigFieldService.deleteFieldMapById(id);
        return new Message<SyncJobConfigField>(Message.SUCCESS).buildResponse();
    }

    @ResponseBody
    @PostMapping(value = {"/mapping-add"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> mappingadd(@RequestBody SyncJobConfigField syncJobConfigField, @CurrentUser UserInfo currentUser) {
        logger.debug("-mapping add  : {}", syncJobConfigField);
        syncJobConfigField.setCreateTime(new Date());
        if (syncJobConfigFieldService.insert(syncJobConfigField)) {
            return new Message<Synchronizers>(Message.SUCCESS).buildResponse();
        } else {
            return new Message<Synchronizers>(Message.FAIL).buildResponse();
        }
    }

    @ResponseBody
    @PutMapping(value = {"/mapping-update"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> mappingupdate(@RequestBody SyncJobConfigField syncJobConfigField, @CurrentUser UserInfo currentUser) {
        logger.debug("-mapping update  : {}", syncJobConfigField);
        syncJobConfigField.setUpdateTime(new Date());
        if (syncJobConfigFieldService.update(syncJobConfigField)) {
            return new Message<Synchronizers>(Message.SUCCESS).buildResponse();
        } else {
            return new Message<Synchronizers>(Message.FAIL).buildResponse();
        }
    }

}
