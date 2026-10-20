package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for parsing file responses from LLM agents.
 * Handles multiple formats of file/code output.
 */
public class FileParser {
    private static final Logger logger = LoggerFactory.getLogger(FileParser.class);

    /**
     * Represents a file with its path and content.
     */
    public record FileEntry(String path, String code) {
        public FileEntry {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Path cannot be null or empty");
            }
            if (code == null) {
                throw new IllegalArgumentException("Code cannot be null");
            }
        }
    }

    // Pattern 1: path/filename\n```code```
    private static final Pattern BLOCK_PATTERN = Pattern.compile(
            "([\\w\\./\\-]+)\\s*\\n\\s*```\\w*\\s*\\n(.*?)```",
            Pattern.DOTALL
    );

    // Pattern 2: path/filename: code (inline)
    private static final Pattern INLINE_PATTERN = Pattern.compile(
            "([\\w\\./\\-]+):\\s*(.+)",
            Pattern.DOTALL
    );

    /**
     * Parse a response string and extract file entries.
     * Handles three formats:
     * 1. filename\n```code```
     * 2. filename: code
     * 3. filename on a line, then code block
     *
     * @param response the LLM response to parse
     * @return list of file entries
     */
    public List<FileEntry> parseResponse(String response) {
        List<FileEntry> entries = new ArrayList<>();

        if (response == null || response.isEmpty()) {
            logger.warn("Empty response to parse");
            return entries;
        }

        logger.debug("Parsing response: {} chars", response.length());

        // Try block pattern first (filename followed by code block)
        Matcher blockMatcher = BLOCK_PATTERN.matcher(response);
        while (blockMatcher.find()) {
            String path = blockMatcher.group(1).trim();
            String code = blockMatcher.group(2).trim();
            
            // Clean up path (remove leading ./ if present)
            if (path.startsWith("./")) {
                path = path.substring(2);
            }

            entries.add(new FileEntry(path, code));
            logger.debug("Parsed file: {} ({} chars)", path, code.length());
        }

        // If no block pattern found, try to parse as single file
        if (entries.isEmpty()) {
            // Check if there's a filename at the start
            String[] lines = response.split("\n", 2);
            if (lines.length >= 2) {
                String firstLine = lines[0].trim();
                // Check if first line looks like a file path
                if (firstLine.matches(".*[\\/\\\\].*\\.[\\w]+") || 
                    firstLine.matches(".*\\.[\\w]+") ||
                    firstLine.toLowerCase().endsWith(".java") ||
                    firstLine.toLowerCase().endsWith(".xml") ||
                    firstLine.toLowerCase().endsWith(".properties") ||
                    firstLine.toLowerCase().endsWith(".css") ||
                    firstLine.toLowerCase().endsWith(".fxml") ||
                    firstLine.toLowerCase().endsWith(".txt") ||
                    firstLine.toLowerCase().endsWith(".md")) {
                    
                    String code = lines[1].trim();
                    // Remove code block markers if present
                    if (code.startsWith("```")) {
                        code = code.substring(3);
                    }
                    if (code.endsWith("```")) {
                        code = code.substring(0, code.length() - 3);
                    }
                    
                    entries.add(new FileEntry(firstLine, code));
                    logger.debug("Parsed inline file: {} ({} chars)", firstLine, code.length());
                }
            }
        }

        logger.info("Parsed {} file entries from response", entries.size());
        return entries;
    }
}
