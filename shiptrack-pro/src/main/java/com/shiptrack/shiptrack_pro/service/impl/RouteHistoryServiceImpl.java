package com.shiptrack.shiptrack_pro.service.impl;

import com.shiptrack.shiptrack_pro.dto.route.RouteHistoryResponse;
import com.shiptrack.shiptrack_pro.dto.route.RouteHistoryResponse.RouteHistoryItem;
import com.shiptrack.shiptrack_pro.entity.Route;
import com.shiptrack.shiptrack_pro.entity.Shipment;
import com.shiptrack.shiptrack_pro.entity.User;
import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import com.shiptrack.shiptrack_pro.repository.ShipmentRepository;
import com.shiptrack.shiptrack_pro.repository.UserRepository;
import com.shiptrack.shiptrack_pro.service.RouteHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteHistoryServiceImpl implements RouteHistoryService {

    private final ShipmentRepository shipmentRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

    private static final List<String> STAFF_ROLES =
            List.of("LOGISTICS_OPERATOR", "SUPPORT_AGENT", "ADMINISTRATOR");

    @Override
    public RouteHistoryResponse getRouteHistory(Long shipmentId, String requesterEmail) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Shipment not found: " + shipmentId));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found: " + requesterEmail));

        boolean isStaff = STAFF_ROLES.contains(requester.getRole());
        boolean isOwner = shipment.getCustomer() != null
                && shipment.getCustomer().getId().equals(requester.getId());

        if (!isStaff && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You do not have access to this shipment's route.");
        }

        Route route = routeRepository.findByShipmentId(shipmentId).orElse(null);

        // NOTE: the Route table currently holds one row per shipment
        // (a route is created once and never reassigned), so there is no
        // stored history of prior routes yet. previousRoutes is therefore
        // always empty today - this endpoint/DTO is shaped so that once
        // route reassignment is supported, past Route rows can be
        // surfaced here without changing the frontend contract.
        RouteHistoryItem current = route == null ? null : toItem(route, true);

        return RouteHistoryResponse.builder()
                .trackingNumber(shipment.getTrackingNumber())
                .currentRoute(current)
                .previousRoutes(List.of())
                .build();
    }

    private RouteHistoryItem toItem(Route route, boolean isCurrent) {
        return RouteHistoryItem.builder()
                .routeId(route.getId())
                .origin(route.getOrigin())
                .destination(route.getDestination())
                .distanceKm(route.getDistanceKm())
                .estimatedTimeMinutes(route.getEstimatedTimeMinutes())
                .actualTimeMinutes(route.getActualTimeMinutes())
                .createdAt(route.getCreatedAt())
                .current(isCurrent)
                .build();
    }
}
