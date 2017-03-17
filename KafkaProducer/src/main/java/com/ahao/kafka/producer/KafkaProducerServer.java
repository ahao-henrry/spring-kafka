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
     * ������Ϣ(����)
     * @param topic ����Ŀ��topic����,���topicΪnull������Ϊ"",���ʹ��xml�����õ�defaultTopic
     * @param key Ҫ���͵ļ�
     * @param value Ҫ���͵�����
     * @param partitionNum ������(����1),��ע�����������topic������ʱ���ָ���ˣ����ܸı���
     * @return ����һ��map������ɹ�codeΪ0,������Ϊʧ��
     * */
    public Map<String, Object> sendMessage(String topic, String key, String value, Integer partitionNum) {
    	if (partitionNum < 2) {
    		Map<String, Object> errMap = new HashMap<String, Object>();
    		errMap.put("code", "2003");
    		errMap.put("message", "����ȷ���÷���");
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
     * ������Ϣ(������)
     * @param topic ����Ŀ��topic����,���topicΪnull������Ϊ"",���ʹ��xml�����õ�defaultTopic
     * @param key Ҫ���͵ļ�
     * @param value Ҫ���͵�����
     * @return ����һ��map������ɹ�codeΪ0,������Ϊʧ��
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
     * ����keyֵ��ȡ��������
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
     * ��鷢�ͷ��ؽ��record
     * @param res
     * @return
     */
    @SuppressWarnings("rawtypes")
	private Map<String,Object> checkProRecord(ListenableFuture<SendResult<String, String>> res){
        Map<String,Object> m = new HashMap<String,Object>();
        if(res!=null){
            try {
                SendResult r = res.get();//���result�����
                /*���recordMetadata��offset���ݣ������producerRecord*/
                Long offsetIndex = r.getRecordMetadata().offset();
                if(offsetIndex!=null && offsetIndex>=0){
                    m.put("code", "0");
                    m.put("message", "���ͳɹ�");
                    return m;
                }else{
                    m.put("code", "2001");
                    m.put("message", "û��offset");
                    return m;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                m.put("code", "2002");
                m.put("message", "���ͳ�ʱ");
                return m;
            } catch (ExecutionException e) {
                e.printStackTrace();
                m.put("code", "2002");
                m.put("message", "���ͳ�ʱ");
                return m;
            }
        }else{
            m.put("code", "2000");
            m.put("message", "�޷���ֵ");
            return m;
        }
    }
    

}