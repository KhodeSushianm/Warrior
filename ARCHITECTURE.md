# WARRIOR — ARCHITECTURE & DESIGN DOCUMENT

## سند معماری و طراحی اپلیکیشن Warrior

**Warrior — ردیاب تمرین شخصی برای Boxing و Strength؛ Local-first، دوزبانه، با تقویم شمسی**

|مشخصه|مقدار|
|---|---|
|نام اپلیکیشن|Warrior|
|Package / namespace|`com.warrior.tracker`|
|نسخه سند|۰.۱|
|وضعیت|Draft — مبنای طراحی و پیاده‌سازی|
|پلتفرم|Android (Kotlin، Jetpack Compose)|
|دامنه V1|Boxing + Strength / Bodyweight|
|نوع استفاده|شخصی، Local-first، بدون Backend|
|زبان‌ها|فارسی (RTL، تقویم شمسی) و انگلیسی (LTR)|
|Database|SQLite|
|ORM|Room (KSP، `exportSchema = true`)|
|معماری نرم‌افزار|Layered + MVVM + Dependency Injection (Hilt)|
|منبع حقیقت|داده‌ی خام Workout (Set و Round)|

> **ساده برای استفاده، مفید برای ثبت، معنادار برای مرور**

## فهرست مطالب

برای رفتن به هر بخش روی عنوان آن کلیک کنید.

- [درباره‌ی این سند](#h1)
- [۱. چشم‌انداز و اصول محصول](#h2)
    - [۱.۱ اصول معماری](#h3)
    - [۱.۲ اهداف و اصول معماری داده](#h4)
- [۲. محدوده نسخه ۱](#h6)
- [۳. ساختار Navigation و صفحات](#h7)
- [۴. معماری نرم‌افزار](#h8)
    - [۴.۱ قانون عبور از لایه‌ها](#h9)
    - [۴.۲ محاسبات Home](#h10)
    - [۴.۳ ساختار Package](#h11)
    - [۴.۴ جریان داده (Data Flow)](#h12)
- [۵. Technology Stack](#h13)
- [۶. مدل دامنه (Domain Model)](#h14)
    - [۶.۱ تعریف Activity](#h15)
    - [۶.۲ چرا Activity یک لایه‌ی مستقل است](#h16)
    - [۶.۳ سلسله‌مراتب مدل](#h17)
    - [۶.۴ Workout](#h18)
    - [۶.۵ مدل Strength](#h19)
    - [۶.۶ مدل Boxing](#h21)
    - [۶.۷ Enumهای نهایی](#h22)
- [۷. تعاریف محاسباتی: Volume، PR، Intensity و Goal](#h23)
    - [۷.۱ Volume و Strength Metrics](#h24)
    - [۷.۲ Boxing Metrics](#h25)
    - [۷.۳ Personal Record](#h26)
    - [۷.۴ Intensity](#h27)
    - [۷.۵ Goal](#h28)
    - [۷.۶ معماری Progress](#h29)
- [۸. Workout در حال انجام و Timer](#h30)
    - [۸.۱ ذخیره‌ی مرحله‌ای (Draft)](#h31)
    - [۸.۲ Timer Engine و Foreground Service](#h32)
    - [۸.۳ جریان ثبت](#h33)
    - [۸.۴ Transactionها](#h34)
    - [۸.۵ Duplicate Workout](#h35)
- [۹. Exercise Library](#h36)
    - [۹.۱ ساختار](#h37)
    - [۹.۲ دسته‌بندی‌ها](#h38)
    - [۹.۳ Seeding و نسخه‌بندی](#h39)
    - [۹.۴ پوشش Bodyweight و Seed نمونه](#h40)
    - [۹.۵ Exercise شخصی](#h41)
    - [۹.۶ تحویل مرحله‌ای](#h42)
- [۱۰. معماری داده و Schema نهایی](#h43)
    - [۱۰.۱ تصمیم‌های کلیدی Schema](#h44)
    - [۱۰.۲ ERD مفهومی](#h45)
    - [۱۰.۳ فهرست جداول V1](#h46)
    - [۱۰.۴ جدول `workouts`](#h47)
    - [۱۰.۵ جدول `activities`](#h48)
    - [۱۰.۶ جدول `sets`](#h49)
    - [۱۰.۷ جدول `rounds`](#h50)
    - [۱۰.۸ جدول `exercises`](#h51)
    - [۱۰.۹ جدول `exercise_muscles`](#h52)
    - [۱۰.۱۰ جدول `goals`](#h53)
    - [۱۰.۱۱ جدول `body_weights`](#h54)
    - [۱۰.۱۲ جدول `exercise_daily_stats` (Cache)](#h55)
    - [۱۰.۱۳ جدول `pr_events` (Cache)](#h56)
    - [۱۰.۱۴ جداولی که عمداً وجود ندارند](#h57)
    - [۱۰.۱۵ ایندکس‌ها](#h58)
    - [۱۰.۱۶ ساختار Room و DAOها](#h59)
    - [۱۰.۱۷ Aggregate جزئیات Workout](#h61)
    - [۱۰.۱۸ استراتژی Cache و Performance](#h62)
    - [۱۰.۱۹ Calendar و History از دید داده](#h63)
    - [۱۰.۲۰ Schema نهایی در یک نگاه](#h64)
- [۱۱. یکپارچگی داده، ویرایش و حذف](#h65)
    - [۱۱.۱ سیاست Foreign Key](#h66)
    - [۱۱.۲ ساختار Foreign Key](#h67)
    - [۱۱.۳ Exercise](#h68)
    - [۱۱.۴ ویرایش و حذف Workout](#h69)
    - [۱۱.۵ اصول Integrity](#h70)
- [۱۲. Backup، Restore و Migration](#h71)
- [۱۳. Localization، تقویم شمسی و واحدها](#h72)
- [۱۴. طراحی ثبت سریع (Fast Input)](#h73)
    - [۱۴.۱ Summary پایان Workout](#h74)
    - [۱۴.۲ طراحی بصری](#h75)
- [۱۵. Validation و Error Handling](#h76)
- [۱۶. استراتژی Testing](#h77)
- [۱۷. الزامات غیرعملکردی، Performance و Privacy](#h78)
- [۱۸. خارج از Scope و نقشه راه](#h79)
    - [۱۸.۱ خارج از Scope نسخه ۱](#h80)
    - [۱۸.۲ مراحل پیاده‌سازی پیشنهادی](#h81)
    - [۱۸.۳ اولویت‌های بعد از V1](#h82)
- [پیوست الف: ردیابی ضعف‌ها و راه‌حل‌ها](#h83)

---

<a id="h1"></a>
# درباره‌ی این سند

این سند، بازنگری کامل پیش‌نویس اولیه است. همه‌ی ضعف‌های شناسایی‌شده در بررسی معماری (۲۷ مورد) به‌صورت تصمیم صریح در متن سند اعمال شده‌اند. جدول ردیابی هر ضعف به راه‌حل و بخش مربوط در پیوست الف آمده است.

شماره‌ی نسخه از ۰.۱ آغاز می‌شود؛ این سند نخستین مبنای یکپارچه‌ی طراحی و پیاده‌سازی است و یک مرجع واحد و خودبسنده به‌شمار می‌رود: هم معماری محصول و نرم‌افزار و هم معماری داده (ERD، تعریف ستون‌به‌ستون جداول، Enumها، ایندکس‌ها، DAOها، Transactionها و Seed کتابخانه) در یک ساختار منسجم و بدون ارجاع بیرونی گرد هم آمده‌اند.

> **مهم‌ترین تغییرات نسبت به پیش‌نویس اولیه**
>
> - مدل Set/Bodyweight بازطراحی شد و حرکات زمان‌محور (Plank، Hold) به‌صورت رسمی پشتیبانی می‌شوند.
> - Volume، PR، Intensity و Goal تعریف دقیق و قابل‌تست دارند؛ آمار از روی Cache بازسازی‌پذیر خوانده می‌شود.
> - Workout در حال انجام: ذخیره‌ی مرحله‌ای (Draft) + Foreground Service برای Timer.
> - Schema یک‌پارچه شد: جداول تکراری حذف، کلید UUID، Soft Delete و سیاست Foreign Key مشخص.
> - Backup (JSON)، Migration، Seed کتابخانه، تقویم شمسی، Testing و DI به معماری اضافه شد.

---

<a id="h2"></a>
# ۱. چشم‌انداز و اصول محصول

**Warrior** یک Workout Tracker شخصی برای ثبت و تحلیل تمرین‌های Boxing و Strength/Bodybuilding است. سه کار اصلی را باید عالی انجام دهد: ثبت سریع تمرین، نگهداری و مشاهده‌ی تاریخچه، و نمایش قابل‌فهم پیشرفت. هدف این نیست که اپ به سیستم Coaching، تغذیه یا Wearable تبدیل شود.

<a id="h3"></a>
## ۱.۱ اصول معماری

|اصل|توضیح|
|---|---|
|Workout مرکز سیستم است|همه‌ی Trackingها از داده‌ی Workout سرچشمه می‌گیرند؛ نه Boxing و نه Strength مرکز مدل داده نیستند.|
|منبع حقیقت، داده‌ی خام است|Set و Round تنها منبع حقیقت‌اند. هر آمار یا PR «مشتق» است و با Cache بازسازی‌پذیر نگهداری می‌شود، نه ورودی دستی.|
|UI ساده، مدل داده دقیق|ظاهر ساده؛ اما تعریف‌های ریاضی (Volume، PR، Goal) بدون ابهام نوشته شده‌اند.|
|Local First|در V1 بدون Backend، بدون Account و حتی بدون مجوز INTERNET.|
|هرگز داده‌ی کاربر از دست نرود|ذخیره‌ی مرحله‌ای، Soft Delete، Migration امن و Backup از روز اول.|
|Avoid Overengineering|هر جزء فقط وقتی وارد می‌شود که ارزش واقعی برای Tracking داشته باشد (مثلاً UseCase فقط برای منطق واقعی).|
|Extensible Core|کلیدهای UUID و فیلد `updated_at`، افزودن Sync یا Wearable را بدون تغییر مدل Workout ممکن می‌کنند.|
|یک Workout می‌تواند ترکیبی باشد|Strength + Boxing در یک Workout؛ «نوع Workout» ذخیره نمی‌شود و از Activityها مشتق می‌شود.|

<a id="h4"></a>
## ۱.۲ اهداف و اصول معماری داده

دیتابیس باید این نیازها را پوشش دهد:

- ثبت Workout
- امکان ترکیب Boxing و Strength در یک Workout
- ثبت Exercise و Set (شامل حرکات زمان‌محور)
- ثبت تمرین‌های Bodyweight
- ثبت Boxing Activity و Round
- ثبت داده‌های مرتبط با Timer (زمان برنامه‌ریزی‌شده و زمان واقعی)
- نگهداری Exercise Library
- نگهداری عضلات و دسته‌بندی Exerciseها
- ثبت Body Weight
- پشتیبانی از Goals
- امکان محاسبه‌ی Progress
- پشتیبانی از History و Calendar
- امکان Edit / Delete / Duplicate Workout
- جلوگیری از Duplicate شدن داده‌های محاسباتی
- امکان توسعه در آینده

و در عین حال:

> **Database نباید پیچیده‌تر از نیاز واقعی اپ باشد.**

<a id="h5"></a>
### قرارداد نام‌گذاری

|موضوع|قرارداد|
|---|---|
|ستون‌های دیتابیس|`snake_case` (مطابق Room و SQL)|
|پراپرتی‌های Kotlin|`camelCase`؛ نگاشت با `@ColumnInfo` یا نام یکسان در Entity|
|Enumها|در Domain به‌صورت Enum تعریف و در دیتابیس به‌صورت `TEXT` ذخیره می‌شوند (با TypeConverter) تا String نامعتبر وارد نشود|
|زمان‌ها|همه‌ی زمان‌ها UTC و به‌صورت Epoch Milliseconds (`INTEGER`)|
|وزن|همیشه `kg` و از نوع `REAL`|
|کلیدها|داده‌ی کاربر: `UUID` به‌صورت `TEXT`؛ Exercise داخلی: کلید متنی پایدار|

---

<a id="h6"></a>
# ۲. محدوده نسخه ۱

|حوزه|قابلیت‌ها|
|---|---|
|Workout|ساخت، ویرایش، حذف (با Undo)، Duplicate، تاریخ، مدت، Notes، ترکیب Boxing و Strength در یک Workout|
|Boxing|Shadow Boxing، Heavy Bag، Pads، Sparring، Jump Rope، Custom؛ Round، Rest، Intensity، Note؛ Round Timer و Rest Timer|
|Strength|Exercise، Set، Weight، Reps یا Duration، RPE اختیاری، Rest؛ حرکات Bodyweight، Weighted، Bodyweight+Weight و Assisted|
|Progress|Strength: Max Weight، Max Reps، Tonnage، e1RM، PR. Boxing: تعداد Session، Round، زمان کل، Intensity Trend. نمودار برای همه|
|History|لیست Workoutها با دو نما (List و Calendar)، فیلتر نوع/تاریخ، جزئیات، ویرایش و حذف|
|Home|آخرین Workout، خلاصه‌ی هفتگی، Goalها، شروع سریع، بازگشت به Workout نیمه‌تمام|
|Goals|وزنه، تعداد Session/Round/زمان در بازه (هفتگی/ماهانه)، وزن بدن|
|Body Tracking|Weight، History و Chart|
|Exercise Library|کتابخانه‌ی جامع (تمرکز Bodyweight)، جست‌وجو، فیلتر، افزودن تمرین شخصی|
|Backup|Export/Import به JSON و Auto Backup اندروید|

---

<a id="h7"></a>
# ۳. ساختار Navigation و صفحات

در بازبینی، دو تب «Calendar» و «History» هر دو نمایشی از یک داده (Workoutهای ثبت‌شده) بودند و با اصل Avoid Overengineering نمی‌خواندند. در این نسخه ادغام شدند و تب چهارم به Progress اختصاص یافت که پیش‌تر صفحه‌ی مستقلی در Navigation نداشت.

```
Bottom Navigation (۴ تب)
┌────────┬──────────┬─────────┬──────────┐
│  Home  │ Workouts │ History │ Progress │
└────────┴──────────┴─────────┴──────────┘
                ⚙ Settings (از آیکون در Home)
```

|تب|محتوا|
|---|---|
|Home|Overview: آخرین Workout، خلاصه‌ی هفتگی، Goalها، دکمه‌ی شروع/ادامه‌ی Workout|
|Workouts|شروع Workout جدید، ثبت دستی Workout گذشته، Duplicate از تمرین قبلی، ورود به Exercise Library|
|History|سوئیچ List / Calendar. تقویم روزهای تمرینی را نشان می‌دهد و با لمس هر روز، Workoutهای آن روز باز می‌شود|
|Progress|تب‌های Strength، Boxing، Body Weight و Goals؛ نمودارها و PRها|

Settings از طریق آیکون در Home باز می‌شود و تب مستقل ندارد.

---

<a id="h8"></a>
# ۴. معماری نرم‌افزار

معماری Layered + MVVM با Dependency Injection (Hilt) انتخاب می‌شود. وابستگی‌ها فقط رو به پایین‌اند و Domain هیچ وابستگی به Android ندارد (Pure Kotlin) تا منطق Progress بدون Emulator تست شود.

```
┌─────────────────────────────────────────────────────────────┐
│ Presentation                                                │
│ Compose UI · Screens · Navigation · ViewModels (StateFlow)   │
├─────────────────────────────────────────────────────────────┤
│ Domain (Pure Kotlin)                                        │
│ Models · Repository Interfaces · UseCases                   │
│ Calculators (Volume, PR, Intensity, Goal)                   │
├─────────────────────────────────────────────────────────────┤
│ Data                                                        │
│ Repository Impl · DAOs · Mappers · Seed Importer            │
│ Backup Serializer                                           │
├─────────────────────────────────────────────────────────────┤
│ Room (SQLite)                                               │
│ Entities · Migrations · Indexes                             │
├─────────────────────────────────────────────────────────────┤
│ Platform Services                                           │
│ WorkoutTimerService (Foreground) · DataStore                │
│ Notifications · SoundPool                                   │
└─────────────────────────────────────────────────────────────┘
```

<a id="h9"></a>
## ۴.۱ قانون عبور از لایه‌ها

در پیش‌نویس اولیه نمودار جریان همه‌چیز را از UseCase عبور می‌داد، در حالی که متن می‌گفت UseCase فقط برای منطق واقعی است. قانون نهایی:

|نوع عملیات|مسیر|مثال|
|---|---|---|
|خواندن ساده|ViewModel ← Repository ← DAO|لیست History، جزئیات Workout، لیست Exercise|
|خواندن ترکیبی / محاسباتی|ViewModel ← UseCase ← Repository|GetHomeSummary، GetExerciseProgress|
|نوشتن ساده|ViewModel → Repository|ثبت وزن بدن، ذخیره‌ی Note|
|نوشتن با منطق|ViewModel → UseCase → Repository (داخل Transaction)|FinishWorkout، DeleteWorkout، LogSet (با تشخیص PR)|

<a id="h10"></a>
## ۴.۲ محاسبات Home

Home منطق سنگین ندارد. آمار هفتگی با Queryهای SUM/COUNT روی ایندکس تاریخ و Cache آماری (بخش ۱۰) محاسبه می‌شود و از طریق یک `GetHomeSummaryUseCase` به‌صورت Flow ترکیب می‌شود. هیچ حلقه‌ی محاسباتی در ViewModel یا Composable وجود ندارد.

<a id="h11"></a>
## ۴.۳ ساختار Package

```
com.warrior.tracker
├── core
│   ├── common        — نتیجه‌ها، خطاها، ثابت‌ها
│   ├── time          — Clock، CalendarSystem (Jalali/Gregorian)، NumberInputParser
│   ├── validation    — قواعد محدوده‌ی ورودی‌ها
│   ├── format        — UnitFormatter، NumberFormat
│   ├── ui            — Theme، کامپوننت‌های مشترک
│   └── navigation    — گراف و مسیرها
├── data
│   ├── local
│   │   ├── database  — AppDatabase، Converters
│   │   ├── dao
│   │   ├── entity
│   │   └── migration
│   ├── repository    — پیاده‌سازی Repositoryها
│   ├── mapper        — Entity ↔ Domain
│   ├── seed          — ExerciseSeedImporter
│   └── backup        — BackupSerializer، BackupImporter
├── domain
│   ├── model
│   ├── repository    — Interfaceها
│   ├── usecase
│   └── calculator    — Volume، PR، Intensity، Goal، Timer
├── service           — WorkoutTimerService، TimerEngine
├── feature
│   ├── home
│   ├── workout
│   ├── boxing
│   ├── strength
│   ├── history
│   ├── progress
│   ├── goals
│   ├── exercises
│   ├── bodyweight
│   └── settings
├── di                — Hilt modules
└── MainActivity.kt
```

`com.warrior.tracker` هم‌زمان `namespace` و `applicationId` در `build.gradle.kts` و ریشه‌ی Package کد است.

در V1 ساختار Multi-module لازم نیست؛ مرز لایه‌ها با Package و قواعد Lint/ArchUnit حفظ می‌شود.

<a id="h12"></a>
## ۴.۴ جریان داده (Data Flow)

```
                    USER
                     │
                     ▼
                Compose UI
                     │
                     ▼
                 ViewModel
                     │
                     ▼
              Domain / UseCase        ← فقط برای عملیات ترکیبی/محاسباتی و نوشتن با منطق
                     │                 (بخش ۴.۱)
                     ▼
                 Repository
                     │
                     ▼
                   Room
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
     Workout      Exercise      Goals
        │
   ┌────┴─────┐
   ▼          ▼
Strength    Boxing
   │          │
   ▼          ▼
  Sets      Rounds
```

UI نباید مستقیماً چند DAO را مدیریت کند؛ دسترسی به داده همیشه از مسیر Repository انجام می‌شود.

---

<a id="h13"></a>
# ۵. Technology Stack

|حوزه|انتخاب|توضیح|
|---|---|---|
|زبان|Kotlin|Coroutines و Flow|
|UI|Jetpack Compose + Material 3|Dark-first، RTL/LTR|
|State|ViewModel + StateFlow|یک UiState برای هر Screen|
|DI|Hilt|اضافه‌شده؛ با این تعداد Repository و ViewModel ضروری است|
|Database|Room + KSP (SQLite)|`exportSchema = true`، Migration صریح|
|Preferences|DataStore|تنها محل تنظیمات؛ جدول settings حذف شد|
|Navigation|Navigation Compose||
|Background|Foreground Service|Timer و Workout فعال|
|Serialization|kotlinx.serialization|Backup JSON و Seed کتابخانه|
|Time|java.time + android.icu PersianCalendar|تقویم شمسی فقط در لایه‌ی نمایش|
|Testing|JUnit5/4، Turbine، Room in-memory، Compose UI Test|بخش ۱۶|
|minSdk|26|سازگاری با java.time و Notification Channel|

---

<a id="h14"></a>
# ۶. مدل دامنه (Domain Model)

<a id="h15"></a>
## ۶.۱ تعریف Activity

در پیش‌نویس اولیه معنی Activity مبهم بود. تعریف نهایی: **Activity یک «آیتم انجام‌شده» داخل Workout است**؛ یعنی یک حرکت Strength (مثلاً Bench Press با چند Set) یا یک بخش Boxing (مثلاً Heavy Bag با چند Round). جدول‌های میانی `strength_entries`/`strength_exercises` و `boxing_activities` حذف و در `activities` ادغام شدند.

```
Workout
├── Activity #1 — type = STRENGTH, exercise = Bench Press
│   ├── Set 1 (80 kg × 8)
│   ├── Set 2 (80 kg × 7)
│   └── Set 3 (80 kg × 6)
├── Activity #2 — type = STRENGTH, exercise = Pull-Up
│   └── Set 1..n (bodyweight × reps)
├── Activity #3 — type = BOXING, boxingType = HEAVY_BAG
│   ├── Round 1 (3:00 / rest 1:00)
│   └── Round 2 ...
└── Activity #4 — type = BOXING, boxingType = SHADOW_BOXING
```

ترکیب Boxing و Strength در یک Workout کاملاً مجاز است. «نوع Workout» ذخیره نمی‌شود و از Activityها مشتق می‌شود.

<a id="h16"></a>
## ۶.۲ چرا Activity یک لایه‌ی مستقل است

این تصمیم یکی از مهم‌ترین بخش‌های مدل داده است. یک Workout می‌تواند هم‌زمان چند بخش داشته باشد:

```
Workout #20
│
├── Chest Strength
├── Back Strength
└── Heavy Bag
```

هرکدام یک Activity است. در نتیجه Workout خودش نمی‌داند دقیقاً چه چیزی داخلش است؛ Activityها مشخص می‌کنند.

این ساختار برای آینده هم مناسب است. اگر روزی نوع دیگری اضافه شود:

```
CONDITIONING
MOBILITY
CARDIO
```

نیازی نیست مدل Workout تغییر اساسی کند؛ فقط یک مقدار جدید به Enum نوع Activity افزوده می‌شود.

رابطه:

```
Workout 1 ─────── N Activity
```

<a id="h17"></a>
## ۶.۳ سلسله‌مراتب مدل

```
Workout
   │
   └── Activity
          │
          ├── type = STRENGTH ── exercise (از Library) ── Set (1..n)
          │
          └── type = BOXING   ── boxing_type              ── Round (1..n)
```

یعنی:

```
Workout
   ↓
Activities
   ↓
Strength / Boxing
   ↓
Sets / Rounds
```

نه اینکه Boxing و Strength دو سیستم کاملاً جدا داشته باشند.

<a id="h18"></a>
## ۶.۴ Workout

|فیلد|نوع|توضیح|
|---|---|---|
|id|UUID|کلید یکتا|
|started_at / ended_at|Epoch ms (UTC)|لحظه‌ی دقیق شروع و پایان|
|local_date|ISO `yyyy-MM-dd` (میلادی)|روز تقویمی برای گروه‌بندی؛ نمایش شمسی فقط تبدیل است|
|timezone_id|String|منطقه‌ی زمانی هنگام ثبت؛ تغییر سفر، تاریخ قدیمی را جابه‌جا نمی‌کند|
|status|IN_PROGRESS / COMPLETED|برای Draft (بخش ۸)|
|active_duration_sec|Int|مدت فعال بدون Pause|
|session_rpe|Real 1..10 (گام ۰٫۵)؟|شدت کلی، اختیاری، ورودی کاربر|
|body_weight_kg_snapshot|Double؟|آخرین وزن بدن ثبت‌شده هنگام پایان Workout|
|notes|String؟||
|created_at / updated_at|Epoch ms|برای Merge و Sync آینده|

ذخیره‌سازی زمان به‌صورت Unix Timestamp / Epoch Milliseconds است و UI مسئول تبدیل آن به تاریخ و ساعت محلی (و نمایش شمسی) است.

<a id="h19"></a>
## ۶.۵ مدل Strength

در پیش‌نویس اولیه سه فیلد `weight`، `additionalWeight` و `loadType` روی Set معنای هم‌پوشان داشتند. اکنون **نوع بار و نوع اندازه‌گیری ویژگی Exercise هستند (نه Set)** و Set فقط یک فیلد بار خارجی دارد.

|ویژگی Exercise|مقدارها|معنا|
|---|---|---|
|load_type|WEIGHTED|وزنه کل بار است (Bench Press، Squat). `external_load_kg > 0`|
||BODYWEIGHT|فقط وزن بدن (Push-Up). `external_load_kg = 0`|
||BODYWEIGHT_PLUS|وزن بدن + وزنه‌ی اضافه (Pull-Up). `external_load_kg ≥ 0`؛ مقدار صفر یعنی اجرای بدون وزنه‌ی اضافه|
||ASSISTED|کمک (Band/ماشین). `external_load_kg` مقدار کمک است و منفی ذخیره نمی‌شود؛ با فلگ نوع، در نمایش «−» می‌گیرد|
|measure_type|REPS|ثبت تعداد تکرار؛ `reps` پر و `duration_sec` خالی است|
||DURATION|ثبت زمان (Plank، Hollow Hold، Handstand Hold). `reps` خالی و `duration_sec` پر است|

مثال‌های ثبت:

|حرکت|load_type|measure_type|ثبت در Set|
|---|---|---|---|
|Push-Up، ۱۰ تکرار|BODYWEIGHT|REPS|`external_load_kg = 0`، `reps = 10`|
|Bench Press، ۶۰kg × ۱۰|WEIGHTED|REPS|`external_load_kg = 60`، `reps = 10`|
|Pull-Up، وزن بدن + ۱۰kg × ۸|BODYWEIGHT_PLUS|REPS|`external_load_kg = 10`، `reps = 8`|
|Pull-Up، فقط وزن بدن × ۸|BODYWEIGHT_PLUS|REPS|`external_load_kg = 0`، `reps = 8`|
|Assisted Pull-Up، کمک ۲۰kg × ۶|ASSISTED|REPS|`external_load_kg = 20` (نمایش: −۲۰)|
|Plank، ۶۰ ثانیه|BODYWEIGHT|DURATION|`duration_sec = 60`، `reps = NULL`|

> **اصلاح نسبت به پیش‌نویس اولیه**
>
> پیش‌تر Pull-Up بدون وزنه با Volume=0 ثبت می‌شد و حرکات زمان‌محور اصلاً قابل ثبت نبودند. هر دو مورد با مدل بالا و قواعد بخش ۷ حل شده‌اند.
>
> انواع Set: `NORMAL`، `WARMUP`، `DROP`، `FAILURE`. Setهای WARMUP در Volume و PR محاسبه نمی‌شوند.

<a id="h20"></a>
### چرا وزن بدن را در Set ذخیره نمی‌کنیم؟

چون وزن بدن در طول زمان تغییر می‌کند. اگر امروز کاربر Pull-Up با وزن بدن بزند و وزنش 75kg باشد، نباید در Set مقدار `75kg` ذخیره شود و با وزنه‌ی خارجی اشتباه گرفته شود. در نتیجه:

```
load_type = BODYWEIGHT  (یا BODYWEIGHT_PLUS با بار خارجی صفر)
external_load_kg = 0
```

و اگر بخواهیم Effective Load را محاسبه کنیم، از `body_weight_kg_snapshot` همان Workout (یا وزن بدن ثبت‌شده در همان تاریخ) استفاده می‌کنیم؛ نه از یک حدس یا مقدار قدیمی (بخش ۷.۱).

<a id="h21"></a>
## ۶.۶ مدل Boxing

|مفهوم|فیلدها|
|---|---|
|Activity (BOXING)|`boxing_type` ∈ {SHADOW_BOXING، HEAVY_BAG، PADS، SPARRING، JUMP_ROPE، CUSTOM}؛ برای CUSTOM نام سفارشی در `custom_name`|
|Round|`round_number`، `planned_duration_sec`، `duration_sec` (واقعی)، `planned_rest_sec`، `rest_sec` (واقعی)، `intensity` 1..10 اختیاری، `note`|

مثال:

```
Heavy Bag
│
├── Round 1 → 180 sec / rest 60 sec
├── Round 2 → 180 sec / rest 60 sec
├── Round 3 → 180 sec / rest 60 sec
└── Round 4 → 180 sec / rest 60 sec
```

مدت و شدت کل Activity ذخیره نمی‌شود و از Roundها مشتق می‌شود؛ بنابراین داده‌ی تکراری ندارد. نام Activity نیز ذخیره نمی‌شود و از `boxing_type` (یا `custom_name`) و در حالت Strength از Exercise مشتق است.

Intensity در دیتابیس فقط به‌صورت عدد صحیح ۱ تا ۱۰ ذخیره می‌شود؛ UI مسئول نمایش Slider یا Rating مناسب است.

<a id="h22"></a>
## ۶.۷ Enumهای نهایی

برای جلوگیری از Stringهای نامعتبر، این موارد در Domain به‌صورت Enum تعریف و با TypeConverter در دیتابیس ذخیره می‌شوند:

```
WorkoutStatus        IN_PROGRESS | COMPLETED

ActivityType         STRENGTH | BOXING

BoxingType           SHADOW_BOXING | HEAVY_BAG | PADS | SPARRING | JUMP_ROPE | CUSTOM

SetType              NORMAL | WARMUP | DROP | FAILURE

LoadType             WEIGHTED | BODYWEIGHT | BODYWEIGHT_PLUS | ASSISTED

MeasureType          REPS | DURATION

PRType               MAX_WEIGHT | REPS_AT_WEIGHT | BEST_E1RM | BEST_SET_VOLUME | MAX_DURATION

ExerciseDifficulty   BEGINNER | INTERMEDIATE | ADVANCED

Equipment            NONE | PULL_UP_BAR | PARALLEL_BARS | BENCH | BARBELL | DUMBBELL
                     CABLE | MACHINE | KETTLEBELL | RESISTANCE_BAND | OTHER

Muscle               CHEST | BACK | SHOULDERS | BICEPS | TRICEPS | FOREARMS
                     ABS | OBLIQUES | LOWER_BACK | GLUTES | QUADRICEPS | HAMSTRINGS
                     CALVES | HIPS | NECK | FULL_BODY

MuscleRole           PRIMARY | SECONDARY

GoalType             STRENGTH_WEIGHT | STRENGTH_REPS | SESSIONS | ROUNDS
                     TRAINING_TIME | BODY_WEIGHT

GoalPeriod           NONE | WEEKLY | MONTHLY

GoalStatus           ACTIVE | ACHIEVED | ARCHIVED
```

مقدارهای مشتق (در دیتابیس ذخیره نمی‌شوند):

```
GoalUnit (از GoalType مشتق)     KG | REPS | ROUNDS | SESSIONS | MINUTES

دسته‌ی نمایشی (از عضله‌ی اصلی مشتق)
    CORE     = { ABS, OBLIQUES, LOWER_BACK }
    CHEST / BACK / SHOULDERS / ARMS / LEGS / ... = نگاشت مستقیم عضله‌ی PRIMARY
```

`NONE` در Equipment به معنای «بدون تجهیز / Bodyweight» است و در UI با عنوان Bodyweight نمایش داده می‌شود.

---

<a id="h23"></a>
# ۷. تعاریف محاسباتی: Volume، PR، Intensity و Goal

این بخش هسته‌ی ارزش محصول است. همه‌ی تعریف‌ها در `domain/calculator` به‌صورت توابع خالص پیاده و با Unit Test پوشش داده می‌شوند. **فقط Setهای Completed و غیر WARMUP شمرده می‌شوند.**

<a id="h24"></a>
## ۷.۱ Volume و Strength Metrics

|نوع Exercise|شاخص اصلی|فرمول|
|---|---|---|
|WEIGHTED|Tonnage|`SUM(external_load × reps)`|
|BODYWEIGHT_PLUS|Tonnage + Total Reps|`SUM(external_load × reps)` و `SUM(reps)`|
|BODYWEIGHT|Total Reps|`SUM(reps)`|
|ASSISTED|Total Reps|`SUM(reps)` (بار کمکی در Tonnage نمی‌آید)|
|DURATION|Total Time|`SUM(duration_sec)`|

Volume یک Set برابر است با `Weight × Reps` و Total Volume برابر `SUM(Weight × Reps)`؛ که در آن Weight همان `external_load_kg` است.

**Effective Volume (شاخص ثانویه):** برای BODYWEIGHT_PLUS اگر `body_weight_kg_snapshot` موجود باشد، مجموع حاصل‌ضرب (وزن بدن + بار خارجی) در تعداد تکرار نیز نمایش داده می‌شود. اگر وزن بدن ثبت نشده باشد این شاخص نمایش داده نمی‌شود، نه اینکه صفر یا حدس باشد.

**Workout Tonnage** = جمع Tonnage همه‌ی Activityهای Strength. این مقدار هرگز ذخیره‌ی دستی نیست و با ویرایش Workout خودکار تغییر می‌کند.

شاخص‌های Strength که از `sets` محاسبه می‌شوند:

```
Max Weight      → MAX(external_load_kg) برای هر Exercise
Max Reps        → MAX(reps)
Volume/Tonnage  → SUM(external_load_kg × reps)
e1RM            → فرمول Epley (بخش ۷.۳)
PR              → مقایسه با بهترین‌های ثبت‌شده (بخش ۷.۳)
```

<a id="h25"></a>
## ۷.۲ Boxing Metrics

شاخص‌های Boxing از `rounds` (و Activityهای BOXING) محاسبه می‌شوند:

```
Total Sessions       → COUNT(DISTINCT workout) با حداقل یک Activity از نوع BOXING
Total Rounds         → COUNT(rounds)
Total Time           → SUM(duration_sec) از Roundها
Average Intensity    → میانگین وزنی intensity با وزن duration_sec
Intensity Trend      → روند میانگین Intensity در بازه‌ی زمانی (هفتگی/ماهانه)
```

<a id="h26"></a>
## ۷.۳ Personal Record

PR به نوع تقسیم می‌شود تا «۸۰kg × ۱۲» با «۸۰kg × ۱» یکی حساب نشود:

|نوع PR|شرط اعمال|تعریف|
|---|---|---|
|MAX_WEIGHT|WEIGHTED و BODYWEIGHT_PLUS|بیشترین بار خارجی با `reps ≥ 1`|
|REPS_AT_WEIGHT|همه‌ی حرکات Reps-based|بیشترین reps در یک بار مشخص (برای Bodyweight بار = 0)|
|BEST_E1RM|فقط reps بین ۱ تا ۱۲|فرمول Epley: `e1RM = w × (1 + reps / 30)` — برای `reps = 1` برابر `w`|
|BEST_SET_VOLUME|WEIGHTED و BODYWEIGHT_PLUS|بیشترین `external_load × reps` در یک Set|
|MAX_DURATION|`measure_type = DURATION`|طولانی‌ترین Hold|

- برابری رکورد، PR جدید نیست (فقط بهتر شدنِ اکید).
- PR هنگام ثبت Set تشخیص داده می‌شود و با مقایسه‌ی Setهای همه‌ی Workoutهای دیگر انجام می‌شود (Workout جاری خودش را ملاک قرار نمی‌دهد).
- در هر Workout برای هر نوع PR حداکثر یک Badge نمایش داده می‌شود (بهترین Set).
- با ویرایش/حذف Workout، PRهای مرتبط با Exerciseهای درگیر بازسازی می‌شوند (بخش ۱۱).

<a id="h27"></a>
## ۷.۴ Intensity

|سطح|مقیاس|معنا|
|---|---|---|
|Round (Boxing)|عدد صحیح ۱ تا ۱۰، اختیاری|ورودی کاربر|
|Set (Strength)|RPE از ۱ تا ۱۰ با گام ۰٫۵، اختیاری|ورودی کاربر|
|Activity (Boxing)|میانگین وزنی Roundها (وزن = duration)|مشتق، ذخیره نمی‌شود|
|Workout|`session_rpe` اختیاری|ورودی کاربر هنگام پایان؛ در نبود آن میانگین Roundها برای نمودار|

راهنمای معنایی در UI: **۱–۳ سبک، ۴–۶ متوسط، ۷–۸ سخت، ۹–۱۰ حداکثر.**

دیتابیس فقط مقدار عددی را ذخیره می‌کند و UI مسئول نمایش Slider یا Rating مناسب است.

<a id="h28"></a>
## ۷.۵ Goal

|فیلد|توضیح|
|---|---|
|type|STRENGTH_WEIGHT / STRENGTH_REPS / SESSIONS / ROUNDS / TRAINING_TIME / BODY_WEIGHT|
|exercise_id|فقط برای Goalهای Strength|
|target_value|مقدار هدف|
|start_value|مقدار شروع هنگام ساخت Goal (برای محاسبه‌ی درصد درست، مخصوصاً کاهش وزن)|
|period|NONE / WEEKLY / MONTHLY — برای Session/Round/Time ضروری است|
|deadline|اختیاری|
|status / achieved_at|ACTIVE / ACHIEVED / ARCHIVED|
|note|توضیح اختیاری کاربر|

واحد (GoalUnit) ذخیره نمی‌شود و از `type` مشتق است: STRENGTH_WEIGHT و BODY_WEIGHT → KG، STRENGTH_REPS → REPS، ROUNDS → ROUNDS، SESSIONS → SESSIONS، TRAINING_TIME → MINUTES.

فیلد `current` ذخیره نمی‌شود و همیشه از داده‌ی واقعی محاسبه می‌شود؛ این تصمیم از ایجاد Data Duplication جلوگیری می‌کند. مثلاً برای Goal «Bench Press → 100kg» مقدار فعلی (مثلاً 80kg) از Workoutهای واقعی خوانده می‌شود.

جهت (افزایش/کاهش) از مقایسه‌ی `start` و `target` تشخیص داده می‌شود:

```
progress = clamp( (current − start) / (target − start), 0, 1 )
```

مثال کاهش وزن: `start = 90`، `target = 80`، `current = 85` → پیشرفت ۵۰٪.

برای Goalهای دوره‌ای، شمارش فقط داخل بازه‌ی جاری انجام و با شروع هر بازه صفر می‌شود. بازه‌ی دقیق از `period` و تاریخ جاری محاسبه می‌شود (و در صورت وجود `deadline` با آن محدود می‌شود)؛ بنابراین نیازی به ذخیره‌ی `period_start`/`period_end` نیست. شروع هفته طبق تنظیم کاربر است (پیش‌فرض: شنبه برای فارسی، دوشنبه برای انگلیسی).

نمونه Goalها:

```
Strength Goal     type = STRENGTH_WEIGHT, exercise = Bench Press, target = 100, unit = KG
Reps Goal         type = STRENGTH_REPS,   exercise = Pull-Up,     target = 15,  unit = REPS
Boxing Goal       type = ROUNDS,          period = WEEKLY,        target = 15,  unit = ROUNDS
Frequency Goal    type = SESSIONS,        period = WEEKLY,        target = 5,   unit = SESSIONS
Time Goal         type = TRAINING_TIME,   period = MONTHLY,       target = 600, unit = MINUTES
Body Goal         type = BODY_WEIGHT,     start = 90, target = 80, unit = KG
```

Goals مستقل از Workout هستند، اما مقدار Current آنها از داده‌های Workout محاسبه می‌شود.

دامنه‌ی شمارش در V1: برای `SESSIONS`، `ROUNDS` و `TRAINING_TIME` همه‌ی Workoutهای COMPLETED در بازه شمرده می‌شوند (Boxing و Strength با هم). تفکیک بر پایه‌ی نوع Activity (مثلاً «فقط Sessionهای Boxing») در صورت نیاز پس از V1 با افزودن یک فیلتر نوع به Goal انجام می‌شود و تغییر Schema در هسته لازم ندارد.

<a id="h29"></a>
## ۷.۶ معماری Progress

Progress جدول مستقلِ «منبع حقیقت» ندارد:

```
Workout
   ↓
Sets / Rounds            ← منبع حقیقت
   ↓
RecomputeStats           ← در همان Transactionِ نوشتن
   ↓
exercise_daily_stats + pr_events   ← Cache مشتق و بازسازی‌پذیر
   ↓
Progress Calculator / Charts
```

هر مقدار نمایشی در Progress از داده‌ی واقعی مشتق است؛ دو جدول Cache فقط برای Performance وجود دارند و در هر زمان قابل حذف و بازسازی از `sets` و `rounds` هستند (بخش ۱۰.۱۸).

---

<a id="h30"></a>
# ۸. Workout در حال انجام و Timer

پیش‌نویس اولیه دو تناقض داشت: از یک طرف می‌گفت داده با خروج ناگهانی نباید از بین برود و از طرف دیگر ذخیره را فقط در پایان مسیر می‌دانست؛ و Timer را «مستقل از Database» و بدون Service تعریف کرده بود که با Background شدن اپ از کار می‌افتد.

<a id="h31"></a>
## ۸.۱ ذخیره‌ی مرحله‌ای (Draft)

- با زدن Start Workout، یک ردیف Workout با `status = IN_PROGRESS` بلافاصله در Room ساخته می‌شود.
- هر Set و Round در لحظه‌ی تأیید، همان‌جا در Room نوشته می‌شود. «Save» انتهایی فقط وضعیت را COMPLETED می‌کند و Summary را محاسبه می‌کند.
- هنگام اجرای اپ، اگر Workout نیمه‌تمام وجود داشته باشد، بنر «ادامه‌ی تمرین» در Home نشان داده می‌شود.
- Workout نیمه‌تمامِ بیش از ۱۲ ساعت، در اجرای بعدی با گزینه‌های «پایان با آخرین فعالیت ثبت‌شده» یا «حذف» پرسیده می‌شود.
- اگر Workout بدون محتوا تمام شود، پیام تأیید دور ریختن نمایش داده می‌شود.

<a id="h32"></a>
## ۸.۲ Timer Engine و Foreground Service

```
TimerEngine (Pure Kotlin, Clock injectable)
├── WorkoutTimer — elapsed active time, pause / resume
├── RoundTimer   — round, rest, next round, presets
└── RestTimer    — between strength sets

WorkoutTimerService (Foreground) ←→ ViewModel (bind / StateFlow)
└── persists timer anchors (endsAt, phase) so state survives process death
```

|موضوع|تصمیم|
|---|---|
|مبنای زمان|Timer با شمارش Tick کار نمی‌کند؛ با لنگر زمانی (`elapsedRealtime` هنگام شروع و پایان فاز) محاسبه می‌شود تا با Sleep/Doze دقت حفظ شود.|
|Service|Foreground Service با Notification دائمی (نمایش زمان Round/Rest، دکمه‌های Pause/Skip). مجوز `POST_NOTIFICATIONS` و نوع Foreground مناسب Android 14 در Manifest تعریف می‌شود.|
|Persist|وضعیت Timer (فاز، لنگر، شماره‌ی Round) در DataStore/Room ذخیره می‌شود تا بعد از Kill شدن Process بازیابی شود.|
|هشدار|زنگ Round و Rest با SoundPool و Vibration؛ قابل خاموش شدن در تنظیمات. Screen-on اختیاری در صفحه‌ی Workout.|
|Rest واقعی و برنامه‌ریزی‌شده|هر دو ذخیره می‌شوند (`planned_*` و مقدار واقعی). تحلیل‌ها از مقدار واقعی استفاده می‌کنند.|
|ثبت بدون Timer|کاربر می‌تواند Roundها را دستی (مثلاً «۸ Round × ۳:۰۰») و Workout گذشته را بدون Timer ثبت کند.|

<a id="h33"></a>
## ۸.۳ جریان ثبت

```
Strength:
Start Workout → Add Exercise → Pre-fill → Complete Set (saved) → Rest Timer → Next Set → Finish

Boxing:
Start Workout → Add Activity → Start Round → Round Timer (saved) → Rest Timer → Next Round → Finish
```

پایان: نمایش Summary (مدت، Tonnage، Round، Intensity) و تغییر وضعیت Workout به COMPLETED. Workout از لحظه‌ی Start به‌صورت IN_PROGRESS در Room وجود دارد.

<a id="h34"></a>
## ۸.۴ Transactionها

هر عملیات نوشتن که بیش از یک ردیف را تغییر می‌دهد یا آمار مشتق را به‌روز می‌کند، داخل یک Transaction انجام می‌شود تا داده‌ی ناقص ذخیره نشود.

**ثبت مرحله‌ای (Draft):**

```
BEGIN TRANSACTION
    Upsert Activity            (اگر Exercise/بخش Boxing تازه اضافه شده)
    Insert Set / Round         (همان لحظه‌ی تأیید)
    RecomputeStats(exerciseIds, localDates)
    Upsert pr_events (در صورت رکورد جدید)
COMMIT   |   ROLLBACK
```

**پایان Workout (FinishWorkout):**

```
BEGIN TRANSACTION
    Update Workout (ended_at, active_duration_sec, session_rpe,
                    body_weight_kg_snapshot, status = COMPLETED)
    RecomputeStats(exerciseIds, localDates)
    Refresh pr_events
COMMIT   |   ROLLBACK
```

**حذف Workout (DeleteWorkout):**

```
BEGIN TRANSACTION
    Delete Workout → CASCADE به activities → sets / rounds
    جمع‌آوری exerciseIds و localDates پیش از حذف
    RecomputeStats(exerciseIds, localDates)
    Refresh pr_events
COMMIT   |   ROLLBACK
```

اگر هر قسمت Fail شود، کل عملیات Rollback می‌شود تا Workout ناقص یا آمار ناسازگار باقی نماند.

<a id="h35"></a>
## ۸.۵ Duplicate Workout

Duplicate یک Workout جدید ایجاد می‌کند:

```
Workout #10  →  Duplicate  →  Workout #11
```

اما `id`، `local_date`، `started_at` و `ended_at` جدید هستند. Exerciseها Reference می‌شوند (کپی نمی‌شوند)، ولی Activityها، Setها و Roundها Copy می‌شوند. فیلدهای مشتق/آمار (Cache و PR) برای Workout جدید دوباره محاسبه می‌شوند.

---

<a id="h36"></a>
# ۹. Exercise Library

<a id="h37"></a>
## ۹.۱ ساختار

|ویژگی|توضیح|
|---|---|
|id|برای داخلی: `builtin:<key>` (مثل `builtin:push_up`). برای شخصی: UUID|
|key|کلید پایدار Seed برای Exerciseهای داخلی (UNIQUE)؛ برای شخصی `NULL`|
|is_builtin|Exerciseهای Seed = true، شخصی = false|
|نام|`name_en` و `name_fa` برای داخلی؛ `custom_name` برای تمرین‌های شخصی. نمایش بر پایه‌ی زبان جاری|
|search_text|متن نرمال‌شده‌ی همه‌ی نام‌ها برای جست‌وجو (یکسان‌سازی ی/ي، ک/ك، ارقام و نیم‌فاصله)|
|load_type / measure_type|طبق بخش ۶.۵|
|equipment|NONE (Bodyweight)، PULL_UP_BAR، PARALLEL_BARS، BENCH، BARBELL، DUMBBELL، CABLE، MACHINE، KETTLEBELL، RESISTANCE_BAND، OTHER|
|difficulty|BEGINNER، INTERMEDIATE، ADVANCED|
|muscles|جدول `exercise_muscles` با نقش PRIMARY/SECONDARY؛ «دسته» از عضله‌ی اصلی مشتق می‌شود|
|instructions_en / instructions_fa|اختیاری؛ تکمیل تدریجی|
|is_archived|Soft Delete|
|seed_version|نسخه‌ی Seedی که Exercise را وارد/به‌روزرسانی کرده است|
|created_at / updated_at|Epoch ms|

این ساختار Library را به Bodyweight محدود نمی‌کند، ولی تمرکز اصلی آن همچنان روی Bodyweight است.

<a id="h38"></a>
## ۹.۲ دسته‌بندی‌ها

**گروه‌های عضلانی (Enum در کد):** Chest، Back، Shoulders، Biceps، Triceps، Forearms، Abs، Obliques، Lower Back، Glutes، Quadriceps، Hamstrings، Calves، Hips، Neck، Full Body.

دسته‌ی نمایشی Exercise از عضله‌ی PRIMARY مشتق می‌شود و ستون جداگانه ندارد. «Core» یک دسته‌ی نمایشی است که عضلات Abs، Obliques و Lower Back را پوشش می‌دهد.

**سختی:** BEGINNER، INTERMEDIATE، ADVANCED.

**نقش عضله:** PRIMARY، SECONDARY.

```
Push-Up
├── Chest                 → PRIMARY
├── Triceps               → SECONDARY
├── Shoulders (Front Delt)→ SECONDARY
└── Abs (Core)            → SECONDARY
```

این ساختار برای فیلتر کردن Exerciseها بر اساس عضله بسیار مفید است. رابطه‌ی Exercise و عضله Many-to-Many است:

```
Exercise ── N:M ── Muscle (Enum)
```

<a id="h39"></a>
## ۹.۳ Seeding و نسخه‌بندی

- داده‌ی اولیه در فایل JSON نسخه‌دار (`assets/exercises_seed_vN.json`) نگهداری می‌شود، با کلید پایدار `key` برای هر حرکت.
- `ExerciseSeedImporter` در اولین اجرا و با افزایش `seed_version`، به‌صورت Upsert بر پایه‌ی `key` وارد می‌کند و هرگز تمرین‌های شخصی یا Exerciseهای آرشیوشده را تغییر نمی‌دهد.
- ترجمه‌ی فارسی/انگلیسی نام‌ها مستقیم در JSON و ستون‌های جدا نگهداری می‌شود؛ بنابراین جست‌وجو روی دیتابیس ممکن است.
- ویرایش Exercise داخلی توسط کاربر به‌صورت «ساخت کپی شخصی» انجام می‌شود تا Seed بعدی آن را بازنویسی نکند.
- هر Exercise استاندارد در اولین اجرای Database وارد می‌شود و برای Bodyweight باید تمام نواحی اصلی بدن پوشش داده شوند.

<a id="h40"></a>
## ۹.۴ پوشش Bodyweight و Seed نمونه

هدف، پوشش استاندارد همه‌ی نواحی بدن و Variationهای مهم است.

|ناحیه|نمونه‌ها|
|---|---|
|Chest|Push-Up، Wide، Diamond، Decline، Archer، Explosive و Variationها|
|Back|Pull-Up، Chin-Up، Wide Pull-Up، Inverted Row، Archer Pull-Up، Muscle-Up|
|Legs|Bodyweight Squat، Split Squat، Reverse Lunge، Bulgarian Split Squat، Pistol Squat، Shrimp Squat، Nordic Curl|
|Shoulders|Pike Push-Up، Handstand Push-Up، Handstand Hold (DURATION)|
|Core|Plank (DURATION)، Side Plank (DURATION)، Hollow Hold (DURATION)، Leg Raise، V-Up، Dragon Flag|

نمای درختی Seed نمونه:

```
Chest
├── Push-Up
├── Wide Push-Up
├── Diamond Push-Up
├── Decline Push-Up
├── Archer Push-Up
└── Explosive Push-Up

Back
├── Pull-Up
├── Chin-Up
├── Wide Pull-Up
├── Inverted Row
├── Archer Pull-Up
└── Muscle-Up

Legs
├── Bodyweight Squat
├── Split Squat
├── Reverse Lunge
├── Bulgarian Split Squat
├── Pistol Squat
└── Shrimp Squat

Core
├── Plank
├── Side Plank
├── Hollow Hold
├── Leg Raise
├── V-Up
└── Dragon Flag
```

این فقط نمونه Seed است؛ **کتابخانه اولیه باید مجموعه کامل Bodyweight مورد توافق پروژه را پوشش دهد** و بعداً امکان افزودن Exercise شخصی هم وجود دارد.

<a id="h41"></a>
## ۹.۵ Exercise شخصی

کاربر باید بتواند Exercise خودش را اضافه کند (مثلاً «My Custom Push-Up»). در این حالت:

```
is_builtin = false
custom_name = "My Custom Push-Up"
id = UUID
```

و Exerciseهای Seed:

```
is_builtin = true
id = builtin:<key>
```

<a id="h42"></a>
## ۹.۶ تحویل مرحله‌ای

برای کنترل حجم کار، تحویل مرحله‌ای تعریف می‌شود: ابتدا هسته‌ی حدود ۱۲۰ حرکت با نام دوزبانه، سپس گسترش تا حدود ۱۵۰ یا بیشتر. کیفیت داده (نام صحیح، عضلات، `load_type` درست) بر تعداد مقدم است.

قابلیت‌ها: Search، Filter (عضله/تجهیزات/سختی)، افزودن Exercise شخصی. تصویر یا ویدیوی آموزشی خارج از V1 است.

---

<a id="h43"></a>
# ۱۰. معماری داده و Schema نهایی

<a id="h44"></a>
## ۱۰.۱ تصمیم‌های کلیدی Schema

**تصمیم‌های ساختاری:**

- کلید اصلی همه‌ی داده‌های کاربر UUID (TEXT) است تا Backup Merge و Sync آینده ممکن باشد؛ Exerciseهای داخلی کلید متنی پایدار دارند.
- همه‌ی زمان‌ها UTC میلی‌ثانیه‌اند؛ `local_date` و `timezone_id` جدا ذخیره می‌شوند.
- وزن همیشه به kg (Double) ذخیره می‌شود؛ تبدیل واحد فقط در نمایش است.
- جدول `settings` و جدول `muscles` حذف شدند (DataStore و Enum).
- دو جدول Cache مشتق‌شده اضافه شد: `exercise_daily_stats` و `pr_events`.

**تصمیم‌های تثبیت‌شده‌ی مدل:**

1. **Workout مرکز سیستم است** — نه Boxing و نه Strength.
2. **یک Workout می‌تواند ترکیبی باشد** — Strength + Boxing.
3. **جدول Progress نداریم** — Progress از داده‌ی واقعی محاسبه می‌شود (Cache مشتق، نه منبع حقیقت).
4. **جدول Calendar نداریم** — Calendar از Workout ساخته می‌شود.
5. **جدول History نداریم** — History از Workout ساخته می‌شود.
6. **Current Goal ذخیره نمی‌شود** — از داده‌های واقعی محاسبه می‌شود.
7. **Bodyweight نوع Load مستقل دارد** — بنابراین با Weight خارجی اشتباه نمی‌شود.
8. **Exercise Library مستقل است** — Workout به Exercise فقط Reference می‌دهد.
9. **Exercise حذف فیزیکی نمی‌شود** — برای حفظ History از `is_archived` استفاده می‌شود.
10. **Settings در Room نیست** — در DataStore نگهداری می‌شود، چون Theme و Language داده‌ی Relational نیستند.
11. **Activity تنها سطح میانی است** — هیچ جدول میانی دیگری بین Activity و Set/Round وجود ندارد.
12. **مدت و شدت Activity ذخیره نمی‌شود** — از Setها/Roundها مشتق است.

<a id="h45"></a>
## ۱۰.۲ ERD مفهومی

```
┌───────────────────────┐
│        WORKOUT        │
│───────────────────────│
│ id (UUID)             │
│ started_at / ended_at │
│ local_date            │
│ timezone_id           │
│ status                │
│ active_duration_sec   │
│ session_rpe           │
│ body_weight_snapshot  │
│ notes                 │
│ created_at/updated_at │
└───────────┬───────────┘
            │ 1:N
            ▼
┌───────────────────────┐
│       ACTIVITY        │
│───────────────────────│
│ id (UUID)             │
│ workout_id  (FK)      │
│ order_index           │
│ type: STRENGTH|BOXING │
│ exercise_id (FK?)     │
│ boxing_type?          │
│ custom_name?          │
│ notes                 │
└─────┬───────────┬─────┘
      │ 1:N       │ 1:N
      ▼           ▼
┌───────────┐ ┌───────────┐
│    SET    │ │   ROUND   │
│ STRENGTH  │ │  BOXING   │
└─────┬─────┘ └───────────┘
      │ N:1 (از طریق exercise_idِ Activity)
      ▼
┌───────────────────────┐
│       EXERCISE        │
│───────────────────────│
│ id / key / is_builtin │
│ name_en / name_fa     │
│ load_type/measure_type│
│ equipment/difficulty  │
│ is_archived           │
└───────────┬───────────┘
            │ N:M
            ▼
┌───────────────────────┐
│   EXERCISE_MUSCLES    │
│ muscle (Enum) + role  │
└───────────────────────┘

در کنار آن:

GOAL ──(exercise_id اختیاری)── EXERCISE
BODY_WEIGHT                        (مستقل)
EXERCISE_DAILY_STATS  ← Cache مشتق از SET
PR_EVENTS             ← Cache مشتق از SET
```

<a id="h46"></a>
## ۱۰.۳ فهرست جداول V1

```
1.  workouts
2.  activities
3.  sets
4.  rounds
5.  exercises
6.  exercise_muscles
7.  goals
8.  body_weights
9.  exercise_daily_stats   (Cache)
10. pr_events              (Cache)
```

<a id="h47"></a>
## ۱۰.۴ جدول `workouts`

موجودیت اصلی Workout.

|Field|Type|Constraint|توضیح|
|---|---|---|---|
|id|TEXT (UUID)|PK|شناسه‌ی یکتا|
|started_at|INTEGER|NOT NULL|Epoch ms (UTC) — زمان شروع|
|ended_at|INTEGER|NULL|Epoch ms (UTC) — زمان پایان؛ تا پایان Workout خالی است|
|local_date|TEXT|NOT NULL|ISO `yyyy-MM-dd` میلادی؛ روز تقویمی برای History/Calendar|
|timezone_id|TEXT|NOT NULL|منطقه‌ی زمانی هنگام ثبت|
|status|TEXT (Enum)|NOT NULL|IN_PROGRESS / COMPLETED|
|active_duration_sec|INTEGER|NULL|مدت فعال بدون Pause|
|session_rpe|REAL|NULL|۱..۱۰ با گام ۰٫۵|
|body_weight_kg_snapshot|REAL|NULL|آخرین وزن بدن هنگام پایان|
|notes|TEXT|NULL|یادداشت|
|created_at|INTEGER|NOT NULL|زمان ایجاد|
|updated_at|INTEGER|NOT NULL|آخرین تغییر (برای Merge/Sync)|

<a id="h48"></a>
## ۱۰.۵ جدول `activities`

هر Workout می‌تواند چند Activity داشته باشد.

|Field|Type|Constraint|توضیح|
|---|---|---|---|
|id|TEXT (UUID)|PK||
|workout_id|TEXT|FK → `workouts.id`، NOT NULL، ON DELETE CASCADE||
|order_index|INTEGER|NOT NULL|ترتیب نمایش؛ یکتا در هر Workout|
|type|TEXT (Enum)|NOT NULL|STRENGTH / BOXING|
|exercise_id|TEXT|FK → `exercises.id`، NULL، ON DELETE RESTRICT|فقط برای STRENGTH|
|boxing_type|TEXT (Enum)|NULL|فقط برای BOXING|
|custom_name|TEXT|NULL|وقتی `boxing_type = CUSTOM`|
|notes|TEXT|NULL||

`duration` و `intensity` در سطح Activity ذخیره نمی‌شوند و از Setها/Roundها مشتق‌اند.

هر Activity از نوع STRENGTH دقیقاً به یک Exercise اشاره می‌کند؛ چند حرکت پشت‌سرهم (مثلاً Bench Press، Pull-Up و Squat) با چند Activity و `order_index` متوالی مدل می‌شوند، نه با یک سطح میانی اضافی.

```
Workout 1 ─────── N Activity
```

حذف Workout:

```
Workout
   ↓ CASCADE
Activity
```

<a id="h49"></a>
## ۱۰.۶ جدول `sets`

مهم‌ترین جدول برای Strength Tracking.

|Field|Type|Constraint|توضیح|
|---|---|---|---|
|id|TEXT (UUID)|PK||
|activity_id|TEXT|FK → `activities.id`، NOT NULL، ON DELETE CASCADE||
|set_number|INTEGER|NOT NULL|یکتا و پیوسته در هر Activity|
|set_type|TEXT (Enum)|NOT NULL، پیش‌فرض NORMAL|NORMAL / WARMUP / DROP / FAILURE|
|reps|INTEGER|NULL|فقط وقتی `measure_type = REPS`|
|duration_sec|INTEGER|NULL|فقط وقتی `measure_type = DURATION`|
|external_load_kg|REAL|NOT NULL، پیش‌فرض 0|بار خارجی؛ برای BODYWEIGHT صفر؛ برای ASSISTED مقدار کمک|
|rpe|REAL|NULL|۱..۱۰ با گام ۰٫۵|
|rest_planned_sec|INTEGER|NULL|استراحت برنامه‌ریزی‌شده|
|rest_actual_sec|INTEGER|NULL|استراحت واقعی|
|is_completed|INTEGER (Boolean)|NOT NULL، پیش‌فرض 0|فقط Setهای Complete در آمار شمرده می‌شوند|
|completed_at|INTEGER|NULL|Epoch ms — لحظه‌ی تأیید Set|

`load_type` و `measure_type` روی Set ذخیره نمی‌شوند؛ از Exerciseِ همان Activity خوانده می‌شوند تا داده‌ی تکراری و ناسازگار ایجاد نشود.

<a id="h50"></a>
## ۱۰.۷ جدول `rounds`

هر Activity از نوع BOXING می‌تواند چند Round داشته باشد.

|Field|Type|Constraint|توضیح|
|---|---|---|---|
|id|TEXT (UUID)|PK||
|activity_id|TEXT|FK → `activities.id`، NOT NULL، ON DELETE CASCADE||
|round_number|INTEGER|NOT NULL|یکتا و پیوسته در هر Activity|
|planned_duration_sec|INTEGER|NULL|پیش‌فرض ۱۸۰|
|duration_sec|INTEGER|NULL|زمان واقعی|
|planned_rest_sec|INTEGER|NULL|پیش‌فرض ۶۰|
|rest_sec|INTEGER|NULL|استراحت واقعی|
|intensity|INTEGER|NULL|۱..۱۰|
|note|TEXT|NULL||

<a id="h51"></a>
## ۱۰.۸ جدول `exercises`

Library اصلی Exerciseها.

|Field|Type|Constraint|توضیح|
|---|---|---|---|
|id|TEXT|PK|داخلی: `builtin:<key>`؛ شخصی: UUID|
|key|TEXT|NULL، UNIQUE|کلید پایدار Seed|
|is_builtin|INTEGER (Boolean)|NOT NULL||
|name_en|TEXT|NULL|نام انگلیسی|
|name_fa|TEXT|NULL|نام فارسی|
|custom_name|TEXT|NULL|فقط برای Exercise شخصی|
|search_text|TEXT|NOT NULL|متن نرمال‌شده برای جست‌وجو|
|load_type|TEXT (Enum)|NOT NULL|WEIGHTED / BODYWEIGHT / BODYWEIGHT_PLUS / ASSISTED|
|measure_type|TEXT (Enum)|NOT NULL|REPS / DURATION|
|equipment|TEXT (Enum)|NOT NULL، پیش‌فرض NONE||
|difficulty|TEXT (Enum)|NOT NULL|BEGINNER / INTERMEDIATE / ADVANCED|
|instructions_en|TEXT|NULL||
|instructions_fa|TEXT|NULL||
|is_archived|INTEGER (Boolean)|NOT NULL، پیش‌فرض 0|Soft Delete|
|seed_version|INTEGER|NULL|نسخه‌ی Seed واردکننده|
|created_at|INTEGER|NOT NULL||
|updated_at|INTEGER|NOT NULL||

`category` ستون مستقل ندارد و از عضله‌ی PRIMARY در `exercise_muscles` مشتق می‌شود.

<a id="h52"></a>
## ۱۰.۹ جدول `exercise_muscles`

چون هر Exercise می‌تواند چند عضله را درگیر کند، رابطه Many-to-Many لازم است.

|Field|Type|Constraint|توضیح|
|---|---|---|---|
|exercise_id|TEXT|FK → `exercises.id`، NOT NULL، ON DELETE CASCADE|بخشی از PK|
|muscle|TEXT (Enum)|NOT NULL|بخشی از PK|
|role|TEXT (Enum)|NOT NULL|PRIMARY / SECONDARY|

**Primary Key:** `(exercise_id, muscle)`

<a id="h53"></a>
## ۱۰.۱۰ جدول `goals`

Goals مستقل از Workout هستند، اما مقدار Current آنها از داده‌های Workout محاسبه می‌شود.

|Field|Type|Constraint|توضیح|
|---|---|---|---|
|id|TEXT (UUID)|PK||
|type|TEXT (Enum)|NOT NULL|STRENGTH_WEIGHT / STRENGTH_REPS / SESSIONS / ROUNDS / TRAINING_TIME / BODY_WEIGHT|
|exercise_id|TEXT|FK → `exercises.id`، NULL، ON DELETE RESTRICT|فقط Goalهای Strength|
|target_value|REAL|NOT NULL|مقدار هدف|
|start_value|REAL|NULL|مقدار شروع؛ مبنای محاسبه‌ی درصد|
|period|TEXT (Enum)|NOT NULL، پیش‌فرض NONE|NONE / WEEKLY / MONTHLY|
|deadline|INTEGER|NULL|Epoch ms|
|status|TEXT (Enum)|NOT NULL، پیش‌فرض ACTIVE|ACTIVE / ACHIEVED / ARCHIVED|
|achieved_at|INTEGER|NULL|Epoch ms|
|note|TEXT|NULL||
|created_at|INTEGER|NOT NULL||
|updated_at|INTEGER|NOT NULL||

`current`، `unit`، `period_start` و `period_end` ذخیره نمی‌شوند و همه مشتق‌اند (بخش ۷.۵).

<a id="h54"></a>
## ۱۰.۱۱ جدول `body_weights`

برای Tracking وزن بدن.

|Field|Type|Constraint|توضیح|
|---|---|---|---|
|id|TEXT (UUID)|PK||
|measured_at|INTEGER|NOT NULL|Epoch ms (UTC)|
|local_date|TEXT|NOT NULL|ISO `yyyy-MM-dd`؛ روز تقویمی ثبت|
|weight_kg|REAL|NOT NULL|۲۰..۳۵۰|
|note|TEXT|NULL||

مثال:

```
2026-09-20 → 74.2 kg
2026-09-25 → 73.8 kg
2026-09-30 → 73.5 kg
```

<a id="h55"></a>
## ۱۰.۱۲ جدول `exercise_daily_stats` (Cache)

آمار تجمیعی هر Exercise در هر روز؛ مشتق از `sets` و در هر زمان بازسازی‌پذیر.

|Field|Type|Constraint|توضیح|
|---|---|---|---|
|exercise_id|TEXT|NOT NULL|بخشی از PK|
|local_date|TEXT|NOT NULL|بخشی از PK|
|max_load|REAL|NULL|بیشترین `external_load_kg`|
|max_reps|INTEGER|NULL|بیشترین `reps`|
|best_e1rm|REAL|NULL|بهترین e1RM (Epley)|
|tonnage|REAL|NULL|`SUM(external_load × reps)`|
|total_reps|INTEGER|NULL|`SUM(reps)`|
|total_duration|INTEGER|NULL|`SUM(duration_sec)`|
|set_count|INTEGER|NULL|تعداد Setهای Complete و غیر WARMUP|
|updated_at|INTEGER|NOT NULL|زمان بازمحاسبه|

**Primary Key:** `(exercise_id, local_date)`

<a id="h56"></a>
## ۱۰.۱۳ جدول `pr_events` (Cache)

|Field|Type|Constraint|توضیح|
|---|---|---|---|
|id|TEXT (UUID)|PK||
|exercise_id|TEXT|NOT NULL||
|pr_type|TEXT (Enum)|NOT NULL|MAX_WEIGHT / REPS_AT_WEIGHT / BEST_E1RM / BEST_SET_VOLUME / MAX_DURATION|
|value|REAL|NOT NULL|مقدار رکورد|
|reps|INTEGER|NULL|برای REPS_AT_WEIGHT و BEST_E1RM|
|set_id|TEXT|NULL|Set مبدأ (ممکن است بعداً حذف شده باشد)|
|workout_id|TEXT|NULL|Workout مبدأ|
|achieved_at|INTEGER|NOT NULL|Epoch ms|
|is_current|INTEGER (Boolean)|NOT NULL|آیا رکورد فعلیِ این نوع PR است|

<a id="h57"></a>
## ۱۰.۱۴ جداولی که عمداً وجود ندارند

|جدول حذف‌شده|جایگزین|دلیل|
|---|---|---|
|`settings`|Android DataStore|Theme و Language داده‌ی Relational نیستند|
|`muscles`|Enum در کد|لیست ثابت و کوچک است؛ رابطه با `exercise_muscles.muscle` نگه داشته می‌شود|
|`strength_exercises` / `boxing_activities`|ادغام در `activities`|جلوگیری از جدول تکراری و Joinهای اضافی|
|`progress`|`exercise_daily_stats` + `pr_events` (مشتق)|Progress از داده‌ی واقعی محاسبه می‌شود؛ Cache منبع حقیقت نیست|
|`calendar`|`workouts.local_date`|Calendar همیشه با History هماهنگ می‌ماند|
|`history`|`workouts` + Relationها|History یک نما از همان داده است|

<a id="h58"></a>
## ۱۰.۱۵ ایندکس‌ها

|ایندکس|کاربرد|
|---|---|
|`workouts(local_date, status)`|History، Calendar، خلاصه‌ی هفتگی|
|`workouts(status)`|یافتن Workout نیمه‌تمام (Draft) در شروع اپ|
|`activities(workout_id, order_index)`|بارگذاری Workout|
|`activities(exercise_id)`|تاریخچه‌ی هر Exercise و «مقدار قبلی» برای Pre-fill|
|`activities(type)`|فیلتر Boxing/Strength|
|`sets(activity_id, set_number)`|Setهای هر Activity|
|`sets(completed_at)`|بازسازی آمار بر پایه‌ی زمان|
|`rounds(activity_id, round_number)`|Roundهای هر Activity|
|`exercise_daily_stats(exercise_id, local_date)`|نمودار Progress بدون اسکن همه‌ی Setها|
|`pr_events(exercise_id, pr_type, is_current)`|نمایش PR جاری|
|`exercises(search_text)`|جست‌وجو|
|`exercises(is_archived)`|فیلتر Exerciseهای فعال در Picker|
|`exercises(is_builtin)`|تفکیک Seed از شخصی|
|`exercises(load_type)`|فیلتر Bodyweight/Weighted|
|`exercise_muscles(muscle)`|فیلتر بر پایه‌ی عضله|
|`goals(status)`|Goalهای فعال در Home|
|`body_weights(local_date)`|نمودار و آخرین وزن|

<a id="h59"></a>
## ۱۰.۱۶ ساختار Room و DAOها

```
data/local/
│
├── AppDatabase
│
├── dao/
│   ├── WorkoutDao
│   ├── ActivityDao
│   ├── SetDao
│   ├── RoundDao
│   ├── ExerciseDao
│   ├── GoalDao
│   ├── BodyWeightDao
│   └── StatsDao            (exercise_daily_stats + pr_events)
│
├── entity/
│   ├── WorkoutEntity
│   ├── ActivityEntity
│   ├── SetEntity
│   ├── RoundEntity
│   ├── ExerciseEntity
│   ├── ExerciseMuscleCrossRef
│   ├── GoalEntity
│   ├── BodyWeightEntity
│   ├── ExerciseDailyStatsEntity
│   └── PrEventEntity
│
├── migration/
│   ├── Migrations          (صریح، برای هر نسخه)
│   └── schemas/            (خروجی exportSchema در Git)
│
└── converter/
    └── DatabaseConverters  (Enum ↔ TEXT، UUID ↔ TEXT)
```

<a id="h60"></a>
### متدهای اصلی DAOها

**WorkoutDao**

```
insertWorkout()
updateWorkout()
deleteWorkout()
getWorkoutById()
getWorkoutsByDate(localDate)
getWorkoutsBetweenDates(from, to)
getRecentWorkouts(limit)
getInProgressWorkout()
markCompleted()
getWeeklySummary(weekStart, weekEnd)
```

**ActivityDao**

```
insertActivity()
updateActivity()
deleteActivity()
getActivitiesForWorkout()
reorderActivities()
getLastActivityForExercise(exerciseId)
```

**SetDao**

```
insertSet()
updateSet()
deleteSet()
getSetsForActivity()
getSetsForExercise()
getLastSessionSets(exerciseId)        // Pre-fill
getCompletedSetsForStats(exerciseId, localDate)
```

**RoundDao**

```
insertRound()
updateRound()
deleteRound()
getRoundsForActivity()
getRoundsBetweenDates(from, to)
```

**ExerciseDao**

```
getAllExercises()
getExerciseById()
searchExercises(query)
getExercisesByMuscle(muscle)
getExercisesByEquipment(equipment)
getBodyweightExercises()
insertCustomExercise()
archiveExercise()
restoreExercise()
upsertFromSeed()
```

**GoalDao**

```
getActiveGoals()
insertGoal()
updateGoal()
markAchieved()
archiveGoal()
deleteGoal()
```

**BodyWeightDao**

```
insertWeight()
updateWeight()
deleteWeight()
getWeightHistory()
getLatestWeight()
getWeightAt(localDate)
```

**StatsDao**

```
upsertDailyStats()
deleteStatsFor(exerciseIds, localDates)
getStatsRange(exerciseId, from, to)
insertPrEvent()
getCurrentPrs(exerciseId)
invalidatePrs(exerciseId)
rebuildAll()
```

<a id="h61"></a>
## ۱۰.۱۷ Aggregate جزئیات Workout

برای نمایش یک Workout کامل، Repository باید بتواند یک Aggregate کامل ایجاد کند:

```
Workout
├── Activities
│
├── Strength
│   ├── Exercise
│   │   └── Sets
│   └── Exercise
│       └── Sets
│
└── Boxing
    └── Rounds
```

این Aggregate با یک یا چند Query ایندکس‌دار در Repository ساخته می‌شود و به‌صورت یک مدل Domain به ViewModel داده می‌شود.

<a id="h62"></a>
## ۱۰.۱۸ استراتژی Cache و Performance

پیش‌نویس اولیه همه‌چیز را هر بار از داده‌ی خام محاسبه می‌کرد. اکنون:

- **منبع حقیقت:** `sets` و `rounds`. جداول `exercise_daily_stats` و `pr_events` «مشتق و بازسازی‌پذیر»اند.
- هنگام ثبت/ویرایش/حذف، فقط ترکیب‌های (exercise، تاریخ) تأثیرپذیر در همان Transaction دوباره محاسبه می‌شوند: `RecomputeStats(exerciseIds, dates)`.
- نمودارها از `exercise_daily_stats` و Queryهای MAX/SUM روی ایندکس خوانده می‌شوند؛ لیست History با Paging یا LIMIT بارگذاری می‌شود.
- گزینه‌ی «بازسازی آمار» در تنظیمات پیشرفته، همه‌ی Cacheها را از داده‌ی خام دوباره می‌سازد. تست خودکار تضمین می‌کند بازسازی کامل با به‌روزرسانی تدریجی نتیجه‌ی یکسان دارد.

<a id="h63"></a>
## ۱۰.۱۹ Calendar و History از دید داده

```
Database
   ↓
workouts.local_date
   ↓
Calendar / History
```

Calendar و History هر دو از `workouts` و Relationهای آن خوانده می‌شوند و جدول جدا ندارند؛ بنابراین همیشه با یکدیگر هماهنگ‌اند.

<a id="h64"></a>
## ۱۰.۲۰ Schema نهایی در یک نگاه

```
┌──────────────────────┐
│       WORKOUT        │
└──────────┬───────────┘
           │ 1:N
           ▼
┌──────────────────────┐
│      ACTIVITY        │
└───────┬────────┬─────┘
        │        │
        ▼        ▼
┌────────────┐ ┌───────────────┐
│    SET     │ │     ROUND     │
│ (STRENGTH) │ │   (BOXING)    │
└─────┬──────┘ └───────────────┘
      │ N:1 (exercise_id روی Activity)
      ▼
┌──────────────────────┐
│       EXERCISE       │
└──────────┬───────────┘
           │ N:M
           ▼
┌──────────────────────┐
│   EXERCISE_MUSCLES   │
│   muscle + role      │
└──────────────────────┘

┌──────────────────────┐        ┌──────────────────────┐
│        GOAL          │        │     BODY_WEIGHT      │
└──────────┬───────────┘        └──────────────────────┘
           │ optional
           ▼
       EXERCISE

┌────────────────────────────┐   ┌──────────────────────┐
│   EXERCISE_DAILY_STATS     │   │      PR_EVENTS       │
│      (Cache مشتق)          │   │     (Cache مشتق)     │
└────────────────────────────┘   └──────────────────────┘
```

---

<a id="h65"></a>
# ۱۱. یکپارچگی داده، ویرایش و حذف

<a id="h66"></a>
## ۱۱.۱ سیاست Foreign Key

|رابطه|سیاست|دلیل|
|---|---|---|
|workouts → activities|CASCADE|حذف Workout همه‌ی اجزای آن را حذف می‌کند|
|activities → sets / rounds|CASCADE||
|activities.exercise_id → exercises|RESTRICT|Exercise استفاده‌شده هرگز Hard Delete نمی‌شود|
|goals.exercise_id → exercises|RESTRICT||
|exercise_muscles → exercises|CASCADE||
|pr_events / exercise_daily_stats|مشتق (بازسازی‌پذیر)|با حذف Workout بازسازی می‌شوند|

<a id="h67"></a>
## ۱۱.۲ ساختار Foreign Key

```
workouts
   │
   └── activities
          ├── sets
          └── rounds

activities.exercise_id ──→ exercises
                              │
                              └── exercise_muscles

goals.exercise_id ──→ exercises (optional)

sets ──(مشتق)──→ exercise_daily_stats
sets ──(مشتق)──→ pr_events
```

<a id="h68"></a>
## ۱۱.۳ Exercise

Exercise Library را Hard Delete نمی‌کنیم، چون ممکن است Workout قدیمی به آن Exercise Reference داشته باشد؛ پس حذف از دید کاربر یعنی `is_archived = true`. این کار History قدیمی را سالم نگه می‌دارد.

- **Soft Delete:** Exercise آرشیوشده از Picker مخفی است اما در History و PR سالم می‌ماند و قابل بازگردانی است.
- **Hard Delete** فقط برای Exercise شخصی که هرگز استفاده نشده مجاز است.
- پس از اولین استفاده، `load_type` و `measure_type` قفل می‌شوند (تغییر آن‌ها داده‌ی تاریخی را بی‌معنا می‌کند). تغییر نام، عضله و توضیحات آزاد است.

<a id="h69"></a>
## ۱۱.۴ ویرایش و حذف Workout

1. پیش از تغییر، Exercise IDها و تاریخ‌های درگیر جمع‌آوری می‌شوند.
2. تغییر یا حذف در یک Transaction انجام می‌شود.
3. `RecomputeStats` برای همان Exercise/تاریخ‌ها اجرا و `pr_events` اصلاح می‌شود.
4. Goalها چون محاسبه‌ای‌اند خودکار به‌روز می‌شوند؛ Goal ACHIEVED در صورت نقض شرط، دوباره ACTIVE می‌شود.

زنجیره‌ی حذف:

```
Workout
   ↓ CASCADE
Activities
   ├──→ Sets
   └──→ Rounds
```

بنابراین هیچ داده‌ی orphan باقی نمی‌ماند.

حذف Workout با Undo Snackbar (۵ ثانیه) انجام می‌شود: حذف واقعی پس از پایان مهلت اجرا می‌شود و در صورت Undo هیچ تغییری رخ نمی‌دهد.

<a id="h70"></a>
## ۱۱.۵ اصول Integrity

- هر Set فقط به یک Activity و هر Round فقط به یک Activity از نوع BOXING تعلق دارد (با Constraint بررسی می‌شود).
- Activity از نوع STRENGTH باید `exercise_id` داشته باشد و از نوع BOXING باید `boxing_type`.
- ترتیب Activity و شماره‌ی Set/Round یکتا و پیوسته است.
- برای هر Exercise در هر `local_date` حداکثر یک ردیف در `exercise_daily_stats` وجود دارد (کلید مرکب).
- در `pr_events` برای هر ترکیب (`exercise_id`، `pr_type`) حداکثر یک ردیف با `is_current = true` وجود دارد.

---

<a id="h71"></a>
# ۱۲. Backup، Restore و Migration

چون داده کاملاً محلی است، گم شدن یا خراب شدن گوشی یعنی از دست رفتن همه‌چیز؛ بنابراین این بخش در V1 الزامی است.

|موضوع|تصمیم|
|---|---|
|Export|خروجی JSON نسخه‌دار (`schemaVersion`) شامل Workout، Activity، Set، Round، Exerciseهای شخصی، Goal، BodyWeight و تنظیمات. Exerciseهای داخلی با `key` ارجاع داده می‌شوند. ذخیره از طریق Storage Access Framework (قابل ذخیره در Drive یا حافظه).|
|Import|حالت Replace (با تأیید) و Merge بر پایه‌ی UUID. پیش از Replace، خروجی ایمنی خودکار گرفته می‌شود. فایل خراب یا نسخه‌ی ناشناخته بدون تغییر در داده رد می‌شود.|
|Auto Backup|Android Auto Backup با `data_extraction_rules` به‌عنوان لایه‌ی دوم فعال می‌شود و روند Restore آن باید تست شود. مرجع اصلی، Export دستی است.|
|Backup زمان‌بندی‌شده|اختیاری: Export هفتگی با WorkManager در پوشه‌ی انتخابی کاربر.|
|Migration دیتابیس|`exportSchema = true` و نگه‌داری Schemaها در Git؛ Migration صریح برای هر نسخه؛ `fallbackToDestructiveMigration` ممنوع؛ تست با MigrationTestHelper.|
|نسخه‌ی Backup|Backup `schemaVersion` جدا از نسخه‌ی Room دارد و Upgrader مجزا.|
|هشدار امنیتی|فایل Backup رمزنگاری نمی‌شود؛ هنگام Export به کاربر یادآوری می‌شود.|

---

<a id="h72"></a>
# ۱۳. Localization، تقویم شمسی و واحدها

|موضوع|تصمیم|
|---|---|
|زبان و جهت|فارسی (RTL) و انگلیسی (LTR) از ابتدا؛ Per-app language با AppCompatDelegate. همه‌ی Composableها با Modifier/Arrangement مستقل از جهت (Start/End).|
|تقویم|ذخیره‌سازی همیشه میلادی؛ نمایش شمسی (Jalali) با `android.icu.util.PersianCalendar` از طریق یک انتزاع `CalendarSystem`. تنظیم: Auto/Jalali/Gregorian. ماه، هفته و نام روزها در Calendar UI بر همین پایه‌اند.|
|شروع هفته|تنظیم‌پذیر؛ پیش‌فرض شنبه (فارسی) و دوشنبه (انگلیسی). روی خلاصه‌ی هفتگی و Goalهای هفتگی اثر می‌گذارد.|
|ارقام ورودی|`NumberInputParser` ارقام فارسی و عربی (۰–۹ و ٠–٩) و جداکننده‌ی اعشار (٫ و , و .) را به عدد استاندارد تبدیل می‌کند؛ مستقل از کیبورد.|
|ارقام نمایش|گزینه‌ی ارقام فارسی یا لاتین؛ قالب‌بندی با NumberFormat مبتنی بر Locale.|
|فونت|Vazirmatn (بسته‌بندی‌شده در اپ) برای فارسی؛ فونت سیستم برای لاتین.|
|واحد|kg فعال است. ذخیره‌سازی همیشه kg؛ برای lb در آینده فقط لایه‌ی تبدیل نمایش/ورودی (`UnitFormatter`) اضافه می‌شود و Schema تغییر نمی‌کند. ورودی lb ابتدا به kg تبدیل می‌شود.|
|متن دوزبانه|نام Exerciseها در ستون‌های جدا؛ رشته‌های UI در Resource (`values` و `values-fa`). نام اپ (Warrior) در `app_name` هر دو زبان یکسان است و ترجمه نمی‌شود.|

---

<a id="h73"></a>
# ۱۴. طراحی ثبت سریع (Fast Input)

- **Pre-fill:** هنگام افزودن Exercise، مقدارهای آخرین Session همان حرکت از تاریخچه‌ی ثبت‌شده (Query ایندکس‌دار) نشان داده می‌شود.
- ثبت یک Set در حالت عادی حداکثر ۳ تپ: تأیید مقدار پیش‌فرض، یا ± وزن/تکرار، سپس Complete.
- Stepper سریع برای وزن (گام ۲٫۵ / ۱٫۲۵ kg قابل تنظیم) و تکرار.
- دکمه‌ی «کپی Set قبلی» و «افزودن N Set/Round مشابه».
- **Boxing:** پیش‌تنظیم Round (پیش‌فرض ۳ دقیقه و ۱ دقیقه استراحت) و ثبت دسته‌جمعی Roundهای مشابه.
- **Duplicate Workout** و «شروع از روی تمرین قبلی» (ساختار کپی می‌شود، مقادیر پیشنهادی از آخرین اجرا).

<a id="h74"></a>
## ۱۴.۱ Summary پایان Workout

مدت کل، تعداد Exercise و Set، Tonnage، تعداد Round و زمان Boxing، PRهای جدید، و انتخاب اختیاری `session_rpe`. سپس تأیید نهایی و وضعیت COMPLETED.

<a id="h75"></a>
## ۱۴.۲ طراحی بصری

Dark-first، مینیمال و مدرن، کارت‌محور با گوشه‌های گرد، اعداد کلیدی درشت، آیکون‌های حداقلی، Bottom Navigation، چگالی اطلاعات فشرده و نمودارهای گرافیکی. رنگ اصلی آبی ملایم و خنثی. تم: Dark / Light / System. اهداف ضربه‌ی لمسی حداقل ۴۸dp، Content Description برای اجزای تعاملی و پشتیبانی از Font Scaling.

---

<a id="h76"></a>
# ۱۵. Validation و Error Handling

|ورودی|محدوده‌ی معتبر|
|---|---|
|Reps|عدد صحیح ۱ تا ۹۹۹|
|external_load_kg|۰ تا ۱۰۰۰ (بر پایه‌ی load_type)، تا دو رقم اعشار|
|Duration Hold|۱ تا ۳۶۰۰ ثانیه (۱ ثانیه تا ۶۰ دقیقه)|
|RPE|۱ تا ۱۰ با گام ۰٫۵|
|Intensity Round|عدد صحیح ۱ تا ۱۰|
|Round Duration|۱۰ ثانیه تا ۶۰ دقیقه|
|Rest|۰ تا ۶۰ دقیقه|
|وزن بدن|۲۰ تا ۳۵۰ kg|
|session_rpe|۱ تا ۱۰ با گام ۰٫۵|

|وضعیت|رفتار|
|---|---|
|Set ناقص|Set تا زمان معتبر بودن Complete نمی‌شود؛ مقدار پیش‌نویس حفظ می‌شود|
|Workout بدون محتوا|هنگام پایان، تأیید دور ریختن|
|خروج ناگهانی / Kill شدن Process|داده به‌دلیل ذخیره‌ی مرحله‌ای سالم است؛ Timer از لنگر زمانی بازیابی می‌شود|
|حذف Workout|Undo Snackbar و بازسازی آمار در Transaction|
|تغییر Exercise|قفل `load_type` و `measure_type` پس از استفاده؛ بقیه‌ی فیلدها آزاد|
|قطع Timer|بازیابی از Service/وضعیت ذخیره‌شده، نه از حافظه‌ی ViewModel|
|خطای Import|رد کامل فایل بدون تغییر در دیتابیس و پیام واضح|
|خطای Room|Transaction و Rollback؛ لاگ محلی و پیام قابل‌فهم|

---

<a id="h77"></a>
# ۱۶. استراتژی Testing

|سطح|محدوده|ابزار|
|---|---|---|
|Unit (اولویت اول)|Volume، e1RM، PR Detection، Goal Progress، خلاصه‌ی هفتگی، NumberInputParser، تبدیل شمسی، TimerEngine با Clock جعلی. هدف پوشش بالا روی `domain/calculator`|JUnit|
|Database|DAO و Queryهای تجمیعی، Cascade/Restrict، بازسازی Cache (معادل بودن بازسازی کامل و تدریجی)|Room in-memory|
|Migration|هر جفت نسخه‌ی Schema|MigrationTestHelper|
|ViewModel|Stateها و Flowها|Turbine + TestDispatcher|
|UI|مسیرهای حیاتی: ثبت Set، پایان Workout، Undo حذف، RTL|Compose UI Test|
|Backup|Export سپس Import روی دیتابیس خالی، برابری داده‌ها|JUnit + فایل نمونه|
|Seed|وارد کردن Seed، اجرای دوباره بدون تغییر داده‌ی شخصی، حفظ Exercise آرشیوشده|JUnit + Room in-memory|

**Edge Caseهای الزامی:** رکورد برابر، Set بدون Weight، Bodyweight بدون وزن بدن ثبت‌شده، جابه‌جایی Timezone، آغاز هفته در شمسی، ورودی با ارقام فارسی، حذف آخرین Set یک Exercise، و Workout نیمه‌تمام قدیمی.

---

<a id="h78"></a>
# ۱۷. الزامات غیرعملکردی، Performance و Privacy

|الزام|هدف|
|---|---|
|Simplicity|ثبت یک Set یا Round بدون فرم پیچیده (بخش ۱۴)|
|Responsiveness|Cold Start زیر ۲ ثانیه؛ ثبت Set بدون تأخیر محسوس؛ نوشتن روی Thread پس‌زمینه|
|Offline First|همه‌ی عملیات بدون اینترنت|
|Scalability داده|History و Progress با چند سال داده روان بمانند (ایندکس، Cache آماری، Paging)|
|Maintainability|مرزهای لایه‌ها، DI، تست پوشش‌دهنده‌ی Domain|
|Data Integrity|ویرایش/حذف، همه‌ی وابستگی‌ها را در Transaction درست به‌روز می‌کند|
|Privacy|بدون Account، بدون Login و بدون مجوز INTERNET در V1؛ هیچ داده‌ای از دستگاه خارج نمی‌شود مگر Export دستی کاربر|
|Accessibility|اهداف لمسی ۴۸dp، Content Description، Font Scaling، کنتراست کافی در Dark|

---

<a id="h79"></a>
# ۱۸. خارج از Scope و نقشه راه

<a id="h80"></a>
## ۱۸.۱ خارج از Scope نسخه ۱

AI Coach، Social/Chat، Nutrition و Calorie Tracking، Wearable و Heart Rate، Sleep و Recovery Score، Advanced Training Load، Online Coaching، Cloud و Multi-user، Subscription.

<a id="h81"></a>
## ۱۸.۲ مراحل پیاده‌سازی پیشنهادی

|فاز|خروجی|
|---|---|
|۰ — پایه|پروژه، Hilt، Room و Schema، Theme، Localization (RTL/شمسی)، CI و تست‌های پایه|
|۱ — هسته‌ی ثبت|Workout Draft، Strength، Fast Input، Timer Service|
|۲ — Boxing|Activityهای Boxing، Round/Rest Timer، ثبت دستی|
|۳ — مرور|History (List/Calendar)، جزئیات، ویرایش، حذف با Undo|
|۴ — Progress|Volume/PR/e1RM، Cache آماری، نمودارها، Goals، Body Weight|
|۵ — کتابخانه و امنیت داده|Seed Exercise Library، Backup/Restore، Migration Test|
|۶ — پرداخت نهایی|Accessibility، Performance، رفع باگ، آماده‌سازی انتشار|

<a id="h82"></a>
## ۱۸.۳ اولویت‌های بعد از V1

- **Template / Routine** (مثلاً «Push Day»): اولین اولویت V1.1. مدل فعلی مانع آن نیست؛ کافی است `workout_templates` و `template_activities` اضافه شود.
- **Unit Switching (lb)**، Plate Calculator، Superset، Merge Exercise.
- **Cloud Sync** (با تکیه بر UUID و `updated_at`) و Wearable.
- انواع جدید Activity (CONDITIONING، MOBILITY، CARDIO) بدون تغییر مدل Workout.

---

<a id="h83"></a>
# پیوست الف: ردیابی ضعف‌ها و راه‌حل‌ها

هر ردیف یک ضعف از بررسی پیش‌نویس اولیه را به تصمیم اعمال‌شده در این نسخه نگاشت می‌کند.

|#|ضعف در پیش‌نویس اولیه|راه‌حل در این نسخه|بخش|
|---|---|---|---|
|۱|مدل Set/Bodyweight ناسازگار؛ Volume صفر برای Bodyweight|load_type/measure_type در Exercise؛ یک فیلد external_load؛ قواعد Volume به تفکیک نوع|۶.۵، ۷.۱|
|۲|Activity مبهم؛ جداول تکراری|تعریف Activity؛ ادغام جداول؛ مدت/شدت مشتق|۶.۱، ۱۰|
|۳|PR نامشخص و بدون Cache|پنج نوع PR؛ exercise_daily_stats و pr_events؛ ایندکس|۷.۳، ۱۰.۱۸|
|۴|تکلیف Exercise استفاده‌شده نامشخص|Soft Delete، RESTRICT، قفل load_type|۱۱.۱، ۱۱.۳|
|۵|نبود Draft و Service|ذخیره‌ی مرحله‌ای IN_PROGRESS و Foreground Service|۸|
|۶|Rest واقعی یا برنامه‌ریزی‌شده؟|هر دو ذخیره؛ تحلیل از واقعی|۸.۲|
|۷|نبود Template|Duplicate و شروع از تمرین قبلی در V1؛ Template اولویت V1.1|۸.۵، ۱۴، ۱۸.۳|
|۸|Goal مبهم (current، بازه، جهت)|start_value، period، جهت خودکار؛ current محاسبه‌ای|۷.۵|
|۹|جدول settings تکراری با DataStore|حذف جدول؛ فقط DataStore|۵، ۱۰.۱۴|
|۱۰|Backup و Migration بدون معماری|Export/Import JSON، Auto Backup، Migration صریح|۱۲|
|۱۱|Seeding و ترجمه‌ی Library نامشخص|Seed JSON نسخه‌دار، key پایدار، ستون‌های دو زبانه، search_text|۹|
|۱۲|مسیر UseCase متناقض|قانون عبور از لایه‌ها|۴.۱|
|۱۳|محل محاسبات Home نامشخص|Query تجمیعی + Cache + GetHomeSummaryUseCase|۴.۲|
|۱۴|Calendar و History تکراری|ادغام؛ تب Progress اضافه|۳|
|۱۵|واحد lb|ذخیره‌ی kg و تبدیل در نمایش|۱۳|
|۱۶|Intensity بدون تعریف|مقیاس ۱ تا ۱۰، ورودی/مشتق، session_rpe|۷.۴|
|۱۷|Timezone و تقویم شمسی|UTC + local_date + timezone_id؛ نمایش Jalali|۶.۴، ۱۳|
|۱۸|نبود Testing|استراتژی کامل Test|۱۶|
|۱۹|ارقام فارسی/عربی در ورودی|NumberInputParser|۱۳|
|۲۰|نبود DI|Hilt|۵|
|۲۱|ناسازگاری Flow ذخیره با Error Handling|Draft و بازیابی از Kill|۸.۱، ۱۵|
|۲۲|حرکات زمان‌محور (Plank/Hold) قابل ثبت نبودند|measure_type = DURATION|۶.۵|
|۲۳|حذف تصادفی Workout|Undo Snackbar|۱۱.۴|
|۲۴|نبود Validation مشخص|جدول محدوده‌ها|۱۵|
|۲۵|Accessibility|الزامات صریح|۱۴.۲، ۱۷|
|۲۶|Schema بدون کلید مناسب Merge/Sync|UUID و updated_at|۱۰.۱|
|۲۷|Privacy غیرقابل تضمین|بدون مجوز INTERNET در V1|۱۷|

---

**پایان سند — نسخه ۰.۱**
