# Changelog del Progetto Tabelline Game

Registro dettagliato di tutte le modifiche, interventi e analisi del progetto.

---

## 2025-10-06 23:45 - Analisi Iniziale Progetto

**Tipo**: 📊 Analisi
**Autore**: Claude Code

### Motivazione
Comprendere la struttura completa del progetto Tabelline Game dopo interruzione sessione precedente, per riprendere lo sviluppo e creare sistema di logging.

### Descrizione
Analisi approfondita dell'architettura e funzionamento del gioco educativo Android per l'apprendimento delle tabelline.

### Dettagli Tecnici

**Struttura Progetto Identificata**:
```
tabelline/
├── app/src/main/java/com/tabelline/
│   ├── MainActivity.java        # Activity principale, coordinatore
│   ├── GameEngine.java          # Logica di gioco
│   ├── GameView.java            # Rendering grafico
│   ├── Ball.java                # Entità pallina
│   ├── InputManager.java        # Gestione input e timer
│   └── KeyboardView.java        # Tastiera numerica custom
├── build.gradle                 # Configurazione build progetto
├── app/build.gradle            # Configurazione build app
├── settings.gradle
├── gradle.properties
├── gradlew                     # Wrapper Gradle
├── release-key.jks            # Chiave firma APK
├── release-key.b64            # Chiave codificata base64
├── tabelline-signed.apk       # APK firmato (2.3 MB)
├── README.md
├── GUIDA-TECNICA-APK.md
└── .github/                   # GitHub Actions CI/CD
```

**Architettura Applicazione**:

1. **MainActivity.java** (195 righe)
   - Pattern: Activity con 3 Listener interfaces
   - Layout verticale: 70% GameView + 5% Display + 25% Keyboard
   - Gestione ciclo vita: onPause/onResume/onDestroy
   - Dialog Game Over con punteggio e livello

2. **GameEngine.java** (100 righe)
   - Gestione stato gioco: level, score, ballsDestroyed
   - Algoritmo spawn progressivo basato su livello
   - Sistema level-up: +1 livello ogni 10 palline
   - Scaling difficoltà:
     * Level 1-5: numeri 1-6, spawn 5s
     * Level 6-10: numeri 1-8, spawn 4s
     * Level 11-15: numeri 1-12, spawn 3s
     * Level 16-20: numeri 1-15, spawn 2s
     * Level 21+: numeri 1-20, spawn 1s

3. **GameView.java**
   - Custom View con Canvas rendering
   - Game loop con thread dedicato
   - Callback GameOverListener

4. **Ball.java**
   - Entità pallina con moltiplicazione casuale
   - Fisica caduta verticale

5. **InputManager.java**
   - Pattern MVC: separa logica input da UI
   - Gestione ProgressBar timer
   - Sistema parsing moltiplicazione (es. "3x4")

6. **KeyboardView.java**
   - Tastiera numerica custom (0-9 + Reset)
   - Layout griglia 3x4

**Configurazione Build**:
- SDK: min 21, target/compile 33
- Namespace: com.tabelline
- Version: 1.0 (versionCode 1)
- Java 17 compatibility
- Dipendenze: androidx.appcompat:1.6.1

**CI/CD**:
- GitHub Actions per build automatica
- APK firmato disponibile come artifact

### Esito
✅ Progetto completamente analizzato e compreso. Pronto per logging sistematico delle future modifiche.

---

## Template per Future Voci

```markdown
## YYYY-MM-DD HH:MM - Titolo Intervento

**Tipo**: 🆕 Creazione | ✏️ Modifica | 🐛 Fix | 🔧 Refactoring | 📊 Analisi | 🚀 Deploy
**File**: percorso/file.ext
**Autore**: Nome

### Motivazione
Perché è stato necessario questo intervento

### Descrizione
Cosa è stato fatto in termini funzionali

### Dettagli Tecnici
Implementazione specifica, codice rilevante, decisioni tecniche

### Esito
Risultato dell'intervento
```

---

*Log creato il 2025-10-06 alle 23:45*
