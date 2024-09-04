package com.cecloud.rabbitmqclienttool.task;

import com.cecloud.rabbitmqclienttool.Opts;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SendAsyncMessages extends Task implements Runnable {
    
    public SendAsyncMessages(String[] args) {
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
            Opts.MESSAGE_NUMBER,
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
        int msgNum = commandLine.hasOption("msg-num") ? Integer.parseInt(commandLine.getOptionValue("msg-num")) : 10;
        
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setHost(hostServer);
        connectionFactory.setVirtualHost(vhost);
        
         try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
            channel.confirmSelect();
            // 异步确认的回调
            ConfirmCallback ackCallback = (deliveryTag, multiple) -> {
                System.out.println("Async message with deliveryTag " + deliveryTag + " has been acknowledged");
            };
            ConfirmCallback nackCallback = (deliveryTag, multiple) -> {
                System.out.println("Async message with deliveryTag " + deliveryTag + " has NOT been acknowledged yet");
            };
            channel.addConfirmListener(ackCallback, nackCallback);
            
            for (int i = 0; i < msgNum; i++) {
                String message = "Hi, this is an async test message.";
                channel.basicPublish(exchange, routingKey, null, message.getBytes());
                System.out.println("message sent: " + message);
                System.out.println("Do other things at the same time");
                Thread.sleep(100);
            }
            Thread.sleep(5000);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
