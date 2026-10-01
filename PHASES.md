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
| ۰ | پایه | پروژه Gradle KTS + Compose/M3 + Hilt + Room Schema کامل (۱۰ جدول) + Theme دوتیره + Localization FA/RTL/شمسی + CI Actions (build+test+APK artifact) + تست‌های پایه | ✅ تکمیل (CI سبز) |
| ۱ | هسته‌ی ثبت | Workout Draft (ذخیره مرحله‌ای)، ثبت Strength، Fast Input، TimerEngine + Foreground Service | ⬜ |
| ۲ | Boxing | Activityهای Boxing، Round/Rest Timer، ثبت دستی Rounds | ⬜ |
| ۳ | مرور | History (List/Calendar شمسی)، صفحه جزئیات، ویرایش، حذف با Undo | ⬜ |
| ۴ | Progress | Volume/PR/e1RM، کش آماری (exercise_daily_stats/pr_events)، نمودارها، Goals، Body Weight | ⬜ |
| ۵ | کتابخانه و امنیت داده | Seed کتابخانه حرکات (نسخه‌دار)، Backup/Restore JSON، Migration Test | ⬜ |
| ۶ | پرداخت نهایی | Accessibility، Performance، رفع باگ، Release Signing، راهنمای گام‌به‌گام ساخت APK | ⬜ |

## گزارش فازها

_(با تکمیل هر فاز، یک بخش «گزارش» شامل تغییرات، فایل‌های جدید، نتایج Build/Test و لینک Actions اضافه می‌شود.)_

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
