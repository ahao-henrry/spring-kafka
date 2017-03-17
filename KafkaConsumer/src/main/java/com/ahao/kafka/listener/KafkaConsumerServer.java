package com.ahao.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;
import org.springframework.kafka.listener.MessageListener;

/**
 * kafka监听器启动
 * 自动监听是否有消息需要消费
 */
public class KafkaConsumerServer implements MessageListener<String, String> {
	Logger logger = Logger.getLogger(KafkaConsumerServer.class);
    /**
     * 监听器自动执行该方法消费消息
     * 自动提交offset,执行业务代码
     * 不提供offset管理，不能指定offset进行消费
     * 请注意此方法是堵塞的，建议多线程处理业务
     */
    @Override
    public void onMessage(ConsumerRecord<String, String> record) {
        String value = record.value();
        logger.info("--****Kafka got message is : " + value);
        //TODO your own code
    }

}