package com.moneycol.indexer.infra.connectivity;

public enum IpAddressType {

    EXTERNAL_IP("ExternalIP"),
    INTERNAL_IP("InternalIP");

    private final static String EXTERNAL_IP_K8S_API_FIELD = "ExternalIP";
    private final static String INTERNAL_IP_K8S_API_FIELD = "InternalIP";
    private final String representation;

    IpAddressType(String representation) {
        this.representation = representation;
    }

    @Override
    public String toString() {
        return representation;
    }
}
