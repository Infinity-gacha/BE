# Flask API 서버
# 메인 실행
import cv2
import dlib
import numpy as np
from model import predict_expression
import time
import requests

SPRING_URL = "http://3.36.105.156:8080/api/emotion"

# 모델 로딩
face_cascade = cv2.CascadeClassifier('./haarcascade/haarcascade_frontalface_default.xml')
start = time.time()
predictor = dlib.shape_predictor('./predictors/shape_predictor_68_face_landmarks.dat')
print("predictor 로딩 시간:", time.time() - start)

start = time.time()
video_capture = cv2.VideoCapture(0)
print("웹캠 열기 시간:", time.time() - start)
video_capture.set(cv2.CAP_PROP_BUFFERSIZE, 1)  # 버퍼 사이즈 줄이기

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

        # 감정 결과를 Spring Boot로 전송!
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