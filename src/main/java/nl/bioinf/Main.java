package nl.bioinf;


public class Main {
    public static void main(String[] args) {
        int exitCode = new picocli.CommandLine(new ArgumentParser()).execute(args);
        System.exit(exitCode); // geef mee aan het systeem of het goed gaat (0 of 1)
//      System.exit(new CommandLine(new MyApp()).execute(args));
        }

    }


