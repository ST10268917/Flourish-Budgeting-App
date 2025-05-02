![Flourish Banner](https://github.com/user-attachments/assets/5897f43f-7708-4a38-9930-c6c3c1198aa2)  # Flourish

**Grow Your Finances, Secure Your Future**

Flourish is an intelligent and user-friendly Android budgeting app designed to simplify money management. It offers instant financial insights, customizable savings targets, and tools to improve your financial knowledge—all in a single, convenient platform. Developed using Kotlin and Firebase, Flourish blends the best features of apps like YNAB and TimelyBills into a gamified, engaging experience that makes handling your finances both effortless and enjoyable (OpenAI, 2025).

## Table of Contents

1.  [Introduction](#introduction)
2.  [Features](#features)
3.  [Functionality Requirements](#functionality-requirements)
4.  [Setup Instructions](#setup-instructions)
5.  [Installation and Setup](#installation-and-setup)
6.  [Building and Running the Prototype](#building-and-running-the-prototype)
7.  [System Functionalities and User Roles](#system-functionalities-and-user-roles)
8.  [Roadmap](#roadmap)
9.  [Demo Video](#demo-video)
10. [Technology Stack](#technology-stack)
11. [Coding Activity](#coding-activity)
12. [Get Started](#get-started)
13. [Contributing](#contributing)
14. [Reference List](#reference-list)
15. [Group Members](#Group-Members)

## 🟢Introduction

Flourish empowers students and young professionals to take control of their finances effortlessly. Unlike traditional budgeting apps, it combines real-time spending insights, interactive financial dashboards, and multi-currency support with a gamified learning experience. (OpenAI, 2025). Built for clarity and engagement, Flourish turns complex money management into an intuitive, educational, and even rewarding journey just from your smartphone. (OpenAI, 2025)

## 🟠 Features

* **Expense Tracking:** Add, view, and categorize expenses with optional receipt images.
* **Budget Planning:** Set monthly spending limits and monitor category-based spending.
* **Smart Insights:** View summaries of total spend per category for a selected period.
* **Receipt Management:** Attach photos from the camera or gallery for better record-keeping.
* **User Authentication:** Sign in securely using Firebase Authentication.
* **Gamified Experience:** Motivating visuals and feedback to encourage better money habits (Final POE - *Note: This is mentioned as a general feature, but the Functionality Requirements specify this is implemented in Part 3*).

## 🟠 Functionality Requirements

* **User Registration & Authentication:**

    * Email sign-up and login
    * Secure password encryption & two-factor authentication
    * Password reset functionality (Figma, 2024)

* **Financial Dashboard:**

    * View total balance across accounts
    * Monthly budget overview
    * Upcoming bills & recent transactions (Figma, 2024)

* **Income & Expense Tracking:**

    * Manual transaction logging with category, method, date, and notes.
    * Optional photo attachment (e.g. receipts) (Figma, 2024)

* **Budgeting Tools:**

    * Set category-specific monthly budgets
    * Visual indicators (progress bars, pie charts)
    * Budget overspend alerts (Figma, 2024)

* **Notifications:**

    * Reminders for bills, overdue payments, and budget updates (Figma, 2024)

* **Multi-Currency Support:**

    * Select default currency
    * Real-time exchange rates
    * Multi-currency transaction recording (Figma, 2024)

* **Financial Literacy Hub:**

    * Videos, podcasts, and articles
    * Categorized by media type and difficulty level (Figma, 2024)

* **Built-in Calculator:**

    * For complex calculations prior to logging transactions. (Figma, 2024)

* **Reports & Statements:**

    * Downloadable PDF statements
    * Graphical insights
    * Custom date range (Figma, 2024)

* **Multiple Accounts:**

    * Separate transaction history, budgets, and currencies per account. (Figma, 2024)

* **Currency Exchange Page:**

    * Real-time exchange rate display and conversions (Figma, 2024)

* **Gamification:** (Implemented only in PART 3)

    * Achievement badges
    * Daily streaks
    * Budgeting leaderboards (optional) (Figma, 2024)

## 🟢 Setup Instructions

**Tools Required:**

* Android Studio (latest version)
* Firebase
* Kotlin SDK
* Emulator or Android device (OpenAI, 2025).

## 🟠 Installation and Setup

1.  Clone the repository
2.  Open it in Android Studio
3.  Sync Gradle and resolve dependencies
4.  Connect your Firebase project
5.  Update `google-services.json`
6.  Run the app on emulator/device (OpenAI, 2025)

## Building and Running the Prototype

* Open the project in Android Studio
* Sync Gradle and connect Firebase
* Run the app using Shift + F10 or the play button (OpenAI, 2025)

## System Functionalities and User Roles

* **User:**

    * Register/login
    * Add expenses/income
    * Create/manage budgets
    * Set savings goals
    * Access financial content
    * Earn badges (OpenAI, 2025)

* **Admin** (optional future feature):

    * Manage flagged content/resources
    * Monitor user activities for QA (OpenAI, 2025)

## 🟢 Roadmap

**Completed**

* Dashboard, authentication, and navigation
* Expense/income logging
* Budget and category management
* Financial literacy hub
* Savings goals and gamification (OpenAI, 2025).

**In Progress**

* Advanced analytics dashboard
* Enhanced PDF report generation (OpenAI, 2025).

**Planned**

* Integration with bank APIs
* Chatbot for financial advice
* Voice-based entry (OpenAI, 2025).

## 🟢 Images

### Flourish UI Screenshots

## Demo Video
https://youtu.be/wzVz7D5z9O0


> Tools and languages used.
<table>
  <tr>
    <td align="center" width="96">
      <a href="#kotlin">
        <img src="https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin_Icon.png" width="48" height="48" alt="Kotlin" />
      </a>
      <br>Kotlin
    </td>
    <td align="center" width="96">
      <a href="#android-studio">
        <img src="https://developer.android.com/static/studio/images/studio-icon.svg" width="48" height="48" alt="Android Studio" />
      </a>
      <br>Android Studio
    </td>
    <td align="center" width="96">
      <a href="#firebase">
        <img src="https://cdn.iconscout.com/icon/free/png-256/free-firebase-3628772-3030134.png" width="48" height="48" alt="Firebase" />
      </a>
      <br>Firebase
    </td>
    <td align="center" width="96">
      <a href="#figma">
        <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/3/33/Figma-logo.svg/1024px-Figma-logo.svg.png" width="48" height="48" alt="Figma" />
      </a>
      <br>Figma
    </td>
    </tr>
</table>

## Coding Activity

* Version-controlled via GitHub
* Sprint-based commits aligning with development timeline
* Unit and UI testing planned during the Testing phase. (OpenAI, 2025).

## Get Started

Follow the [Installation and Setup](#installation-and-setup) and [Building and Running the Prototype](#building-and-running-the-prototype) instructions to begin using Flourish on your Android device. (OpenAI, 2025).

## 🟠 Contributing

We welcome your ideas and fixes:

1.  Fork the repo
2.  Create a new branch
3.  Make your changes
4.  Push to your fork
5.  Submit a Pull Request (OpenAI, 2025).

## 🟢 Reference List

Draw.io, 2023. draw.io. [online] Available at: [https://app.diagrams.net/](https://app.diagrams.net/) [Accessed 2 April 2025].

Figma, 2025. Figma. [online] Available at: [https://www.figma.com/](https://www.figma.com/) [Accessed 2 April 2025].

Google Fonts. n.d. Browse Fonts - Google Fonts. [online] Available at: [https://fonts.google.com/](https://fonts.google.com/) [Accessed 2 May 2025].

Android Developers. n.d.-a. CameraX overview | Android media | Android Developers

OpenAI. 2024. Chat-GPT (Version 4). [Large language model]. Available at: [https://chat.openai.com](https://chat.openai.com) [Accessed: 2 April 2025].

## 🟠 Group Members
- Aisha Bilal Jakhura - ST10268917
- Khatija Moosa Amod - ST10258766
- Atiyyah Moola - ST10053786
- Yashna Komla - ST10202387







                                                                                              

