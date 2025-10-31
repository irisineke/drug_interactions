# Drug Interactions

This project uses two drug inputs and two data files (`interactions.tsv` and `drugs.tsv`) to perform an assessment. It evaluates whether two drugs can be safely combined based on known interactions. **Note:** This tool does not provide medical advice but offers indicative support to identify potential risks at an early stage.

## 📁 Project Structure

```
src/main/java/nl/bioinf/
├── io/
│ ├── CombinationScoreEffect # Enumeration for drug interaction effects
│ ├── OutputGenerator # Handles writing output files (.txt / .pdf)
│ ├── ReadFiles # Reads and processes input .tsv data files
│ └── Validate # Validates file paths and input arguments
│
├── logic/
│ └── InteractionChecker # Core logic: compares drugs, finds overlaps, calculates results
│
├── models/
│ ├── ArgumentParser # Handles CLI input and program configuration
│ └── Main # Entry point of the application
```

Each class is responsible for a single, clear task:
- **ReadFiles** loads and processes the `.tsv` data.
- **InteractionChecker** performs the logic: overlaps, scores, and combination outcomes.
- **OutputGenerator** handles report generation (supports `.txt` and `.pdf`).
- **ArgumentParser** coordinates input and runs the analysis.
- **Data model classes** (`Drug`, `Interaction`, `Combination`, `GeneScore`) represent structured, immutable domain data.


## 🛠 Installation

### ✅ Requirements

* **Java 24 or higher** (Java 24+ recommended)
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

### ⚠️ Warning

If you are using a drug with a special charicter please use "" around the drugs

For example: 

"BRAF(V600E) Kinase Inhibitor RO5212054"

## 🧪 Testing (JUnit 5)

Tests focus on:
* File validation (fileNotEmptyCheck)
* CLI parsing (Picocli)
* Data reading (ReadFiles)
* Core logic (InteractionChecker)
* Output generation and exception handling (OutputGenerator)

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
