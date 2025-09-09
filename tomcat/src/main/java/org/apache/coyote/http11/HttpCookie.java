package org.apache.coyote.http11;

import java.util.HashMap;
import java.util.Map;

public class HttpCookie {
    private final String name;
    private final String value;

    public HttpCookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static Map<String, HttpCookie> parse(String headerValue) {
        Map<String, HttpCookie> map = new HashMap<>();
        if (headerValue == null || headerValue.isBlank()) return map;

        String[] pairs = headerValue.split(";");
        for (String pair : pairs) {
            String[] nv = pair.trim().split("=", 2);
            if (nv.length == 2) {
                String n = nv[0].trim();
                String v = nv[1].trim();
                if (!n.isEmpty()) map.put(n, new HttpCookie(n, v));
            }
        }
        return map;
    }

    public String getName() { return name; }
    public String getValue() { return value; }
}
