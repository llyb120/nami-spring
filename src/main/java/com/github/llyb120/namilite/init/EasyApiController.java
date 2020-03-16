package com.github.llyb120.namilite.init;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/nami/easyapi")
@RestController
public class EasyApiController {

    @RequestMapping("/{col}/{action}")
    public Object call(
            @PathVariable String col,
            @PathVariable String action
    ){
        return null;
    }
}
