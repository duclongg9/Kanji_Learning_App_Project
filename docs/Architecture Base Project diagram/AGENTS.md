
# SRS — Kanji Learner (Android, Java, Clean Architecture)  
**Version:** 1.0 • **Date:** 2025‑10‑23 • **Owner:** Principal Software Engineer

> **Ngôn ngữ / Language:** Tài liệu song ngữ. Mỗi mục gồm phần **VI (Tiếng Việt)** và **EN (English)**.

---

## 1. Giới thiệu / Introduction

### 1.1 Mục tiêu (Goal) — VI
Tài liệu SRS mô tả đầy đủ yêu cầu cho ứng dụng **Kanji Learner** nhằm giúp người học tiếng Nhật (N5→N1) ghi nhớ Kanji qua **flashcards + SRS** (SM‑2/Leitner), **tra cứu** On/Kun, ví dụ, bộ thủ; hỗ trợ **offline‑first**, đa ngôn ngữ (JA/VI/EN), và trải nghiệm mượt mà trên điện thoại & máy tính bảng Android.

### 1.1 Goal — EN
This SRS specifies requirements for **Kanji Learner** to help learners (JLPT N5→N1) memorize Kanji with **flashcards + SRS** (SM‑2/Leitner), **lookup** On/Kun, examples, radicals; focusing on **offline‑first**, multilingual (JA/VI/EN), and smooth UX on Android phones & tablets.

---

### 1.2 Phạm vi (Scope) — VI
- Nền tảng: Android phone & tablet.
- Công nghệ: **Java‑only app code**, Android Studio, **Gradle Kotlin DSL**.
- Clean Architecture + MVVM + Repository; **Room** (offline‑first), **Hilt DI**, **Navigation**, **DataStore**, **WorkManager** (nhắc SRS), **Retrofit** (đồng bộ tuỳ chọn).
- Chức năng lõi: Onboarding, đăng nhập Google/ẩn danh, danh sách & chi tiết Kanji, SRS/flashcards, tìm kiếm/lọc/sắp xếp, bookmark/ghi chú, tải offline/seed, cài đặt (theme/font/kích cỡ/ngôn ngữ), đồng bộ tuỳ chọn.

### 1.2 Scope — EN
- Platform: Android phone & tablet.
- Tech: **Java‑only app**, Android Studio, **Gradle Kotlin DSL**.
- Clean Architecture + MVVM + Repository; **Room**, **Hilt**, **Navigation**, **DataStore**, **WorkManager**, **Retrofit** (optional sync).
- Core features: Onboarding, Google/Anonymous sign‑in, Kanji lists & detail, SRS/flashcards, search/filter/sort, bookmarks/notes, offline/seed data, settings, optional sync.

---

### 1.3 Thuật ngữ (Terminology) — VI
| Thuật ngữ | Mô tả |
|---|---|
| Kanji | Chữ Hán trong tiếng Nhật |
| On/Kun | Âm On’yomi/Kun’yomi |
| Radical (Bộ thủ) | Thành phần cấu tạo ký tự |
| JLPT | Cấp độ N5→N1 |
| SRS | Spaced Repetition System (SM‑2/Leitner) |
| Due | Thẻ đến hạn ôn |
| Seed | Bộ dữ liệu khởi tạo offline |

### 1.3 Terminology — EN
| Term | Description |
|---|---|
| Kanji | Japanese logograph |
| On/Kun | On’yomi/Kun’yomi readings |
| Radical | Component building block of Kanji |
| JLPT | N5→N1 proficiency levels |
| SRS | Spaced Repetition System (SM‑2/Leitner) |
| Due | Card is due for review |
| Seed | Initial offline dataset |

---

### 1.4 Độc giả mục tiêu (Audience) — VI
- Product Owner, PM, Designer, Mobile Engineers, QA, Localization, Academic advisors (Japanese).

### 1.4 Intended Audience — EN
- Product owner, PM, designers, mobile engineers, QA, localization, Japanese advisors.

---

## 2. Tổng quan hệ thống / Overall Description

### 2.1 Persona — VI
| Persona | Mục tiêu | Hành vi |
|---|---|---|
| **Mai (SV N5)** | Đậu N5 trong 3 tháng | Học flashcards hằng ngày 15–20’ trên điện thoại |
| **Khang (N3)** | Tăng tốc đọc hiểu | Tìm nhanh On/Kun, ví dụ; lọc theo JLPT |
| **Linh (Giáo viên)** | Theo dõi tiến độ lớp | Xuất thống kê, gợi ý ôn lại (thiết bị tablet) |

### 2.1 Persona — EN
| Persona | Goals | Behaviors |
|---|---|---|
| **Mai (N5 student)** | Pass N5 within 3 months | Daily 15–20’ flashcard sessions on phone |
| **Khang (N3)** | Improve reading speed | Quick lookup On/Kun & examples; filter JLPT |
| **Linh (Teacher)** | Track class progress | View stats, suggest reviews (tablet) |

---

### 2.2 User Stories — VI/EN
- **US‑01**: As a learner, I can **sign in with Google** or continue **anonymously** to start quickly.  
- **US‑02**: As a learner, I can **browse Kanji by JLPT** and open **detail** (On/Kun, meanings, examples, radical).  
- **US‑03**: As a learner, I can **review due flashcards** using **SRS (SM‑2/Leitner)** with spaced scheduling.  
- **US‑04**: As a learner, I can **search/filter/sort**, **bookmark**, and **add notes**.  
- **US‑05**: As a learner, I can **download offline seed** and use app without network.  
- **US‑06**: As a learner, I can configure **theme, font, text size, language**.  
- **US‑07** *(optional)*: As a learner, I can **sync** progress across devices.

---

### 2.3 Giả định & Phụ thuộc / Assumptions & Dependencies
**VI**: Thiết bị Android có Google Play Services (đăng nhập Google); dữ liệu seed được đóng gói trong APK/AAB; đồng bộ máy chủ là tuỳ chọn (có thể tích hợp sau).  
**EN**: Device has Google Play Services (Google Sign‑In); seed data bundled in APK/AAB; backend sync optional for later phase.

---

### 2.4 Ràng buộc kỹ thuật / Technical Constraints
**VI/EN**  
- **Java‑only** application code (cho Team & policy hiện tại).  
- **minSdk 24**, **targetSdk 35**.  
- **Gradle Kotlin DSL (`build.gradle.kts`)**.  
- Architecture libs: Room/Hilt/Navigation/DataStore/WorkManager; Retrofit optional.

---

## 3. Yêu cầu chức năng / Functional Requirements (by Epic)

> Mỗi chức năng có: **Mô tả**, **Tiêu chí chấp nhận (Gherkin)**, **Ưu tiên**, **Rủi ro**.

### 3.1 Epic: Onboarding & Đăng nhập / Onboarding & Sign‑In

**Mô tả — VI**: Cho phép chọn đăng nhập Google hoặc dùng ẩn danh; thiết lập ngôn ngữ, theme cơ bản khi khởi động lần đầu.  
**Description — EN**: Choose Google Sign‑In or anonymous; initial language/theme setup on first launch.

**Acceptance (Gherkin)**
```gherkin
Scenario: First launch with network
  Given the app is freshly installed
  When the user opens the app
  Then the user can choose "Sign in with Google" or "Continue as guest"
  And the app stores the selection locally

Scenario: Continue as guest
  Given the user selects "Continue as guest"
  When onboarding completes
  Then the user lands on Home and can access all offline features

Scenario: Google Sign-In success
  Given Google Play Services is available
  When the user signs in with Google
  Then the app stores an auth token locally and shows the user's name/avatar
```

**Ưu tiên / Priority**: **Must‑have**  
**Rủi ro / Risks**: Thiết bị không có Google Services; xử lý lỗi đăng nhập.

---

### 3.2 Epic: Danh sách Kanji theo JLPT & Chi tiết / JLPT Lists & Kanji Detail

**Mô tả — VI**: Duyệt danh sách theo JLPT; xem chi tiết gồm On/Kun, nghĩa VI/EN, ví dụ, bộ thủ, số nét; điều hướng giữa các Kanji.  
**Description — EN**: Browse by JLPT; detail screen shows On/Kun, VI/EN meanings, examples, radical, strokes; navigate adjacent Kanji.

**Acceptance (Gherkin)**
```gherkin
Scenario: Browse JLPT list offline
  Given seed data is installed
  When the user opens "JLPT N5"
  Then the app lists N5 Kanji within 200 ms

Scenario: Open Kanji detail
  Given a Kanji is selected in any list
  When navigating to Detail
  Then On/Kun, meanings, examples, radical, and strokes are visible
```

**Ưu tiên**: **Must‑have**  
**Rủi ro**: Dữ liệu seed sai lệch hoặc thiếu ví dụ; cần kiểm duyệt nội dung.

---

### 3.3 Epic: SRS/Flashcards (Lập lịch, học lại, thống kê, nhắc học)

**Mô tả — VI**: Ôn tập theo SM‑2/Leitner; hiển thị số thẻ đến hạn; ghi log kết quả (quality 0–5); nhắc học bằng WorkManager.  
**Description — EN**: Review using SM‑2/Leitner; show due count; log results (quality 0–5); reminders via WorkManager.

**Acceptance (Gherkin)**
```gherkin
Scenario: See due cards
  Given the user has reviewed cards previously
  When opening Study
  Then the app shows the number of due cards for today

Scenario: Review a card with SM-2
  Given a due card is shown
  When the user rates it "Good (q=4)"
  Then next due date is scheduled per SM-2 and saved to the database

Scenario: Daily reminder
  Given user sets reminder at 20:00
  When local time reaches 20:00
  Then a notification appears if there are due cards
```

**Ưu tiên**: **Must‑have**  
**Rủi ro**: WorkManager bị hạn chế bởi OEM; thời gian hệ thống thay đổi; pin/battery optimization.

---

### 3.4 Epic: Tìm kiếm/Lọc/Sort; Bookmark/Note; Tải offline/Seed

**Mô tả — VI**: Tìm kiếm FTS (On/Kun/meaning); lọc theo JLPT, số nét; sắp xếp; bookmark thẻ; thêm ghi chú; tải seed offline.  
**Description — EN**: FTS search (On/Kun/meaning); filter by JLPT, strokes; sort; bookmark cards; add notes; install offline seed.

**Acceptance (Gherkin)**
```gherkin
Scenario: FTS search offline
  Given the device is offline
  When the user searches "gaku" or "学"
  Then results appear under 200 ms with matching Kanji

Scenario: Bookmark & note
  Given a Kanji detail is open
  When the user taps "Bookmark" and adds a note
  Then the Kanji appears in Bookmarks with the note persisted
```

**Ưu tiên**: **Must‑have**  
**Rủi ro**: Kết quả FTS không chính xác ngôn ngữ; phân tách từ đa nghĩa.

---

### 3.5 Epic: Cài đặt / Settings (Theme, Font, Text Size, Language)

**Mô tả — VI**: Đổi theme (light/dark/system), font (có font Nhật đẹp), kích thước chữ, ngôn ngữ (JA/VI/EN).  
**Description — EN**: Change theme (light/dark/system), fonts, text size, language (JA/VI/EN).

**Acceptance (Gherkin)**
```gherkin
Scenario: Change language
  Given Settings is open
  When the user selects "English"
  Then the app reloads strings in English and persists the preference
```

**Ưu tiên**: **Should‑have**  
**Rủi ro**: Dịch thuật thiếu/không đồng nhất; font thay đổi layout.

---

### 3.6 Epic: Đồng bộ hoá (Optional Sync)

**Mô tả — VI**: Tuỳ chọn đăng nhập để đồng bộ tiến trình, bookmark, notes. Không bắt buộc để học offline.  
**Description — EN**: Optional sign‑in to sync progress, bookmarks, notes. Not required for offline learning.

**Acceptance (Gherkin)**
```gherkin
Scenario: Sync after sign-in
  Given the user signs in on a second device
  When sync is enabled
  Then review logs and bookmarks reconcile without duplicates
```

**Ưu tiên**: **Could‑have**  
**Rủi ro**: Xung đột dữ liệu khi offline dài ngày; bảo mật API.

---

## 4. Yêu cầu phi chức năng / Non‑functional Requirements

| Nhóm / Category | VI (Mô tả) | EN (Description) | Chỉ tiêu / Target |
|---|---|---|---|
| Hiệu năng | Mở danh sách < 200 ms; TTI màn hình Home < 2 s | List open < 200 ms; Home TTI < 2 s | TTI < 2 s |
| UX Frame‑rate | Cuộn mượt, ≥ 60 FPS | Smooth scroll, ≥ 60 FPS | 60 FPS |
| Bảo mật cục bộ | DB Room mã hoá (nếu bật), DataStore private, không log dữ liệu nhạy cảm | Local DB encryption (if enabled), private DataStore, no sensitive logs | Không rò rỉ |
| i18n/l10n | JA/VI/EN đủ coverage, plural/rtl ready | JA/VI/EN coverage, plural/rtl ready | ≥ 95% strings |
| Accessibility | Text scaling, TalkBack labels, contrast | Text scaling, TalkBack, contrast | Meets WCAG‑AA |
| Độ tin cậy nhắc học | WorkManager chạy ≥ 95%/ngày | WorkManager fires ≥ 95%/day | ≥ 95% |
| Testability | DAO unit tests; UI smoke; SRS logic tests | DAO/unit; UI smoke; SRS tests | CI green > 95% |

---

## 5. Dữ liệu & Từ điển thuật ngữ / Data & Glossary

### 5.1 Định nghĩa dữ liệu lõi — VI/EN
| Entity | Fields (rút gọn) | Notes |
|---|---|---|
| **Kanji** | id, character, onyomi[], kunyomi[], meanings{vi,en}, jlptLevel, strokes, radicalId | Seed |
| **Radical** | id, name, variants, strokes | Linked by radicalId |
| **Card** | id, kanjiId, deckId, note, tags | 1‑1 với Kanji mặc định |
| **ReviewLog** | id, cardId, lastReviewedAt, nextDueAt, intervalDays, easeFactor, repetitions, lapses, algorithm | SM‑2/Leitner |
| **Deck** | id, name, jlptLevel, createdAt | Prebuilt N5→N1 |

### 5.2 Glossary — VI/EN
**JLPT**: Chuẩn năng lực Nhật ngữ; **SRS**: thuật toán lặp lại cách quãng; **Radical**: bộ thủ; **Due**: đến hạn ôn.

---

## 6. Giới hạn phạm vi / Out‑of‑Scope

**VI**: OCR/nhận dạng ảnh Kanji; diễn đàn/xã hội; bài tập ngữ pháp; TTS/giọng nói nâng cao; backend bắt buộc.  
**EN**: OCR Kanji; social/community; grammar exercises; advanced TTS; mandatory backend.

---

## 7. Tiêu chí nghiệm thu & Đo lường / Acceptance & Metrics

- **Stability**: Crash‑free users ≥ **99.5%** (crash rate < **0.5%**).  
- **Performance**: Home **TTI < 2 s**; search **< 200 ms** offline.  
- **Battery**: Background work < **1%/day** on mid‑range devices.  
- **Reminder Reliability**: WorkManager fire rate ≥ **95%** daily.  
- **Storage**: App data (seed + logs) ≤ **150 MB**.  
- **L10n Coverage**: ≥ **95%** strings translated JA/VI/EN.

---

## 8. Phụ lục / Appendix

### 8.1 Ma trận ưu tiên (MoSCoW)
| Epic/Feature | M/S/C/W | Ghi chú / Notes |
|---|---|---|
| Onboarding & Sign‑In | **M** | Guest + Google |
| JLPT Lists & Detail | **M** | Core content |
| SRS/Flashcards | **M** | SM‑2/Leitner |
| Search/Bookmark/Notes | **M** | FTS offline |
| Settings (Theme/Font/Lang) | **S** | i18n |
| Offline Seed | **M** | Offline‑first |
| Sync | **C** | Optional backend |

### 8.2 Ma trận truy vết / Traceability Matrix
| Req ID | Use Case | Test Case |
|---|---|---|
| FR‑ONB‑01 | US‑01 (Choose sign‑in) | TC‑ONB‑01 (guest), TC‑ONB‑02 (Google) |
| FR‑KAN‑01 | US‑02 (Browse JLPT) | TC‑KAN‑01 (N5 list), TC‑KAN‑02 (Detail) |
| FR‑SRS‑01 | US‑03 (Review due) | TC‑SRS‑01 (schedule), TC‑SRS‑02 (persist) |
| FR‑SRH‑01 | US‑04 (Search) | TC‑SRH‑01 (FTS offline) |
| FR‑SET‑01 | US‑06 (Language) | TC‑SET‑01 (persist & reload) |
| FR‑OFF‑01 | US‑05 (Seed) | TC‑OFF‑01 (no‑network flow) |
| FR‑SYN‑01 | US‑07 (Sync) | TC‑SYN‑01 (reconcile) |

### 8.3 UI Checklist (quick)
- Navigation rõ ràng (Home, Study, Search, Bookmarks, Settings).  
- Nút hành động lớn, vùng chạm ≥ 48dp.  
- Font Nhật hiển thị tốt Kanji (ligatures/variants).  
- State rỗng (empty states) có hướng dẫn.  
- Dark mode đạt tương phản.  
- TalkBack labels cho icon & hình.

---

## 9. Ràng buộc build & cấu hình (tóm tắt) / Build & Config Summary

- **Java‑only**, Android Studio, **Gradle Kotlin DSL**.  
- `minSdk=24`, `targetSdk=35`.  
- Libraries: Room, Hilt, Navigation, DataStore, WorkManager, Retrofit (optional).  
- Offline seed bundled; SRS scheduler implemented locally; sync optional.

---

**Kết thúc SRS / End of SRS**
