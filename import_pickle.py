import pickle

with open('mental_health_pipelines.pkl', 'rb') as f:
    pipeline = pickle.load(f)
print(pipeline.keys())
