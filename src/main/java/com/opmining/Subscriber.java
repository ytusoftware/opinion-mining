 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opmining;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.kafka.common.TopicPartition;

public class Subscriber {
	
    private String topicName;
    private KafkaConsumer<String,String> kafkaConsumer;
    private TopicPartition topicPartition;
    private ArrayList<String> dataList;
    
    public Subscriber(String topicName) {
    	this.topicName = topicName;
    	this.kafkaConsumer = new KafkaConsumer<String, String>(createConsumerConfig());
    	this.topicPartition = new TopicPartition(topicName,0);     
        this.dataList = new ArrayList<String>();
    }
    
    // We can fetch all existing data on the given topic, with this method
    // This method returns the array list which consist of the strings
    public ArrayList<String> fetchAllData(){
	ArrayList<String> resultList = new ArrayList<String>();	
	// Firstly we should subscribe to the given topic
        this.kafkaConsumer.subscribe(Arrays.asList(this.topicName));
        // We want to take all data and we don't want to wait too much, because of them we need a flag
        boolean flag = true;
        
        while(flag) {
	    ConsumerRecords<String, String> records = this.kafkaConsumer.poll(100);
	    this.kafkaConsumer.seekToBeginning(Collections.singletonList(this.topicPartition));
            int i = 0;
	    for (ConsumerRecord<String, String> record : records) {
	        //System.out.println("-->"+record.value());
                // We may already have this data
                if(!this.dataList.contains(record.value())){
                    resultList.add(record.value());
                    i += 1;
                }
	    }
            // If there is any new data in the topic, we should extract info from them
            if(i > 0){
                flag = false;
            }
        }
        
        return resultList;	
    }
	
    public KafkaConsumer<String,String> getKafkaConsumer(){
        return this.kafkaConsumer;
    }
	
	// We should set the configuration settings for Subscriber
    private static Properties createConsumerConfig() {
        Properties configProperties = new Properties();
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        configProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "buyukveriGroup");
        configProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "client_id");
        return configProperties;
    }
	
    // Finally, we should close the Kafka consumer
    public void closeConsumer() {
        this.kafkaConsumer.close();
    }
}