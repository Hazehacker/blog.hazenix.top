package top.hazenix.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hazenix.service.NotifyActionService;

@RestController
@RequiredArgsConstructor
public class NotifyActionController {

    private final NotifyActionService notifyActionService;

    @GetMapping(value = "/api/notify/link-action", produces = MediaType.TEXT_HTML_VALUE)
    public String linkAction(@RequestParam String token) {
        return notifyActionService.consumeToken(token);
    }
}
