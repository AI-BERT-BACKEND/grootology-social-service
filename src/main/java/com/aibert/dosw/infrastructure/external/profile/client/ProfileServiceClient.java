package com.aibert.dosw.infrastructure.external.profile.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
    name = "profile-service",
    url = "${app.services.profile-service-url}"
)
public interface ProfileServiceClient {

    @GetMapping("/api/profile/{userId}/academic")
    Object getAcademicProfile(@PathVariable("userId") UUID userId);
}
