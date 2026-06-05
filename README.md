# PulseGuard — Backend API

Sistem de monitorizare semne vitale (HR + SpO2).  
Arduino NANO ESP32 + senzor MAX30102 → Spring Boot REST API → MySQL → Flutter App.

---

## Cuprins

1. [Cerințe prealabile](#1-cerințe-prealabile)
2. [Pornire bază de date MySQL cu Docker](#2-pornire-bază-de-date-mysql-cu-docker)
3. [Prima rulare — creare schema](#3-prima-rulare--creare-schema)
4. [Rulare server Spring Boot](#4-rulare-server-spring-boot)
5. [Accesare Swagger UI](#5-accesare-swagger-ui)
6. [Autentificare în Swagger (JWT)](#6-autentificare-în-swagger-jwt)
7. [Rulare simulator senzor](#7-rulare-simulator-senzor)
8. [Scenarii disponibile simulator](#8-scenarii-disponibile-simulator)
9. [Rulare aplicație Flutter Web](#9-rulare-aplicație-flutter-web)
10. [Flux complet demo](#10-flux-complet-demo)
11. [Oprire aplicație](#11-oprire-aplicație)

---

## 1. Cerințe prealabile

Instalează următoarele înainte de a continua:

| Unealtă | Versiune minimă | Verificare |
|---|---|---|
| **Docker Desktop** | orice versiune recentă | `docker --version` |
| **Java JDK** | 21 | `java --version` |
| **Maven** | 3.9+ (sau wrapper din IntelliJ) | `mvn --version` |
| **Python** | 3.10+ | `python --version` |
| **Flutter SDK** | 3.x | `flutter --version` |
| **Chrome** | orice versiune recentă | — |

> Docker Desktop trebuie să fie **pornit** (iconița din system tray să fie activă) înainte de orice alt pas.

> Pe Windows, Flutter necesită **Developer Mode** activ pentru a crea symlink-uri.  
> Activare: `start ms-settings:developers` → toggle **Developer Mode** → Yes.

---

## 2. Pornire bază de date MySQL cu Docker

Deschide un terminal în directorul rădăcină al proiectului backend (unde se află `docker-compose.yml`) și rulează:

```bash
docker-compose up -d
```

**Ce face această comandă:**
- Descarcă imaginea MySQL 8.4 (la prima rulare, poate dura 1-2 minute)
- Creează un container numit `pulseguard-mysql`
- Expune MySQL pe portul **3307** al mașinii tale locale
- Creează automat baza de date `pulseguard` cu userul `pulseguard_user`
- Salvează datele într-un volum persistent (`pulseguard_mysql_data`)

**Verificare că MySQL rulează:**

```bash
docker ps
```

Trebuie să apară:

```
CONTAINER ID   IMAGE       STATUS          PORTS                    NAMES
abc123def456   mysql:8.4   Up 30 seconds   0.0.0.0:3307->3306/tcp   pulseguard-mysql
```

> Dacă `STATUS` nu este `Up`, rulează `docker-compose logs mysql` pentru a vedea eroarea.

---

## 3. Prima rulare — creare schema

**Doar la prima pornire a aplicației**, setarea din `application.properties` trebuie să fie:

```properties
spring.jpa.hibernate.ddl-auto=create
```

Această setare îi spune Hibernate să **șteargă și să recreeze** toate tabelele la pornire.  
Este necesară o singură dată pentru a genera schema corectă.

**Fișierul se găsește la:**
```
src/main/resources/application.properties
```

**După prima pornire reușită**, schimbă înapoi la:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Cu `update`, Hibernate păstrează datele existente și doar adaugă coloane/tabele lipsă.

> ⚠️ Nu lăsa `create` activ în mod permanent — la fiecare restart **toate datele se șterg**.

---

## 4. Rulare server Spring Boot

### Opțiunea A — Butonul Run din IntelliJ (recomandat)

1. Deschide proiectul backend (`PulseGuardServerAPI`) în IntelliJ IDEA
2. Așteaptă să se încarce dependențele Maven (bara de progres din dreapta jos)
3. Navighează la fișierul:
   ```
   src/main/java/com/personal/pulseguardserverapi/PulseGuardServerApiApplication.java
   ```
4. Click pe **săgeata verde ▶** de lângă metoda `main`
5. Sau apasă **Shift + F10**

### Opțiunea B — Terminal

```bash
mvn spring-boot:run
```

### Semne că serverul a pornit cu succes

În consolă trebuie să apară:

```
Started PulseGuardServerApiApplication in X.XXX seconds
Tomcat started on port 8080
```

Dacă apare `APPLICATION FAILED TO START`, verifică:
- Docker-ul rulează și containerul MySQL este `Up`
- Portul 8080 nu este ocupat de altă aplicație
- Setarea `ddl-auto` este corectă

**Verificare rapidă** — deschide în browser:
```
http://localhost:8080/v1/health
```
Răspuns așteptat: `{"status":"UP"}`

---

## 5. Accesare Swagger UI

Cu serverul pornit, deschide în browser:

```
http://localhost:8080/swagger-ui.html
```

Vei vedea toate endpoint-urile grupate pe categorii:

| Grup | Endpoint-uri incluse |
|---|---|
| **Authentication** | POST /register, POST /login |
| **Users** | GET /users/me, PUT /users/me |
| **Devices** | POST /devices, GET /devices |
| **Sessions** | GET /sessions, POST /sessions, POST /sessions/{id}/end |
| **Measurements** | GET /measurements/latest, GET /measurements, POST /measurements |
| **Alerts** | GET /alerts, PATCH /alerts/{id}/acknowledge |
| **Analysis** | GET /analysis/summary |
| **FHIR** | GET /fhir/observations |
| **Health** | GET /health |

---

## 6. Autentificare în Swagger (JWT)

### Pasul 1 — Creează un cont (o singură dată)

În Swagger UI, deschide **POST /v1/auth/register**, click **Try it out** și trimite:

```json
{
  "email": "razvan@email.com",
  "password": "stringst",
  "name": "Razvan",
  "sex": "M",
  "age": 25,
  "consentGiven": true
}
```

### Pasul 2 — Loghează-te

Deschide **POST /v1/auth/login** și trimite:

```json
{
  "email": "razvan@email.com",
  "password": "stringst"
}
```

### Pasul 3 — Copiază token-ul și autorizează

1. Copiază valoarea câmpului `token` din răspuns (șirul lung care începe cu `eyJ...`)
2. Click pe butonul **Authorize 🔓** din colțul dreapta-sus
3. Lipește token-ul → **Authorize** → **Close**

De acum toate request-urile din Swagger includ automat `Authorization: Bearer <token>`.

---

## 7. Rulare simulator senzor

Simulatorul Python imită un Arduino NANO ESP32 care trimite date de la senzorul MAX30102.

### Instalare dependențe (o singură dată)

```bash
cd companion
pip install requests
```

### Rulare

```bash
python sensor_simulator.py [scenariu] [durata_in_secunde]
```

**Exemplu:**

```bash
python sensor_simulator.py normal 120
```

### Ce face simulatorul

```
1. Login automat cu razvan@email.com / stringst
         ↓
2. Verifică dacă există deja un dispozitiv → refolosește sau creează unul nou
         ↓
3. Deschide o sesiune de monitorizare
         ↓
4. Trimite HR + SpO2 la fiecare 2 secunde (batch de 5 la un POST)
   → serverul generează alerte automat dacă valorile depășesc pragurile
         ↓
5. La final, închide sesiunea și afișează mediile HR și SpO2
```

---

## 8. Scenarii disponibile simulator

| Comandă | Scenariu | HR generat | SpO2 generat | Alerte așteptate |
|---|---|---|---|---|
| `python sensor_simulator.py normal 120` | Valori normale | 65–95 bpm | 96.5–99.5% | Niciuna |
| `python sensor_simulator.py low_hr 60` | Bradicardie | 32–49 bpm | 96–99% | `LOW_HR` (MEDIUM) |
| `python sensor_simulator.py high_hr 60` | Tahicardie | 121–145 bpm | 96–99% | `HIGH_HR` (MEDIUM) |
| `python sensor_simulator.py low_spo2 60` | Hipoxemie | 70–90 bpm | 80–91.9% | `LOW_SPO2` (HIGH) |
| `python sensor_simulator.py critical 30` | Criză simultană | 121–150 bpm | 80–91.9% | `HIGH_HR` + `LOW_SPO2` |

**Pragurile de alertă:**

```
SpO2 < 92%   →  LOW_SPO2   (severity: HIGH)
HR   > 120   →  HIGH_HR    (severity: MEDIUM)
HR   < 50    →  LOW_HR     (severity: MEDIUM)
```

---

## 9. Rulare aplicație Flutter Web

### Cerințe

- Flutter SDK instalat și în PATH (`flutter --version`)
- Developer Mode activat pe Windows (`start ms-settings:developers`)
- Serverul Spring Boot **trebuie să ruleze** înainte de a porni Flutter

### Instalare dependențe Flutter (o singură dată)

Deschide un terminal în directorul proiectului Flutter:

```bash
cd C:\Users\Razvan\AndroidStudioProjects\pulseguard-flutter-2
flutter pub get
```

### Pornire aplicație în browser

```bash
flutter run -d chrome
```

> Flutter compilează aplicația și o deschide automat în Chrome pe un port local (ex: `http://localhost:55555`).

### Autentificare în Flutter

La primul acces, folosește contul creat în Swagger:

```
Email:    razvan@email.com
Parolă:   stringst
```

### Notă despre URL-ul serverului

Fișierul `lib/services/api_service.dart` conține adresa serverului:

```dart
// Pentru Flutter Web (browser pe aceeași mașină cu serverul):
static const String baseUrl = 'http://localhost:8080';

// Pentru emulator Android — schimbă la:
// static const String baseUrl = 'http://10.0.2.2:8080';
```

> `10.0.2.2` este adresa specială prin care emulatorul Android accesează `localhost`-ul mașinii gazdă.

---

## 10. Flux complet demo

Acesta este ordinea corectă de pornire a întregului sistem:

### Pasul 1 — Pornește MySQL

```bash
# în directorul backend
docker-compose up -d
```

### Pasul 2 — Pornește serverul Spring Boot

Din IntelliJ: **Shift + F10**  
Sau din terminal: `mvn spring-boot:run`

Verificare: `http://localhost:8080/v1/health` → `{"status":"UP"}`

### Pasul 3 — Pornește simulatorul senzor

```bash
# într-un terminal separat, din directorul companion/
python sensor_simulator.py normal 120
```

Lasă terminalul deschis — simulatorul trimite date la fiecare 2 secunde.

### Pasul 4 — Pornește aplicația Flutter

```bash
# într-un alt terminal separat
cd C:\Users\Razvan\AndroidStudioProjects\pulseguard-flutter-2
flutter run -d chrome
```

### Pasul 5 — Folosește aplicația

```
Login → razvan@email.com / stringst
    ↓
Home → "Conectează dispozitiv" → buton Conectare → "Dispozitiv conectat"
    ↓
Home → "Monitorizare Live"
    → valorile HR și SpO2 se actualizează la fiecare 2 secunde
    → afișează exact datele trimise de simulatorul Python
    → dacă simulatorul nu rulează, apare bannerul "Așteptare date de la senzor..."
    ↓
Apasă "Oprește monitorizarea"
    ↓
Home → "Alerte"      → alertele generate de valorile anormale
Home → "Analiză"     → scor sănătate + statistici agregate
Home → "Istoric"     → toate măsurătorile salvate
```

### Schimbarea scenariului în timp real

Poți opri simulatorul (`Ctrl+C`) și reporni cu un scenariu diferit fără să închizi Flutter:

```bash
python sensor_simulator.py low_spo2 60   # → alerte HIGH în Flutter
python sensor_simulator.py high_hr 60    # → alerte MEDIUM în Flutter
python sensor_simulator.py critical 30   # → alerte multiple simultan
```

Aplicația Flutter va afișa automat noile valori la următorul poll (max 2 secunde).

---

## 11. Oprire aplicație

### Oprire Flutter

```
Ctrl+C  în terminalul unde rulează flutter run
```

### Oprire simulator Python

```
Ctrl+C  în terminalul unde rulează sensor_simulator.py
```

Simulatorul trimite batch-ul rămas și închide sesiunea înainte de a se opri.

### Oprire server Spring Boot

- Din IntelliJ: butonul roșu **Stop ⏹**
- Din terminal: `Ctrl+C`

### Oprire MySQL Docker

```bash
# Oprește containerul (păstrează datele)
docker-compose stop

# Oprește și șterge containerul (datele rămân în volum)
docker-compose down

# Șterge totul inclusiv datele (resetare completă)
docker-compose down -v
```

> Folosește `docker-compose down -v` doar dacă vrei să pornești de la zero.

---

## Referință rapidă

| Element | Valoare |
|---|---|
| URL server | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Health check | `http://localhost:8080/v1/health` |
| MySQL host | `localhost:3307` |
| MySQL database | `pulseguard` |
| MySQL user / parolă | `pulseguard_user` / `pulseguard_pass` |
| Cont demo | `razvan@email.com` / `stringst` |
| Flutter Web | `http://localhost:XXXXX` (port atribuit automat de Flutter) |
