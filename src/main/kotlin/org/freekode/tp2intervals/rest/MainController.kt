package org.freekode.tp2intervals.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MainController {
    @GetMapping("test")
    fun test(): String {
        return "ok";
    }
}
