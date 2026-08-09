/*
 * Copyright (c) - Paul Pinault (aka disk91) - 2026.
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 *    and associated documentation files (the "Software"), to deal in the Software without restriction,
 *    including without limitation the rights to use, copy, modify, merge, publish, distribute,
 *    sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in all copies or
 *    substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *    FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 *    OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 *    IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.disk91.alerts.services;

import com.disk91.alerts.interfaces.ContactTarget;
import com.disk91.common.config.CommonConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrossAlertWrapperService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    Object privContactWrapperService = null;

    @Autowired
    protected CommonConfig commonConfig;

    @Autowired(required = false)
    private AutowireCapableBeanFactory beanFactory;

    /**
     * To wrap the Non Community Edition Feature, transparently, with no compilation
     * impact, this class is a wrapper to the NCE implementation and ensure a fallback
     * response for the CE version.
     */
    @PostConstruct
    private void initContactCrossWrapperService() {
        log.info("[contacts] Init CrossWrapper Service");
        if ( commonConfig.isCommonNceEnable() ) {
            try {
                Class<?> clazz = Class.forName("com.disk91.contacts.services.PrivContactWrapperService");
                privContactWrapperService = beanFactory.createBean(clazz);
                log.info("\u001B[34m[Contacts] Running Non Community Edition features\u001B[0m");
                return;
            } catch (ClassNotFoundException e) {
                privContactWrapperService = null;
            } catch (Exception e) {
                log.error("[Contacts] Failed to load the PrivContactWrapperService class : {}", e.getMessage());
            }
        }
        log.info("[Contacts] Running Community Edition");
    }

    public boolean isNceEnabled() {
        return ( privContactWrapperService != null && commonConfig.isCommonNceEnable() );
    }

    /**
     * This function retrieves the information for a contact and fills the target structure using the contact
     * ID passed in the group list.
     *
     * @param contact - contact id starting with "ctc_"
     * @return
     */
    public ContactTarget getTargetFromContact(String contact) {
        if ( isNceEnabled() ) {
            try {
                return (ContactTarget) privContactWrapperService.getClass()
                        .getMethod("getTargetFromContact", String.class)
                        .invoke(privContactWrapperService, contact);
            } catch (Exception e) {
                log.error("[alerts] Failed to call getTargetFromContact : {}", e.getMessage());
            }
        }
        // default Community Edition behavior : no captcha
        if ( commonConfig.isCommonNceEnable() ) {
            log.warn("[alerts] Contacts feature is not supported in Community Edition, please activate non community edition");
            log.warn("[alerts] With CE version, please deactivate alert with contacts (not supported)");
        }
        return null;
    }


    /**
     * This function returns information about a set of contacts identified as belonging to the same group.
     * It is used, among other things, for alerts, allowing an alert to be broadcast to a contact group based on
     * the group associated with the user.
     * @param groupId - group Id
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ContactTarget> getTargetsFromGroup(String groupId) {
        if ( isNceEnabled() ) {
            try {
                return (List<ContactTarget>) privContactWrapperService.getClass()
                        .getMethod("getTargetsFromGroup", String.class)
                        .invoke(privContactWrapperService, groupId);
            } catch (Exception e) {
                log.error("[alerts] Failed to call getTargetsFromGroup : {}", e.getMessage());
            }
        }
        // default Community Edition behavior : no captcha
        if ( commonConfig.isCommonNceEnable() ) {
            log.warn("[alerts] Contacts feature is not supported in Community Edition, please activate non community edition");
            log.warn("[alerts] With CE version, please deactivate alert with contacts (not supported)");
        }
        return null;
    }

}
