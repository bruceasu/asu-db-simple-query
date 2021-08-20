package me.asu.db.editor;

import java.beans.PropertyEditorSupport;

public class ShortEditor extends PropertyEditorSupport {
        @Override
        public void setValue(Object value) {
            if (value instanceof Short) {
                super.setValue(value);
            } else if (value instanceof Number) {
                super.setValue(Short.valueOf(((Number) value).shortValue()));
            } else {
                throw new java.lang.IllegalArgumentException(""+value);
            }
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            final Short a = Short.valueOf(text);
            setValue(a);
        }
    }