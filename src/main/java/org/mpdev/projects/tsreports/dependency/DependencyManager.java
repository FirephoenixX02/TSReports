package org.mpdev.projects.tsreports.dependency;

import org.mpdev.projects.tsreports.TSReports;

import java.nio.file.Files;
import java.nio.file.Path;

public class DependencyManager {

    private final TSReports plugin = TSReports.getInstance();

    public Path downloadDependency(Dependency dependency, Path file) {
        // if the file already exists, don't attempt to re-download it.
        if (Files.exists(file)) {
            return file;
        }

        // attempt to download the dependency from each repo in order.
        for (Repository repo : Repository.values()) {
            try {
                plugin.getLogger().info("Downloading " + dependency.getFileName());
                repo.download(dependency, file);
                plugin.getLogger().info("Successfully downloaded: " + dependency.getFileName());
                if (dependency.equals(Dependency.PROTOCOLIZE_BUNGEECORD)){
                    plugin.getLogger().severe("Protocolize has been downloaded, please restart the server for it to load.");
                }
                return file;
            } catch (DependencyDownloadException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
