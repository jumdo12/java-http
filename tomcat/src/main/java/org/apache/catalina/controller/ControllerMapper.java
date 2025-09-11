package org.apache.catalina.controller;

import com.techcourse.controller.LoginController;
import com.techcourse.controller.RegisterController;
import com.techcourse.controller.RootController;
import java.util.Map;
import org.apache.coyote.http11.Http11Request;

public class ControllerMapper {

    private static final Map<String, Controller> pathMappings = Map.of(
            "/", RootController.getInstance(),
            "/login", LoginController.getInstance(),
            "/register", RegisterController.getInstance()
    );
    private static final ControllerMapper INSTANCE = new ControllerMapper();

    public Controller getController(final String path) {
        System.out.println("controller path: " + path);
        return pathMappings.getOrDefault(path, AbstractController.getInstance());
    }

    public static ControllerMapper getInstance() {
        return INSTANCE;
    }
}
