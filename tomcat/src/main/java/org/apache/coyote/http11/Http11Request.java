package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Http11Request {

    private String method;
    private String uri;
    private String protocol;
    private String body = "";
    private Map<String, String> headers = new HashMap<String, String>();

    public Http11Request(final BufferedReader bufferedReader) throws IOException {
        readRequestLine(bufferedReader);
        readHeaders(bufferedReader);
        readBody(bufferedReader);
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

    public String getProtocol() {
        return protocol;
    }

    public String getHeader(final String headerName) {
        return headers.get(headerName);
    }
}
