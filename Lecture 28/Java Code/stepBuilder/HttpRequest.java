package stepBuilder;

import java.util.*;

public class HttpRequest {
    private String url;
    private String method;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private String body;
    private int timeout; // in seconds

    // Private constructor - can only be accessed by the Builder
    private HttpRequest() {
        headers = new HashMap<>();
        queryParams = new HashMap<>();
        body = "";
    }

    // Method to execute the HTTP request
    public void execute() {
        System.out.println("Executing " + method + " request to " + url);

        if (!queryParams.isEmpty()) {
            System.out.println("Query Parameters:");
            for (Map.Entry<String, String> param : queryParams.entrySet()) {
                System.out.println("  " + param.getKey() + "=" + param.getValue());
            }
        }

        System.out.println("Headers:");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            System.out.println("  " + header.getKey() + ": " + header.getValue());
        }

        if (!body.isEmpty()) {
            System.out.println("Body: " + body);
        }

        System.out.println("Timeout: " + timeout + " seconds");
        System.out.println("Request executed successfully!");
    }

    // Nested Step interfaces
    interface UrlStep {
        MethodStep withUrl(String url);
    }

    interface MethodStep {
        HeaderStep withMethod(String method);
    }

    interface HeaderStep {
        OptionalStep withHeader(String key, String value);
    }

    interface OptionalStep {
        OptionalStep withBody(String body);
        OptionalStep withTimeout(int timeout);
        HttpRequest build();
    }

    // Concrete step builder as static nested class
    static class HttpRequestStepBuilder implements UrlStep, MethodStep, HeaderStep, OptionalStep {
        private HttpRequest req;

        private HttpRequestStepBuilder() {
            req = new HttpRequest();
        }

        // UrlStep implementation
        public MethodStep withUrl(String url) {
            req.url = url;
            return this;
        }

        // MethodStep implementation
        public HeaderStep withMethod(String method) {
            req.method = method;
            return this;
        }

        // HeaderStep implementation
        public OptionalStep withHeader(String key, String value) {
            req.headers.put(key, value);
            return this;
        }

        // OptionalStep implementation
        public OptionalStep withBody(String body) {
            req.body = body;
            return this;
        }

        public OptionalStep withTimeout(int timeout) {
            req.timeout = timeout;
            return this;
        }

        public HttpRequest build() {
            if (req.url == null || req.url.isEmpty()) {
                throw new RuntimeException("URL cannot be empty");
            }
            return req;
        }

        // Static method to start the building process
        public static UrlStep getBuilder() {
            return new HttpRequestStepBuilder();
        }
    }
}
