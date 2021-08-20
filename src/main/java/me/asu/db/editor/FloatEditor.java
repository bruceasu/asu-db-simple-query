package me.asu.db.editor;

import java.beans.PropertyEditorSupport;

public class FloatEditor extends PropertyEditorSupport {
        @Override
        public void setValue(Object value) {
            if (value instanceof Float) {
                super.setValue(value);
            } else if (value instanceof Number) {
                super.setValue(Float.valueOf(((Number) value).floatValue()));
            } else {
                throw new java.lang.IllegalArgumentException(""+value);
            }
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            final Float a = Float.valueOf(text);
            setValue(a);
        }
    }