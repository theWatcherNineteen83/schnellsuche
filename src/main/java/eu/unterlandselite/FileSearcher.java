package eu.unterlandselite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class FileSearcher {

	private final JLabel statusLabel;

	public FileSearcher(JLabel statusLabel) {
		this.statusLabel = statusLabel;
	}

	public List<File> searchByName(File directory, String filenamePattern) {
		ForkJoinPool pool = new ForkJoinPool();
		FileSearchTask task = new FileSearchTask(directory, filenamePattern);
		return pool.invoke(task); // Startet die parallele Suche
	}

	private class FileSearchTask extends RecursiveTask<List<File>> {
		private final File directory;
		private final String filenamePattern;

		public FileSearchTask(File directory, String filenamePattern) {
			this.directory = directory;
			this.filenamePattern = filenamePattern;
		}

		@Override
		protected List<File> compute() {
			List<File> matchedFiles = new ArrayList<>();
			List<FileSearchTask> subTasks = new ArrayList<>();

			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						updateStatus("Durchsuche: " + file.getAbsolutePath());
						FileSearchTask task = new FileSearchTask(file, filenamePattern);
						task.fork();
						subTasks.add(task);
					} else if (matchesPattern(file.getName(), filenamePattern)) {
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

	private boolean matchesPattern(String filename, String pattern) {
		String regex = pattern.replace(".", "\\.").replace("*", ".*");
		return filename.matches("(?i)" + regex);
	}

	private void updateStatus(String status) {
		SwingUtilities.invokeLater(() -> statusLabel.setText(status));
	}
}
