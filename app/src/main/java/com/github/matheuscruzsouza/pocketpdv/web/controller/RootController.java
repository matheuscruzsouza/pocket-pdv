package com.github.matheuscruzsouza.pocketpdv.web.controller;

import com.github.matheuscruzsouza.nanospring.annotation.GetMethod;
import com.github.matheuscruzsouza.nanospring.annotation.RestController;

@RestController("")
public class RootController {

    @GetMethod(value = "/", mimeType = "text/html")
    public String index() {
        return "<!DOCTYPE html><html><head><meta http-equiv=\"refresh\" content=\"0;url=/login\"></head>" +
               "<body><script>window.location.href='/login';</script></body></html>";
    }
}
