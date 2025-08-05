# Description Logic Reasoning Benchmark

This framework benchmarks the reasoning ability of large language models (LLMs) using Description Logic (DL) axioms. It follows a pipeline that extracts EL/ELH axioms from an OWL ontology, generates natural language queries, and evaluates model predictions against formal reasoning results.

## Supported DL Profiles
- **EL** (Basic Description Logic fragment)
- **ELH** (EL with Role Hierarchy)

## Workflow Steps

### 1. Extract EL-Only Ontology
Run: `extract_EL.ipynb`

This notebook extracts EL-profile axioms from a full ontology, in this case, Pizza Ontology, and saves them to:

`src/main/resources/ontology/pizza-el.owl`

---

### 2. Generate EL Benchmark

Compile and run: `BenchmarkRunner.java`
 
**What this does:**
- Reads `pizza-el.owl`
- Groups axioms by subject using `AxiomGrouper.java`
- Generates natural language queries using `QueryGenerator.java`
- Validates entailment using `ReasoningValidator.java`
- Saves the result as: `benchmark_output.json`

---

### 3. Extend to ELH Profile

Compile and run: `ELHExtender.java`

 **What this does:**
- Reads `benchmark_output.json`
- Adds role inclusion axioms (e.g., `r ⊑ superOfR`)
- Changes DL profile to ELH when applicable
- Outputs: `benchmark_output_with_elh.json`

---

### 4. Evaluate Large Language Models (LLMs)

Run: `evaluate_LLMs.py`

 **What this does:**
- Sends natural language queries from both EL and ELH benchmark files to selected LLMs
- Compares their answers to the ground-truth entailment result (`Yes` / `No`)
- Models evaluated include:
    - `gpt-4o`
    - `gemini-2.5-pro`
    - `llama3:latest`
    - `gemma3:latest`

---

##  Output Files

| File Name                     | Description                               |
|------------------------------|-------------------------------------------|
| `pizza-el.owl`               | OWL ontology (EL profile only)            |
| `benchmark_output.json`      | EL benchmark queries + answers (MR-0)     |
| `benchmark_output_with_elh.json` | ELH-extended version (includes role hierarchy) |
| `evaluate_LLMs.py` results   | Comparison between LLM predictions and DL reasoning |

---

---

## Requirements

- Java 17+ with OWL API library
- Python 3.8+ with OpenAI, Google Gemini, or Hugging Face LLM APIs
- Jupyter Notebook or Google Colaboratory (Colab) for `.ipynb` execution

---

