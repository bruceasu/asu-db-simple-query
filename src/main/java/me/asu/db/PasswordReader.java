package me.asu.db;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class PasswordReader {
    static final char LF = '\n';

    public static final String readPassword(String prompt) {
        final Console console = System.console();
        if (console == null) {
            return readFromStdin(prompt);
        } else {
            return readFromConsole(console, prompt);
        }
    }

    private static String readFromStdin(String prompt) {
        if (prompt != null) {
            System.out.printf(prompt);
        }
        CleanThread t = new CleanThread();
        t.setDaemon(true);
        t.start();
        try (InputStreamReader in = new InputStreamReader(System.in);
             BufferedReader reader = new BufferedReader(in)) {
            return reader.readLine();
        } catch (IOException e) {
            return "";
        } finally {
            t.cancel();
        }
    }

    private static String readFromConsole(Console console, String prompt) {
        return new String(console.readPassword(prompt));
    }

    private static class CleanThread extends Thread {
        volatile boolean running = false;

        @Override
        public void run() {
            running = true;
            while (running) {
                System.out.printf("\b");
                try {
                    Thread.sleep(5L);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println();
        }

        public void cancel() {
            this.running = false;
            this.interrupt();
        }
    }

}
