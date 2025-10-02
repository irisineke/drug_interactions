package nl.bioinf;

public class Main {


    public static void main(String[] args) {
        int exitCode = new picocli.CommandLine(new ArgumentParser()).execute(args);

        OutputGenerator generator = new OutputGenerator();
        InteractionChecker checker = new InteractionChecker();


        System.exit(exitCode);
    }
}


