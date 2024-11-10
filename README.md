# dhenu_climate_llama_impact_hackathon

First create llama directory: adb shell mkdir -p /data/local/tmp/llama

To copy model:

adb push /Users/lokesh/Downloads/llama3_2.pte /data/local/tmp/llama/ adb push /Users/lokesh/.llama/checkpoints/Llama3.2-1B-Instruct:int4-spinquant-eo8/tokenizer.model /data/local/tmp/llama/
