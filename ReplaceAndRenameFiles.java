import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class ReplaceAndRenameFiles {

	public static void main(String[] args) {
		// TODO Add all the words you want to replace here. Case-sensitive
		Map<String, String> replacements = new HashMap<>();
		replacements.put("oldWord", "newWord");
		replacements.put("oldWord1", "newWord1");

		String directoryPath = "./"; // TODO Replace with the directory you want to start from otherwise it will start from the current directory
		processFiles(new File(directoryPath), replacements);
	}

	private static void processFiles(File folder, Map<String, String> replacements) {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				processFiles(fileEntry, replacements);
			} else {
				if (!fileEntry.getName().equals("ReplaceAndRenameFiles.java")) { // Skip this file
					replaceInFile(fileEntry, replacements);
				}
			}

			// Check if the file or folder name needs to be changed
			renameFileOrFolder(fileEntry, replacements);
		}
	}

	private static void replaceInFile(File file, Map<String, String> replacements) {
		Path path = Paths.get(file.getPath());
		try {
			String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
			String newContent = content;

			for (Map.Entry<String, String> entry : replacements.entrySet()) {
				newContent = newContent.replaceAll(entry.getKey(), entry.getValue());
			}

			if (!content.equals(newContent)) { // Write only if there are changes
				Files.write(path, newContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
				System.out.println("File content changed: " + path);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void renameFileOrFolder(File fileOrFolder, Map<String, String> replacements) {
		String name = fileOrFolder.getName();
		if (name.equals("ReplaceAndRenameFiles.java")) { // Skip this file
			return;
		}

		String newName = name;

		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			newName = newName.replaceAll(entry.getKey(), entry.getValue());
		}

		if (!name.equals(newName)) { // Rename only if there are changes
			System.out.println("Renamed: " + name + " -> " + newName);
			File newFile = new File(fileOrFolder.getParent(), newName);
			if (!fileOrFolder.renameTo(newFile)) {
				System.out.println("Failed to rename: " + fileOrFolder.getAbsolutePath());
			}
		}
	}
  
}
