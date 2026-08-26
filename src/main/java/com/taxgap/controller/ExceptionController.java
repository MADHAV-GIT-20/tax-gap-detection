package com.taxgap.controller;

import com.taxgap.domain.enums.Severity;
import com.taxgap.dto.ExceptionView;
import com.taxgap.service.ExceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exceptions")
@RequiredArgsConstructor
public class ExceptionController {

    private final ExceptionService exceptionService;

    /**
     * Get all exceptions, optionally filtered by customerId, severity and/or ruleName.
     */
    @GetMapping
    public List<ExceptionView> list(@RequestParam(required = false) String customerId,
                                    @RequestParam(required = false) Severity severity,
                                    @RequestParam(required = false) String ruleName) {
        return exceptionService.search(customerId, severity, ruleName).stream()
                .map(ExceptionView::from)
                .toList();
    }
}
