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
| ۰ | پایه | پروژه Gradle KTS + Compose/M3 + Hilt + Room Schema کامل (۱۰ جدول) + Theme دوتیره + Localization FA/RTL/شمسی + CI Actions (build+test+APK artifact) + تست‌های پایه | ⏳ در انتظار تأیید |
| ۱ | هسته‌ی ثبت | Workout Draft (ذخیره مرحله‌ای)، ثبت Strength، Fast Input، TimerEngine + Foreground Service | ⬜ |
| ۲ | Boxing | Activityهای Boxing، Round/Rest Timer، ثبت دستی Rounds | ⬜ |
| ۳ | مرور | History (List/Calendar شمسی)، صفحه جزئیات، ویرایش، حذف با Undo | ⬜ |
| ۴ | Progress | Volume/PR/e1RM، کش آماری (exercise_daily_stats/pr_events)، نمودارها، Goals، Body Weight | ⬜ |
| ۵ | کتابخانه و امنیت داده | Seed کتابخانه حرکات (نسخه‌دار)، Backup/Restore JSON، Migration Test | ⬜ |
| ۶ | پرداخت نهایی | Accessibility، Performance، رفع باگ، Release Signing، راهنمای گام‌به‌گام ساخت APK | ⬜ |

## گزارش فازها

_(با تکمیل هر فاز، یک بخش «گزارش» شامل تغییرات، فایل‌های جدید، نتایج Build/Test و لینک Actions اضافه می‌شود.)_
