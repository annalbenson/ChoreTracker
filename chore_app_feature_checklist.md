
# Adult Household Chore App – Feature Checklist

A feature checklist for a **non‑gamified chore management app designed for adults sharing a household (partners or roommates).**

---

# Core Features

## 1. Household & User Management
- Create a household
- Invite members via email or invite link
- Join multiple households (optional)
- Household roles
  - Owner / Admin
  - Member
- Shared household data synced across devices

---

## 2. Room / Area Organization
- Create rooms or areas
  - Kitchen
  - Bathroom
  - Living Room
  - Garage
- Assign chores to specific rooms
- Custom room types
- Room icons or colors

---

## 3. Chore / Task Creation
- Create tasks
- Custom task descriptions
- Task categories
  - Cleaning
  - Maintenance
  - Errands
- Attach tasks to rooms
- Optional instructions
- Estimated time to complete

---

## 4. Recurring Task Scheduling
- Daily frequency
- Weekly frequency
- Monthly frequency
- Custom intervals
- One‑time tasks
- Seasonal chores
- Automatically schedule next occurrence after completion

---

## 5. Task Assignment
- Assign chores to specific household members
- Allow unassigned chores
- Optional rotation of chores between members
- Optional workload balancing

---

## 6. Task Views
- Today’s chores
- Upcoming chores
- Overdue chores
- Room-based view
- Calendar view

---

## 7. Notifications & Reminders
- Push notifications
- Daily reminders
- Overdue reminders
- Custom reminder times

---

## 8. Task Completion & History
- Mark task as completed
- Track last completed date
- View completion history
- Show which household member completed a task

---

## 9. Priority / Urgency System
- Task priority levels
- Overdue indicators
- Sorting tasks by urgency

---

## 10. Sync & Cross‑Device Support
- Real-time synchronization
- Multiple devices per user
- Offline support with later sync

---

## 11. Templates & Suggested Tasks
- Prebuilt chore templates
- Room-based suggested tasks
- Quick-add common chores

---

## 12. Maintenance & Infrequent Chores
Examples:
- Replace HVAC filter
- Replace smoke detector batteries
- Clean gutters
- Deep clean appliances
- Seasonal household maintenance

---

## 13. Basic Analytics / Overview
- Tasks completed this week
- Overview of overdue tasks
- Rooms needing attention

---

# Optional Future Features

- Smart scheduling suggestions
- Shared household lists
  - Groceries
  - Cleaning supplies
- Attach notes or photos to tasks
- Smart home integrations

---

# Minimum Viable Product (MVP)

If building **version 1**, these features are enough for a functional app:

- Household creation + member invites
- Rooms / areas
- Task creation
- Recurring scheduling
- Task assignment
- Today / upcoming task views
- Task completion history
- Notifications

---

# Potential Data Model (High Level)

Typical backend entities:

- **User**
- **Household**
- **HouseholdMember**
- **Room**
- **Task**
- **TaskSchedule**
- **TaskInstance**
- **TaskCompletionHistory**
