package com.shiptrack.shiptrack_pro.service;

import com.shiptrack.shiptrack_pro.dto.route.RouteHistoryResponse;

public interface RouteHistoryService {

    // requesterEmail is used to enforce that a CUSTOMER/BUSINESS_CLIENT can
    // only view route history for their own shipments. Staff roles
    // (LOGISTICS_OPERATOR, SUPPORT_AGENT, ADMINISTRATOR) can view any.
    RouteHistoryResponse getRouteHistory(Long shipmentId, String requesterEmail);
}
