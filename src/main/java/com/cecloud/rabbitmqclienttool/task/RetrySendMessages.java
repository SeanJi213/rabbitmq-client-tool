package com.cecloud.rabbitmqclienttool.task;

import com.cecloud.rabbitmqclienttool.Opts;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RetrySendMessages extends Task implements Runnable {
    
    public RetrySendMessages(String[] args) {
        super(args);
    }
    
    @Override
    public void run() {
        Options options = Opts.forHelp(
            Opts.HOST_SERVER,
            Opts.USERNAME,
            Opts.PASSWORD,
            Opts.VHOST,
            Opts.EXCHANGE,
            Opts.ROUTING_KEY
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
        String exchange = commandLine.hasOption("exchange") ? commandLine.getOptionValue("exchange") : "test-exchange";
        String routingKey = commandLine.hasOption("routing-key") ? commandLine.getOptionValue("routing-key") : "test";
        
        int MAX_RETRY_ATTEMPTS = 3; // 最大重试次数
        long RETRY_DELAY_MS = 2000; // 每次重试之间的等待时间（毫秒）
        
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setHost(hostServer);
        connectionFactory.setVirtualHost(vhost);
        
        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
            String message = "Hello, RabbitMQ with Retry!";
            boolean success = false;
            int attempts = 0;

            while (!success && attempts < MAX_RETRY_ATTEMPTS) {
                try {
                    // 发送消息
                    channel.basicPublish(exchange, routingKey, null, message.getBytes());
                    System.out.println(" [x] Sent '" + message + "'");
                    if (attempts < MAX_RETRY_ATTEMPTS - 1) {
                        throw new Exception("Message needs retry!");
                    }
                    success = true; // 如果发送成功，设置 success 为 true
                    System.out.println("Message retry sent successfully");
                } catch (Exception e) {
                    attempts++;
                    System.err.println("Failed to send message. Attempt " + attempts + " of " + MAX_RETRY_ATTEMPTS);

                    // 如果未达到最大重试次数，等待一段时间后重试
                    if (attempts < MAX_RETRY_ATTEMPTS) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            System.err.println("Retry interrupted");
                            Thread.currentThread().interrupt(); // 恢复中断状态
                        }
                    } else {
                        System.err.println("Exceeded max retry attempts. Message sending failed.");
                    }
                }
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
