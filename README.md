# Inventory Management System

This is a Spring Boot application for a complete Inventory Management System. It includes features for managing products, billing, and viewing statistics, with a user interface built using Thymeleaf and Bootstrap.

The application is designed for easy deployment on cloud platforms like Render and includes powerful AI features for dynamic content translation.

## Features

- **Product Management**: Add, edit, delete, and view products.
- **Billing**: Create new bills for customers.
- **Bill History**: View and search past bills.
- **Dashboard**: See key statistics and recent activities.
- **Excel Export**: Export product lists and bill histories to `.xlsx` files.
- **Internationalization (i18n)**: Support for English and Tamil languages.
- **AI-Powered Translation**: Dynamically translate text using a free, production-ready AI service.

---

## AI-Powered Features

This project uses **Spring AI** to integrate Large Language Models (LLMs) for advanced functionality.

### Dynamic Translation API

A REST API is available to translate any text on the fly. This is powered by a local Gemma model (via Ollama) in development and a free, serverless Hugging Face API in production.

- **Endpoint**: `POST /api/v1/translate`
- **Request Body**:
  ```json
  {
    "text": "Text to be translated",
    "targetLanguage": "Tamil"
  }
  ```
- **Response**:
  ```json
  {
    "translatedText": "மொழிபெயர்க்கப்பட வேண்டிய உரை"
  }
  ```

---

## Getting Started

### Prerequisites

- **Java 21**: Ensure you have JDK 21 installed.
- **(Optional) Ollama**: For local AI development, [install Ollama](https://ollama.com/) and pull the Gemma model:
  ```sh
  ollama pull gemma
  ```

### Running Locally

1.  **Clone the repository**:
    ```sh
    git clone https://github.com/your-username/Inventory-Management-System.git
    cd Inventory-Management-System
    ```

2.  **Run the application**:
    The included Gradle wrapper will handle all dependencies.
    ```sh
    ./gradlew bootRun
    ```
    The application will be available at `http://localhost:8080`.

    - The H2 database console is at `http://localhost:8080/h2-console`.
    - API documentation (Swagger UI) is at `http://localhost:8080/swagger-ui/index.html`.

---

## Deployment (Render)

This application is pre-configured for deployment on **Render**.

1.  **Create a Hugging Face Account**:
    - Sign up for a free account at [huggingface.co](https://huggingface.co).
    - Go to **Settings -> Access Tokens** and create a new **read** token.

2.  **Deploy on Render**:
    - Create a new "Web Service" on Render and connect it to your GitHub repository.
    - Render will automatically detect and use the `render.yaml` file.
    - In the **Environment** tab, add a new **Environment Variable**:
      - **Key**: `HUGGINGFACE_API_TOKEN`
      - **Value**: Paste the token you created in the previous step.

    Render will automatically build and deploy the application. The `SPRING_PROFILES_ACTIVE` environment variable is set to `render`, which configures the application to use the production PostgreSQL database and the Hugging Face API for translations.
