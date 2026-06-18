package top.hazenix.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * 预渲染通知服务 — 当内容变更时，调用 prerender-server 进行增量重渲染。
 *
 * prerender-server 地址通过 application.yml 中的 prerender.server-url 配置。
 * 如果未配置（默认 ""），则跳过通知（本地开发或未部署 prerender-server 时）。
 */
@Slf4j
@Service
public class PrerenderNotifyService {

    @Value("${prerender.server-url:}")
    private String serverUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        if (serverUrl.isEmpty()) {
            log.info("prerender.server-url 未配置，增量预渲染通知已禁用");
        } else {
            log.info("预渲染通知服务已启用: {}", serverUrl);
        }
    }

    /**
     * 通知 prerender-server 重新渲染受影响页面。异步执行，不阻塞主请求。
     */
    @Async
    public void notifyContentChanged(String type, Long id) {
        if (serverUrl.isEmpty()) return;

        try {
            String url = serverUrl + "/render-affected?type=" + type + "&id=" + id;
            log.debug("通知预渲染: {}", url);
            restTemplate.postForObject(url, null, String.class);
        } catch (Exception e) {
            log.warn("预渲染通知失败 ({} / {}): {}", type, id, e.getMessage());
        }
    }

    /**
     * 通知全量重新渲染
     */
    @Async
    public void notifyFullRender() {
        if (serverUrl.isEmpty()) return;

        try {
            String url = serverUrl + "/render-all";
            log.info("触发全量预渲染: {}", url);
            restTemplate.postForObject(url, null, String.class);
        } catch (Exception e) {
            log.warn("全量预渲染通知失败: {}", e.getMessage());
        }
    }
}
