package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.service.GeocodingService;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class NominatimGeocodingService implements GeocodingService {
    private static final Pattern HOUSE_NUMBER_PATTERN = Pattern.compile("\\bno\\s*[:.]?\\s*\\d+[a-z]?\\b");

    private final RestTemplate restTemplate;
    private final String nominatimBaseUrl;
    private final String userAgent;
    private final int maxResults;
    private final double minMatchScore;

    public NominatimGeocodingService(
            @Value("${geocoding.nominatim.base-url:https://nominatim.openstreetmap.org}") String nominatimBaseUrl,
            @Value("${geocoding.nominatim.user-agent:EarthquakeCheck/1.0 (contact: local-dev)}") String userAgent,
            @Value("${geocoding.nominatim.max-results:5}") int maxResults,
            @Value("${geocoding.nominatim.min-match-score:0.45}") double minMatchScore) {
        this.restTemplate = new RestTemplate();
        this.nominatimBaseUrl = nominatimBaseUrl;
        this.userAgent = userAgent;
        this.maxResults = maxResults;
        this.minMatchScore = minMatchScore;
    }

    @Override
    public Optional<CoordinatePair> getCoordinatesFromAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return Optional.empty();
        }
        String normalizedAddress = normalizeAddress(address);
        String addressHash = hashAddress(normalizedAddress);

        URI requestUri = UriComponentsBuilder.fromUriString(nominatimBaseUrl)
                .path("/search")
                .queryParam("q", normalizedAddress)
                .queryParam("format", "jsonv2")
                .queryParam("countrycodes", "tr")
                .queryParam("accept-language", "tr")
                .queryParam("addressdetails", 1)
                .queryParam("limit", maxResults)
                .build()
                .encode()
                .toUri();

        RequestEntity<Void> request = RequestEntity
                .get(requestUri)
                .header(HttpHeaders.USER_AGENT, userAgent)
                .header(HttpHeaders.ACCEPT, "application/json")
                .build();

        try {
            ResponseEntity<List<NominatimResult>> response = restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<>() {
                    });

            List<NominatimResult> body = response.getBody();
            if (body == null || body.isEmpty()) {
                logGeocodingDecision(addressHash, null, "fallback_no_result", null, null, true);
                return Optional.empty();
            }

            Optional<ScoredResult> bestResult = body.stream()
                    .filter(this::hasCoordinates)
                    .map(candidate -> new ScoredResult(candidate, calculateScore(normalizedAddress, candidate)))
                    .max(Comparator.comparingDouble(ScoredResult::score));

            if (bestResult.isEmpty()) {
                logGeocodingDecision(addressHash, null, "fallback_no_result", null, null, true);
                return Optional.empty();
            }

            ScoredResult selectedResult = bestResult.get();
            if (selectedResult.score() < minMatchScore) {
                logGeocodingDecision(
                        addressHash,
                        selectedResult.score(),
                        "fallback_low_score",
                        selectedResult.candidate().type(),
                        selectedResult.candidate().className(),
                        true);
                return Optional.empty();
            }

            double latitude = Double.parseDouble(selectedResult.candidate().lat());
            double longitude = Double.parseDouble(selectedResult.candidate().lon());
            logGeocodingDecision(
                    addressHash,
                    selectedResult.score(),
                    "accepted",
                    selectedResult.candidate().type(),
                    selectedResult.candidate().className(),
                    false);
            return Optional.of(new CoordinatePair(latitude, longitude));
        } catch (RestClientException | NumberFormatException ex) {
            log.warn(
                    "geocodingDecision addressHash={} bestScore=null threshold={} decision=fallback_no_result selectedType=null selectedClass=null usedDefaultCoordinates=true errorType={}",
                    addressHash,
                    minMatchScore,
                    ex.getClass().getSimpleName(),
                    ex);
            return Optional.empty();
        }
    }

    private void logGeocodingDecision(
            String addressHash,
            Double bestScore,
            String decision,
            String selectedType,
            String selectedClass,
            boolean usedDefaultCoordinates) {
        log.info(
                "geocodingDecision addressHash={} bestScore={} threshold={} decision={} selectedType={} selectedClass={} usedDefaultCoordinates={}",
                addressHash,
                bestScore,
                minMatchScore,
                decision,
                selectedType,
                selectedClass,
                usedDefaultCoordinates);
    }

    private boolean hasCoordinates(NominatimResult candidate) {
        return StringUtils.hasText(candidate.lat()) && StringUtils.hasText(candidate.lon());
    }

    private double calculateScore(String normalizedAddress, NominatimResult candidate) {
        Set<String> inputTokens = tokenize(normalizedAddress);
        Set<String> candidateTokens = tokenize(normalizeAddress(candidate.displayName()));

        if (inputTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 0.0;
        }

        long matchedCount = inputTokens.stream().filter(candidateTokens::contains).count();
        double score = (double) matchedCount / inputTokens.size();

        if (containsHouseNumber(normalizedAddress)) {
            score += containsHouseNumber(normalizeAddress(candidate.displayName())) ? 0.20 : -0.15;
        }

        if ("house".equals(candidate.type()) || "building".equals(candidate.type())) {
            score += 0.15;
        } else if ("residential".equals(candidate.className())) {
            score += 0.08;
        }

        return Math.max(0.0, Math.min(1.0, score));
    }

    private Set<String> tokenize(String value) {
        String alphaNumeric = value.replaceAll("[^\\p{L}\\p{N}\\s]", " ").replaceAll("\\s+", " ").trim();
        if (alphaNumeric.isEmpty()) {
            return Set.of();
        }

        Set<String> tokens = new HashSet<>();
        Arrays.stream(alphaNumeric.split("\\s+"))
                .filter(token -> token.length() > 1)
                .forEach(tokens::add);
        return tokens;
    }

    private boolean containsHouseNumber(String address) {
        return HOUSE_NUMBER_PATTERN.matcher(address).find();
    }

    private String normalizeAddress(String address) {
        return Normalizer.normalize(address, Normalizer.Form.NFKC)
                .toLowerCase()
                .replace("mah.", "mahalle")
                .replace("mh.", "mahalle")
                .replace("cad.", "caddesi")
                .replace("cd.", "caddesi")
                .replace("sok.", "sokak")
                .replace("sk.", "sokak")
                .replace("blv.", "bulvari")
                .replace("bul.", "bulvari")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String hashAddress(String normalizedAddress) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(normalizedAddress.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes).substring(0, 12);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algoritmasi bulunamadi", ex);
        }
    }

    private record NominatimResult(
            String lat,
            String lon,
            @JsonProperty("display_name") String displayName,
            String type,
            @JsonProperty("class") String className) {
    }

    private record ScoredResult(NominatimResult candidate, double score) {
    }
}
