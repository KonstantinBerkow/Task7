package me.mythreads;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Queue;
import java.util.regex.Pattern;

/**
 * Implementation of first thread.
 */
class Run1 implements Runnable {

    /**
     * Mask of files we are looking for
     */
    String fileMask;
    /**
     * File object associated with folder in which we are searching files.
     */
    File workingDirectory;
    /**
     * Synchronized collection to store files which haven't been scanned yet.
     */
    Queue<File> queueToProcess;
    /**
     * Watcher to determine whether folder or it's content was modified.
     */
    WatchService watcher = FileSystems.getDefault().newWatchService();
    /**
     * Key to watcher provides information about modifications in folder
     */
    WatchKey keySignaler;
    /**
     * If we had scanned whole folder earlier there is no need to scan it again
     */
    boolean wasScanned = false;
    /**
     * This variable allows us pausing thread
     */
    boolean work = true;

    /**
     * Stops thread
     */
    public void stop() {
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
     * Constructor of new runnable implementation
     *
     * @param fileMask         Mask to search in folder for
     * @param workingDirectory Folder in which we are searching
     * @param queueToProcess   Collection in which will be chosen files added.
     * @throws IOException
     */
    public Run1(String fileMask, File workingDirectory, Queue<File> queueToProcess) throws IOException {
        this.queueToProcess = queueToProcess;
        this.fileMask = fileMask.replace(".", "\\.").replace("?", ".").replace("*", ".*?");
        if (!workingDirectory.isDirectory()) {
            throw new IOException("Not a directory!");
        }
        this.workingDirectory = workingDirectory;
        this.keySignaler = workingDirectory.toPath().register(
                watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);
    }

    /**
     * If current thread is running then this method will search for files which have such mask
     * and ad them to a queueToProcess, after iteration this method calls sleep for 10000 mls
     */
    @Override
    public void run() {
        while (work) {
            if (!wasScanned) {
                File[] files = workingDirectory.listFiles();
                if (files == null) {
                    wasScanned = true;
                    continue;
                }
                for (File f : files) {
                    if (Pattern.matches(fileMask, f.getName())) {
                        queueToProcess.add(f);
                        System.out.println(f.getAbsolutePath());
                    }
                }
                wasScanned = true;
            }

            for (WatchEvent<?> event : keySignaler.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.ENTRY_CREATE ||
                        kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    if (Pattern.matches(fileMask, filename.toString())) {
                        queueToProcess.add(filename.toFile());
                        System.out.println(filename.toFile().getAbsolutePath());
                    }
                }
            }
            keySignaler.reset();
            if (!work) return;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
