## Drug Interactions
This project uses two drug inputs and two file inputs (drug.tsv and interaction.tsv) and performs an assessment. The program then assesses whether these can be safely combined. The program does not offer binding medical advice, but rather indicative support to identify potential risks at an earlier stage.

### Structure
- io/Leesbestanden: reads the files and saves them temporaly
- io/OutputGenerator: combines InteractionChecker obtained data into output

- logic/InteractionChecker: extracts the necessary information from the data

- methods/Placeholder: creates custom data types from the columns in the data

- ArgumentParser: manages the command line input
- Main: runs everything


### Installation
#### Step 1:
Download the data from the [drugbank site](https://dgidb.org/downloads).

#### Step 2:
Clone the Repository: git@github.com:irisineke/drug_interactions.git.



### How to run the code:
java -jar build/libs/drug_interactions-1.0-SNAPSHOT-all.jar -intF Path/To/interactions.tsv -drF Path/To/drugs.tsv -d1 first_drug_of_choice -d2 second_drug_of_choice -o Path/To/Output

*Example with filled in drugs:*
java -jar build/libs/drug_interactions-1.0-SNAPSHOT-all.jar -intF Path/To/interactions.tsv -drF Path/To/drugs.tsv -d1 clonidine -d2 Compro -o Path/To/Output