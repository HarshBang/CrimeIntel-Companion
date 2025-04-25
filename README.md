![logo](https://github.com/user-attachments/assets/7b390205-719a-4b14-85a1-941b6999e6f1)

# AI-Powered Crime Scene Assistant App

A mobile Android application designed to assist law enforcement officers directly at crime scenes, especially in regions lacking timely forensic support. The app aims to improve conviction rates in minor cases by enabling real-time evidence capture, secure storage, AI assistance, and SOP guidance.

## 🎯 Project Goals
- Support law enforcement officers during on-site crime scene documentation
- Improve traceability, accuracy, and handling of crime scene evidence
- Minimize delays caused by forensic expert shortages or lab backlogs


## Key Features (So Far)
| Feature | Status |
|--------|--------|
| **Authentication with Firebase** | ✅ Implemented |
| **Geo-tagging with device GPS** | ✅ Implemented |
| **Upload to AWS S3 (with IAM & Cognito)** | ✅ Implemented |
| **Object Detection (Faster R-CNN model with 14 classes)** | 🔄 Model built, app integration pending |
| **In-app Chatbot (OpenAI Generative API)** | ✅ Initial version done |
| **SOPs PDF-based Assistance (via chatbot)** | 🔄 In progress |
| **Offline-ready LLM + RAG Model for SOP Q&A** | 🔜 Planned |


## 🧠 Evidence Detection Model (Coming Soon)
A custom Faster R-CNN model trained on **4,463 images** across **14 evidence classes**, such as:
- Weapons
- Blood & Fingerprints
- Human hand & Body
- Hair, Rope, Shoe-print, etc.

> 🔗 [Model Repo – CIC-Evidence-Detection-RCNN-Prototype](https://github.com/HarshBang/CIC-Evidence-Detection-RCNN-Prototype)

## 📸 Demo & Previews

| Interface | Description |
|----------|-------------|
| <img src="https://github.com/user-attachments/assets/c3dc2830-9c83-415b-a188-d42f3e31b033" width="200 "/> | Main screen |
| <img src="https://github.com/user-attachments/assets/5e559f4a-7113-49c0-92a8-90e50a93b868" width="200"/> | Geotagging screen |
| <img src="https://github.com/user-attachments/assets/b0772d49-bf47-41e0-87b8-f67b089d070f" width="200"/> | Evidence Detection |

## 🚧 Roadmap
- [ ] Integrate object detection in-app (via lightweight or server-hosted model)
- [ ] Implement SOP-based RAG Q&A using PDFs
- [ ] Add offline LLM (Phi-2, Mistral, etc.) for edge environments
- [ ] Create dashboards with role-based access
- [ ] Case-wise evidence log with map and media view

## 👤 About Me
I am a passionate student at NMIMS School of Technology Management and Engineering, persuing Computer Science Engineering specializing in Data Science.
CrimeIntel Companion reflects my drive to solve real-world societal challenges — particularly in the public safety and law enforcement space — through technology that can have real impact in the field.

This project combines my interests in AI, mobile app development, and cloud infrastructure to address critical gaps in crime scene documentation and evidence handling in underserved regions.


## 🤝 Acknowledgements
- **Dr. Naresh Vurukonda, Dr. Bhanushree Yalamanchili, Prof Vinayak Mukkawar** – Project Mentors  
- **Telangana Police SOP PDFs**  
- **Forensic Science & Criminal Psychology Podcast** – Priyanshi Jain

## 📩 Contact Information
Feel free to connect for collaborations, feedback, or inquiries:

- **Email:** harshbang10@gmail.com 
- **LinkedIn:** [Harsh-Bang](https://www.linkedin.com/in/harshbang/)


**License:** All rights reserved. The code is intended solely for academic purposes.  
