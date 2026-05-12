# SpringAI – AI-Integrerad Spring Boot Service

En middleware-tjänst som fungerar som brygga mellan användaren och en LLM via OpenRouter.

## Förutsättningar

- Java 25
- Maven 3.9+
- Ett konto på [OpenRouter](https://openrouter.ai) med en API-nyckel

## Starta applikationen

### 1. Klona repot

```bash
git clone https://github.com/DITTNAMN/SpringAI.git
cd SpringAI
```

### 2. Sätt miljövariabler

**Windows (PowerShell):**
```powershell
$env:AI_API_KEY="din-api-nyckel-här"
$env:AI_BASE_URL="https://openrouter.ai/api/v1"
$env:AI_MODEL="openai/gpt-4o-mini"
```

**Mac/Linux:**
```bash
export AI_API_KEY="din-api-nyckel-här"
export AI_BASE_URL="https://openrouter.ai/api/v1"
export AI_MODEL="openai/gpt-4o-mini"
```

### 3. Starta

```bash
mvn spring-boot:run
```

### 4. Använd API:et

Öppna Swagger i webbläsaren: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Eller anropa direkt:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "personality": "helper",
    "message": "Hej, vad heter du?",
    "sessionId": "session-1"
  }'
```

## Tillgängliga personligheter

| Personality | Beskrivning |
|-------------|-------------|
| `helper`    | Hjälpsam assistent |
| `pirate`    | Svarar som en pirat |
| `coder`     | Fokuserad på programmering |

## Kör tester

```bash
mvn clean test
```