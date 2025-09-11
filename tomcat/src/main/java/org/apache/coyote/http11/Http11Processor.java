package org.apache.coyote.http11;

import com.techcourse.db.InMemoryUserRepository;
import com.techcourse.exception.UncheckedServletException;
import com.techcourse.model.User;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.catalina.session.Session;
import org.apache.catalina.session.SessionManager;
import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("connect host: {}, port: {}", connection.getInetAddress(), connection.getPort());
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (
                final var bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                final var writer = connection.getOutputStream()) {
            Http11Request http11Request = new Http11Request(bufferedReader);


            if (http11Request.getPath().startsWith("login") && http11Request.getMethod().equals("GET")) {
                Optional<String> optionalSessionId = http11Request.getSession("JSESSIONID");

                if(optionalSessionId.isPresent()) {
                    String sessionId = optionalSessionId.get();

                    if(SessionManager.getInstance().findSession(sessionId) != null) {
                        Session session = SessionManager.getInstance().findSession(sessionId);
                        log.info("JSESSIONID: {}", sessionId);

                        User user = (User) session.getAttribute("user");
                        log.info("user: {}", user);

                        byte[] body = readFromResourcePath("/index.html");
                        String contentType = guessContentTypeByFileExtension(http11Request.getPath());
                        Http11Response http11Response = new Http11Response(body, contentType, Http11Status.FOUND);

                        writer.write(http11Response.getResponseHeader());
                        writer.write(http11Response.getBody());
                        writer.flush();
                        return;
                    }
                }
            }

            if(http11Request.getPath().startsWith("login") && http11Request.getMethod().equals("POST")) {
                Map<String, String> parseQuery = http11Request.parseBody();

                String account = parseQuery.get("account");
                Optional<User> optionalUser = InMemoryUserRepository.findByAccount(account);

                if(!optionalUser.isEmpty() && optionalUser.get().checkPassword(parseQuery.get("password"))) {
                    User user = optionalUser.get();
                    log.info("user: {}", user);

                    Session session = SessionManager.getInstance().createSession();
                    session.setAttribute("user", user);

                    Http11Cookie http11Cookie = new Http11Cookie("JSESSIONID", session.getId());
                    List<Http11Cookie> cookies = new ArrayList<>();
                    cookies.add(http11Cookie);

                    byte[] body = readFromResourcePath("/index.html");
                    String contentType = guessContentTypeByFileExtension(http11Request.getPath());

                    Http11Response http11Response = new Http11Response(body, contentType, Http11Status.FOUND, cookies);

                    writer.write(http11Response.getResponseHeader());
                    writer.write(http11Response.getBody());
                    writer.flush();
                    return;
                }

                byte[] body = readFromResourcePath("/401.html");
                String contentType = guessContentTypeByFileExtension("/401.html");
                Http11Response http11Response = new Http11Response(body, contentType, Http11Status.FOUND);

                writer.write(http11Response.getResponseHeader());
                writer.write(http11Response.getBody());
                writer.flush();
                return;
            }

            if(http11Request.getPath().startsWith("register") && http11Request.getMethod().equals("POST")) {
                Map<String, String> parseQuery = http11Request.parseBody();

                String account = parseQuery.get("account");
                String email = parseQuery.get("email");
                String password = parseQuery.get("password");

                User user = new User(account, password, email);
                InMemoryUserRepository.save(user);

                byte[] body = readFromResourcePath("/index.html");
                String contentType = guessContentTypeByFileExtension("/index.html");
                Http11Response http11Response = new Http11Response(body, contentType, Http11Status.FOUND);

                writer.write(http11Response.getResponseHeader());
                writer.write(http11Response.getBody());
                writer.flush();
                return;
            }

            byte[] body = readFromResourcePath(http11Request.getPath());

            if(body == null) {
                String notFoundBody = "<h1>404 Not Found</h1>";
                Http11Response http11Response = new Http11Response(notFoundBody.getBytes(), "text/html",
                        Http11Status.NOT_FOUND);

                writer.write(http11Response.getResponseHeader());
                writer.write(http11Response.getBody());
                writer.flush();
                return;
            }

            String content = guessContentTypeByFileExtension(http11Request.getPath());
            Http11Response http11Response = new Http11Response(body, content, Http11Status.OK);

            writer.write(http11Response.getResponseHeader());
            writer.write(http11Response.getBody());
            writer.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String guessContentTypeByFileExtension(String path) {
        if (path.endsWith(".html") || path.equals("/")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";

        return "text/html";
    }

    private byte[] readFromResourcePath(final String resourcePath) throws IOException {
        if(resourcePath.isEmpty()) {
            String response = "Hello world!";

            return response.getBytes();
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("static/");

        if(resourcePath.contains("?")) {
            stringBuilder.append(resourcePath, 0, resourcePath.indexOf("?"));
            stringBuilder.append(".html");
        }
        else if(!resourcePath.contains(".")){
            stringBuilder.append(resourcePath);
            stringBuilder.append(".html");
        }
        else {
            stringBuilder.append(resourcePath);
        }

        String classPath = stringBuilder.toString();
        try (InputStream resourceAsStream = getClass().
                getClassLoader().
                getResourceAsStream(classPath)) {
            if(resourceAsStream == null) {
                return null;
            }

            return resourceAsStream.readAllBytes();
        }
    }
}
