package com.cecloud.rabbitmqclienttool.task;

import com.cecloud.rabbitmqclienttool.Opts;
import com.rabbitmq.client.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AsyncGetMessages extends Task implements Runnable {
    
    public AsyncGetMessages(String[] args) {
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
        
        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            // 创建一个回调消费者
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] This is an Async callback function. Received '" + message + "'");
            };
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            while(true) {
                channel.basicConsume(queue, true, deliverCallback, consumerTag -> { });
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
