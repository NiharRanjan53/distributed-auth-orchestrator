package com.mro.orchestrator.security;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID = "correlationId";
    private static final String CORRELATION_HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String uniqueId;

            SpanContext spanContext = Span.current().getSpanContext();

            if (spanContext.isValid()) {
                uniqueId = spanContext.getTraceId();
                log.debug("Using OpenTelemetry TraceId={}", uniqueId);
            } else {
                uniqueId = UUID.randomUUID().toString();
                log.debug("Generated fallback CorrelationId={}", uniqueId);
            }

            MDC.put(CORRELATION_ID, uniqueId);

            response.setHeader(CORRELATION_HEADER, uniqueId);

            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}