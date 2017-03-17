package com.ahao.kafka.producer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
public class KafkaProducerServer{
	private static ApplicationContext kafkaCTX= new ClassPathXmlApplicationContext(
			   "ApplicationContext/Kafka-producer.xml");
	@SuppressWarnings("unchecked")
	private static KafkaTemplate<String, String> kafkaTemplate = 
				(KafkaTemplate<String, String>) kafkaCTX.getBean("KafkaTemplate");
    
    /**
     * 发送信息(分区)
     * @param topic 发送目的topic名称,如果topic为null或者是为"",则会使用xml中配置的defaultTopic
     * @param key 要发送的键
     * @param value 要发送的数据
     * @param partitionNum 分区数(大于1),请注意分区数是在topic创建的时候就指定了，不能改变了
     * @return 返回一个map。如果成功code为0,其他则为失败
     * */
    public Map<String, Object> sendMessage(String topic, String key, String value, Integer partitionNum) {
    	if (partitionNum < 2) {
    		Map<String, Object> errMap = new HashMap<String, Object>();
    		errMap.put("code", "2003");
    		errMap.put("message", "请正确设置分区");
			return errMap;
		}
    	key = key + "-" + value.hashCode();
    	int partitionIndex = getPartitionIndex(key, partitionNum);
    	
    	if (null == topic || "".equals(topic)) {
    		topic = kafkaTemplate.getDefaultTopic();
		}
        ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send(topic, partitionIndex, key, value);
        Map<String,Object> res = checkProRecord(result);
        kafkaTemplate.getDefaultTopic();
        return res;
	}
    
    /**
     * 发送信息(不分区)
     * @param topic 发送目的topic名称,如果topic为null或者是为"",则会使用xml中配置的defaultTopic
     * @param key 要发送的键
     * @param value 要发送的数据
     * @return 返回一个map。如果成功code为0,其他则为失败
     * */
    public Map<String, Object> sendMessage(String topic, String key, String value) {
    	if (null == topic || "".equals(topic)) {
    		topic = kafkaTemplate.getDefaultTopic();
		}
    	ListenableFuture<SendResult<String, String>> result = kafkaTemplate.send(topic, key, value);
        Map<String,Object> res = checkProRecord(result);
        return res;
	}

    /**
     * 根据key值获取分区索引
     * @param key
     * @param partitionNum
     * @return
     */
    private int getPartitionIndex(String key, int partitionNum){
        if (key == null) {
            Random random = new Random();
            return random.nextInt(partitionNum);
        }
        else {
            int result = Math.abs(key.hashCode())%partitionNum;
            return result;
        }
    }
    
    /**
     * 检查发送返回结果record
     * @param res
     * @return
     */
    @SuppressWarnings("rawtypes")
	private Map<String,Object> checkProRecord(ListenableFuture<SendResult<String, String>> res){
        Map<String,Object> m = new HashMap<String,Object>();
        if(res!=null){
            try {
                SendResult r = res.get();//检查result结果集
                /*检查recordMetadata的offset数据，不检查producerRecord*/
                Long offsetIndex = r.getRecordMetadata().offset();
                if(offsetIndex!=null && offsetIndex>=0){
                    m.put("code", "0");
                    m.put("message", "发送成功");
                    return m;
                }else{
                    m.put("code", "2001");
                    m.put("message", "没有offset");
                    return m;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                m.put("code", "2002");
                m.put("message", "发送超时");
                return m;
            } catch (ExecutionException e) {
                e.printStackTrace();
                m.put("code", "2002");
                m.put("message", "发送超时");
                return m;
            }
        }else{
            m.put("code", "2000");
            m.put("message", "无返回值");
            return m;
        }
    }
    

}