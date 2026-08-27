package com.taxgap.controller;

import com.taxgap.entity.ExceptionRecord;
import com.taxgap.enums.Severity;
import com.taxgap.service.ExceptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exceptions")
public class ExceptionController {

    private final ExceptionService exceptionService;

    public ExceptionController(ExceptionService exceptionService) {
        this.exceptionService = exceptionService;
    }

    /** Get all exceptions, optionally filtered by customerId, severity and/or ruleName. */
    @GetMapping
    public List<ExceptionRecord> list(@RequestParam(required = false) String customerId,
                                      @RequestParam(required = false) Severity severity,
                                      @RequestParam(required = false) String ruleName) {
        return exceptionService.search(customerId, severity, ruleName);
    }
}
