package com.sizeok.ingestion;

import com.sizeok.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;

@Component
public class SsrfUrlValidator {

    public URI validateAndNormalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new BadRequestException("INVALID_URL", "Product URL cannot be blank");
        }
        URI uri;
        try {
            uri = URI.create(rawUrl.trim());
        } catch (Exception e) {
            throw new BadRequestException("INVALID_URL", "Malformed URL: " + rawUrl);
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme))) {
            throw new BadRequestException("SSRF_BLOCKED_SCHEME", "Only HTTP/HTTPS product URLs are permitted");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new BadRequestException("INVALID_URL", "URL host is missing");
        }
        String lowerHost = host.toLowerCase();
        if ("localhost".equals(lowerHost)
                || lowerHost.endsWith(".local")
                || lowerHost.endsWith(".internal")
                || "169.254.169.254".equals(lowerHost)
                || "metadata.google.internal".equals(lowerHost)) {
            throw new BadRequestException("SSRF_BLOCKED_HOST", "URL host is restricted for security reasons: " + host);
        }

        // Check IP literal ranges (loopback, private RFC1918, link-local, metadata)
        if (lowerHost.matches("^[0-9.]+$") || lowerHost.contains(":")) {
            try {
                InetAddress addr = InetAddress.getByName(lowerHost);
                if (addr.isAnyLocalAddress()
                        || addr.isLoopbackAddress()
                        || addr.isLinkLocalAddress()
                        || addr.isSiteLocalAddress()
                        || addr.isMulticastAddress()) {
                    throw new BadRequestException("SSRF_BLOCKED_IP", "Private, loopback, or link-local IP addresses are forbidden");
                }
            } catch (BadRequestException e) {
                throw e;
            } catch (Exception e) {
                throw new BadRequestException("INVALID_URL_HOST", "Invalid IP address in URL: " + host);
            }
        }
        return uri;
    }

    public String detectSupportedRetailer(URI uri) {
        String host = uri.getHost().toLowerCase();
        if (host.contains("zara.com")) return "Zara";
        if (host.contains("hm.com")) return "H&M";
        if (host.contains("nike.com")) return "Nike";
        if (host.contains("myntra.com")) return "Myntra";
        if (host.contains("amazon.in") || host.contains("amazon.com")) return "Amazon";
        if (host.contains("levi.in") || host.contains("levi.com")) return "Levi's";
        return null;
    }
}
