# PulseGuard — Backend API

Sistem de monitorizare semne vitale (HR + SpO2).  
Arduino NANO ESP32 + senzor MAX30102 → Spring Boot REST API → MySQL → Flutter App.

---

## Cuprins

1. [Cerințe prealabile](#1-cerințe-prealabile)
2. [Pornire bază de date MySQL cu Docker](#2-pornire-bază-de-date-mysql-cu-docker)
3. [Prima rulare — creare schema](#3-prima-rulare--creare-schema)
4. [Rulare aplicație din IntelliJ IDEA](#4-rulare-aplicație-din-intellij-idea)
5. [Accesare Swagger UI](#5-accesare-swagger-ui)
6. [Autentificare în Swagger (JWT)](#6-autentificare-în-swagger-jwt)
7. [Rulare simulator senzor](#7-rulare-simulator-senzor)
8. [Scenarii disponibile simulator](#8-scenarii-disponibile-simulator)
9. [Oprire aplicație](#9-oprire-aplicație)

---

## 1. Cerințe prealabile

Instalează următoarele înainte de a continua:

| Unealtă | Versiune minimă | Verificare |
|---|---|---|
| **Docker Desktop** | orice versiune recentă | `docker --version` |
| **Java JDK** | 21 | `java --version` |
| **Maven** | 3.9+ (sau folosești Maven wrapper din IntelliJ) | `mvn --version` |
| **Python** | 3.10+ | `python --version` |
| **pip** | orice | `pip --version` |

> Docker Desktop trebuie să fie **pornit** (iconița din system tray să fie activă) înainte de orice alt pas.

---

## 2. Pornire bază de date MySQL cu Docker

Deschide un terminal în directorul rădăcină al proiectului (unde se află `docker-compose.yml`) și rulează:

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

Trebuie să apară ceva de genul:

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

## 4. Rulare aplicație din IntelliJ IDEA

### Opțiunea A — Butonul Run din IntelliJ (recomandat)

1. Deschide proiectul în IntelliJ IDEA
2. Așteaptă să se încarce dependențele Maven (bara de progres din dreapta jos)
3. Navighează la fișierul:
   ```
   src/main/java/com/personal/pulseguardserverapi/PulseGuardServerApiApplication.java
   ```
4. Click pe **săgeata verde ▶** de lângă metoda `main`
5. Sau apasă **Shift + F10**

### Opțiunea B — Terminal / Maven

```bash
mvn spring-boot:run
```

### Semne că aplicația a pornit cu succes

În consolă trebuie să apară (printre altele):

```
Started PulseGuardServerApiApplication in X.XXX seconds
Tomcat started on port 8080
```

Dacă apare `APPLICATION FAILED TO START`, verifică:
- Docker-ul rulează și containerul MySQL este `Up`
- Portul 8080 nu este ocupat de altă aplicație
- Setarea `ddl-auto` este corectă

---

## 5. Accesare Swagger UI

Cu aplicația pornită, deschide în browser:

```
http://localhost:8080/swagger-ui.html
```

Vei vedea interfața Swagger cu toate endpoint-urile grupate pe categorii:

| Grup | Endpoint-uri incluse |
|---|---|
| **Authentication** | POST /register, POST /login |
| **Users** | GET /users/me |
| **Devices** | POST /devices, GET /devices |
| **Sessions** | POST /sessions, POST /sessions/{id}/end |
| **Measurements** | POST /measurements |
| **Alerts** | GET /alerts, PATCH /alerts/{id}/acknowledge |
| **Analysis** | GET /analysis/summary |
| **FHIR** | GET /fhir/observations |
| **Health** | GET /health |

---

## 6. Autentificare în Swagger (JWT)

Endpoint-urile marcate cu 🔒 necesită un token JWT. Urmează pașii de mai jos.

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

Deschide **POST /v1/auth/login**, click **Try it out** și trimite:

```json
{
  "email": "razvan@email.com",
  "password": "stringst"
}
```

Răspunsul va arăta astfel:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI...",
  "tokenType": "Bearer",
  "userId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "email": "razvan@email.com",
  "name": "Razvan"
}
```

### Pasul 3 — Copiază token-ul

Selectează **doar valoarea câmpului `token`** (șirul lung care începe cu `eyJ...`).  
**Nu** copia cuvântul `"Bearer"` — Swagger îl adaugă automat.

### Pasul 4 — Autorizează în Swagger

1. Click pe butonul **Authorize 🔓** din colțul dreapta-sus al paginii Swagger
2. În câmpul **Value**, lipește token-ul copiat
3. Click **Authorize**
4. Click **Close**

De acum, toate request-urile din Swagger UI vor include automat header-ul:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Verificare rapidă

Deschide **GET /v1/users/me** → **Try it out** → **Execute**.  
Ar trebui să primești datele profilului tău cu status `200 OK`.

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

**Exemplu — scenariu normal, 60 de secunde:**

```bash
python sensor_simulator.py normal 60
```

### Ce face simulatorul automat

La pornire, scriptul execută toți pașii din fluxul real:

```
1. Login (sau creare cont dacă nu există)
         ↓
2. Verifică dacă există deja un dispozitiv înregistrat
   → dacă nu, înregistrează unul nou
         ↓
3. Deschide o sesiune de monitorizare (POST /v1/sessions)
         ↓
4. Generează măsurători HR + SpO2 la fiecare 2 secunde
   → trimite câte un batch de 5 măsurători odată
   → afișează în consolă fiecare citire + eventuale alerte
         ↓
5. La final, închide sesiunea (POST /v1/sessions/{id}/end)
   → afișează media HR și SpO2 pentru sesiunea respectivă
```

### Exemplu ieșire consolă

```
[12:30:00] ==================================================
[12:30:00]        PulseGuard  —  Sensor Simulator
[12:30:00] ==================================================
[12:30:00]   Scenariu : normal
[12:30:00]   Durată   : 60s
[12:30:00]   Cont     : razvan@email.com
[12:30:00]   Server   : http://localhost:8080
[12:30:00] ==================================================

[12:30:00] Login reușit  →  razvan@email.com
[12:30:01] Dispozitiv existent  →  3f4a1b2c-...
[12:30:01] Sesiune pornită  →  7e8d9c0a-...

[12:30:01] Trimit date la fiecare 2 secunde (batch trimis la fiecare 5 citiri)...

[12:30:03] HR:  78 BPM  |  SpO2:  97.3%
[12:30:05] HR:  82 BPM  |  SpO2:  98.1%
[12:30:07] HR:  75 BPM  |  SpO2:  97.8%
[12:30:09] HR:  80 BPM  |  SpO2:  96.9%
[12:30:11] HR:  77 BPM  |  SpO2:  98.4%
[12:30:11]   → batch trimis: 5 măsurători salvate
...
[12:31:01] Sesiune oprită  →  avg HR: 79.2 BPM  |  avg SpO2: 97.6%

✓ Simulare finalizată.
```

---

## 8. Scenarii disponibile simulator

| Comandă | Scenariu | HR generat | SpO2 generat | Alerte așteptate |
|---|---|---|---|---|
| `python sensor_simulator.py normal 60` | Valori normale | 65–95 bpm | 96.5–99.5% | Niciuna |
| `python sensor_simulator.py low_hr 60` | Bradicardie | 32–49 bpm | 96–99% | `LOW_HR` (severity: MEDIUM) |
| `python sensor_simulator.py high_hr 60` | Tahicardie | 121–145 bpm | 96–99% | `HIGH_HR` (severity: MEDIUM) |
| `python sensor_simulator.py low_spo2 60` | Hipoxemie | 70–90 bpm | 80–91.9% | `LOW_SPO2` (severity: HIGH) |
| `python sensor_simulator.py critical 30` | Criză simultană | 121–150 bpm | 80–91.9% | `HIGH_HR` + `LOW_SPO2` |

**Pragurile de alertă (definite în `MeasurementService.java`):**

```
SpO2 < 92%   →  alertă LOW_SPO2   (severity: HIGH)
HR   > 120   →  alertă HIGH_HR    (severity: MEDIUM)
HR   < 50    →  alertă LOW_HR     (severity: MEDIUM)
```

### Verificare alerte după simulare

În Swagger UI, după ce simulatorul a rulat, deschide:

```
GET /v1/alerts
```

Sau filtrat doar pe cele neconfirmate:

```
GET /v1/alerts?acknowledged=false
```

Pentru a confirma o alertă:

```
PATCH /v1/alerts/{alertId}/acknowledge
```

### Verificare rezumat sănătate

```
GET /v1/analysis/summary
```

Returnează: număr măsurători, medie HR, medie SpO2, min/max HR, min/max SpO2, număr alerte, ultima măsurătoare.

### Oprire forțată simulator

Apasă `Ctrl+C` în terminalul unde rulează scriptul.  
Simulatorul va trimite batch-ul rămas și va închide sesiunea înainte de a se opri.

---

## 9. Oprire aplicație

### Oprire Spring Boot

- Din IntelliJ: click pe **butonul roșu Stop ⏹** din bara de instrumente
- Din terminal: `Ctrl+C`

### Oprire MySQL Docker

```bash
# Oprește containerul (păstrează datele)
docker-compose stop

# Sau oprește și șterge containerul (datele rămân în volum)
docker-compose down

# Șterge totul inclusiv volumul cu date (resetare completă)
docker-compose down -v
```

> Folosește `docker-compose down -v` doar dacă vrei să pornești de la zero.  
> Altfel, folosește `docker-compose stop` și `docker-compose start` pentru a păstra datele între sesiuni.

---

## Referință rapidă — Credențiale și URL-uri

| Element | Valoare |
|---|---|
| URL aplicație | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Health check | `http://localhost:8080/v1/health` |
| MySQL host | `localhost:3307` |
| MySQL database | `pulseguard` |
| MySQL user | `pulseguard_user` |
| MySQL password | `pulseguard_pass` |
| Cont demo | `razvan@email.com` / `stringst` |
