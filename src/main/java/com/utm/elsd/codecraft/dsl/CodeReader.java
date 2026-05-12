package com.utm.elsd.codecraft.dsl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CodeReader {
    private final String standardPath;

    public CodeReader(String standardPath) {
        this.standardPath = standardPath;
    }

    public String read(String filepath) {
        if (filepath == null) {
            throw new IllegalArgumentException("filepath must not be null");
        }

        String extension = extensionOf(filepath);
        if (!"codecraft".equalsIgnoreCase(extension)) {
            throw new IllegalArgumentException("Invalid file extension: " + extension + ". Expected .codecraft");
        }

        // Resolve the filepath: if absolute, use as-is; if relative, check in minecraft/codecraft directory
        Path resolvedPath = resolvePath(filepath);

        try {
            byte[] bytes = Files.readAllBytes(resolvedPath);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + resolvedPath, e);
        }
    }

    private Path resolvePath(String filepath) {
        Path path = Path.of(filepath);
        
        // If the path is absolute, return it as-is
        if (path.isAbsolute()) {
            return path;
        }
        
        // If relative, resolve it from the current working directory (which is .minecraft when mod runs)
        // Prepend 'codecraft/' to organize CodeCraft scripts in the minecraft directory
        return Path.of(standardPath).resolve(filepath);
    }

    private String extensionOf(String filepath) {
        int dot = filepath.lastIndexOf('.');
        if (dot < 0 || dot == filepath.length() - 1) {
            return "";
        }
        return filepath.substring(dot + 1);
    }

}

