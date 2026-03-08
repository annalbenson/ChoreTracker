# Tidy

A personal chore tracker for Android with an AI assistant named **Tilly**.

Tilly guides you through onboarding, helps you build a cleaning routine, and answers cleaning questions on demand.

---

## Features

### Home Screen
- **Today strip** — horizontal scroll of daily chores with emoji room icons; tap to mark done, done cards fade and show ✓
- **Coming Up list** — weekly/biweekly/monthly chores sorted by frequency, card-style with swipe gestures
- **Swipe right** to mark a chore done (green flash); **swipe left** for Edit / Delete
- **+ FAB** to add a chore with name + frequency picker
- **Time-aware greeting** using your name from your home profile
- **Declutter button** in the header for quick access

### Chore Detail
- Tap any chore to see frequency, last done date, and full completion history
- Mark Done from the detail screen

### Tilly AI Chat
- Personalized chat assistant powered by Gemini 2.0 Flash
- Accessible via FAB from the main screen
- Responds to natural language requests:
  - **5-minute chore** — "give me something quick to do"
  - **Daily cleaning plan** — "what should I clean today"
  - **Declutter mode** — "I want to declutter my office"
  - **Re-onboard** — "start over" / "reset my profile"

### Declutter Mode
- Card-by-card declutter session (5 tasks per session)
- Room-aware: detects the room from your message and loads tailored tasks
- Supported rooms: kitchen, bathroom, bedroom, living room, office, entryway, closet, laundry room, garage
- Done / Skip per task; completion screen adapts message based on how many you tackled

### Onboarding
- Chat-driven setup capturing: name, home type, bedrooms, bathrooms, laundry situation, household members, cleaning style, pain points
- Gemini generates a personalized starter chore list from your profile
- Fallback default chore list if API is unavailable

---

## Tech

- **Language:** Java
- **Min SDK:** 21 | **Target SDK:** 34
- **Database:** SQLite via `SQLiteOpenHelper` (v3) — FK cascade, index on CompletionTable.ChoreId, single-row profile table
- **AI:** Gemini 2.0 Flash via Retrofit
- **UI:** Material Components 1.11, RecyclerView, ConstraintLayout, CardView, NestedScrollView, ItemTouchHelper

---

## Dev Setup

1. Clone the repo
2. Add to `local.properties`:
   ```
   sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
   GEMINI_KEY=your_key_here
   ```
3. Build with Android Studio or `./gradlew assembleDebug`

> In debug builds, Gemini calls are replaced with scripted responses so you can develop without API billing active.
