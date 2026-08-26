package com.taxgap.controller;

import com.taxgap.domain.enums.EventType;
import com.taxgap.dto.AuditLogView;
import com.taxgap.service.AuditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditQueryService auditQueryService;

    /** View the audit trail, optionally filtered by transactionId or eventType. */
    @GetMapping
    public List<AuditLogView> list(@RequestParam(required = false) String transactionId,
                                   @RequestParam(required = false) EventType eventType) {
        return auditQueryService.find(transactionId, eventType).stream()
                .map(AuditLogView::from)
                .toList();
    }
}
