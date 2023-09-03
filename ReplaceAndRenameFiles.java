package com.vroomview.datareports;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ReplaceAndRenameFiles {

	private final static Map<String, String> REPLACEMENTS = new HashMap<>();
	private final static String THIS_CLASS_NAME = ReplaceAndRenameFiles.class.getSimpleName() + ".java";
	private final static String DIRECTORY_PATH;
	private final static Set<String> IGNORED_FOLDERS = new HashSet<>();
	private final static Set<String> IGNORED_PREFIXES = new HashSet<>();
	private final static List<String> VALID_EXTENSIONS;
	private static final AtomicInteger changesCount = new AtomicInteger(0);

	//-------------------------------------------------------------------------
	// Configuration

	static {
		// TODO 1/5 Replace with the directory you want to start from.
		//  By default it's the current directory
		DIRECTORY_PATH = "./";


		// TODO 2/5 Add all the words you want to replace here. Case-sensitive
		REPLACEMENTS.put("oldCharacters", "newCharacters");
		REPLACEMENTS.put("oldCharacters", "newCharacters");


		// TODO 3/5 Add all the extensions you want to process here
		VALID_EXTENSIONS = Arrays.asList(
				".java",
				".yml",
				".json",
				".js",
				".xml",
				".html",
				".md");


		// TODO 4/5  Ignored folder names
		IGNORED_FOLDERS.add("target");
		IGNORED_FOLDERS.add("bin");


		// TODO 5/5 Ignored file/folder prefixes
		IGNORED_PREFIXES.add(".");
	}

	//-------------------------------------------------------------------------

	public static void main(String[] args) {
		processFiles(new File(DIRECTORY_PATH));
		System.out.println("Changes made: " + changesCount);
	}

	private static void processFiles(File folder) {
		File[] files = folder.listFiles();
		if (files == null) return;

		Stream.of(files).forEach(fileEntry -> {
			if (fileEntry.isDirectory()) {
				if (shouldProcessFolder(fileEntry)) {
					processFiles(fileEntry);
					renameFileOrFolder(fileEntry);
				}
			} else if (shouldProcessFile(fileEntry)) {
				replaceInFile(fileEntry);
				renameFileOrFolder(fileEntry);
			}
		});
	}

	private static boolean shouldProcessFolder(File folder) {
		String folderName = folder.getName();
		return IGNORED_PREFIXES.stream().noneMatch(folderName::startsWith) &&
				IGNORED_FOLDERS.stream().noneMatch(folderName::equals);
	}

	private static boolean shouldProcessFile(File file) {
		String fileName = file.getName();
		return IGNORED_PREFIXES.stream().noneMatch(fileName::startsWith) &&
				VALID_EXTENSIONS.stream().anyMatch(fileName::endsWith);
	}

	private static void replaceInFile(File file) {
		Path path = file.toPath();
		Path tempPath = Paths.get(file.getAbsolutePath() + ".temp");

		try (
				BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
				BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)
		) {
			String line;
			boolean changed = false;

			while ((line = reader.readLine()) != null) {
				StringBuilder builder = replaceInStringBuilder(new StringBuilder(line));

				if (!line.contentEquals(builder)) {
					changed = true;
				}

				writer.write(builder.toString());
				writer.newLine();
			}

			if (changed) {
				Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Content changed: \t" + path.getFileName() + "\t\t\t\t\t\t\t\t path: " + path);
				changesCount.incrementAndGet();
			} else {
				Files.delete(tempPath);  // Delete the temporary file if no change.
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void renameFileOrFolder(File fileOrFolder) {
		String name = fileOrFolder.getName();
		if (THIS_CLASS_NAME.equals(name)) {
			return;
		}

		StringBuilder newNameBuilder = replaceInStringBuilder(new StringBuilder(name));

		String newName = newNameBuilder.toString();

		if (!name.equals(newName)) {
			System.out.println("Renamed: \t\t\t" + name + " \t-> " + newName);
			File newFile = new File(fileOrFolder.getParent(), newName);
			changesCount.incrementAndGet();
			if (!fileOrFolder.renameTo(newFile)) {
				System.err.println("Failed to rename: " + fileOrFolder.getAbsolutePath());
				changesCount.decrementAndGet();
			}
		}
	}

	private static StringBuilder replaceInStringBuilder(StringBuilder builder) {
		for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			int index;
			while ((index = builder.indexOf(key)) != -1) {
				builder.replace(index, index + key.length(), value);
			}
		}
		return builder;
	}

}
