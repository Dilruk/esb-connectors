/*
 *  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.connector.core.util.ConnectorUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Properties;

public class KafkaUtils {

    /**
     * Read the value from the input parameter
     */
    public static String lookupTemplateParameter(MessageContext messageContext,
                                                 String paramName) {
        return (String) ConnectorUtils.lookupTemplateParamater(messageContext, paramName);
    }

    /**
     * The ProducerConfig class encapsulates the values required for establishing the connection with brokers such as the broker list, message
     * partition class, serializer class for the message, and partition key,etc.
     */
    public static Producer<String, String> getProducer(MessageContext messageContext) {

        Axis2MessageContext axis2mc = (Axis2MessageContext) messageContext;
        axis2mc.getAxis2MessageContext();
        String brokers = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.brokerList");
        String serializationClass = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.serializationClass");
        String requiredAck = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.requiredAck");
        String producerType = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.producerType");
        String compressionCodec = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.compressionCodec");
        String keySerializerClass = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.keySerializerClass");
        String partitionClass = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.partitionClass");
        String compressedTopics = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.compressedTopics");
        String messageSendMaxRetries = (String) axis2mc
                .getAxis2MessageContext().getOperationContext()
                .getProperty("kafka.messageSendMaxRetries");
        String retryBackOff = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.retryBackOff");
        String refreshInterval = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.refreshInterval");
        String bufferingMaxMessages = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext()
                .getProperty("kafka.bufferingMaxMessages");
        String batchNoMessages = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.batchNoMessages");
        String sendBufferSize = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.sendBufferSize");
        String requestTimeout = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.requestTimeout");
        String bufferingMaxTime = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.bufferingMaxTime");
        String enqueueTimeout = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.enqueueTimeout");
        String clientId = (String) axis2mc.getAxis2MessageContext()
                .getOperationContext().getProperty("kafka.clientId");

        Properties producerConfigProperties = new Properties();
        producerConfigProperties.put(KafkaConnectConstants.BROKER_LIST, brokers);
        producerConfigProperties.put(KafkaConnectConstants.SERIALIZATION_CLASS, serializationClass);
        producerConfigProperties.put(KafkaConnectConstants.REQUIRED_ACK, requiredAck);
        producerConfigProperties.put(KafkaConnectConstants.PRODUCER_TYPE, producerType);
        producerConfigProperties.put(KafkaConnectConstants.COMPRESSION_TYPE, compressionCodec);
        producerConfigProperties.put(KafkaConnectConstants.KEY_SERIALIZER_CLASS, keySerializerClass);
        producerConfigProperties.put(KafkaConnectConstants.PARTITION_CLASS, partitionClass);
        producerConfigProperties.put(KafkaConnectConstants.COMPRESSED_TOPIC, compressedTopics);
        producerConfigProperties.put(KafkaConnectConstants.MESSAGE_SEND_MAX_RETRIES,
                messageSendMaxRetries);
        producerConfigProperties.put(KafkaConnectConstants.TIME_REFRESH_METADATA, retryBackOff);
        producerConfigProperties.put(KafkaConnectConstants.TIME_REFRESH_METADATA_AFTER_TOPIC,
                refreshInterval);
        producerConfigProperties.put(KafkaConnectConstants.BUFFER_MAX_MESSAGES,
                bufferingMaxMessages);
        producerConfigProperties.put(KafkaConnectConstants.NO_MESSAGE_BATCHED_PRODUCER,
                batchNoMessages);
        producerConfigProperties.put(KafkaConnectConstants.BUFFER_SIZE, sendBufferSize);
        producerConfigProperties.put(KafkaConnectConstants.REQUEST_TIMEOUT, requestTimeout);
        producerConfigProperties.put(KafkaConnectConstants.BUFFER_MAX_TIME, bufferingMaxTime);
        producerConfigProperties.put(KafkaConnectConstants.ENQUEUE_TIMEOUT, enqueueTimeout);
        producerConfigProperties.put(KafkaConnectConstants.CLIENT_ID, clientId);

        return new Producer<String, String>(new ProducerConfig(producerConfigProperties));
    }

    /**
     * Format the messages when the messages are sent to the kafka broker
     */
    public static String formatMessage(
            org.apache.axis2.context.MessageContext messageContext) throws AxisFault {
        OMOutputFormat format = BaseUtils.getOMOutputFormat(messageContext);
        MessageFormatter messageFormatter;
        messageFormatter = MessageProcessorSelector.getMessageFormatter(messageContext);
        OutputStream out;
        StringWriter stringWriter;
        stringWriter = new StringWriter();
        out = new WriterOutputStream(stringWriter, format.getCharSetEncoding());
        try {
            if (out != null) {
                messageFormatter.writeTo(messageContext, format, out, true);
                out.close();
            }
        } catch (IOException e) {
        }

        return stringWriter.toString();
    }
}
