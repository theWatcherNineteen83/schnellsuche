package fastSearch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSearcher {

    public List<File> searchByName(File directory, String filenamePattern) {
        List<File> result = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.addAll(searchByName(file, filenamePattern));
                } else if (matchesPattern(file.getName(), filenamePattern)) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    private boolean matchesPattern(String filename, String pattern) {
        // Konvertiert das Pattern von "*.iso" zu einem regulären Ausdruck: ".*\\.iso"
        String regex = pattern.replace(".", "\\.").replace("*", ".*");
        return filename.matches(regex);
    }
}
