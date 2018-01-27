package com.migs.zk.inspectorzk.domain;

/**
 * Created by: mig.c on 1/11/18.
 */
public enum ZKSchemeDefs {

    AUTH("auth"),
    DIGEST("digest"),
    IP("ip"),
    SASL("sasl"),
    WORLD("world");

    private String schemeValue;

    ZKSchemeDefs(String schemeValue) {
        this.schemeValue = schemeValue;
    }

    public String getSchemeValue() {
        return schemeValue;
    }
}
