# Tidy Backlog

Items are roughly priority-ordered within each section.

---

## v2 Ideas

- [ ] Room filter chip — filter Coming Up by room (e.g. show only Kitchen chores)
- [ ] Tilly room awareness — "what needs doing in the bathroom?" pulls chores with matching roomId
- [ ] Room shown on chore detail screen — add a Room row to the info card
- [ ] Chore detail: edit inline — edit name/frequency/room from the detail screen instead of needing to go back to the list
- [ ] Onboarding room seeding — auto-assign rooms to Gemini-generated chores based on name keywords
- [ ] Account name edit — allow changing the profile name after onboarding
- [ ] Release build test — verify Gemini chat path end-to-end in a release build

---

## Completed

- [x] Project scaffold — modernized from 2018 skeleton (Gradle 8.5, AGP 8.2.2, AndroidX, SDK 34)
- [x] Package renamed to `com.annabenson.tidy`, app name set to Tidy
- [x] Tidy color palette — sage green, dusty teal, warm cream, terracotta
- [x] App icon — adaptive icon set across all density buckets
- [x] SQLite DB (v5) — ChoreTable + CompletionTable (FK cascade, index on ChoreId) + HomeProfileTable (PK, NOT NULL) + RoomTable (8 seeded rooms, ON DELETE SET NULL FK)
- [x] Tilly onboarding — 8-step AI chat flow capturing home profile
- [x] Gemini integration — starter chore list generation + Tilly AI chat (Gemini 2.0 Flash, TEST_MODE for debug)
- [x] Tilly AI chat — stain tips, cleaning advice, 5-min chores, daily plan, re-onboard
- [x] Chore frequency picker — Daily / Weekly / Biweekly / Monthly / As needed (add + edit dialogs)
- [x] Recurring auto-scheduling — next due date calculates automatically after mark-done (atomic transaction)
- [x] Home screen redesign — Today horizontal strip (daily chores + emoji icons) + Coming Up vertical list
- [x] Swipe gestures — swipe right = mark done, swipe left = edit/delete
- [x] Chore detail screen — frequency, next due date, last done, full completion history, Mark Done button
- [x] Declutter mode — room-aware card sessions (9 rooms, 5 tasks), launched from header or Tilly chat
- [x] Time-aware greeting with profile name
- [x] Empty state, + FAB, overdue-first sort in Coming Up
- [x] Overdue indicators — due label turns terracotta; overdue chores sort to top automatically
- [x] Filter chips — All / Overdue / This week / As needed, sticky below header
- [x] Room organization — 8 built-in rooms; room picker in add/edit dialogs; room shown in list rows ("Weekly · 🍳 Kitchen")
- [x] Push notifications — daily 8 AM reminder for overdue/due-today chores (WorkManager, POST_NOTIFICATIONS permission)
- [x] Tilly avatar button — circular profile photo button (ShapeableImageView) replacing the FAB icon

---

## 🍯 Honey Do List
*Good ideas that can wait*

- [ ] Estimated time per chore
- [ ] Task categories — Cleaning / Maintenance / Errands
- [ ] Calendar view
- [ ] Analytics — tasks completed this week, streak tracking, rooms needing attention
- [ ] Seasonal / maintenance chores — HVAC filter, smoke detector batteries, clean gutters
- [ ] Notes or photos attached to a chore
- [ ] Shared grocery / cleaning supplies list
- [ ] Household sharing — shared chore lists and assignments for couples or roommates
- [ ] Multi-device sync
- [ ] Smart home integrations
- [ ] **Desktop / web companion** — shared backend (Firebase or Supabase); web app for weekly/monthly planning sessions
