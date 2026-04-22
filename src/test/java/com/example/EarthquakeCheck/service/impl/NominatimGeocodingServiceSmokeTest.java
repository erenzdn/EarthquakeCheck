package com.example.EarthquakeCheck.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.EarthquakeCheck.service.GeocodingService.CoordinatePair;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class NominatimGeocodingServiceSmokeTest {

    @Test
    void smokeShouldAcceptForClearAddress() {
        NominatimGeocodingService service =
                new NominatimGeocodingService("https://nominatim.test", "test-agent", 5, 0.45);
        MockRestServiceServer server = bindServer(service);
        ListAppender<ILoggingEvent> appender = attachLogAppender();

        server.expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                                [
                                  {
                                    "lat":"41.0082",
                                    "lon":"28.9784",
                                    "display_name":"Istanbul Besiktas Barbaros Bulvari No 10",
                                    "type":"house",
                                    "class":"residential"
                                  }
                                ]
                                """,
                        MediaType.APPLICATION_JSON));

        Optional<CoordinatePair> result = service.getCoordinatesFromAddress("Istanbul Besiktas Barbaros Bulvari No 10");

        assertTrue(result.isPresent());
        assertTrue(logsContain(appender, "decision=accepted"));
        assertTrue(logsContain(appender, "threshold=0.45"));
        server.verify();
    }

    @Test
    void smokeShouldFallbackForLowScoreAddress() {
        NominatimGeocodingService service =
                new NominatimGeocodingService("https://nominatim.test", "test-agent", 5, 0.60);
        MockRestServiceServer server = bindServer(service);
        ListAppender<ILoggingEvent> appender = attachLogAppender();

        server.expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                                [
                                  {
                                    "lat":"39.9334",
                                    "lon":"32.8597",
                                    "display_name":"Ankara, Turkiye",
                                    "type":"city",
                                    "class":"boundary"
                                  }
                                ]
                                """,
                        MediaType.APPLICATION_JSON));

        Optional<CoordinatePair> result =
                service.getCoordinatesFromAddress("Ankara Cankaya Ataturk Bulvari No 100");

        assertFalse(result.isPresent());
        assertTrue(logsContain(appender, "decision=fallback_low_score"));
        assertTrue(logsContain(appender, "threshold=0.6"));
        server.verify();
    }

    @Test
    void smokeShouldFallbackForInvalidAddressNoResult() {
        NominatimGeocodingService service =
                new NominatimGeocodingService("https://nominatim.test", "test-agent", 5, 0.45);
        MockRestServiceServer server = bindServer(service);
        ListAppender<ILoggingEvent> appender = attachLogAppender();

        server.expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        Optional<CoordinatePair> result = service.getCoordinatesFromAddress("xxxxxx-gecersiz-adres");

        assertFalse(result.isPresent());
        assertTrue(logsContain(appender, "decision=fallback_no_result"));
        assertTrue(logsContain(appender, "threshold=0.45"));
        server.verify();
    }

    private MockRestServiceServer bindServer(NominatimGeocodingService service) {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        return MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    }

    private ListAppender<ILoggingEvent> attachLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(NominatimGeocodingService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);
        return appender;
    }

    private boolean logsContain(ListAppender<ILoggingEvent> appender, String expected) {
        List<String> messages = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
        return messages.stream().anyMatch(message -> message.contains(expected));
    }
}
