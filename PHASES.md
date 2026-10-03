# WARRIOR — فازهای اجرا و گزارش پیشرفت

> همگام با بخش ۱۸.۲ سند ARCHITECTURE.md، با این الزام که **هر فاز در GitHub Actions
> باید Build موفق و APK قابل نصب تولید کند**.

## قوانین اجرا

1. هر فاز فقط پس از تأیید فاز قبلی توسط کاربر شروع می‌شود.
2. پایان هر فاز = گزارش (تغییرات + شواهد Build/Test محلی) + کد Push-ready روی همین ریپو.
3. معیار قبولی هر فاز: `./gradlew assembleDebug` و `./gradlew test` سبز باشند
   (همان مسیری که CI اجرا می‌کند).
4. تا فاز ۶ هیچ کلید API/Backend استفاده نمی‌شود؛ بدون مجوز INTERNET (بخش ۱۷ سند).

## نقشه فازها

| فاز | عنوان | خروجی قابل تحویل | وضعیت |
|---|---|---|---|
| ۰ | پایه | پروژه Gradle KTS + Compose/M3 + Hilt + Room Schema کامل (۱۰ جدول) + Theme دوتیره + Localization FA/RTL/شمسی + CI Actions (build+test+APK artifact) + تست‌های پایه | ✅ تکمیل + بازبینی و رفع ۴۳ باگ (۸۵ تست، CI سبز) |
| ۱ | هسته‌ی ثبت | Workout Draft، ثبت Strength و Fast Input؛ بخش وزن بدن در v0.2.0 تکمیل شده، Timer و وزنه خارجی باقی است | 🟨 در حال اجرا |
| ۲ | Boxing | Activityهای Boxing، Round/Rest Timer، ثبت دستی Rounds | ⬜ |
| ۳ | مرور | History (List/Calendar شمسی)، صفحه جزئیات، ویرایش، حذف با Undo | ⬜ |
| ۴ | Progress | Volume/PR/e1RM، کش آماری (exercise_daily_stats/pr_events)، نمودارها، Goals، Body Weight | ⬜ |
| ۵ | کتابخانه و امنیت داده | Seed کتابخانه حرکات (نسخه‌دار)، Backup/Restore JSON، Migration Test | ⬜ |
| ۶ | پرداخت نهایی | Accessibility، Performance، رفع باگ، Release Signing، راهنمای گام‌به‌گام ساخت APK | ⬜ |

## گزارش فازها

_(با تکمیل هر فاز، یک بخش «گزارش» شامل تغییرات، فایل‌های جدید، نتایج Build/Test و لینک Actions اضافه می‌شود.)_

> **یادداشت:** پیش از شروع فاز ۱، فاز ۰ به‌طور کامل بازبینی و ۳۷ باگ آن رفع شد.
> جزئیات، شواهد و موارد عمداً رفع‌نشده در «گزارش بازبینی و رفع باگ فاز ۰» در انتهای همین فایل آمده است.
> یک نکته‌ی مهم: باگ شماره‌ی ۳۸ (`LocalDate.ofInstant` → کرش روی اندروید ۸ تا ۱۳) فقط به‌خاطر افزودن گام Lint به CI کشف شد؛
> یعنی ارزش خودِ گام Lint در همین بازبینی ثابت شد.

---

## گزارش فاز ۰ — تکمیل شد ✅

**تغییرات کلیدی:**
- پیاده‌سازی UI Shell (خانه، تنظیمات، صفحات placeholder تاریخچه/پیشرفت/تمرینات) با Material 3 و پشتیبانی از تم تیره/روشن
- سیستم تنظیمات کامل: زبان (FA/EN)، تقویم شمسی/میلادی، ارقام فارسی/لاتین با DataStore + Flow
- موتور تقویم جلالی بدون وابستگی به ICU (الگوریتم جدول مرجع jdatetime)
- Room Schema کامل با ۱۰ جدول (Workout, Activity, Set, Round, Exercise, Goal, PrEvent, BodyWeight, DailyStats, ...)
- ماشین‌حساب‌های دامنه: VolumeCalculator (Tonnage)، IntensityCalculator، PrCalculator، GoalCalculator
- اعتبارسنجی ورودی‌ها (NumberInputParser برای ارقام فارسی، InputValidator برای کران‌ها)
- ۷ کلاس تست واحد (۳۳ Assertion سبز)
- GitHub Actions Workflow: build.yml شامل Unit Tests + assembleDebug + APK Artifact

**فایل‌های جدید:** ۸۴ فایل (کد Kotlin، تست‌ها، منابع XML، تنظیمات Gradle، Workflow)

**شواهد Build/Test محلی:**
- `./gradlew test` → BUILD SUCCESSFUL (۳۳ تست سبز)
- `./gradlew assembleDebug` → BUILD SUCCESSFUL (APK تولید شده)

**لینک Actions:** https://github.com/KhodeSushianm/Warrior/actions/runs/36850464805 (Workflow ID: 36850464805)

**Artifact APK:** warrior-debug-apk (17.3 MB) قابل دانلود از صفحه Summary این Run

---

## گزارش تحویل ثبت تمرین وزن بدن — نسخه ۰.۲.۰ ✅

- مسیر کامل ساخت یا ادامه‌ی Workout Draft و ذخیره فوری هر Set در Room
- کتابخانه داخلی ۱۹ حرکت دوزبانه برای Push، Pull، Legs و Core
- پشتیبانی از حرکت‌های تکراری و زمان‌محور با اعتبارسنجی مرکزی
- افزودن/حذف حرکت، ثبت/حذف ست، شماره‌گذاری مجدد و پایان Workout
- نمایش تاریخ و ارقام بر اساس تنظیمات فارسی/انگلیسی و شمسی/میلادی
- خلاصه تمرین‌های تکمیل‌شده: تعداد حرکت، ست، تکرار، زمان Hold و مدت Session
- بازسازی ردیف‌های `exercise_daily_stats` در Transaction پایان تمرین
- نسخه اپ: `versionCode=2` و `versionName=0.2.0`

موارد باقی‌مانده از فاز ۱: ثبت وزنه خارجی، TimerEngine و Foreground Service.

---

## گزارش بازبینی و رفع باگ فاز ۰ — انجام شد ✅

بازبینی کامل فاز ۰ (۵۱ فایل Kotlin، ۹ فایل XML، Gradle، CI) انجام و **۴۳ باگ/نقص** رفع شد.
معیار قبولی طبق قوانین بالا: تست‌ها سبز + بیلد موفق.

### روش راستی‌آزمایی

چون سندباکس این بازبینی فقط ۱ گیگابایت رم داشت، بیلد کامل Gradle در آن OOM می‌شد. برای همین
راستی‌آزمایی چندلایه انجام شد:

| لایه | ابزار | نتیجه |
|---|---|---|
| منطق خالص دامنه | kotlinc 1.9.24 + JUnit 4.13.2 (خارج از Gradle) | **۷۵ تست سبز** (پیش از بازبینی: ۳۳) |
| type-check کل اپ | kotlinc روی هر ۵۱ فایل main + classpath کامل Android/Compose/AAR | **۰ خطای frontend** |
| تولید کد Room/Hilt | `./gradlew :app:kspDebugKotlin` | **سبز** |
| پایداری Schema | diff بایت‌به‌بایت `1.json` قبل/بعد | **۹ از ۱۰ جدول کاملاً یکسان** |
| موتور تقویم | مقایسه با الگوریتم مرجع jalaali روی ۷۳٬۴۱۴ تاریخ | **۰ اختلاف** |
| بیلد کامل + APK + Lint | GitHub Actions | **سبز** (Run #3) |
| Lint | `:app:lintDebug` در CI + بررسی گزارش | **۱ خطای NewApi پیدا و رفع شد** (شماره‌ی ۳۸) |

### باگ‌های بحرانی

| # | باگ | شواهد | رفع |
|---|---|---|---|
| ۱ | **Schema اتاق به بیرون از ریپو صادر می‌شد** — `"$projectDir/../../docs/schemas"` به `<repo>/../docs/schemas` حل می‌شد | فایل `1.json` واقعاً در `"$ARENA_WORKSPACE"/docs/…` ساخته شد، نه در ریپو؛ هیچ schema ای در Git نبود | `$rootDir/docs/schemas` + commit شدن `1.json` + گارد جدید در CI (§12: «نگهداری Schemaها در Git») |
| ۲ | **`jalaliLeapByCycle()` برای همه‌ی سال‌ها `true` برمی‌گرداند** | ۱۷ اختلاف با الگوریتم مرجع در بازه‌ی AP 1330..1460؛ همه‌ی سال‌های بیرون جدول کبیسه گزارش می‌شدند | جایگزینی با الگوریتم رسمی زیرواحد ۳۳‌ساله‌ی ایرانی (`jalCal`)؛ اکنون ۰ اختلاف در AP 1280..1519 |
| ۳ | **`toJalali()` تاریخ ناممکن تولید می‌کرد** | `2072-03-21 → JalaliDate(1450, **13**, 1)` و `2072-03-22 → (1450, 11, 14)` یعنی **حرکت به عقب در زمان** | مرز جدول با طول واقعی سال کنترل می‌شود + جست‌وجوی دودویی؛ `init` کلاس `JalaliDate` اکنون ماه/روز نامعتبر را با استثنا رد می‌کند. ۰ خروجی نامعتبر در پویش ۱۸۸۰–۲۱۳۰ |
| ۴ | **`format()` به Locale وابسته بود** | با `Locale=ar` خروجی «ارقام فارسی» شد `١٤٠٥` (U+0661 عربی) نه `۱۴۰۵` (U+06F1 فارسی)؛ با `Locale=fa` پارامتر `persianDigits=false` نادیده گرفته می‌شد | `String.format(Locale.ROOT, …)`؛ تست روی ۹ زبان |
| ۵ | **تغییر زبان روی اندروید ۱۲ و پایین‌تر بی‌اثر بود** | `MainActivity : ComponentActivity` در حالی‌که `AppCompatDelegate.setApplicationLocales` فقط توسط `AppCompatActivity` اعمال می‌شود؛ `android:localeConfig` هم وجود نداشت؛ تم هم `android:Theme.Material` بود | `AppCompatActivity` + تم `Theme.AppCompat.DayNight.NoActionBar` + `res/xml/locales_config.xml` + بازیابی زبان از DataStore هنگام start سرد (§13) |
| ۳۸ | **`Clock.localDateToday()` روی اندروید ۸ تا ۱۳ کرش می‌کرد** — `LocalDate.ofInstant` فقط از **API 34** وجود دارد ولی minSdk برابر ۲۶ است؛ بدون desugaring یعنی `NoSuchMethodError`. این همان تابعی است که `workouts.local_date` را می‌سازد، یعنی **هر** ثبت تمرین روی اکثر دستگاه‌های واقعی می‌ترکید. چون هیچ‌کس صدايش نمی‌زد و Lint هم اجرا نمی‌شد، در فاز ۰ دیده نشد — **توسط گام Lint جدید در همین بازبینی کشف شد** | `now().atZone(zone).toLocalDate()` (هر دو API 26) + ارتقای `NewApi` به severity خطا در Lint + تست جدید `ClockTest` |
| ۳۹ | **آیکون اپ در Manifest اعلام نشده بود** (`MissingApplicationIcon`) → اپ با آیکون پیش‌فرض اندروید نصب می‌شد و در نتیجه `ic_launcher_background`، `ic_launcher_foreground` و هر ۵ فایل PNG آیکون **بلااستفاده** بودند | افزودن `android:icon="@mipmap/ic_launcher"`؛ زنجیره‌ی adaptive icon وصل شد |
| ۴۰ | `mipmap-anydpi-v26` با minSdk=26 بی‌معنا بود (`ObsoleteSdkInt`) | تغییر نام به `mipmap-anydpi` |
| ۴۱ | رشته‌ی `coming_soon_phase_2` در هر دو زبان تعریف شده بود ولی هیچ صفحه‌ای مصرفش نمی‌کرد (`UnusedResources`) | حذف؛ در فاز ۲ همراه با صفحه‌ی Boxing برمی‌گردد |
| ۴۲ | `android:label` روی Activity تکراری بود (`RedundantLabel`) و `localeConfig` هشدار `UnusedAttribute` می‌گرفت | حذف label اضافی + `tools:targetApi="tiramisu"` |
| ۶ | **`DatabaseConverters` کاملاً کد مُرده بود** | `grep -ro DatabaseConverters` روی **کل** کد تولیدشده‌ی KSP → **۰ مورد**. همه‌ی ستون‌های enum به‌صورت `String` آزاد بودند و DAOها literal داشتند | تایپ شدن همه‌ی ستون‌های enum در هر ۱۰ entity + پارامترهای DAO. نتیجه: ۹ از ۱۰ جدول بایت‌به‌بایت یکسان ماند (فقط نوع Kotlin عوض شد، ستون همچنان TEXT). نقض §6.7/§10.16 برطرف شد |

### باگ‌های مهم

| # | باگ | رفع |
|---|---|---|
| ۷ | `provideAppScope()` بدون `@Singleton` → هر تزریق یک `CoroutineScope` جدید با `SupervisorJob` می‌ساخت که هیچ‌وقت cancel نمی‌شد (نشتی) | افزودن `@Singleton` |
| ۸ | یک بایت خراب در DataStore کل UI را از کار می‌انداخت: `ThemeMode.valueOf()` داخل `Flow.map` استثنا می‌داد و Flow برای همیشه error می‌شد | `enumValueOrDefault` + تست |
| ۹ | ایندکس تکراری روی `exercise_daily_stats` دقیقاً روی همان دو ستونِ کلید اصلی مرکب (SQLite خودش autoindex می‌سازد) → هزینه‌ی نوشتن دو برابر روی داغ‌ترین جدول Cache | حذف ایندکس؛ تنها تغییر عمدی Schema |
| ۱۰ | `InputValidator` برای مقدار **کمتر از حد** پیام `TOO_LARGE` می‌داد (`reps=0`، `load=-0.5`، `bodyWeight=19`، …) و **تست‌های موجود همان رفتار غلط را assert می‌کردند** | تفکیک `TOO_SMALL`/`TOO_LARGE` برای هر کران؛ ثابت‌های کران عمومی و مطابق جدول §15 تست شدند؛ تست‌ها اکنون هر دو سمت هر کران را می‌سنجند |
| ۱۱ | **`NaN` به‌عنوان وزن بدن پذیرفته و ذخیره می‌شد** — همه‌ی مقایسه‌های ترتیبی با NaN مقدار `false` می‌دهند | `isNaN() → INVALID_FORMAT`؛ بی‌نهایت‌ها درست به کران‌ها می‌افتند |
| ۱۲ | Auto Backup **همه‌ی تنظیمات** را از دست می‌داد: DataStore در `files/datastore/` است نه `shared_prefs/`؛ ضمناً `domain="rootdir"` اصلاً مقدار معتبری نیست (معتبر: root/file/database/sharedpref/external/device_*) | دامنه‌های معتبر + `include domain="file" path="datastore/"` در هر دو فایل قواعد |
| ۱۳ | `upsertExercises`/`upsertMuscles` با وجود نام «upsert» از `IGNORE` استفاده می‌کردند → هرگز به‌روز نمی‌کردند و نسخه‌بندی Seed (§9.3) شکسته بود | `REPLACE` + `upsertSeedBatch` تراکنشی + `getInstalledSeedVersion()` |
| ۱۴ | `isInUse()` با قرارداد مستندش نمی‌خواند: KDoc می‌گفت «Workoutهای **COMPLETED**» ولی کوئری هیچ Join یا فیلتر وضعیت نداشت | حفظ بررسی گسترده (به‌عنوان قفل حذف فیزیکی، چون FK از نوع RESTRICT است) + افزودن `isReferencedByCompletedWorkout()` برای متن UI (§11.3) |
| ۱۵ | `getSetsForExercise` **Draft و WARMUP را وارد آمار می‌کرد** — بدون `status='COMPLETED'`، بدون `is_completed=1`، بدون `set_type != 'WARMUP'` (نقض صریح مقدمه‌ی §7) | تفکیک `getCountedSetsForExercise` (آمار/PR) از `getAllSetsForExercise` (نمایش جزئیات) |
| ۱۶ | `getExerciseIdsTouchedByWorkout` از طریق `sets` Join می‌شد → اگر آخرین Set یک Exercise حذف می‌شد، آن Exercise از دامنه‌ی ابطال بیرون می‌افتاد و ردیف کهنه‌ی Cache **برای همیشه** باقی می‌ماند (دقیقاً سناریوی §11.4) | خواندن از `activities` |
| ۱۷ | `searchExercises`: جای `COLLATE NOCASE` غلط بود (به الگو می‌چسبد و بی‌اثر است)؛ `%` و `_` ورودی کاربر به wildcard تبدیل می‌شد؛ Exerciseهای آرشیوشده در جست‌وجو می‌آمدند ولی در بقیه‌ی Pickerها نه | `(search_text COLLATE NOCASE) LIKE :q ESCAPE '\'` + هلپر `escapeLike()` + `is_archived = 0` + افزودن `getActiveExercises()` |
| ۱۸ | `getLastSessionSets` به‌جای «آخرین Session» **کل تاریخچه** را برمی‌گرداند (Pre-fill §14 اشتباه پر می‌شد) | ساب‌کوئری همبسته برای محدود کردن به یک Workout |
| ۱۹ | **daemon گریدل OOM می‌شد**: `-Xmx1024m` همراه با `kotlin.compiler.execution.strategy=in-process` (کامپایلر کاتلین داخل همان heap) | بازتولید شد: crash در `:app:mergeExtDexDebug` با `hs_err_pid*.log` و پیام «insufficient memory for the Java Runtime Environment». رفع: heap 2048m + metaspace 512m + کامپایلر out-of-process + `HeapDumpOnOutOfMemoryError`؛ `.gitignore` هم `hs_err_pid*.log` و `*.hprof` گرفت |

### باگ‌های متوسط

| # | باگ | رفع |
|---|---|---|
| ۲۰ | `toJalali()` حدود **۴۸٫۱ میکروثانیه** طول می‌کشید — تا ۱۱۰ بار `LocalDate.parse()` در هر فراخوانی | جدول epoch-day از پیش محاسبه‌شده + جست‌وجوی دودویی → **۰٫۱۳۹ میکروثانیه (۳۴۶ برابر سریع‌تر)**؛ یک صفحه‌ی تقویم ۶ هفته‌ای از ۲٫۰۲ms به ۰٫۰۰۶ms رسید. تست regression عملکرد اضافه شد (§17) |
| ۲۱ | `BEST_E1RM` برای حرکات Bodyweight ردیف PR با مقدار **صفر** می‌ساخت که هرگز شکسته نمی‌شد (چون تساوی رکورد جدید نیست) و Cache را آلوده می‌کرد | افزودن شرط `loadTypeApplies` هم‌راستا با §7.3 |
| ۲۲ | `BEST_SET_VOLUME` ستون‌های DURATION را مستثنا نمی‌کرد | افزودن گارد |
| ۲۳ | `GoalCalculator.progress` برای هدف با span صفر همیشه `1.0` می‌داد، حتی وقتی `isAchieved` می‌گفت `false` → نوار پیشرفت پر ولی Badge نرسیده | واگذاری به `isAchieved`؛ همچنین `current` غیرمتناهی دیگر `NaN` بیرون نمی‌دهد (`coerceIn` مقدار NaN را عبور می‌دهد) |
| ۲۴ | `periodWindow` می‌توانست بازه‌ی **وارونه** برگرداند (`end < start`) وقتی deadline گذشته بود → هر کوئری `BETWEEN` بی‌صدا هیچ چیز برنمی‌گرداند | `coerceAtLeast(start)` |
| ۲۵ | تنظیمات به‌صورت رشته‌ی آزاد (`"AUTO"`/`"JALALI"`) بین Repository، ViewModel و Screen دست‌به‌دست می‌شد — نقض §6.7؛ ضمناً `weekStart` که `GoalCalculator` و `WeeklySummaryCalculator` لازم داشتند **هیچ تأمین‌کننده‌ای نداشت** (کد مرده) | enumهای `CalendarSystem`/`DigitSystem`/`AppLanguage` + `ResolvedSettings` که `AUTO` را بر پایه‌ی زبان حل می‌کند و `weekStart` را از §13 مشتق می‌کند |
| ۲۶ | `AppCompatDelegate.setApplicationLocales` از dispatcher نامعلوم صدا زده می‌شد در حالی که وضعیت Activity را تغییر می‌دهد | `withContext(Dispatchers.Main.immediate)` |
| ۲۷ | `SystemClockImpl` سازنده‌ی `@Inject` نداشت ولی `BindsModule.bindClock` به آن نیاز داشت — Dagger binding را **تنها در صورت درخواست** اعتبارسنجی می‌کند، پس «اتفاقی» کامپایل می‌شد و در فاز ۱ با اولین UseCase می‌ترکید | افزودن `@Inject constructor()`؛ حذف پارامتر گمراه‌کننده‌ی `now(zone)` (مقدار `Instant` ذاتاً مستقل از zone است)؛ افزودن `zoneId()` برای `workouts.timezone_id` |
| ۲۸ | `fallbackToDestructiveMigrationOnDowngrade()` با §12 («fallbackToDestructiveMigration ممنوع») در تضاد بود؛ در اپ local-first یعنی پاک شدن بی‌صدای داده | حذف شد |
| ۲۹ | flash سرد در حالت روشن تیره بود: تم والد `android:Theme.Material` (فقط تیره) با `windowBackground` ثابت `#0F1115` | `Theme.AppCompat.DayNight.NoActionBar` + `values-night/` |
| ۳۰ | نقش‌های رنگی M3 تعریف نشده بودند: `NavigationBar` از `surfaceContainer`، `Card`/`FilterChip` از `surfaceContainerLow`/`secondaryContainer` و `TopAppBar` از `surface` استفاده می‌کنند و همه به پالت خنثی پیش‌فرض می‌افتادند | تعریف کامل نردبان surface-container + نقش‌های container/outline/inverse در هر دو پالت |
| ۳۱ | KDoc تم به `[MetricTypography]` ارجاع می‌داد که **وجود نداشت** | اصلاح و مستند کردن شیوه‌ی واقعی (style در محل فراخوانی) |
| ۳۲ | `app_name` ترجمه شده بود (`وریور`) در حالی که §13 صریحاً می‌گوید «نام اپ در هر دو زبان یکسان است و ترجمه نمی‌شود» | حذف از `values-fa` + `translatable="false"` روی `app_name` و دو endonym زبان |
| ۳۳ | پلاگین serialization فقط در `:app` resolve می‌شد و یک کپی دوم از classpath پلاگین Kotlin به buildscript تزریق می‌کرد | `apply false` در `build.gradle.kts` ریشه |
| ۳۴ | `enableEdgeToEdge()` **پیش از** `super.onCreate()` صدا زده می‌شد (پنجره هنوز attach نشده) | جابه‌جایی ترتیب |
| ۳۵ | ردیف چیپ‌های تنظیمات scroll نداشت → با برچسب‌های انگلیسی یا Font Scaling بزرگ (§17) بریده می‌شد | `horizontalScroll` + `Arrangement.spacedBy` |
| ۳۶ | `README.md` فقط یک خط بود (`# Warrior`) | نگارش کامل README |
| ۳۷ | CI هیچ گاردی برای Schema نداشت و Lint هم اجرا نمی‌کرد | افزودن گام «Verify Room schema is committed and up to date» + گام `lintDebug` + artifact گزارش Lint |

### موارد شناخته‌شده‌ی رفع‌نشده (عمدی)

| مورد | دلیل |
|---|---|
| فونت **Vazirmatn** بسته‌بندی نشده (§13) | نیاز به افزودن باینری فونت با بررسی لایسنس دارد؛ در `WarriorTheme` به‌صورت TODO مستند شد |
| انتزاع `CalendarSystem` و `UnitFormatter` در لایه‌ی نمایش (§13) | در فاز ۰ هنوز صفحه‌ای تاریخ/عدد رندر نمی‌کند؛ `ResolvedSettings.useJalali`/`usePersianDigits` نقطه‌ی تصمیم را فراهم می‌کنند |
| استفاده نکردن از `android.icu.util.PersianCalendar` (§5 و §13) | **انحراف عمدی و بهتر**: تقویم ICU وابسته به OEM/Locale است، ولی موتور纯 Kotlin روی همه‌ی دستگاه‌ها یکسان است. پیشنهاد: اصلاح §5/§13 سند |
| وزن `1` برای Round با مدت صفر/نامعلوم در `IntensityCalculator` | انحراف از میانگین وزنی خالص §7.2 (اختلاط عدد بی‌بعد با ثانیه)؛ رفتار مستند و تست‌شده است و تغییرش تصمیم محصولی است |
| پارامتر بلااستفاده‌ی `durationSec` در `VolumeCalculator.setVolume` | برای تقارن محل فراخوانی نگه داشته شد؛ فقط warning کامپایلر |
| `targetSdk 34` / AGP 8.5.2 / Compose BOM 2024.06 | یک نسل عقب‌تر از زمان بازبینی؛ به‌روزرسانی، ریسک مستقل خودش را دارد و بهتر است در فاز ۶ انجام شود (هشدار `OldTargetApi` عمداً روشن نگه داشته شد) |
| شکل آیکون‌های Launcher (`IconLauncherShape`) | بازطراحی گرافیکی است نه رفع باگ؛ در API 26+ لایه‌ی adaptive icon اولویت دارد. به فاز ۶ موکول و در پیکربندی Lint مستند شد |
| `abortOnError = false` در Lint | تا وقتی baseline نداشته باشیم، یک قاعده‌ی تازه می‌تواند دروازه‌ی فاز (§16) را بی‌ربط قرمز کند. در فاز ۶ با commit شدن baseline به `true` تبدیل می‌شود |

### آزمون‌ها

- **۳۳ → ۸۵ تست** (۵۲ تست جدید)، همه سبز.
- `JalaliTest` (جدید، ۱۸ تست): Nowruzهای منتشرشده، سال‌های کبیسه‌ی AP 1330..1459 از الگوریتم مرجع، مرزهای دقیق جدول، پویش ۱۸۸۰–۲۱۳۰ برای «هرگز ماه/روز ناممکن نده»، رفت‌وبرگشت Gregorian↔Jalali روی ۷۳٬۴۱۴ روز، رفت‌وبرگشت همه‌ی تاریخ‌های معتبر AP 1340..1450، مستقل بودن `format()` از ۹ زبان، نام روزها و ماه‌ها، طول ماه‌ها، و تست regression عملکرد.
- `EnumsTest` (جدید، ۸ تست): پارس امن، `fromTag`، شروع هفته، حل `AUTO`، RTL، و **تست پایداری `name` همه‌ی enumها** (چون §10.16 ذخیره بر پایه‌ی `name` است، تغییر نام = migration).
- `ExerciseDaoQueryTest` (جدید، ۵ تست): الگوی LIKE برای جست‌وجو — escape شدن `%`/`_`/`\`، رفت‌وبرگشت با unescape برای ورودی‌های بیمارگونه، متن فارسی بدون دستکاری، و اینکه U+066F (درصد عربی) با `%` اشتباه گرفته نشود.
- `ClockTest` (جدید، ۵ تست): edge case الزامی §16 یعنی «جابه‌جایی Timezone» — یک instant واحد در UTC/تهران/نیویورک/Kiritimati، پویش ساعت‌به‌ساعت یک روز کامل در چهار zone، سناریوی ثبت تمرین دقیقاً قبل از نیمه‌شب، و رفت‌وبرگشت `local_date` با لایه‌ی نمایش شمسی. همراه با `FixedClock` در سورس تست (تا در APK release نرود).
- `InputValidatorTest` بازنویسی: هر کران از **هر دو سمت**، NaN/بی‌نهایت، و تطابق ثابت‌ها با جدول §15.
- `GoalCalculatorTest` و `PrCalculatorTest` گسترش یافتند.

### شواهد Build/Test

- `kotlinc + JUnit` روی کل `domain`/`core`/`data` → `OK (85 tests)`
- type-check هر ۵۱ فایل main با classpath کامل Android → `0 frontend errors`
- `./gradlew :app:kspDebugKotlin` → سبز؛ `docs/schemas/…/1.json` داخل ریپو تولید شد
- diff Schema: تنها `exercise_daily_stats` تغییر کرد (حذف ایندکس تکراری)؛ `identityHash` از `06a8f098…` به `ec1650f0…` تغییر کرد که برای نسخه‌ی ۱ بدون کاربر بی‌خطر است
- اثبات رفع باگ ۶: ارجاع به `DatabaseConverters` در کد تولیدشده‌ی Room از **۰ → ۱۸ مورد** در ۶ فایل `*_Impl.java` رسید
- **GitHub Actions روی `a26a3da`: هر ۱۶ گام سبز** (Unit tests · Android Lint · assembleDebug · گارد Schema · سه artifact)
  - لینک: https://github.com/KhodeSushianm/Warrior/actions/runs/36877625621
  - تأیید از خودِ artifact تست CI (نه از اجرای محلی):

    | کلاس تست | تعداد | خطا | skip |
    |---|---|---|---|
    | JalaliTest | ۲۰ | ۰ | ۰ |
    | GoalCalculatorTest | ۱۱ | ۰ | ۰ |
    | PrCalculatorTest | ۱۱ | ۰ | ۰ |
    | InputValidatorTest | ۱۰ | ۰ | ۰ |
    | EnumsTest | ۸ | ۰ | ۰ |
    | VolumeCalculatorTest | ۶ | ۰ | ۰ |
    | ClockTest | ۵ | ۰ | ۰ |
    | ExerciseDaoQueryTest | ۵ | ۰ | ۰ |
    | IntensityCalculatorTest | ۵ | ۰ | ۰ |
    | NumberInputParserTest | ۴ | ۰ | ۰ |
    | **جمع** | **۸۵** | **۰** | **۰** |

  - Artifact اپ: `warrior-debug-apk` (۱۶٫۵۲ مگابایت)

### نتیجه‌ی Lint

از **۱۵ مورد (۱ خطا)** در ابتدای بازبینی به **۱ مورد (۰ خطا)** رسید:

| یافته | تعداد اولیه | وضعیت |
|---|---|---|
| `NewApi` (**خطا**) | ۱ | ✅ رفع شد — باگ ۳۸، کرش روی اندروید ۸ تا ۱۳ |
| `MissingApplicationIcon` | ۱ | ✅ رفع شد — باگ ۳۹ |
| `UnusedResources` | ۴ | ✅ رفع شد — باگ ۳۹ و ۴۱ |
| `ObsoleteSdkInt` | ۱ | ✅ رفع شد — باگ ۴۰ |
| `RedundantLabel` / `UnusedAttribute` | ۲ | ✅ رفع شد — باگ ۴۲ |
| `IconLauncherShape` | ۵ | ⏸ موکول به فاز ۶: بازطراحی آیکون کار گرافیکی است نه رفع باگ؛ در API 26+ هم adaptive icon اولویت دارد. در `lint { disable }` مستند شد |
| `OldTargetApi` (targetSdk 34) | ۱ | ⏸ عمداً روشن نگه داشته شد به‌عنوان سیگنال صادقانه؛ ارتقا در فاز ۶ |

`abortOnError` فعلاً `false` است تا یک قاعده‌ی تازه‌افزوده‌شده نتواند دروازه‌ی فاز (§16: بیلد سبز + APK قابل نصب) را بی‌ربط قرمز کند؛ گزارش به‌عنوان artifact منتشر می‌شود. در فاز ۶ همراه با commit شدن baseline به `true` تبدیل می‌شود.
