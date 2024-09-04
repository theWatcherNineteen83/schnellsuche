package eu.unterlandselite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ContentSearcher {

    private final ExecutorService executor;

    public ContentSearcher(int maxThreads) {
        this.executor = Executors.newFixedThreadPool(maxThreads);
    }

    public List<File> searchInFiles(List<File> files, String searchTerm, boolean caseSensitive) throws InterruptedException, ExecutionException {
        List<Future<File>> futures = new ArrayList<>();
        List<File> resultFiles = new ArrayList<>();

        for (File file : files) {
            futures.add(executor.submit(() -> searchInFile(file, searchTerm, caseSensitive)));
        }

        for (Future<File> future : futures) {
            File result = future.get();
            if (result != null) {
                resultFiles.add(result);
            }
        }

        return resultFiles;
    }

    private File searchInFile(File file, String searchTerm, boolean caseSensitive) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (caseSensitive ? line.contains(searchTerm) : line.toLowerCase().contains(searchTerm.toLowerCase())) {
                    return file;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
