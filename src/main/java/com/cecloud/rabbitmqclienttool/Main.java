package com.cecloud.rabbitmqclienttool;

import com.cecloud.rabbitmqclienttool.task.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;

public class Main {

    public static void main(String[] args) throws Exception {
        Opts.OPTIONS.addOption(Opts.HELP);
        Opts.OPTIONS.addOption(Opts.MODE);
        CommandLine commandLine = null;
        try {
            commandLine = Opts.PARSER.parse(Opts.OPTIONS, args, true);
        } catch (MissingOptionException exp) {
            System.out.println(exp.getMessage());
            System.exit(0);
        }
        switch (commandLine.getOptionValue("mode")) {
            case "0":
                new Thread(new SendSyncMessages(args)).start();
                break;
            case "1":
                new Thread(new SendAsyncMessages(args)).start();
                break;
            case "2":
                new Thread(new SyncGetMessages(args)).start();
                break;
            case "3":
                new Thread(new AsyncGetMessages(args)).start();
                break;
            case "4":
                new Thread(new RetrySendMessages(args)).start();
                break;
            case "5":
                new Thread(new RetryGetMessages(args)).start();
                break;
            
            default:
        }
    }
}
