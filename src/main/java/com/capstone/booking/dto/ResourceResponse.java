package com.capstone.booking.dto;

import com.capstone.booking.model.Resource;

/**
 * The JSON shape for a resource in the GET /api/resources list.
 */
public record ResourceResponse(
        Long resourceId,
        String name,
        String type,
        Integer capacity
) {
    public static ResourceResponse from(Resource resource) {
        return new ResourceResponse(
                resource.getResourceId(),
                resource.getName(),
                resource.getType(),
                resource.getCapacity()
        );
    }
}
