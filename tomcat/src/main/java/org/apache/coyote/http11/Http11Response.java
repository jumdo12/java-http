package org.apache.coyote.http11;

import java.util.HashMap;
import java.util.Map;

public class Http11Response {

    private String protocol;
    private String body = "";
    private String statusCode;
    private Map<String, String> headers = new HashMap<>();
    private final Map<String, HttpCookie> cookies = new HashMap<>();


}
