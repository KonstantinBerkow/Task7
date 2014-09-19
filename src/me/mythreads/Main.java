package me.mythreads;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class to test two threads.
 */
public class Main {

    /**
     * Enum of commands to control threads
     */
    enum Command {
        END, STOP1, STOP2, RESUME1, RESUME2
    }

    /**
     * Place for tests. Due to problems with mask input through terminal/console, there is a dialogue.
     *
     * @param args No arguments yet
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Input destination folder:");
        File dir = new File(bufferedReader.readLine());
        System.out.println("Input mask:");
        String mask = bufferedReader.readLine();
        System.out.println("Input regular expression to search for:");
        String regex = bufferedReader.readLine();
        File[] files = dir.listFiles();
        Queue<File> mainQ = new ConcurrentLinkedQueue<File>();
        Run1 R1 = new Run1(mask, dir, mainQ);
        Run2 R2 = new Run2(regex, mainQ);
        Thread t1 = new Thread(R1, "FirstThread");
        Thread t2 = new Thread(R2, "Second Thread");
        t1.start();
        t2.start();
        M:
        while (true) {
            switch (Command.valueOf(bufferedReader.readLine())) {
                case END:
                    R1.stop();
                    R2.stop();
                    t1.interrupt();
                    t2.interrupt();
                    break M;
                case STOP1:
                    R1.stop();
                    break;
                case STOP2:
                    R2.stop();
                    break;
                case RESUME1:
                    R1.resume();
                    break;
                case RESUME2:
                    R2.resume();
                    break;
            }
        }
    }
}
