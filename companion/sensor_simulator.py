#!/usr/bin/env python3

import requests
import random
import time
from datetime import datetime

BASE_URL = "http://localhost:8080"
EMAIL    = "razvan@email.com"
PASSWORD = "stringst"

token      = None
device_id  = None
session_id = None


def log(msg):
    print(f"[{datetime.now().strftime('%H:%M:%S')}] {msg}")


def auth():
    global token

    resp = requests.post(f"{BASE_URL}/v1/auth/login", json={
        "email": EMAIL,
        "password": PASSWORD
    })

    if resp.status_code == 200:
        token = resp.json()["token"]
        log(f"Login reușit  →  {EMAIL}")
        return

    resp = requests.post(f"{BASE_URL}/v1/auth/register", json={
        "email": EMAIL,
        "password": PASSWORD,
        "name": "Razvan",
        "consentGiven": True
    })

    if resp.status_code == 201:
        token = resp.json()["token"]
        log(f"Cont creat    →  {EMAIL}")
    else:
        log(f"Eroare autentificare: {resp.status_code} — {resp.text}")
        exit(1)


def headers():
    return {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }


def pair_device():
    global device_id

    resp = requests.get(f"{BASE_URL}/v1/devices", headers=headers())
    if resp.status_code == 200:
        devices = resp.json()
        if devices:
            device_id = devices[0]["deviceId"]
            log(f"Dispozitiv existent  →  {device_id}")
            return

    resp = requests.post(f"{BASE_URL}/v1/devices", headers=headers(), json={
        "ipAddress":       "192.168.1.50",
        "firmwareVersion": "2.1.3",
        "status":          "ACTIVE"
    })

    if resp.status_code == 201:
        device_id = resp.json()["deviceId"]
        log(f"Dispozitiv înregistrat  →  {device_id}")
    else:
        log(f"Eroare înregistrare dispozitiv: {resp.status_code} — {resp.text}")
        exit(1)


def start_session():
    global session_id

    resp = requests.post(f"{BASE_URL}/v1/sessions", headers=headers(), json={
        "deviceId": device_id
    })

    if resp.status_code == 201:
        session_id = resp.json()["sessionId"]
        log(f"Sesiune pornită  →  {session_id}")
    else:
        log(f"Eroare pornire sesiune: {resp.status_code} — {resp.text}")
        exit(1)


def stop_session():
    resp = requests.post(
        f"{BASE_URL}/v1/sessions/{session_id}/end",
        headers=headers()
    )
    if resp.status_code == 200:
        data = resp.json()
        log(f"Sesiune oprită  →  avg HR: {data.get('avgHr')} BPM  |  avg SpO2: {data.get('avgSpo2')}%")
    else:
        log(f"Eroare oprire sesiune: {resp.status_code} — {resp.text}")


def generate_measurement(scenario: str) -> tuple[int, float]:
    if scenario == "normal":
        hr   = random.randint(65, 95)
        spo2 = round(random.uniform(96.5, 99.5), 1)

    elif scenario == "low_hr":
        hr   = random.randint(32, 49)
        spo2 = round(random.uniform(96.0, 99.0), 1)

    elif scenario == "high_hr":
        hr   = random.randint(121, 145)
        spo2 = round(random.uniform(96.0, 99.0), 1)

    elif scenario == "low_spo2":
        hr   = random.randint(70, 90)
        spo2 = round(random.uniform(80.0, 91.9), 1)

    elif scenario == "critical":
        hr   = random.randint(121, 150)
        spo2 = round(random.uniform(80.0, 91.9), 1)

    else:
        hr   = random.randint(65, 95)
        spo2 = round(random.uniform(96.5, 99.5), 1)

    return hr, spo2


def send_batch(batch: list):
    payload = {
        "deviceId":     device_id,
        "sessionId":    session_id,
        "measurements": batch
    }
    resp = requests.post(
        f"{BASE_URL}/v1/measurements",
        headers=headers(),
        json=payload
    )
    if resp.status_code == 201:
        data = resp.json()
        saved  = data.get("savedCount", 0)
        alerts = data.get("alertCount", 0)
        suffix = f"  ⚠️  {alerts} alertă/alerte generate!" if alerts else ""
        log(f"  → batch trimis: {saved} măsurători salvate{suffix}")
    else:
        log(f"Eroare trimitere batch: {resp.status_code} — {resp.text}")


def run_simulation(duration_seconds: int = 60, scenario: str = "normal"):
    log("=" * 50)
    log("       PulseGuard  —  Sensor Simulator")
    log("=" * 50)
    log(f"  Scenariu : {scenario}")
    log(f"  Durată   : {duration_seconds}s")
    log(f"  Cont     : {EMAIL}")
    log(f"  Server   : {BASE_URL}")
    log("=" * 50)
    print()

    auth()
    pair_device()
    start_session()

    print()
    log("Trimit date la fiecare 2 secunde (batch trimis la fiecare 5 citiri)...")
    print()

    batch   = []
    elapsed = 0

    try:
        while elapsed < duration_seconds:
            hr, spo2 = generate_measurement(scenario)

            now = datetime.now().strftime("%Y-%m-%dT%H:%M:%S")

            batch.append({
                "heartRate":  hr,
                "spo2":       spo2,
                "measuredAt": now
            })

            if spo2 < 92 and hr > 120:
                tag = "  🔴 SpO2 scăzut + HR ridicat"
            elif spo2 < 92:
                tag = "  🟠 SpO2 scăzut"
            elif hr > 120:
                tag = "  🟡 HR ridicat"
            elif hr < 50:
                tag = "  🟡 HR scăzut"
            else:
                tag = ""

            log(f"HR: {hr:3d} BPM  |  SpO2: {spo2:5.1f}%{tag}")

            if len(batch) >= 5:
                send_batch(batch)
                batch = []

            time.sleep(2)
            elapsed += 2

    except KeyboardInterrupt:
        print()
        log("Oprire manuală (Ctrl+C)")

    if batch:
        log("Trimit ultimul batch rămas...")
        send_batch(batch)

    print()
    stop_session()
    print()
    log("✓ Simulare finalizată.")


if __name__ == "__main__":
    import sys

    scenario = sys.argv[1] if len(sys.argv) > 1 else "normal"
    duration = int(sys.argv[2]) if len(sys.argv) > 2 else 60

    run_simulation(duration_seconds=duration, scenario=scenario)
