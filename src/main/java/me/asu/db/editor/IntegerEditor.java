package me.asu.db.editor;

import java.beans.PropertyEditorSupport;

public class IntegerEditor extends PropertyEditorSupport {
        @Override
        public void setValue(Object value) {
            if (value instanceof Integer) {
                super.setValue(value);
            } else if (value instanceof Number) {
                super.setValue(Integer.valueOf(((Number) value).intValue()));
            } else {
                throw new java.lang.IllegalArgumentException(""+value);
            }
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            final Integer a = Integer.valueOf(text);
            setValue(a);
        }
    }
