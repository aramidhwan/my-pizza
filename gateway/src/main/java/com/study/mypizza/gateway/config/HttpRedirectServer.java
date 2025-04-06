package com.study.mypizza.gateway.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@Component
public class HttpRedirectServer {

    @Value("${mypizza.domain.http-port:8080}")
    private int httpPort;

    @Value("${mypizza.domain.https-port:90}")
    private int httpsPort;

    @PostConstruct
    public void startRedirectServer() {
        System.out.println("🚀 HTTP 리디렉션 서버가 시작됩니다. 포트: " + httpPort);
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
