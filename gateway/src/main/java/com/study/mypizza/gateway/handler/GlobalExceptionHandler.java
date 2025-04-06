package com.study.mypizza.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.gateway.dto.ResponseDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-1)
@RequiredArgsConstructor
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper objectMapper;

    // 이 것은 gateway의 자체 오류 발생(ex:router 경로 잘못된 경로) 시 수행됨
    @Override
    public Mono<Void> handle(@NotNull ServerWebExchange exchange, Throwable ex) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        log.error("### gateway 자체 오류 발생(ex:router 경로 잘못된 경로) : {}", request.getURI().getPath());
        log.error("### ERROR : {}",ex.getMessage());

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

//        return returnJsonData(response, ex) ;
        return returnHtml(response, ex) ;
    }

    private Mono<Void> returnHtml(ServerHttpResponse response, Throwable ex) {
        //header
        response.getHeaders().setContentType(MediaType.TEXT_HTML);

        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(((ResponseStatusException) ex).getStatusCode());
        }

        return response
                .writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    try {
//                        GWErrorResponse gwErrorResponse = GWErrorResponse.defaultBuild(ex.getMessage());
                        StringBuffer errorPage = new StringBuffer() ;
                        errorPage.append("<HTML>") ;
                        errorPage.append("<body>") ;
                        errorPage.append("<br><br><br><br><br><br>") ;
                        errorPage.append("<h1>") ;
                        errorPage.append(ex.getMessage()) ;
                        errorPage.append("</h1>") ;
                        errorPage.append("</body>") ;
                        errorPage.append("</HTML>") ;
                        log.debug("### 설마 여기????????????????");
                        log.error("error", ex);
                        byte[] errorResponse = objectMapper.writeValueAsBytes(errorPage);
                        return bufferFactory.wrap(errorResponse);
                    } catch (Exception e) {
                        log.error("error", e);
                        return bufferFactory.wrap(new byte[0]);
                    }
                }));
    }

    private Mono<Void> returnJsonData(ServerHttpResponse response, Throwable ex) {

        //header
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(((ResponseStatusException) ex).getStatusCode());
        }

        return response
                .writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    try {
//                        GWErrorResponse gwErrorResponse = GWErrorResponse.defaultBuild(ex.getMessage());
                        log.debug("### 설마 여기????????????????");
                        ResponseDto responseDto = ResponseDto.builder()
                                .httpStatus(HttpStatus.UNAUTHORIZED)
                                .BIZ_SUCCESS(9)
                                .msg("gateway: "+ex.getMessage())
//                                .jwtAccessToken(accessToken)
                                .build();
//                        byte[] errorResponse = objectMapper.writeValueAsBytes(gwErrorResponse);
                        byte[] errorResponse = objectMapper.writeValueAsBytes(responseDto);
                        return bufferFactory.wrap(errorResponse);
                    } catch (Exception e) {
                        log.error("error", e);
                        return bufferFactory.wrap(new byte[0]);
                    }
                }));
    }
}

// Response
//@Getter
//public class GWErrorResponse {
//    private String errorMessage;
//    private LocalDateTime localDateTime;
//    private Map<String, Object> addtionInfos = new HashMap<>();
//
//    public GWErrorResponse(String errorMessage, LocalDateTime localDateTime) {
//        this.errorMessage = errorMessage;
//        this.localDateTime = localDateTime;
//    }
//
//    public static GWErrorResponse defaultBuild(String errorMessage) {
//        return new GWErrorResponse(errorMessage, LocalDateTime.now());
//    }
//
//}