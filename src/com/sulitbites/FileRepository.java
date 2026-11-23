package com.sulitbites;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Keeps everything in one JSON file (data/db.json). */
public final class FileRepository implements Repository {
    private final Path file;
    public FileRepository(Path file) { this.file = file; }
    @Override public void load(Store store) {
        try {
            if (Files.exists(file)) Codec.fill(store, Json.parse(Files.readString(file, StandardCharsets.UTF_8)));
        } catch (IOException | RuntimeException e) { System.err.println("Could not read " + file + ": " + e.getMessage()); }
    }
    @Override public void save(Store store) {
        try {
            Path dir = file.toAbsolutePath().getParent(); if (dir != null) Files.createDirectories(dir);
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, Json.write(Codec.tree(store)), StandardCharsets.UTF_8);
            try { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (IOException e) { Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { System.err.println("Could not save " + file + ": " + e.getMessage()); }
    }
}
