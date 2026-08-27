package com.taxgap.controller;

import com.taxgap.entity.AuditLog;
import com.taxgap.enums.EventType;
import com.taxgap.service.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    /** View the audit trail, optionally filtered by transactionId or eventType. */
    @GetMapping
    public List<AuditLog> list(@RequestParam(required = false) String transactionId,
                               @RequestParam(required = false) EventType eventType) {
        return auditService.find(transactionId, eventType);
    }
}
