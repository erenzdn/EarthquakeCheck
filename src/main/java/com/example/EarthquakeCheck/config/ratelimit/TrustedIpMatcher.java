package com.example.EarthquakeCheck.config.ratelimit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public final class TrustedIpMatcher {

    private TrustedIpMatcher() {}

    public static boolean isTrusted(String remoteAddress, List<String> trustedEntries) {
        if (remoteAddress == null || remoteAddress.isBlank() || trustedEntries == null || trustedEntries.isEmpty()) {
            return false;
        }

        String normalizedRemote = normalize(remoteAddress);
        for (String entry : trustedEntries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }

            String normalizedEntry = entry.trim();
            if (normalizedEntry.contains("/")) {
                if (matchesCidr(normalizedRemote, normalizedEntry)) {
                    return true;
                }
                continue;
            }

            if (normalizedRemote.equals(normalize(normalizedEntry))) {
                return true;
            }
        }

        return false;
    }

    private static boolean matchesCidr(String ip, String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }

        try {
            int prefixLength = Integer.parseInt(parts[1]);
            byte[] addressBytes = InetAddress.getByName(ip).getAddress();
            byte[] networkBytes = InetAddress.getByName(parts[0]).getAddress();
            if (addressBytes.length != networkBytes.length) {
                return false;
            }

            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (addressBytes[i] != networkBytes[i]) {
                    return false;
                }
            }

            if (remainingBits == 0) {
                return true;
            }

            int mask = 0xFF << (8 - remainingBits);
            return (addressBytes[fullBytes] & mask) == (networkBytes[fullBytes] & mask);
        } catch (NumberFormatException | UnknownHostException ex) {
            return false;
        }
    }

    private static String normalize(String value) {
        if (value.startsWith("::ffff:")) {
            return value.substring("::ffff:".length());
        }
        return value.trim();
    }
}
