from flask import Flask, request, jsonify
import joblib
import pandas as pd

app = Flask(__name__)

pipeline = joblib.load("mental_health_pipelines.pkl")

models = pipeline['models']
encoders = pipeline['encoders']

stress_model = models['Stress_Level']
anxiety_model = models['Anxiety_Level']
depression_model = models['Depression_Level']

stress_encoder = encoders['Stress_Level']
anxiety_encoder = encoders['Anxiety_Level']
depression_encoder = encoders['Depression_Level']


@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.json['answers']
        if len(data) != 31:
            return jsonify({'error': 'Expected 31 answers'}), 400

        df = pd.DataFrame([data], columns=pipeline['features'])

        pred_stress = stress_encoder.inverse_transform(stress_model.predict(df))[0]
        pred_anxiety = anxiety_encoder.inverse_transform(anxiety_model.predict(df))[0]
        pred_depression = depression_encoder.inverse_transform(depression_model.predict(df))[0]

        return jsonify({
            "Stress": pred_stress,
            "Anxiety": pred_anxiety,
            "Depression": pred_depression
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True)
