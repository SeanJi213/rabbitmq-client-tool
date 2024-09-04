package com.cecloud.rabbitmqclienttool.task;

import com.cecloud.rabbitmqclienttool.Opts;
import com.rabbitmq.client.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryGetMessages extends Task implements Runnable {
    
    public RetryGetMessages(String[] args) {
        super(args);
    }
    
    @Override
    public void run() {
        Options options = Opts.forHelp(
            Opts.HOST_SERVER,
            Opts.USERNAME,
            Opts.PASSWORD,
            Opts.VHOST,
            Opts.QUEUE
        );
        
        CommandLine commandLine = null;
        try {
            commandLine = Opts.PARSER.parse(Opts.OPTIONS, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        
        String hostServer = commandLine.hasOption("server") ? commandLine.getOptionValue("server") : "localhost";
        String username = commandLine.hasOption("user") ? commandLine.getOptionValue("user") : "root";
        String password = commandLine.hasOption("password") ? commandLine.getOptionValue("password") : "root";
        String vhost = commandLine.hasOption("vhost") ? commandLine.getOptionValue("vhost") : "/";
        String queue = commandLine.hasOption("queue") ? commandLine.getOptionValue("queue") : "test-queue";
        
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setHost(hostServer);
        connectionFactory.setVirtualHost(vhost);
        
        AtomicInteger retryTimes = new AtomicInteger();
        int retryMaxTimes = 3;
        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                try {
                    // 模拟消息处理
                    if (retryTimes.get() < retryMaxTimes - 1) {
                        retryTimes.getAndIncrement();
                        throw new Exception("Consume message failed");
                    }
                    // 手动确认消息
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    System.out.println(" [x] Succeeded to process message: " + message);
                    
                } catch (Exception e) {
                    System.err.println(" [x] Failed to process message: " + e.getMessage());

                    // 如果消息处理失败，可以选择重新投递消息
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);

                    // 也可以选择不重投，而是丢弃或者发送到死信队列
                    // channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                }
            };
            
            while (true) {
                channel.basicConsume(queue, false, deliverCallback, consumerTag -> {});
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
