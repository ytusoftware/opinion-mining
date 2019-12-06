
 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.opmining;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
 

public class Publisher {

	private Properties configProperties;
	private KafkaProducer producer;
	
	// This is the constructor of the Publisher Class
	public Publisher(){
				
		// Configure the Producer
		Properties configProperties = new Properties();
		configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				"localhost:9092");
		configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.ByteArraySerializer");
		configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");

		this.producer = new KafkaProducer<String, String>(configProperties);
		
	}

	// We can publish data to the given topic with this method
	public void publish(String topicName,String data){
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(topicName, data);
		this.producer.send(record);
	}
     
    public void flush(){
        this.producer.flush();
    }


}
