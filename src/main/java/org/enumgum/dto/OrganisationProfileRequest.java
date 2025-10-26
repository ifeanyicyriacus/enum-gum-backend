// package org.enumgum.dto;
//
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size;
//
// public record OrganisationProfileRequest(
//    @NotBlank(message = "Organisation name is required")
//        @Size(max = 100, message = "Organisation name must not exceed 100 characters")
//        String name,
//    @Size(max = 500, message = "Description must not exceed 500 characters") String description,
//    String website,
//    String industry,
//    String size) {}
