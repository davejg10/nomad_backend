package com.nomad.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/helloworld")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hello backend, a change";
    }
}