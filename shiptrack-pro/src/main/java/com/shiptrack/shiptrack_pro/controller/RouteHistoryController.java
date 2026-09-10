package com.shiptrack.shiptrack_pro.controller;

import com.shiptrack.shiptrack_pro.dto.route.RouteHistoryResponse;
import com.shiptrack.shiptrack_pro.service.RouteHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteHistoryController {

    private final RouteHistoryService routeHistoryService;

    // GET /api/routes/{shipmentId}/history
    // Open to any authenticated role; ownership for CUSTOMER/BUSINESS_CLIENT
    // is enforced inside the service (staff roles can view any shipment).
    @GetMapping("/{shipmentId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RouteHistoryResponse> getRouteHistory(
            @PathVariable Long shipmentId,
            Authentication authentication) {

        return ResponseEntity.ok(
                routeHistoryService.getRouteHistory(shipmentId, authentication.getName())
        );
    }
}
