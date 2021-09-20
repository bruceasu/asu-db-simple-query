package me.asu.db.editor;

import java.beans.PropertyEditorSupport;

public class DoubleEditor extends PropertyEditorSupport {
        @Override
        public void setValue(Object value) {
            if (value instanceof Double) {
                super.setValue(value);
            } else if (value instanceof Number) {
                super.setValue(Double.valueOf(((Number) value).doubleValue()));
            } else {
                throw new java.lang.IllegalArgumentException(""+value);
            }
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            final Double a = Double.valueOf(text);
            setValue(a);
        }
    }
