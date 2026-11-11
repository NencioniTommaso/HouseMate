package com.housemate.backend.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LoggingContextFilter Unit Tests")
class LoggingContextFilterTest {

    private LoggingContextFilter filter;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        filter = new LoggingContextFilter();
        MDC.clear();

        logger = (Logger) LoggerFactory.getLogger(LoggingContextFilter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        if (logger != null && listAppender != null) {
            logger.detachAppender(listAppender);
        }
    }

    @Test
    @DisplayName("Incoming request without X-Request-ID generates correlation ID, sets MDC, and attaches response header")
    void incomingRequestWithoutXRequestId_generatesIdAndPropagatesHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcRequestIdInChain = new AtomicReference<>();
        AtomicReference<String> mdcUserIdInChain = new AtomicReference<>();
        AtomicReference<String> mdcClientIpInChain = new AtomicReference<>();

        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                mdcRequestIdInChain.set(MDC.get(LoggingContextFilter.MDC_KEY_REQUEST_ID));
                mdcUserIdInChain.set(MDC.get(LoggingContextFilter.MDC_KEY_USER_ID));
                mdcClientIpInChain.set(MDC.get(LoggingContextFilter.MDC_KEY_CLIENT_IP));
            }
        };

        filter.doFilter(request, response, filterChain);

        // Verify MDC context inside filter chain
        assertThat(mdcRequestIdInChain.get()).isNotNull().hasSize(8);
        assertThat(mdcUserIdInChain.get()).isEqualTo("anonymous");
        assertThat(mdcClientIpInChain.get()).isEqualTo("10.0.0.1");

        // Verify response header propagation
        assertThat(response.getHeader(LoggingContextFilter.HEADER_REQUEST_ID))
                .isEqualTo(mdcRequestIdInChain.get());

        // Verify MDC is cleared after filter completion
        assertThat(MDC.get(LoggingContextFilter.MDC_KEY_REQUEST_ID)).isNull();
        assertThat(MDC.get(LoggingContextFilter.MDC_KEY_USER_ID)).isNull();
        assertThat(MDC.get(LoggingContextFilter.MDC_KEY_CLIENT_IP)).isNull();
    }

    @Test
    @DisplayName("Incoming request with existing X-Request-ID preserves and propagates it")
    void incomingRequestWithExistingXRequestId_preservesId() throws Exception {
        String existingRequestId = "custom-req-12345";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/households");
        request.addHeader(LoggingContextFilter.HEADER_REQUEST_ID, existingRequestId);
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcRequestIdInChain = new AtomicReference<>();

        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                mdcRequestIdInChain.set(MDC.get(LoggingContextFilter.MDC_KEY_REQUEST_ID));
            }
        };

        filter.doFilter(request, response, filterChain);

        assertThat(mdcRequestIdInChain.get()).isEqualTo(existingRequestId);
        assertThat(response.getHeader(LoggingContextFilter.HEADER_REQUEST_ID)).isEqualTo(existingRequestId);
        assertThat(MDC.get(LoggingContextFilter.MDC_KEY_REQUEST_ID)).isNull();
    }

    @Test
    @DisplayName("X-Forwarded-For header is parsed to resolve client IP correctly")
    void xForwardedForHeader_resolvesFirstClientIp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/profile");
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcClientIpInChain = new AtomicReference<>();

        MockFilterChain filterChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                mdcClientIpInChain.set(MDC.get(LoggingContextFilter.MDC_KEY_CLIENT_IP));
            }
        };

        filter.doFilter(request, response, filterChain);

        assertThat(mdcClientIpInChain.get()).isEqualTo("203.0.113.195");
    }

    @Test
    @DisplayName("HTTP access log line outputs method, URI without query parameters, status code, and duration")
    void httpAccessLogEmitted_strictlyWithoutQueryParameters() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/chores");
        request.setQueryString("filter=active&secret=doNotLogMe");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        MockFilterChain filterChain = new MockFilterChain();
        filter.doFilter(request, response, filterChain);

        assertThat(listAppender.list).isNotEmpty();
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(logEvent.getFormattedMessage())
                .startsWith("HTTP GET /api/chores -> 200")
                .contains("ms)")
                .doesNotContain("secret")
                .doesNotContain("filter=active")
                .doesNotContain("doNotLogMe");
    }

    @Test
    @DisplayName("MDC is cleared even when an exception is thrown downstream in the chain")
    void exceptionInChain_clearsMdcInFinally() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/error");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain failingChain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                throw new IllegalStateException("Database connection failed");
            }
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, failingChain))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Database connection failed");

        // MDC must still be cleaned up
        assertThat(MDC.get(LoggingContextFilter.MDC_KEY_REQUEST_ID)).isNull();
        assertThat(MDC.get(LoggingContextFilter.MDC_KEY_USER_ID)).isNull();
        assertThat(MDC.get(LoggingContextFilter.MDC_KEY_CLIENT_IP)).isNull();
    }
}
