package com.cecloud.rabbitmqclienttool;

import org.apache.commons.cli.*;

public interface Opts {

    Options OPTIONS = new Options();
    CommandLineParser PARSER = new DefaultParser();
    HelpFormatter FORMATTER = new HelpFormatter();

    static Options forHelp(Option... options) {
        Options forHelp = new Options();
        return forHelp(forHelp, options);
    }

    static Options forHelp(Options tar, Option... options) {
        if (options == null || options.length == 0) {
            return tar;
        }
        for (Option option : options) {
            OPTIONS.addOption(option);
            tar.addOption(option);
        }
        return tar;
    }

    Option HOST_SERVER = Option.builder().option(null).longOpt("server").hasArg(true).desc("host server address, default: localhost").build();
    Option USERNAME = new Option(null, "user", true, "user name");
    Option PASSWORD = new Option(null, "password", true, "password, default:''");
    Option VHOST = new Option(null, "vhost", true, "vhost, default: '/'");
    Option EXCHANGE = new Option(null, "exchange", true, "exchange");
    Option MESSAGE_NUMBER = new Option(null, "msg-num", true, "message number");
    Option ROUTING_KEY = new Option(null, "routing-key", true, "routing key, better be the same as the binding key");
    Option QUEUE = new Option(null, "queue", true, "queue");



    Option MODE = Option.builder().option("m").longOpt("mode").hasArg(true).argName("mode").required(true).desc(
            "mode:\n" +
            "0: send messages synchronously\n" +
            "1: send messages asynchronously\n" +
            "2: consume messages synchronously\n" +
            "3: consume messages asynchronously\n" +
            "4: retry produce messages\n" +
            "5: retry consume messages"
    ).build();

    Option HELP = Option.builder().option("h").longOpt("help").hasArg(false).desc("get usage").build();

}
