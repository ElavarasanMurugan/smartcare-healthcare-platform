# SmartCare – Smart AI Powered Healthcare Navigation and Patient Support System

## 1. Project Overview

SmartCare is an AI-powered healthcare navigation and patient support platform designed to help users access healthcare information and services more conveniently.

The system aims to provide preliminary symptom assessment, specialist recommendations, nearby hospital discovery, caretaker assistance, and basic health and dietary support through an integrated platform.

The project uses a microservices-based architecture to separate its major functionalities into independent services.

## 2. Problem Statement

Finding appropriate healthcare services, identifying suitable medical specialists, and accessing timely patient support can be challenging.

SmartCare aims to simplify this process by integrating AI-assisted healthcare guidance, hospital navigation, and patient support features into a single platform.

## 3. Proposed Solution

SmartCare proposes an integrated platform that allows users to:

- Enter symptoms through text or voice.
- Receive preliminary health guidance and specialist recommendations.
- Discover nearby hospitals and healthcare providers.
- Access caretaker assistance and emergency support.
- Track basic dietary and calorie information.
- Navigate healthcare services using voice-assisted features.

The platform is intended to support healthcare navigation and awareness, not replace professional medical diagnosis or treatment.

## 4. Key Features

- **AI-Assisted Symptom Guidance:** Preliminary symptom analysis using AI technologies.
- **Specialist Recommendation:** Suggestions for relevant medical specialists based on reported symptoms.
- **Hospital Navigation:** Discovery of nearby hospitals and healthcare providers.
- **Caretaker Support:** Caretaker management and emergency assistance.
- **Diet and Calorie Tracking:** Basic dietary information and calorie summaries.
- **Voice Interaction:** Voice-based symptom input and navigation.

## 5. Technology Stack

| Component | Technology |
|---|---|
| Backend | Java 17, Spring Boot |
| Backend Architecture | Microservices |
| Database | MySQL 8 |
| AI Service | Python, FastAPI |
| AI Integration | Google Gemini API |
| Frontend | React.js |
| API Communication | REST APIs |
| API Gateway | Spring Cloud Gateway |
| Testing | JUnit, Mockito, Postman |
| Version Control | Git and GitHub |

## 6. Planned Microservices Architecture

The backend is being developed using independently organized services.

| Service | Responsibility |
|---|---|
| API Gateway | Central entry point for backend requests |
| User Service | User registration and account management |
| Patient Service | Patient-related information |
| AI Service | Symptom analysis and AI integration |
| Hospital Service | Hospital and specialist information |
| Caretaker Service | Caretaker management and emergency support |
| Health Service | Diet and health-related information |

## 7. Project Structure

```text
smartcare-healthcare-platform/
│
├── backend/
│   ├── user-service/
│   ├── api-gateway/
│   ├── patient-service/
│   ├── hospital-service/
│   ├── caretaker-service/
│   └── health-service/
│
├── ai-service/
├── frontend/
├── database/
├── docs/
│
├── .gitignore
├── README.md
└── LICENSE
```

Services and directories will be added incrementally during development.

## 8. Future Enhancements

- Secure authentication and authorization.
- AI-assisted symptom guidance improvements.
- Real-time hospital and healthcare provider information.
- Voice-assisted healthcare navigation.
- Online deployment and production configuration.

## 9. Project Disclaimer

SmartCare is an academic project intended to provide healthcare navigation and preliminary informational support. It is not a substitute for professional medical advice, diagnosis, or treatment.