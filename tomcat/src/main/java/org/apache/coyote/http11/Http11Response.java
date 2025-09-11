package org.apache.coyote.http11;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Http11Response {

    private byte[] body;
    private Http11Status http11Status;
    private String contentType;
    private String responseHeader;
    private final List<Http11Cookie> cookies;

    public Http11Response(final byte[] body,
                           final String contentType,
                           final Http11Status http11Status,
                           final List<Http11Cookie> cookies) {
        this.body = body;
        this.contentType = contentType;
        this.http11Status = http11Status;
        this.cookies = cookies;
        this.responseHeader = getHeader();
    }

    public Http11Response(final byte[] body,
                          final String contentType,
                          final Http11Status http11Status) {
        this.body = body;
        this.contentType = contentType;
        this.http11Status = http11Status;
        this.cookies = new ArrayList<>();
        this.responseHeader = getHeader();
    }

    private String getHeader() {
        StringBuilder responseBuilder = new StringBuilder();

        responseBuilder.append("HTTP/1.1 ")
                .append(http11Status.toString() + "\r\n")
                .append("Content-Type: " + contentType + "; charset=utf-8\r\n")
                .append("Content-Length: " + body.length + "\r\n");

        cookies.forEach(cookie ->
                responseBuilder.append("Set-Cookie: ")
                        .append(cookie.getName() + " = " + cookie.getValue() + ";")
                        .append("\r\n")
        );
        responseBuilder.append("\r\n");

        return responseBuilder.toString();
    }

    public byte[] getBody() {
        return body;
    }

    public byte[] getResponseHeader() {
        return responseHeader.getBytes(StandardCharsets.UTF_8);
    }
}
