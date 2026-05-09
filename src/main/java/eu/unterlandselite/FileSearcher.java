package eu.unterlandselite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class FileSearcher {

    private static final int MAX_DIRECTORY_DEPTH = 100;
    private final JLabel statusLabel;
    private final ForkJoinPool pool;

    public FileSearcher(JLabel statusLabel) {
        this.statusLabel = statusLabel;
        this.pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }

    public List<File> searchByName(File directory, String filenamePattern) {
        String regex = filenamePattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        FileSearchTask task = new FileSearchTask(directory, pattern, 0);
        return pool.invoke(task);
    }

    private class FileSearchTask extends RecursiveTask<List<File>> {
        private final File directory;
        private final Pattern pattern;
        private final int depth;

        public FileSearchTask(File directory, Pattern pattern, int depth) {
            this.directory = directory;
            this.pattern = pattern;
            this.depth = depth;
        }

        @Override
        protected List<File> compute() {
            List<File> matchedFiles = new ArrayList<>();
            List<FileSearchTask> subTasks = new ArrayList<>();

            // Prevent infinite recursion via symlinks
            if (depth > MAX_DIRECTORY_DEPTH) {
                return matchedFiles;
            }

            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() && !isSymlink(file)) {
                        updateStatus("Durchsuche: " + file.getAbsolutePath());
                        FileSearchTask task = new FileSearchTask(file, pattern, depth + 1);
                        task.fork();
                        subTasks.add(task);
                    } else if (file.isFile() && pattern.matcher(file.getName()).matches()) {
                        matchedFiles.add(file);
                    }
                }
            }

            for (FileSearchTask task : subTasks) {
                matchedFiles.addAll(task.join());
            }

            return matchedFiles;
        }
    }

    private boolean isSymlink(File file) {
        try {
            return !file.getAbsolutePath().equals(file.getCanonicalPath());
        } catch (Exception e) {
            return true; // Treat as symlink on error
        }
    }

    private void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }
}
