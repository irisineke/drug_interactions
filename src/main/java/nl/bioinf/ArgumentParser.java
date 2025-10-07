package nl.bioinf;

import picocli.CommandLine.*;
import java.io.File;
import java.util.List;
import java.util.Map;



// name, help en version erin, description
@Command (name = "argumentparser",
        mixinStandardHelpOptions = true,
        version = "argumentparser 1.0",
        description = "handels the argument input")


// Callable: Integer of wat anders teruggeven ?
public class ArgumentParser implements Runnable {
//    vangt input file op
    @Option(names = { "-intF", "--interactionsFile" },
            paramLabel = "interactionsFile", // zo heet hij in help
            description = "the input file: interactions.tsv", // ook in help
            required = true)
    File interactionsFile;

    @Option(names = { "-drF", "--drugsFile" },
            paramLabel = "drugsFile", // zo heet hij in help
            description = "the input file: drugs.tsv", // ook in help
            required = true)
    File drugsFile;

    @Option(names = {"-drug1", "-d1"},
            paramLabel = "firstDrugInput",
            description = "put the first drug you want to compare here",
            required = true)
    File firstDrugInput;

    @Option(names = {"-drug2", "-d2"},
            paramLabel = "secondDrugInput",
            description = "put the second drug you want to compare here",
            required = true)
    File secondDrugInput;


    @Override
    public void run() {

        try {
            // roept de fileNotEmptyCheck aan met de files als input
            fileNotEmptyCheck("Interactions file", interactionsFile.getAbsolutePath());
            fileNotEmptyCheck("Drugs file", drugsFile.getAbsolutePath());

            LeesBestanden lb = new LeesBestanden(interactionsFile,drugsFile);
            lb.process();

            Map<String, List<String>> data = lb.process();

            InteractionChecker checker = new InteractionChecker();
            checker.run(data.get("interactions"), data.get("drugs"));


        } catch (Exception e) {
            System.err.println("❌ FOUT: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }


    public void fileNotEmptyCheck(String name, String value){
        // verwijder deze regel niet nog een keer per ongeluk pls !!!
        File file = new File(value);

        // checken of opgegeven file bestaat
        if (!file.exists()) {
            throw new IllegalArgumentException("❌ FOUT: " + name + " does not exist :(");
        }
        // checken of opgegeven file daadwerklijk een file is (super handig) (ipv directory bv)
        if (!file.isFile()){
            throw new IllegalArgumentException("❌ FOUT: " + name + " is not a file :(");
        }
        // checken of file niet leeg is
            // ! moest dit nou met == of .equals ?
        if (file.length() == 0) {
            throw new IllegalArgumentException("❌ FOUT: " + name + " is empty :(");
        }
    }
}
// maakt commandline object van de class argumentparser
//    public static void main(String[] args) {
//        int exitCode = new picocli.CommandLine(new ArgumentParser()).execute(args);
//        System.exit(exitCode); // geef mee aan het systeem of het goed gaat (0 of 1)
//          System.exit(new CommandLine(new MyApp()).execute(args));
//    }



// runnen: ./gradlew run --args='-intF data/raw/interactions.tsv -drF data/raw/drugs.tsv -d1 a -d2 b'
// ./gradlew run --args='-intF /home/Jonkerjas/Downloads/interactions.tsv  -drF /home/Jonkerjas/Downloads/drugs.tsv -d1 a -d2 b'