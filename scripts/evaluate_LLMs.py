# === 1. Imports and Configuration ===
import os
import json
import time
import requests
from dotenv import load_dotenv
from tqdm import tqdm

# OpenAI
from openai import OpenAI
# Gemini
import google.generativeai as genai

load_dotenv()
client = OpenAI()
genai.configure(api_key=os.getenv("GEMINI_API_KEY"))
GEMINI_MODEL = genai.GenerativeModel("gemini-2.5-pro")

# === 2. File Paths ===
INPUT_FILE_EL = "../output_StructuralReasoner/benchmark_output.json"
OUTPUT_FILE_EL = "../results_{model}_el.json"

INPUT_FILE_ELH = "../output_StructuralReasoner/benchmark_output_with_elh.json"
OUTPUT_FILE_ELH = "../results_{model}_elh.json"

# === 3. Prompt Builder ===
def build_prompt(entry):
    axioms = entry["axioms"]
    axioms_text = axioms if isinstance(axioms, str) else "\n".join(f"- {ax['symbolic']}" for ax in axioms)

    dl_profile = entry.get("dl_profile", "unknown")
    if dl_profile == "EL":
        description = "Description Logic EL (simple axioms with no role hierarchy extensions or existential quantifiers)"
    elif dl_profile == "ELH":
        description = "Description Logic ELH (extended with role hierarchies and existential quantifiers)"
    else:
        description = "Description Logic (unknown profile)"

    return f"""Given the following logical axioms written in {description}:

{axioms_text}

Answer the following question as truthfully as possible:
{entry['query']}

Reply only with "Yes" or "No", and do not explain your answer."""

# === 4. Unified LLM Caller ===
def call_llm(prompt, model_name="gpt-4o", retries=3):
    for attempt in range(retries):
        try:
            if model_name.startswith("gpt"):
                response = client.chat.completions.create(
                    model=model_name,
                    messages=[{"role": "user", "content": prompt}],
                    temperature=0,
                )
                return response.choices[0].message.content.strip()

            elif model_name.startswith("gemini"):
                response = GEMINI_MODEL.generate_content(prompt)
                return response.text.strip() if response.text else "Error: Empty Gemini response"

            elif model_name in {"llama3:latest",
                                "gemma3:latest",
                                "deepseek-coder:6.7b-instruct",
                                "qwen:7b",
                                "mistral:latest"}:
                # Ollama API
                res = requests.post(
                    "http://localhost:11434/api/generate",
                    json={
                        "model": model_name,  # e.g., "llama3:70b"
                        "prompt": prompt,
                        "stream": False
                    }
                )
                if res.status_code != 200:
                    raise ValueError(f"Ollama error: {res.status_code} - {res.text}")
                return res.json()["response"].strip()

            else:
                raise ValueError(f"Unknown model: {model_name}")

        except Exception as e:
            print(f"[Retry {attempt + 1}/{retries}] Error from {model_name}: {e}")
            time.sleep(2 ** attempt)

    return "Error: LLM call failed after retries"

# === 5. Evaluator Function ===
def evaluate(input_file, output_file, model_name):
    with open(input_file, "r") as f:
        data = json.load(f)

    results = []
    for entry in tqdm(data, desc=f"Evaluating with {model_name}"):
        prompt = build_prompt(entry)
        answer = call_llm(prompt, model_name=model_name)

        expected = entry["expected"].lower().strip()
        predicted = answer.lower().strip()
        match = "Yes" if expected == predicted else "No"

        results.append({
            "id": entry["id"],
            "mr": entry.get("mr", ""),
            "dl_profile": entry.get("dl_profile", ""),
            "query": entry["query"],
            "expected": entry["expected"],
            "llm": model_name,
            "llm_answer": answer,
            "match": match,
        })

    with open(output_file, "w") as f:
        json.dump(results, f, indent=2)
    print(f"\n Saved results to {output_file}")

    # Summary
    total = len(results)
    correct = sum(1 for r in results if r["match"] == "Yes")
    accuracy = (correct / total) * 100 if total else 0
    print(f"\n {model_name} Accuracy: {correct}/{total} correct ({accuracy:.2f}%)")

    print("\n False Positives (Expected: No, Got: Yes):")
    for r in results:
        if r["expected"].lower() == "no" and r["llm_answer"].lower() == "yes":
            print(f"- ID: {r['id']} | Query: {r['query']}")

    print("\n False Negatives (Expected: Yes, Got: No):")
    for r in results:
        if r["expected"].lower() == "yes" and r["llm_answer"].lower() == "no":
            print(f"- ID: {r['id']} | Query: {r['query']}")

# === 6. Runner ===
if __name__ == "__main__":
    models = ["qwen:7b"]
    #"gpt-4o", "gemini-2.5-pro", "llama3:latest", "gemma3:latest", "deepseek-coder:6.7b-instruct", "qwen:7b", "mistral:latest"

    for model in models:
        print(f"\n Running {model} on EL")
        evaluate(INPUT_FILE_EL, OUTPUT_FILE_EL.format(model=model.replace(":", "_")), model)

        print(f"\n Running {model} on ELH")
        try:
            evaluate(INPUT_FILE_ELH, OUTPUT_FILE_ELH.format(model=model.replace(":", "_")), model)
        except FileNotFoundError:
            print(f"  Skipped ELH: '{INPUT_FILE_ELH}' not found.")