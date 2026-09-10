package com.shiptrack.shiptrack_pro.dto.route;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteHistoryResponse {

    private String trackingNumber;
    private RouteHistoryItem currentRoute; // null if no route has been created yet
    private List<RouteHistoryItem> previousRoutes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteHistoryItem {
        private Long routeId;
        private String origin;
        private String destination;
        private Double distanceKm;
        private Integer estimatedTimeMinutes;
        private Integer actualTimeMinutes;
        private LocalDateTime createdAt;
        private boolean current;
    }
}
