package com.disk91.users.mdb.entities.sub;

import com.disk91.common.tools.CloneableObject;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserAlertPreference implements CloneableObject<UserAlertPreference> {

        // accept email alert
        @Schema(
                description = "Accept email alert",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private boolean emailAlert;

        // accept sms alert
        @Schema(
                description = "Accept SMS alert",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private boolean smsAlert;

        // accept push alert
        @Schema(
                description = "Accept push alert",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private boolean pushAlert;

        // === GETTER / SETTER ===

        public boolean isEmailAlert() {
            return emailAlert;
        }

        public void setEmailAlert(boolean emailAlert) {
            this.emailAlert = emailAlert;
        }

        public boolean isSmsAlert() {
            return smsAlert;
        }

        public void setSmsAlert(boolean smsAlert) {
            this.smsAlert = smsAlert;
        }

        public boolean isPushAlert() {
            return pushAlert;
        }

        public void setPushAlert(boolean pushAlert) {
            this.pushAlert = pushAlert;
        }

        // === INT ===

        public static UserAlertPreference of() {
            UserAlertPreference pref = new UserAlertPreference();
            pref.setEmailAlert(true);
            pref.setSmsAlert(false);
            pref.setPushAlert(false);
            return pref;
        }

        // === CLONE ===

        @Override
        public UserAlertPreference clone() {
            UserAlertPreference u = new UserAlertPreference();
            u.setEmailAlert(this.emailAlert);
            u.setSmsAlert(this.smsAlert);
            u.setPushAlert(this.pushAlert);
            return u;
        }
}
