package com.es.phoneshop.exceptions;

public class PropertiesNotFoundException extends RuntimeException {
    public PropertiesNotFoundException(String path) {
        super(path + ".properties not found");
    }
}
