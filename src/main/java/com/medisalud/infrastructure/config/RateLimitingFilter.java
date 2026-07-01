package com.medisalud.infrastructure.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(1)
public class RateLimitingFilter implements WebFilter {

    private static final Duration IP_BUCKET_EXPIRY = Duration.ofMinutes(10);
    private static final int MAX_IP_BUCKETS = 10_000;
    private static final Duration ENDPOINT_BUCKET_EXPIRY = Duration.ofMinutes(5);
    private static final int MAX_ENDPOINT_BUCKETS = 100;
    private static final String RETRY_AFTER_HEADER = "X-Rate-Limit-Retry-After-Seconds";
    private static final String RETRY_AFTER_SECONDS = "60";

    private Cache<String, Bucket> ipBuckets;
    private Cache<String, Bucket> endpointBuckets;
    
    @Value("${medisalud.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;
    
    @Value("${medisalud.rate-limit.endpoint-requests-per-minute:200}")
    private int endpointRequestsPerMinute;
    
    @Value("${medisalud.rate-limit.enabled:true}")
    private boolean enabled;

    @PostConstruct
    public void init() {
        log.info("Initializing RateLimitingFilter with requestsPerMinute={}, endpointRequestsPerMinute={}, enabled={}", 
                requestsPerMinute, endpointRequestsPerMinute, enabled);
        this.ipBuckets = Caffeine.newBuilder()
                .expireAfterAccess(IP_BUCKET_EXPIRY)
                .maximumSize(MAX_IP_BUCKETS)
                .build();
        this.endpointBuckets = Caffeine.newBuilder()
                .expireAfterAccess(ENDPOINT_BUCKET_EXPIRY)
                .maximumSize(MAX_ENDPOINT_BUCKETS)
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }
        
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/api-docs")) {
            return chain.filter(exchange);
        }

        String endpoint = normalizeEndpoint(path);
        Bucket endpointBucket = endpointBuckets.get(endpoint, this::createEndpointBucket);
        if (!endpointBucket.tryConsume(1)) {
            log.warn("Endpoint rate limit exceeded for: {}", endpoint);
            return rejectWithTooManyRequests(exchange);
        }

        String clientIp = getClientIp(exchange);
        Bucket bucket = ipBuckets.get(clientIp, this::createBucket);

        if (bucket.tryConsume(1)) {
            return chain.filter(exchange);
        }

        log.warn("Rate limit exceeded for IP: {}", clientIp);
        return rejectWithTooManyRequests(exchange);
    }
    
    private Mono<Void> rejectWithTooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add(RETRY_AFTER_HEADER, RETRY_AFTER_SECONDS);
        return exchange.getResponse().setComplete();
    }

    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute, Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    private Bucket createEndpointBucket(String key) {
        Bandwidth limit = Bandwidth.classic(endpointRequestsPerMinute, Refill.greedy(endpointRequestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    private String normalizeEndpoint(String path) {
        return path.replaceAll("/[0-9a-fA-F-]{36}", "/{id}");
    }

    private String getClientIp(ServerWebExchange exchange) {
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        String directIp = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
        
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String forwardedIp = xForwardedFor.split(",")[0].trim();
            if (isValidIpAddress(forwardedIp) && isTrustedProxy(directIp)) {
                return forwardedIp;
            }
            log.debug("Ignoring X-Forwarded-For from untrusted source: directIp={}, forwardedIp={}", directIp, forwardedIp);
        }
        return directIp;
    }
    
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        return ip.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$") ||
               ip.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$") ||
               ip.matches("^::1$");
    }
    
    private boolean isTrustedProxy(String directIp) {
        return directIp.startsWith("10.") || 
               directIp.startsWith("172.16.") || directIp.startsWith("172.17.") ||
               directIp.startsWith("172.18.") || directIp.startsWith("172.19.") ||
               directIp.startsWith("172.2") || directIp.startsWith("172.30.") || directIp.startsWith("172.31.") ||
               directIp.startsWith("192.168.") ||
               directIp.equals("127.0.0.1") || directIp.equals("::1");
    }
}
