# Team Dhenu: Full-Native Climate Resilient Agriculture CoPilot


https://github.com/user-attachments/assets/3a413222-957d-42e8-a060-4420680d4d8f



First create llama directory: 
```
adb shell mkdir -p /data/local/tmp/llama
```
To copy model:
```
adb push /Users/lokesh/Downloads/llama3_2.pte /data/local/tmp/llama/llama3_2.pte
adb push /Users/lokesh/.llama/checkpoints/Llama3.2-1B-Instruct:int4-spinquant-eo8/tokenizer.model /data/local/tmp/llama/tokenizer.
```

## For RAG
- Upload your knowledge base PDF to app/src/main/res/raw/ folder
- To change the embedding model, replace model in app/src/main/assets folder and make changes in SentenceEmbeddingProvider.kt file
## Climate Resilient Agriculture (CRA) Fine-tuned Llama3.2-1b model
Huggingface Repo for the fine-tuned model is [here.](chheplo/dhenu2-in-climate-llama3.2-1b)

## Datasets used for the fine-tuning model
During the hackathon, we built synthetic data pipelines to generate instruction pairs for the CRA data, and also used factual Q&A from institutes.
Dataset built during this hackathon to train the model are [synthetic](https://huggingface.co/datasets/KissanAI/llama-hackathon-climate-synth-qa) and [knowledge based](https://huggingface.co/datasets/KissanAI/llama-hackathon-climate-kb-qa)

## Reference Resources
[Android-Document-QA](https://github.com/shubham0204/Android-Document-QA)
[Executorch](https://github.com/pytorch/executorch/blob/main/examples/models/llama/README.md#step-4-run-on-your-computer-to-validate)

