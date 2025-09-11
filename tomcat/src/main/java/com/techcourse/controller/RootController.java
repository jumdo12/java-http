package com.techcourse.controller;

import org.apache.catalina.controller.AbstractController;
import org.apache.coyote.http11.Http11Request;
import org.apache.coyote.http11.Http11Response;
import org.apache.coyote.http11.Http11Status;

public class RootController extends AbstractController {

    @Override
    protected Http11Response doGet(Http11Request http11Request) {
        String body = "/";
        String contentType = guessContentTypeByFileExtension(body);

        return new Http11Response(body, contentType, Http11Status.OK);
    }
}
