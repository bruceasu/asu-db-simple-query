package me.asu.db.editor;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.math.BigInteger;

public  class BigDecimalEditor extends PropertyEditorSupport {
        @Override
        public void setValue(Object value) {
            if (value instanceof BigDecimal) {
                super.setValue(value);
            } else if (value instanceof BigInteger) {
                super.setValue(BigDecimal.valueOf(((BigInteger) value).longValue()));
            } else if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
                super.setValue(BigDecimal.valueOf(((Number) value).longValue()));
            } else if (value instanceof Double || value instanceof Float ) {
                super.setValue(BigDecimal.valueOf(((Number) value).doubleValue()));
            } else if (value != null){
                final BigDecimal a = new BigDecimal(value.toString());
                super.setValue(a);
            } else {
                super.setValue(value);
            }
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            final BigDecimal a = new BigDecimal(text);
            setValue(a);
        }
    }