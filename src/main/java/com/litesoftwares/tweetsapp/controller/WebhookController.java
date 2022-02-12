package com.litesoftwares.tweetsapp.controller;

import com.litesoftwares.tweetsapp.service.WebhookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WebhookController {

    @Autowired
    private final WebhookService webhookService = new WebhookService();

    @GetMapping("/")
    public Map<String, String> ping(){

        Map<String, String> map = new HashMap<>();
        map.put("ping", "Hey, it works.");
        return map;
    }

    @RequestMapping(value = "/webhook/receive", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody void receiver(HttpServletRequest httpRequest) throws Exception{
        webhookService.processIncoming(httpRequest);
    }

}
