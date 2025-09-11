package com.techcourse.controller;

import java.io.IOException;
import org.apache.catalina.controller.AbstractController;
import org.apache.coyote.http11.Http11Request;
import org.apache.coyote.http11.Http11Response;
import org.apache.coyote.http11.Http11Status;

public class RootController extends AbstractController {

    @Override
    protected Http11Response doGet(Http11Request http11Request) throws IOException {
        System.out.println("asd" + http11Request.getPath());

        String path = "";
        String contentType = guessContentTypeByFileExtension(path);
        byte[] body = readFromResourcePath(path);

        return new Http11Response(body,contentType, Http11Status.OK);
    }
}
