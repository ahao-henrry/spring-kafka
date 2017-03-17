package com.ahao.kafka.test;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ahao.kafka.producer.KafkaProducerServer;
import com.alibaba.fastjson.JSONObject;

@Controller
@RequestMapping("/test/")
public class KafkaProducerTest {
	private static KafkaProducerServer kafkaProducer = new KafkaProducerServer();
	private static PropertyConfigUtil propertyConfigUtil = 
			PropertyConfigUtil.getInstance("properties/kafka.properties");
	
	/**
     * 使用分区发送
     * */
	@ResponseBody
	@RequestMapping("testPartitionProducer")
    public JSONObject kafkaPartitionProducerTest() {
    	JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "ahao");
        jsonObject.put("age", "19");
        jsonObject.put("sex", "male");
        
        Integer partitionNum = Integer.valueOf(propertyConfigUtil.getValue("partitionNum"));
        String key = "test";//用来生成key
        String topic = propertyConfigUtil.getValue("topicName");
        Map<String,Object> res = kafkaProducer.sendMessage(topic, key, jsonObject.toJSONString(), partitionNum);
        
        String message = (String)res.get("message");
        String code = (String)res.get("code");
        System.out.println("code:"+code);
        System.out.println("message:"+message);
        return jsonObject;
    }
	
	/**
     * 不使用分区发送
     * */
	@ResponseBody
	@RequestMapping("testNormalProducer")
    public JSONObject kafkaProducerTest() {
    	JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "ahao-1");
        jsonObject.put("age", "18");
        jsonObject.put("sex", "famale");
        
        String key = "test";//用来生成key
        String topic = propertyConfigUtil.getValue("topicName");
        Map<String,Object> res = kafkaProducer.sendMessage(topic, key, jsonObject.toJSONString());
        
        String message = (String)res.get("message");
        String code = (String)res.get("code");
        System.out.println("code:"+code);
        System.out.println("message:"+message);
        return jsonObject;
    }
	
}