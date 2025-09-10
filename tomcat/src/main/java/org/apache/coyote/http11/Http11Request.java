package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.catalina.session.Session;
import org.apache.catalina.session.SessionManager;

public class Http11Request {

    private String method;
    private String uri;
    private String protocol;
    private String body = "";
    private Map<String, String> headers = new HashMap<String, String>();
    private final Map<String, HttpCookie> cookies = new HashMap<>();

    public Http11Request(final BufferedReader bufferedReader) throws IOException {
        readRequestLine(bufferedReader);
        readHeaders(bufferedReader);
        readBody(bufferedReader);
    }

    public Optional<Session> getSession(final String sessionId) {
        return getCookie(sessionId)
                .map(HttpCookie::getValue)
                .map(SessionManager.getInstance()::findSession);
    }

    public Session createSession() {
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId);
        SessionManager.getInstance().add(session);
        return session;
    }

    private void readRequestLine(final BufferedReader bufferedReader) throws IOException {
        String requestLine = bufferedReader.readLine();
        String[] splitRequestLine = requestLine.split(" ");

        method = splitRequestLine[0];
        uri = splitRequestLine[1];
        protocol = splitRequestLine[2];
    }

    private void readHeaders(final BufferedReader bufferedReader) throws IOException {
        String line;
        while((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
            String[] split = line.split(":");
            String header = split[0].trim();
            String value = split[1].trim();

            if ("Cookie".equalsIgnoreCase(header)) {
                cookies.putAll(HttpCookie.parse(value));
                continue;
            }

            headers.put(header, value);
        }
    }

    private void readBody(final BufferedReader bufferedReader) throws IOException {
        String lenHeader = headers.get("Content-Length");
        if (lenHeader == null) {
            body = "";
            return;
        }
        int len;
        try {
            len = Integer.parseInt(lenHeader);
        } catch (NumberFormatException e) {
            body = "";
            return;
        }
        if (len <= 0) {
            body = "";
            return;
        }

        char[] buf = new char[len];
        int read = 0;
        while (read < len) {
            int r = bufferedReader.read(buf, read, len - read);
            if (r == -1) break;
            read += r;
        }
        body = new String(buf, 0, read);
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getBody(){
        return body;
    }

    public Optional<HttpCookie> getCookie(String name) {
        HttpCookie httpCookie = cookies.get(name);

        return Optional.ofNullable(httpCookie);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHeader(final String headerName) {
        return headers.get(headerName);
    }
}
