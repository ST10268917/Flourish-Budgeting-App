 ## Flourish Budgeting App
![Flourish Banner](https://github.com/user-attachments/assets/5897f43f-7708-4a38-9930-c6c3c1198aa2) 

**Grow Your Finances, Secure Your Future**

Flourish is an intelligent and user-friendly Android budgeting app designed to simplify money management. It offers instant financial insights, customizable savings targets, and tools to improve your financial knowledge—all in a single, convenient platform. Developed using Kotlin and Firebase, Flourish blends the best features of apps like YNAB and TimelyBills into a gamified, engaging experience that makes handling your finances both effortless and enjoyable (OpenAI, 2025). 

## Table of Contents

1.  [Introduction](#introduction)
2.  [Timeline](#timeline)
3.  [Improvements](#improvements)
4.  [Features](#features)
5.  [Two Features](#features)
6.  [Functionality Requirements](#functionality-requirements)
7.  [Setup Instructions](#setup-instructions)
8.  [Installation and Setup](#installation-and-setup)
9.  [Building and Running the Prototype](#building-and-running-the-prototype)
10.  [System Functionalities and User Roles](#system-functionalities-and-user-roles)
11.  [Roadmap](#roadmap)
12.  [Demo Video](#demo-video)
13. [Technology Stack](#technology-stack)
14. [Coding Activity](#coding-activity)
15. [Get Started](#get-started)
16. [GitHub and GitHub Actions Usage](#github-and-github-actions-usage)
17. [Contributing](#contributing)
18. [Reference List](#reference-list)
19. [Group Members](#Group-Members)

## 🟢Introduction

Flourish empowers students and young professionals to take control of their finances effortlessly. Unlike traditional budgeting apps, it combines real-time spending insights, interactive financial dashboards, and multi-currency support with a gamified learning experience. (OpenAI, 2025). Built for clarity and engagement, Flourish turns complex money management into an intuitive, educational, and even rewarding journey just from your smartphone. (OpenAI, 2025).

## 🟠 Timeline 

* **Part 1:** In Part 1, we conducted research, developed a detailed project plan, and created design documents to guide the development process.
* **Part 2:** In Part 2,We started implementing the core features based on the Flourish app requirements.
* **Part 3:** In Part 3, we refined the application by incorporating feedback from our lecturer. The end result is a fully functional mobile app that runs smoothly on a mobile device.

## 🟠 Improvements
* In Part 1, we followed the project requirements and planned the structure of our Flourish app.
* In Part 2, we began building the application and received feedback from our lecturer, who suggested adding a navigation bar for better usability.
* In Part 3, we implemented the navigation bar and refined the app accordingly.
  

## 🟠 Features

* **Expense Tracking:** Add, view, and categorize expenses with optional receipt images.
* **Budget Planning:** Set monthly spending limits and monitor category-based spending.
* **Smart Insights:** View summaries of total spend per category for a selected period.
* **Receipt Management:** Attach photos from the camera or gallery for better record-keeping.
* **User Authentication:** Sign in securely using Firebase Authentication.
* **Currency Converter:** Flourish's Currency Exchange allows users to convert currencies in real time using up-to-date rates.
* **Gamified Experience:** Motivating visuals and feedback to encourage better money habits.

## 🟠 Two Features 

* **1**  **Currency Converter** 

Flourish includes a dedicated Currency Exchange feature, allowing users to view and convert between currencies using real-time exchange rates. This supports international students or users who earn/spend across different currencies.

**Main Functions/Purpose**  :

* Converts between global currencies.

* Real-time rates fetched from online APIs.

* Accessible under the Currency Exchange page.

* Tied directly into expense tracking and category summaries.

* **2** **Gamification**

To boost user motivation and engagement, Flourish introduces a gamified budgeting journey. Users are rewarded with visual achievements and can build streaks for consistent budgeting behavior.

**Main Functions/Purpose**  :

* Achievement Badges: Earned for milestones like first budget set, week-long streak, or consistent saving.

*  Daily Streaks: Encourages daily logins and financial activity.

*  Leaderboard (Stretch Goal): Compete with friends via secure code.



## 🟠 Functionality Requirements

* **User Registration & Authentication:**

    * Email sign-up and login
    * Secure password encryption 

* **Financial Dashboard:**

    * View total balance across accounts
    * Monthly budget overview
    * Upcoming bills & recent transactions
    * View graphs 

* **Income & Expense Tracking:**

    * Manual transaction logging with category, method, date, and notes.
    * Optional photo attachment (e.g. receipts)

* **Budgeting Tools:**

    * Set category-specific monthly budgets
    * Visual indicators (progress bars, pie charts)
    * Budget overspend alerts

* **Notifications:**

    * Reminders for bills, overdue payments, and budget updates.
      

* **Built-in Calculator:**

    * For complex calculations prior to logging transactions.

* **Currency Converter:**

    * Real-time exchange rate display and conversions 

* **Gamification:** 

    * Achievement badges
    * Daily streaks
    * Budgeting leaderboards (optional) 

## 🟢 Setup Instructions

**Tools Required:**

* Android Studio (latest version)
* Firebase
* Kotlin SDK
* Emulator or Android device 

## 🟠 Installation and Setup

1.  Clone the repository
2.  Open it in Android Studio
3.  Sync Gradle and resolve dependencies
4.  Connect your Firebase project
5.  Update `google-services.json`
6.  Run the app on emulator/device 

## Building and Running the Prototype

* Open the project in Android Studio
* Sync Gradle and connect Firebase
* Run the app using Shift + F10 or the play button (OpenAI, 2025).

## System Functionalities and User Roles

* **User:**

    * Register/login
    * Add expenses/income
    * Create/manage budgets
    * Set savings goals
    * Access financial content
    * Earn badges


## 🟢 Roadmap

**Completed**

* Dashboard, authentication, and navigation
* Expense/income logging
* Budget and category management


**In Progress**

* Advanced analytics dashboard
* Enhanced PDF report generation
* Financial literacy hub
* Savings goals and gamification 

## 🟢 Images

### Flourish UI Screenshots

**Welcome/Authentication:**

![image](https://github.com/user-attachments/assets/66b0aa04-f901-475b-a085-18a95ebb460c)



-The entry point for new and returning users, offering options to log in or sign up.


**Signup with Currency Selection:**

![image](https://github.com/user-attachments/assets/6e1734e3-f3d6-4562-a8bc-5a3da08a570f)


-New users can create an account and choose their preferred currency for budgeting.


**Login Screen:**

![image](https://github.com/user-attachments/assets/19e4be41-9d93-4d46-a070-b30e342e22b3)


-Existing users can securely access their Flourish account.

**Personalized Dashboard:**

![Screenshot 2025-05-02 230930 (1)](https://github.com/user-attachments/assets/469b3e9f-6150-44bd-910c-92f63ae04bf6)  



-A central overview of the user's current financial status and recent activity.

**Creating Spending Categories:**

![image](https://github.com/user-attachments/assets/9e04b09f-80a6-4346-8615-fb5184dcc0ab)


-Users can define custom categories to organize their expenses.

**Category Transactions:**

![image](https://github.com/user-attachments/assets/aebdecdc-7748-44da-893b-539b4d293e49)


-A detailed view of all expenses recorded within a specific category.

**Adding a New Expense:**

![image](https://github.com/user-attachments/assets/6d25962e-01a9-4e71-a3ff-620d8f673ed7)


-Users can input details of their spending, with optional receipt capture via camera or gallery.

**Viewing Expense Details:**

![image](https://github.com/user-attachments/assets/de4c0c80-c8c5-464b-a1bf-f55b7664e9d1)


-Tapping on an expense allows users to see all associated information, including any attached receipt.

**Setting Monthly Budgets:**

![image](https://github.com/user-attachments/assets/4b2dc3a1-3e80-4874-91f1-77fe7f996975)
![image](https://github.com/user-attachments/assets/75c034d1-5328-4960-b8fe-b745ac6875b7)


-Users can define maximum and minimum spending limits for each month.

**Filtering Spending by Date:**

![image](https://github.com/user-attachments/assets/3c8965e7-f255-4d7b-aaf7-8f92713302e6)


-Users can analyze their spending within specific date ranges.

## Demo Video
**Youtube Link:** https://youtu.be/wzVz7D5z9O0

## Github Repository
**Github Link:** 


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
    </tr>
</table>

## Coding Activity

* Version-controlled via GitHub
* Sprint-based commits aligning with development timeline

## Get Started

Follow the [Installation and Setup](#installation-and-setup) and [Building and Running the Prototype](#building-and-running-the-prototype) instructions to begin using Flourish on your Android device. (OpenAI, 2025).

## 🟠GitHub and  GitHub Actions Usage

Repository Management

This project was version-controlled using GitHub, ensuring all team members could collaborate effectively. Each sprint phase was mapped to specific branches (e.g., feature-authentication, feature-dashboard, feature-gamification), and pull requests were used to merge tested code into main.

GitHub Actions Integration

To maintain code quality and streamline builds:

*  Gradle Build Automation ran checks on every push.
*  Firebase App Distribution (planned) to automate app testing.

GitHub Activity Highlights:

* Daily commits and sprint logs

* Detailed PRs with peer reviews

* Branch naming conventions and issue linking for traceability



## 🟠 Contributing

We welcome your ideas and fixes:

1.  Fork the repo
2.  Create a new branch
3.  Make your changes
4.  Push to your fork
5.  Submit a Pull Request (OpenAI, 2025).

## 🟢 Reference List


Google Fonts. n.d. Browse Fonts - Google Fonts. [online] Available at: [https://fonts.google.com/](https://fonts.google.com/) [Accessed 2 May 2025].

Android Developers. n.d.-a. CameraX overview | Android media | Android Developers

OpenAI. 2024. Chat-GPT (Version 4). [Large language model]. Available at: [https://chat.openai.com](https://chat.openai.com) [Accessed: 2 April 2025].

## 🟠 Group Members
- Aisha Bilal Jakhura - ST10268917
- Khatija Moosa Amod - ST10258766
- Atiyyah Moola - ST10053786
- Yashna Komla - ST10202387







                                                                                              

