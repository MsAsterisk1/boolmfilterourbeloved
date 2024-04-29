import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        maliciousPhishTest();
    }

    private static void maliciousPhishTest() {
        try {
            System.out.println("Test: matching phishing URLs");

            List<String[]> csvContents = readCSV("./malicious_phish.csv");
            csvContents.removeFirst();  // strip header

            List<String> malicious = new ArrayList<>();
            for (String[] csvLine : csvContents) {
                if (!csvLine[1].equals("benign")) {
                    malicious.add(csvLine[0]);
                }
            }

            BloomFilter<String> urlFilter = new BloomFilter<>(malicious.size(), 0.05);
            for (String url : malicious) {
                urlFilter.insert(url);
            }

            int falsePositives = 0;
            int falseNegatives = 0;

            for (String[] csvLine : csvContents) {
                boolean flaggedAsMalicious = urlFilter.contains(csvLine[0]);

                boolean isBenign = csvLine[1].equals("benign");

                if (flaggedAsMalicious && isBenign) {
                    falsePositives++;
                }

                if (!flaggedAsMalicious && !isBenign) {
                    falseNegatives++;
                }
            }

            System.out.printf("%d URLs processed%n", csvContents.size());
            System.out.println();
            System.out.println("Ground truth:");
            System.out.printf("    %d benign    %d malicious%n", csvContents.size() - malicious.size(), malicious.size());
            System.out.println("Bloom filter results:");
            System.out.printf("    %d benign    %d malicious%n", csvContents.size() - malicious.size() - falsePositives, malicious.size() + falsePositives);
            System.out.println();
            System.out.printf("False positive rate: %f%n", (double) falsePositives / (csvContents.size() - malicious.size()));
            System.out.printf("Expected FPR: %f%n", urlFilter.expectedFPR());
            System.out.printf("False negative rate: %f%n", (double) falseNegatives / (malicious.size() + falseNegatives));
            System.out.printf("Expected FNR: %f", 0.0);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String[]> readCSV(String path) throws FileNotFoundException {
        List<String[]> contents = new ArrayList<>();
        try (Scanner in = new Scanner(new File(path))) {
            while (in.hasNextLine()) {
                StringBuilder line = new StringBuilder(in.nextLine());
                while (!line.toString().contains(",")) {
                    line.append(in.nextLine());
                }
                contents.add(line.toString().split(","));
            }
        }
        return contents;
    }
}
