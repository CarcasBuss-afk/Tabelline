# Guida Tecnica: Creare APK Android da Termux con GitHub Actions

Questa guida spiega come compilare automaticamente APK Android usando Termux, Git e GitHub Actions, con firma digitale inclusa.

## Indice
1. [Prerequisiti](#prerequisiti)
2. [Setup Ambiente Termux](#setup-ambiente-termux)
3. [Struttura Progetto Android](#struttura-progetto-android)
4. [Configurazione GitHub Actions](#configurazione-github-actions)
5. [Firma Digitale APK](#firma-digitale-apk)
6. [Troubleshooting](#troubleshooting)

---

## 1. Prerequisiti

### Software necessario in Termux
```bash
pkg update && pkg upgrade
pkg install git openjdk-21 apksigner keytool wget unzip
```

### Account e Servizi
- **GitHub Account** (gratuito)
- **Personal Access Token** GitHub con permessi `repo` e `workflow`

---

## 2. Setup Ambiente Termux

### 2.1 Configurazione Git
```bash
git config --global user.name "TuoUsername"
git config --global user.email "tua@email.com"
```

### 2.2 Creare Repository GitHub
1. Vai su https://github.com/new
2. Nome repository: `nome-progetto`
3. Visibilità: **Public** (GitHub Actions gratis)
4. NON selezionare README, .gitignore o license
5. Crea repository

### 2.3 Generare Personal Access Token
1. Vai su https://github.com/settings/tokens
2. "Generate new token" → "Classic"
3. Seleziona permessi:
   - ✅ `repo` (tutte le voci)
   - ✅ `workflow`
4. Genera e **copia il token** (si vede una volta sola)

---

## 3. Struttura Progetto Android

### 3.1 Creare Struttura Directory
```bash
mkdir -p nome-progetto/app/src/main/{java/com/nomeapp,res/values}
mkdir -p nome-progetto/.github/workflows
cd nome-progetto
```

### 3.2 File Essenziali

#### `app/src/main/AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.nomeapp">

    <application
        android:label="Nome App"
        android:theme="@android:style/Theme.Material.Light">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

#### `app/src/main/java/com/nomeapp/MainActivity.java`
```java
package com.nomeapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Hello World!");
        tv.setTextSize(32);
        setContentView(tv);
    }
}
```

#### `app/src/main/res/values/strings.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Nome App</string>
</resources>
```

#### `app/build.gradle`
```gradle
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.nomeapp'
    compileSdk 33

    defaultConfig {
        applicationId "com.nomeapp"
        minSdk 21
        targetSdk 33
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
}
```

#### `build.gradle` (root)
```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.2.0'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

#### `settings.gradle`
```gradle
rootProject.name = "NomeProgetto"
include ':app'
```

#### `gradle.properties`
```properties
android.useAndroidX=true
android.enableJetifier=true
org.gradle.jvmargs=-Xmx2048m
```

#### `gradlew` (placeholder)
```bash
#!/bin/sh
echo "Gradle wrapper - GitHub Actions will handle this"
```

Rendi eseguibile:
```bash
chmod +x gradlew
```

---

## 4. Configurazione GitHub Actions

### 4.1 File Workflow

Crea `.github/workflows/build.yml`:

```yaml
name: Build APK

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Setup Gradle
      uses: gradle/gradle-build-action@v2
      with:
        gradle-version: 8.5

    - name: Build with Gradle
      run: gradle assembleRelease

    - name: Sign APK
      run: |
        echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > release-key.jks
        $ANDROID_HOME/build-tools/34.0.0/apksigner sign \
          --ks release-key.jks \
          --ks-pass pass:${{ secrets.KEYSTORE_PASSWORD }} \
          --key-pass pass:${{ secrets.KEYSTORE_PASSWORD }} \
          --ks-key-alias ${{ secrets.KEY_ALIAS }} \
          --out app/build/outputs/apk/release/app-signed.apk \
          app/build/outputs/apk/release/app-release-unsigned.apk

    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: app-release
        path: app/build/outputs/apk/release/app-signed.apk
```

### 4.2 Spiegazione Workflow

- **`on: push`**: Triggera il build ad ogni push su main/master
- **`runs-on: ubuntu-latest`**: Usa macchina virtuale Ubuntu
- **`setup-java@v3`**: Installa Java 17 (richiesto per Gradle 8+)
- **`gradle-build-action@v2`**: Installa Gradle 8.5 (stabile)
- **`gradle assembleRelease`**: Compila APK in modalità release
- **Sign APK**: Firma l'APK con apksigner v2 (richiesto da Android 7+)
- **`upload-artifact@v4`**: Carica l'APK come artifact scaricabile

---

## 5. Firma Digitale APK

### 5.1 Generare Keystore

In Termux, genera una chiave di firma:

```bash
keytool -genkeypair -v \
  -keystore release-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias app-key \
  -storepass tuapassword \
  -keypass tuapassword \
  -dname "CN=Nome App, OU=Dev, O=Organizzazione, L=Città, S=Provincia, C=IT"
```

**IMPORTANTE**: Salva il file `.jks` in un posto sicuro! Se lo perdi, non potrai più aggiornare l'app sul Play Store.

### 5.2 Convertire Keystore in Base64

```bash
base64 release-key.jks | tr -d '\n' > release-key.b64
cat release-key.b64
```

Copia l'output (stringa lunga).

### 5.3 Configurare GitHub Secrets

1. Vai su `https://github.com/USERNAME/REPO/settings/secrets/actions`
2. Clicca **"New repository secret"**
3. Crea 3 secrets:

| Nome | Valore |
|------|--------|
| `KEYSTORE_BASE64` | Output di `cat release-key.b64` |
| `KEYSTORE_PASSWORD` | La password che hai usato (`tuapassword`) |
| `KEY_ALIAS` | L'alias della chiave (`app-key`) |

### 5.4 Perché Base64?

GitHub Secrets accetta solo testo. Il keystore è un file binario, quindi:
1. Convertiamo in Base64 (testo)
2. Salviamo come secret
3. Nel workflow, riconvertiamo in binario con `base64 -d`

---

## 6. Push su GitHub

### 6.1 Inizializzare Repository

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
```

### 6.2 Collegare a GitHub

```bash
git remote add origin https://github.com/USERNAME/REPO.git
```

### 6.3 Push con Token

```bash
git push -u origin main
```

Quando chiede:
- **Username**: `USERNAME`
- **Password**: Incolla il **Personal Access Token**

**Alternativa** (token nell'URL):
```bash
git remote set-url origin https://TOKEN@github.com/USERNAME/REPO.git
git push -u origin main
```

---

## 7. Scaricare e Installare APK

### 7.1 Verificare Build

1. Vai su `https://github.com/USERNAME/REPO/actions`
2. Verifica che il workflow sia ✅ verde
3. Clicca sul workflow completato
4. Scorri in basso fino a **"Artifacts"**
5. Clicca su **"app-release"** per scaricare

### 7.2 Estrarre e Installare

1. Estrai il file ZIP scaricato
2. Copia l'APK sul telefono
3. **Abilita installazione da fonti sconosciute**:
   - Impostazioni → Sicurezza → Installazione app sconosciute
   - Abilita per File Manager/Browser
4. Apri l'APK e installa

### 7.3 Verifica Firma (opzionale)

In Termux:
```bash
apksigner verify --verbose app-signed.apk
```

Dovrebbe mostrare:
```
Verified using v1 scheme (JAR signing): false
Verified using v2 scheme (APK Signature Scheme v2): true
```

---

## 8. Troubleshooting

### 8.1 Errori Comuni

#### "DOES NOT VERIFY - requires v2 signature"
**Causa**: Uso di `jarsigner` invece di `apksigner`
**Soluzione**: Usa `apksigner` come nel workflow sopra

#### "Configuration contains AndroidX dependencies but android.useAndroidX is not enabled"
**Causa**: Manca `gradle.properties`
**Soluzione**: Crea il file con `android.useAndroidX=true`

#### "Deprecated Gradle features - incompatible with Gradle 10"
**Causa**: Gradle troppo recente
**Soluzione**: Specifica `gradle-version: 8.5` nel workflow

#### "refusing to allow Personal Access Token without workflow scope"
**Causa**: Token senza permesso `workflow`
**Soluzione**: Rigenera token con permesso `workflow` attivato

#### "Package not valid" durante installazione
**Causa 1**: APK non firmata
**Soluzione**: Verifica che il workflow includa lo step "Sign APK"

**Causa 2**: Fonti sconosciute disabilitate
**Soluzione**: Abilita in Impostazioni → Sicurezza

### 8.2 Debug Build

Per vedere log dettagliati:

1. Vai su GitHub Actions workflow fallito
2. Clicca sullo step rosso ❌
3. Espandi i log per vedere l'errore

Oppure in Termux:
```bash
# Scarica log ultima run
curl -H "Authorization: token GITHUB_TOKEN" \
  https://api.github.com/repos/USER/REPO/actions/runs | jq
```

### 8.3 Test Locale (senza GitHub)

Se vuoi testare la compilazione localmente in Termux:

```bash
# Nota: richiede Android SDK completo (2-3 GB)
# Sconsigliato su smartphone
```

È molto più efficiente usare GitHub Actions.

---

## 9. Best Practices

### 9.1 Sicurezza

- ✅ **MAI** committare file `.jks` nel repository
- ✅ Usa sempre GitHub Secrets per password e chiavi
- ✅ Usa token con permessi minimi necessari
- ✅ Revoca token quando non servono più
- ❌ **NON** hardcodare password nel codice

### 9.2 Versioning

Aggiorna versione in `app/build.gradle`:
```gradle
versionCode 2      // Incrementa ad ogni release
versionName "1.1"  // Versione leggibile dall'utente
```

### 9.3 Git Ignore

Crea `.gitignore`:
```
*.jks
*.keystore
*.b64
release-key.*
local.properties
.gradle/
build/
*.apk
*.aab
```

### 9.4 Per il Play Store

L'APK firmata funziona per test, ma per pubblicare sul Play Store:

1. **Genera AAB invece di APK**:
   ```yaml
   - name: Build AAB
     run: gradle bundleRelease
   ```

2. **Firma AAB**:
   ```yaml
   - name: Sign AAB
     run: |
       $ANDROID_HOME/build-tools/34.0.0/apksigner sign \
         --ks release-key.jks \
         --ks-pass pass:${{ secrets.KEYSTORE_PASSWORD }} \
         --out app-release.aab \
         app/build/outputs/bundle/release/app-release.aab
   ```

3. Carica su Play Console

---

## 10. Architettura Completa

```
┌─────────────┐
│   Termux    │ ← Sviluppo codice Java
│             │
│  git push   │
└─────┬───────┘
      │
      ↓
┌─────────────────────────┐
│   GitHub Repository     │
│                         │
│  ┌──────────────────┐   │
│  │ Codice sorgente  │   │
│  └──────────────────┘   │
│                         │
│  ┌──────────────────┐   │
│  │ GitHub Actions   │   │
│  │  Workflow        │   │
│  └────────┬─────────┘   │
└───────────┼─────────────┘
            │
            ↓
┌─────────────────────────┐
│  Ubuntu VM (GitHub)     │
│                         │
│  1. Setup Java 17       │
│  2. Setup Gradle 8.5    │
│  3. Compile APK         │
│  4. Sign con apksigner  │
│  5. Upload artifact     │
└─────────────────────────┘
            │
            ↓
┌─────────────────────────┐
│  APK Firmata Pronta     │
│                         │
│  ✅ Installabile         │
│  ✅ Play Store ready     │
└─────────────────────────┘
```

---

## 11. Comandi Rapidi di Riferimento

### Setup Iniziale
```bash
# Installa tool
pkg install git openjdk-21 apksigner keytool

# Genera keystore
keytool -genkeypair -v -keystore release.jks -keyalg RSA -keysize 2048 \
  -validity 10000 -alias mykey -storepass mypass -keypass mypass \
  -dname "CN=App, OU=Dev, O=Org, L=City, S=State, C=IT"

# Converti in base64
base64 release.jks | tr -d '\n' > release.b64

# Init git
git init
git add .
git commit -m "Initial commit"
git remote add origin https://TOKEN@github.com/USER/REPO.git
git push -u origin main
```

### Verifica APK
```bash
# Verifica firma
apksigner verify --verbose app.apk

# Info APK
aapt dump badging app.apk | grep -E "(package|version)"

# Estrai APK
unzip app.apk -d extracted/
```

### Debug
```bash
# Log GitHub Actions
gh run list --repo USER/REPO
gh run view RUN_ID --log

# Test connessione GitHub
curl -H "Authorization: token TOKEN" \
  https://api.github.com/user
```

---

## 12. Risorse Utili

- **Android Developers**: https://developer.android.com
- **Gradle Docs**: https://docs.gradle.org
- **GitHub Actions**: https://docs.github.com/actions
- **apksigner**: https://developer.android.com/studio/command-line/apksigner

---

## Conclusione

Questa guida permette di:
✅ Sviluppare app Android da Termux
✅ Compilare automaticamente con GitHub Actions
✅ Firmare APK con signature v2
✅ Evitare di usare Android Studio
✅ Build gratuite illimitate su GitHub

**Vantaggi**:
- Nessun PC necessario
- Build cloud automatiche
- Firma sicura con secrets
- Scalabile a progetti complessi

**Limiti**:
- Interfaccia grafica limitata (meglio creare UI programmaticamente)
- Debug più complesso rispetto ad Android Studio
- Richiede connessione internet per ogni build

Per progetti professionali, considera l'uso di Android Studio su PC, ma per prototipi, MVP e app semplici, questo setup è perfetto! 🚀
