package com.disk91.common.tools;

public class CustomField implements CloneableObject<CustomField> {

        private String name;
        private String value;

        public CustomField() {
        }

        public CustomField(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public CustomField clone() {
            return new CustomField(this.name, this.value);
        }

        // === GETTER / SETTER ===

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
}
