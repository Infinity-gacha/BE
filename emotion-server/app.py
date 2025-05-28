import cv2
import dlib
import numpy as np
from flask import Flask, request, jsonify
from PIL import Image
import io
import threading
import time
import requests
from model import predict_expression

SPRING_URL = "http://localhost:8081/api/emotion"

app = Flask(__name__)

# Flask API 엔드포인트 정의
@app.route('/analyze', methods=['POST'])
def analyze():
    if 'image' not in request.files:
        return jsonify({'error': 'No image uploaded'}), 400

    file = request.files['image']
    image = Image.open(file.stream).convert('L')
    image = image.resize((64, 64))
    image_np = np.array(image)

    try:
        label = predict_expression(image_np)
        return jsonify({'emotion': label})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# Flask 스레드 실행 함수
def run_flask():
    app.run(host='0.0.0.0', port=5000)

# OpenCV & Flask 함께 실행
if __name__ == '__main__':
    # Flask 서버 스레드 실행
    flask_thread = threading.Thread(target=run_flask)
    flask_thread.daemon = True
    flask_thread.start()

    # 모델 로딩
    face_cascade = cv2.CascadeClassifier('./haarcascade/haarcascade_frontalface_default.xml')
    predictor = dlib.shape_predictor('./predictors/shape_predictor_68_face_landmarks.dat')

    video_capture = cv2.VideoCapture(0)
    video_capture.set(cv2.CAP_PROP_BUFFERSIZE, 1)

    while True:
        ret, frame = video_capture.read()
        if not ret:
            break

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30))

        for (x, y, w, h) in faces:
            cv2.rectangle(frame, (x, y), (x+w, y+h), (0, 255, 0), 2)
            face_roi = gray[y:y+h, x:x+w]
            face_roi = cv2.resize(face_roi, (64, 64))
            label = predict_expression(face_roi)

            try:
                response = requests.post(SPRING_URL, json={"emotion": label})
                print(f"[전송됨] 감정: {label}, 응답: {response.status_code}")
            except Exception as e:
                print(f"[전송 실패] {e}")
            cv2.putText(frame, label, (x, y-10), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 255, 0), 2)

        cv2.imshow('Expression Recognition', frame)
        if cv2.waitKey(25) == 27:
            break

    video_capture.release()
    cv2.destroyAllWindows()