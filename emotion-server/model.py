# model.py
# 모델 불러오기 + 표정 분류
import numpy as np
from keras.models import load_model
import time

expression_labels = ['Angry', 'Disgust', 'Fear', 'Happy', 'Sad', 'Surprise', 'Neutral']
start = time.time()
model = load_model('./saved_model/emotion_model.hdf5', compile=False)
print("모델 로딩 시간:", time.time() - start)

# def predict_expression(face_roi):
#     face_roi = np.expand_dims(face_roi, axis=-1)
#     face_roi = np.expand_dims(face_roi, axis=0)
#     face_roi = face_roi / 255.0
#     output = model.predict(face_roi)[0]
#     frame_count += 1
#     if frame_count % 10 == 0:  # 10프레임마다 예측
#         output = model.predict(face_roi)[0]
#         expression_index = np.argmax(output)
#         expression_label = expression_labels[expression_index]
#     index = np.argmax(output)
#     return expression_labels[index]

def predict_expression(face_roi):
    face_roi = np.expand_dims(face_roi, axis=-1)
    face_roi = np.expand_dims(face_roi, axis=0)
    face_roi = face_roi / 255.0

    output = model.predict(face_roi)[0]
    expression_index = np.argmax(output)
    return expression_labels[expression_index]
