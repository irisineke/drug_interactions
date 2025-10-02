package nl.bioinf;

public class Main {
    public static void main(String[] args) {
        int exitCode = new picocli.CommandLine(new ArgumentParser()).execute(args);
        System.exit(exitCode);
    }
}


