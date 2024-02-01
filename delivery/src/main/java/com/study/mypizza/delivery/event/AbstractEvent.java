package com.study.mypizza.delivery.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.delivery.DeliveryApplication;
import lombok.Data;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.MimeTypeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class AbstractEvent {
    private String eventType ;
    private String timestamp ;
    private static String bindingName = "deliveryEvent-out-0" ;

    public AbstractEvent() {
        this.setEventType(this.getClass().getSimpleName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMdd-HHmmss") ;
        this.setTimestamp(simpleDateFormat.format(new Date()));
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
        if( json != null ){
            StreamBridge streamBridge = DeliveryApplication.applicationContext.getBean(StreamBridge.class);
            streamBridge.send(bindingName, MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());
        }
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

    public boolean validate(){
        return getEventType().equals(getClass().getSimpleName());
    }
}
