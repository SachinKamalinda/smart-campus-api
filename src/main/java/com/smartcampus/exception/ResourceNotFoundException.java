package com.smartcampus.exception;

/** Thrown when a requested resource cannot be found by its ID. */
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final String id;

    public ResourceNotFoundException(String resourceType, String id) {
        super(resourceType + " not found: " + id);
        this.resourceType = resourceType;
        this.id           = id;
    }

    public String getResourceType() { return resourceType; }
    public String getId()           { return id; }
}