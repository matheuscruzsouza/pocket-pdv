package com.github.matheuscruzsouza.pocketpdv.web.controller;

import android.content.Context;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.GetMethod;
import com.github.matheuscruzsouza.nanospring.annotation.RestController;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@RestController("/assets")
public class AssetController {

    @Autowired
    private Context context;

    private static String cachedCss = null;
    private static String cachedHtmx = null;

    private Context getContext() {
        if (context != null) return context;
        return PocketPdvService.getAppContext();
    }

    private String readAsset(String filename) {
        try {
            Context ctx = getContext();
            if (ctx == null) return "";
            try (InputStream is = ctx.getAssets().open(filename);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                return baos.toString("UTF-8");
            }
        } catch (Exception e) {
            return "/* Asset not found: " + filename + " */";
        }
    }

    @GetMethod(value = "/style.css", mimeType = "text/css")
    public String getStyleCss() {
        if (cachedCss == null) {
            cachedCss = readAsset("style.css");
        }
        return cachedCss;
    }

    @GetMethod(value = "/htmx.min.js", mimeType = "application/javascript")
    public String getHtmxJs() {
        if (cachedHtmx == null) {
            cachedHtmx = readAsset("htmx.min.js");
        }
        return cachedHtmx;
    }
}
