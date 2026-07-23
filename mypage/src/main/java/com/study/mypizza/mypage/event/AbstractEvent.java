package com.study.mypizza.mypage.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.mypage.MyPageApplication;
import lombok.Data;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.MimeTypeUtils;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class AbstractEvent {
    private String eventType ;
    private String fullClassName ;
    private String timestamp ;
    private static String bindingName = "mypageEvent-out-0" ;

    public AbstractEvent() {
        this.setEventType(this.getClass().getSimpleName());
        this.setFullClassName(this.getClass().getName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMdd-HHmmss") ;
        this.setTimestamp(simpleDateFormat.format(new Date()));
    }

    /**
     * 동일 aggregate의 이벤트 순서를 보장하기 위한 Kafka key.
     * 각 key는 AbstractEvent를 상속받는 이벤트에서 직접 구현한다.
     * getPartitionKey() 구현이 없는 이벤트는 null을 반환한다.
     */
    protected String getPartitionKey() {
        return null;
    }

    @SuppressWarnings("deprecation")
    public void publishAfterCommit(){
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    publish();
                }
            }
        });
    }

    private void publish() {
        this.publish(this.toJson());
    }

    private void publish(String json){
        if( json == null ){
            return ;
        }

        StreamBridge streamBridge = MyPageApplication.applicationContext.getBean(StreamBridge.class);
        MessageBuilder<String> builder = MessageBuilder
                .withPayload(json)
                .setHeader(
                        MessageHeaders.CONTENT_TYPE,
                        MimeTypeUtils.APPLICATION_JSON
                );

        // kafka 파티션 키 설정 (옵션이지만 파티션이 여러개일 경우 분산을 위해 해주는 것이 좋음)
        String partitionKey = getPartitionKey();
        if (partitionKey != null && !partitionKey.isBlank()) {
            builder.setHeader(
                    KafkaHeaders.KEY,
                    partitionKey.getBytes(StandardCharsets.UTF_8)
            );
        }

        streamBridge.send(bindingName, builder.build());
    }

    private String toJson() {
        String json = null ;

        try {
            json = new ObjectMapper().writeValueAsString(this) ;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return json ;
    }

    public boolean validate() {
        return getEventType().equals(getClass().getSimpleName());
    }
}
