package com.example.demo.agent;

import com.example.demo.llm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent with file system operations.
 * Can read, write files and directories.
 */
public class FileAgent extends BaseAgent {
    private static final Logger logger = LoggerFactory.getLogger(FileAgent.class);

    private final List<String> createdFiles = new ArrayList<>();
    private final List<String> buildErrors = new ArrayList<>();

    /**
     * Create a new FileAgent.
     */
    protected FileAgent(String name, String systemMessage, LlmClient llmClient, LlmConfig config) {
        super(name, systemMessage, llmClient, config);
    }

    /**
     * Write content to a file. Creates parent directories if needed.
     *
     * @param filepath  path to the file
     * @param content   content to write
     * @return true if successful
     */
    public boolean writeFile(String filepath, String content) {
        try {
            Path path = Paths.get(filepath);
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, content);
            createdFiles.add(filepath);
            logger.info("FileAgent: Wrote {} bytes to {}", content.length(), filepath);
            return true;
        } catch (IOException e) {
            logger.error("FileAgent: Error writing file {}: {}", filepath, e.getMessage());
            buildErrors.add("Error writing " + filepath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Read content from a file.
     *
     * @param filepath path to the file
     * @return file content, or null if not found
     */
    public String readFile(String filepath) {
        try {
            String content = Files.readString(Paths.get(filepath));
            logger.debug("FileAgent: Read {} bytes from {}", content.length(), filepath);
            return content;
        } catch (IOException e) {
            logger.error("FileAgent: Error reading file {}: {}", filepath, e.getMessage());
            return null;
        }
    }

    /**
     * Create a directory (and parent directories).
     *
     * @param dirpath path to the directory
     * @return true if successful
     */
    public boolean createDirectory(String dirpath) {
        try {
            Files.createDirectories(Paths.get(dirpath));
            logger.info("FileAgent: Created directory {}", dirpath);
            return true;
        } catch (IOException e) {
            logger.error("FileAgent: Error creating directory {}: {}", dirpath, e.getMessage());
            return false;
        }
    }

    /**
     * List files in a directory.
     *
     * @param dirpath path to the directory
     * @return list of file/directory names, or empty list on error
     */
    public List<String> listDirectory(String dirpath) {
        try {
            List<String> entries = Files.list(Paths.get(dirpath))
                    .map(p -> p.getFileName().toString())
                    .toList();
            logger.debug("FileAgent: Listed {} entries in {}", entries.size(), dirpath);
            return entries;
        } catch (IOException e) {
            logger.error("FileAgent: Error listing directory {}: {}", dirpath, e.getMessage());
            return List.of();
        }
    }

    /**
     * Get list of files created by this agent.
     */
    public List<String> getCreatedFiles() {
        return List.copyOf(createdFiles);
    }

    /**
     * Get list of build errors.
     */
    public List<String> getBuildErrors() {
        return List.copyOf(buildErrors);
    }
}
