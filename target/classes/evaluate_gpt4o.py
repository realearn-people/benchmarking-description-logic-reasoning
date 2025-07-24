import os
import json
import time
import openai
from dotenv import load_dotenv
from tqdm import tqdm

load_dotenv()
openai.api_key = os.getenv("OPENAI_API_KEY")

# Constants for EL
INPUT_FILE_EL = "../benchmark_output.json"  # Use this for EL
OUTPUT_FILE_EL = "../llm_eval_results.json"  # Output for EL

# Constants for ELH
INPUT_FILE_ELH = "../benchmark_output_with_elh.json"  # Use this for ELH
OUTPUT_FILE_ELH = "../llm_eval_results_with_elh.json"  # Output for ELH

MODEL = "gpt-4o"

def build_prompt(entry):
    """Generate the prompt for the LLM based on the axioms and the query"""
    axioms = entry["axioms"]

    if isinstance(axioms, str):
        axioms_text = axioms  # fallback (won't be parsed)
    else:
        axioms_text = "\n".join(f"- {ax['symbolic']}" for ax in axioms)

    dl_profile = entry["dl_profile"]

    # Customizing the prompt based on the DL profile (EL or ELH)
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

Reply only with "Yes" or "No" without explanation."""

def call_gpt(prompt, retries=3):
    """Call the GPT model to process the prompt"""
    for attempt in range(retries):
        try:
            response = openai.ChatCompletion.create(
                model=MODEL,
                messages=[{"role": "user", "content": prompt}],
                temperature=0,
            )
            return response.choices[0].message["content"].strip()

        except openai.error.RateLimitError as e:
            # Handle rate limit errors
            print(f"Rate limit exceeded. Waiting before retrying. Error: {e}")
            time.sleep(60)  # Adjust the sleep time based on your rate limit constraints
        except Exception as e:
            print(f"Retry {attempt+1}/{retries} due to error: {e}")
            time.sleep(2 ** attempt)  # Exponential backoff
    return "Error"

def evaluate(input_file, output_file):
    """Evaluate the test cases based on the input file and save the results"""
    with open(input_file, "r") as f:
        data = json.load(f)

    results = []
    for entry in tqdm(data, desc="Evaluating with GPT-4o"):
        prompt = build_prompt(entry)
        gpt_answer = call_gpt(prompt)

        # Improved matching logic (Exact match)
        match = "No"
        if gpt_answer.strip().lower() == entry["expected"].strip().lower():
            match = "Yes"

        result = {
            "id": entry["id"],
            "mr": entry["mr"],
            "dl_profile": entry["dl_profile"],
            "query": entry["query"],
            "expected": entry["expected"],
            "gpt_answer": gpt_answer,
            "match": match
        }
        results.append(result)

    with open(output_file, "w") as f:
        json.dump(results, f, indent=2)
    print(f"\nLLM evaluation complete! Results saved to `{output_file}`")

if __name__ == "__main__":
    # Evaluate EL (simpler axioms)
    print("Starting evaluation for EL...")
    evaluate(INPUT_FILE_EL, OUTPUT_FILE_EL)

    # Evaluate ELH (extended axioms)
    print("Starting evaluation for ELH...")
    evaluate(INPUT_FILE_ELH, OUTPUT_FILE_ELH)