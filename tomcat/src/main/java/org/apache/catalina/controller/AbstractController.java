package org.apache.catalina.controller;

import com.techcourse.controller.LoginController;
import java.io.IOException;
import java.io.InputStream;
import org.apache.coyote.http11.Http11Request;
import org.apache.coyote.http11.Http11Response;
import org.apache.coyote.http11.Http11Status;

public class AbstractController implements Controller {

    private static final AbstractController INSTANCE = new AbstractController();

    protected AbstractController() {

    }

    @Override
    public Http11Response service(final Http11Request http11Request) throws IOException {
        if(http11Request.getMethod().equals("GET")) {
            Http11Response http11Response = doGet(http11Request);

            return createWithBody(http11Response);
        }
        if(http11Request.getMethod().equals("POST")) {
            Http11Response http11Response = doPost(http11Request);

            return createWithBody(http11Response);
        }

        Http11Response redirect401Response = createRedirect401Response();

        return createWithBody(redirect401Response);
    }

    protected Http11Response doPost(Http11Request http11Request) throws IOException {
        Http11Response redirect401Response = createRedirect401Response();

        return createWithBody(redirect401Response);
    }

    protected Http11Response doGet(Http11Request http11Request) throws IOException {
        String path = http11Request.getPath();
        String contentType = guessContentTypeByFileExtension(path);

        Http11Response http11Response = new Http11Response(path, contentType, Http11Status.OK);

        return createWithBody(http11Response);
    }

    protected String guessContentTypeByFileExtension(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";

        return "text/html";
    }

    private byte[] readFromResourcePath(final String resourcePath) throws IOException {
        if(resourcePath.equals("/")) {
            String response = "Hello world!";

            return response.getBytes();
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("static");

        stringBuilder.append(resourcePath);
        if(!resourcePath.contains(".")){
            stringBuilder.append(".html");
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

    private Http11Response createWithBody(final Http11Response http11Response) throws IOException {
        System.out.println("response path : " + http11Response.getPath());
        byte[] body = readFromResourcePath(http11Response.getPath());

        return new Http11Response(
                http11Response.getContentType(),
                http11Response.getHttp11Status(),
                body,
                http11Response.getCookies()
        );
    }

    private Http11Response createRedirect401Response() {
        String failResourceName = "401.html";

        return new Http11Response(failResourceName, "text/html", Http11Status.NOT_FOUND);
    }

    public static AbstractController getInstance() {
        return INSTANCE;
    }
}
