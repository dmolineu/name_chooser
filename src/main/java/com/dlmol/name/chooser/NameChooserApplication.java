package com.dlmol.name.chooser;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class NameChooserApplication {
	public static final String ONE = "1";
	public static final String TWO = "2";

	private static final DateTimeFormatter FORMAT_TIMESTAMP =
			DateTimeFormat.forPattern("yyyy-MM-dd'T'HH-mm-ss");

	public static void main(String[] args) {
		if (args == null || args.length < 3) {
			System.out.println("Invalid input! Two arguments are expected, " +
					"the first is a text file containing a name on each line, " +
					"the second is the reject threshold, " +
					"the optional third argument is the user's name.");
			return;
		}
		File nameFile = new File(args[0]);
		if (!nameFile.isFile() || nameFile.length() == 0) {
			System.out.println("File \"" + nameFile.getAbsolutePath() + " does not exist or is empty!");
			return;
		}
		int rejectThreshold;
		try {
			rejectThreshold = Integer.valueOf(args[1]);
		} catch (Exception e) {
			System.out.println("\"" + args[1] + "\" is not a valid integer.");
			return;
		}

		List<Name> names;
		try {
			names = getNames(nameFile);
		} catch (IOException e) {
			System.out.println("Error! Unable to read lines from file: " + nameFile.getAbsolutePath());
			e.printStackTrace();
			return;
		}

		String userName;
		if (args.length >= 3 && args[2].length() > 0)
			userName = args[2];
		else
			try {
				userName = getUserName();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
//        userName = cleanName(userName);

		final NameResults results = chooseName(names, rejectThreshold);
		writeOutput(userName, results);
		System.out.println("\nYour chosen name is: \"" + results.getChosenName() + "\"!!!");
	}

//    private static String cleanName(String userName) {
//        return userName.replaceAll(".", "");
//    }

	public static NameResults chooseName(List<Name> names, int rejectThreshold) {
		NameResults results = new NameResults();
		results.setNames(names);

		int choiceCount = 0;
		while (containsNamesUnderRejectThreshold(names, rejectThreshold))
			pickName(results, rejectThreshold, ++choiceCount);

		return results;
	}

	private static List<Name> getNames(File nameFile) throws IOException {
		List<String> fileData = Files.readAllLines(Paths.get(nameFile.getAbsolutePath()));
		List<Name> names = new ArrayList<>(fileData.size());
		for (String item : fileData)
			names.add(new Name(item));
		return names;
	}

	private static String getUserName() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("What is YOUR name?");
		String name = reader.readLine();
		return name;
	}

	private static void writeOutput(String userName, NameResults data) {
		DecimalFormat df = new DecimalFormat("#.#");
		List<Name> names = data.getNames();
		Collections.sort(names);
		data.addResult("\n*** Choice Detail: ***");
		for (Name name : names)
			data.addResult(
					df.format(name.getAcceptPercent() * 100) + "%:\t" + name.getName() +
					" (rejected " + name.getRejectCount() + " times (" +
					df.format(name.getRejectPercent() * 100) + "%), chosen )" + name.getAcceptCount() + " times (" +
					df.format(name.getAcceptPercent() * 100) + "%))"
			);
		data.addResult("\n");
		data.addResult(userName + " chose the name \"" + data.getChosenName() + "\"!");
		String ts = FORMAT_TIMESTAMP.print(System.currentTimeMillis());
		File results = new File("results");
		if (!results.exists() && !results.isDirectory())
			results.mkdir();
		String outfileName = "results/NameChooser_" + userName + "_" + ts + ".txt";
		try {
			Files.write(Paths.get(outfileName), data.getResultDetails().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to write output to file!");
		}
		System.out.println("\nDetailed results written to: " + outfileName);
	}

	private static boolean containsNamesUnderRejectThreshold(List<Name> names, int rejectThreshold) {
		return names.stream()
				.filter(n -> n.getRejectCount() < rejectThreshold)
				.count() > 1;
	}

	public static Name pickName(NameResults results, int rejectThreshold, int choiceCount) {
		List<Integer> eligibleIndices = getEligibleIndices(results.getNames(), rejectThreshold);
		checkEligibleIndices(eligibleIndices);
		final int firstEligibleIndex = getRand(0, eligibleIndices.size());
		int nameFirstIndex = eligibleIndices.get(firstEligibleIndex);
		eligibleIndices.remove(firstEligibleIndex);
		Name firstName = results.getNames().get(nameFirstIndex);
//        System.out.println("Choose first index '" + nameFirstIndex + "': " + firstName.toString());
		final int secondEligibleIndex = getRand(0, eligibleIndices.size());
		int nameSecondIndex = eligibleIndices.get(secondEligibleIndex);
		eligibleIndices.remove(secondEligibleIndex);
		Name secondName = results.getNames().get(nameSecondIndex);
//        System.out.println("Choose second index '" + nameSecondIndex + "': " + secondName.toString());

		if (isFirstChosen(firstName, secondName)) {
			results.addResult(choiceCount + ":\t" + firstName + " chosen, " + secondName + " rejected.");
			firstName.incrementAcceptCount();
			secondName.incrementRejectCount();
			return firstName;
		} else {
			results.addResult(choiceCount + ":\t" + secondName + " chosen, " + firstName + " rejected.");
			firstName.incrementRejectCount();
			secondName.incrementAcceptCount();
			return secondName;
		}
	}

	private static void checkEligibleIndices(List<Integer> eligibleIndices) {
		if (eligibleIndices.size() < 2) {
			System.out.println("ERROR: eligibleIndices size is: " + eligibleIndices.size() +
			"\n\tContents: " + StringUtils.join(eligibleIndices, ", "));
		}
	}

	public static List<Integer> getEligibleIndices(List<Name> names, int rejectThreshold) {
		List<Integer> eligibleIndices = new ArrayList<>(names.size());
		if (CollectionUtils.isEmpty(names)) {
			System.out.println("getEligibleIndices(): 'names' param is empty!");
			return eligibleIndices;
		}

		List<String> targetNames = names.stream()
				.filter(n -> n.getRejectCount() < rejectThreshold)
				.sorted(Comparator.comparing(Name::getRejectCount).reversed())
				.sorted(Comparator.comparing(Name::getTotalCount))
				.map(Name::getName)
				.limit(2)
				.collect(Collectors.toList());

		for (int i = 0; i < names.size(); i++) {
			final Name name = names.get(i);
			if (targetNames.contains(name.getName()))
				eligibleIndices.add(i);
		}
		return eligibleIndices;
	}

	private static long getCountOfNamesUnderTotalChoiceCount(List<Name> names, int totalChoiceCount) {
		return names.stream()
				.filter(n -> n.getTotalCount() <= totalChoiceCount)
				.count();
	}

	private static boolean isFirstChosen(Name firstName, Name secondName) {
		System.out.print("\nChoose:\n\t1: " + firstName + "\n\t2: " + secondName + "\nSelection: ");
		if (false) //bypass user input
			return true;
		else {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String input = null;
			do {
				try {
					input = reader.readLine();
				} catch (IOException e) {
				}
				if (!input.equalsIgnoreCase(ONE) && !input.equalsIgnoreCase(TWO))
					System.out.print("Error! Invalid input, must be \"1\" or \"2\".\nTry again: ");
			} while (input == null || (!input.equalsIgnoreCase(ONE) && !input.equalsIgnoreCase(TWO)));

			return input.equalsIgnoreCase(ONE);
		}
	}

	/**
	 * @param min
	 * @param max
	 * @return A random int from @min up to, but not including @max
	 */
	public static int getRand(int min, int max) {
		final int rnd = (int) (Math.random() * max) + min;
//        System.out.println("For min = " + min + " and max = " + max + ", getRand is returning: " + rnd);
		return rnd;
	}

}
