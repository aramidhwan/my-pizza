package com.study.mypizza.gateway.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@Component
@Slf4j
public class HttpRedirectServer {

    @Value("${mypizza.domain.redirect.enabled:false}")
    private boolean redirect;

    @Value("${mypizza.domain.redirect.http-port:8080}")
    private int httpPort;

    @Value("${mypizza.domain.redirect.https-port:90}")
    private int httpsPort;

    @PostConstruct
    public void startRedirectServer() {
        if ( redirect ) {
            log.info("🚀 HTTP 리디렉션 서버가 시작됩니다. 포트: " + httpPort);
            HttpServer.create()
                    .port(httpPort)
                    .bindAddress(() -> new java.net.InetSocketAddress("0.0.0.0", httpPort))
                    .handle((req, res) -> {
                        String host = req.requestHeaders().get("Host");
                        String redirectHost = host.contains(":")
                                ? host.replaceFirst(":\\d+", ":" + httpsPort)
                                : host + ":" + httpsPort;
                        String location = "https://" + redirectHost + req.uri();
                        return res.status(301)
                                .header("Location", location)
                                .send();
                    })
                    .bindNow();
        }
    }
}
