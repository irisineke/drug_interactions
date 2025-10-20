# Drug Interactions

This project uses two drug inputs and two data files (`interactions.tsv` and `drugs.tsv`) to perform an assessment. It evaluates whether two drugs can be safely combined based on known interactions. **Note:** This tool does not provide medical advice but offers indicative support to identify potential risks at an early stage.

## 📁 Project Structure

```
src/main/java/nl/bioinf/
│
├── io/
│   ├── ReadFiles          # Reads and processes input files
│   ├── OutputGenerator    # Generates output based on analysis
│
├── logic/
│   └── InteractionChecker # Extracts interaction data and determines outcomes
│
├── methods/
│   └── (Data Models)      # Custom data types: Drug, Interaction, Combination
│
├── ArgumentParser         # Handles command-line input via Picocli
└── Main                    # Entry point of the application
```

## 🛠 Installation

### ✅ Requirements

* **Java 17 or higher** (Java 21+ recommended)
* Access to drug interaction data from **DGIdb** or **DrugBank**

### 1️⃣ Download Data

Get `.tsv` data files from: [https://dgidb.org/downloads](https://dgidb.org/downloads)

### 2️⃣ Clone Repository

```bash
git clone git@github.com:irisineke/drug_interactions.git
cd drug_interactions
```

## ▶️ How to Run

Build the JAR (if using Gradle or Maven), then run:

```bash
java -jar build/libs/drug_interactions-1.0-SNAPSHOT-all.jar \
  -intF Path/To/interactions.tsv \
  -drF Path/To/drugs.tsv \
  -d1 First_Drug_Name \
  -d2 Second_Drug_Name \
  -o Path/To/output.txt
```

### 💻 Example

```bash
java -jar build/libs/drug_interactions-1.0-SNAPSHOT-all.jar \
  -intF data/interactions.tsv \
  -drF data/drugs.tsv \
  -d1 clonidine \
  -d2 Compro \
  -o results/output.txt
```

## 🧪 Testing (JUnit 5)

Tests focus on:

* File validation (`fileNotEmptyCheck`)
* CLI argument parsing (Picocli)
* Data reading (ReadFiles)
* Interaction logic (InteractionChecker)

Run tests with:

```bash
./gradlew test
```

## ⚠️ Disclaimer

This tool **does not provide medical advice**. It is intended for research and educational purposes only.

## 📄 License

This project is licensed under the MIT License.

---

Feel free to contribute or open issues for improvements! 🚀
