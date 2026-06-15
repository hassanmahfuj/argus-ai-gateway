package uk.mahfuj.argus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class AngularForwardController {

    @GetMapping("{path:^(?!api|v1|public|swagger)[^\\.]*}/**")
    public String handleForward() {
        return "forward:/";
    }

}
