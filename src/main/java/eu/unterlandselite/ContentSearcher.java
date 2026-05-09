package eu.unterlandselite;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ContentSearcher {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB Limit

    private final ExecutorService executor;

    public ContentSearcher(int maxThreads) {
        this.executor = Executors.newFixedThreadPool(maxThreads);
    }

    public List<File> searchInFiles(List<File> files, String searchTerm, boolean caseSensitive)
            throws InterruptedException, ExecutionException {
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
        // Skip files larger than the limit
        if (file.length() > MAX_FILE_SIZE) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            String term = caseSensitive ? searchTerm : searchTerm.toLowerCase();
            while ((line = reader.readLine()) != null) {
                String haystack = caseSensitive ? line : line.toLowerCase();
                if (haystack.contains(term)) {
                    return file;
                }
            }
        } catch (IOException e) {
            // Skip unreadable files (binary, permission denied, etc.)
        }
        return null;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
