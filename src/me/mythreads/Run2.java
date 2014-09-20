package me.mythreads;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of second thread.
 */
class Run2 implements Runnable {

    /**
     * Regular expression we are looking for in files
     */
    String regex;
    /**
     * Synchronized collection in which files to process stored.
     */
    Queue<File> queueToProcess;
    /**
     * This variable allows us pausing thread
     */
    boolean work;

    /**
     * Pause thread
     */
    public void pause() {
        work = false;
    }

    /**
     * Resumes thread
     */
    public void resume() {
        work = true;
        run();
    }

    /**
     * Constructs new instance of this thread with given regular expression and queue with files to process.
     *
     * @param regex          expression for which we will look in files
     * @param queueToProcess collection of files which we will process
     */
    public Run2(String regex, Queue<File> queueToProcess) {
        this.regex = regex;
        this.queueToProcess = queueToProcess;
    }

    /**
     * Processing of one file? scanning it for regex
     *
     * @throws IOException
     */
    private void work() throws IOException {
        Pattern pattern = Pattern.compile(regex);

        File cFile = queueToProcess.remove();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(cFile));

        String line;
        int count = 0;
        while ((line = bufferedReader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find())
                count++;
        }

        System.out.println("Matches in " + cFile.getName() + ": " + count);
    }

    /**
     * If queue isn't empty we will scan first file fow regex, else we will periodically(every 10000 mls) check queue's capacity
     */
    @Override
    public void run() {
        this.work = true;
        do {
            if (this.work) {
                while (!queueToProcess.isEmpty()) {
                    try {
                        work();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                while (queueToProcess.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        } while (true);
    }
}