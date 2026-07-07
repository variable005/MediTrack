# MediTrack

> [!WARNING]
> **Disclaimer:** This project is open-source and provided "as is" without warranty of any kind, express or implied. It is a personal development project and is not intended to substitute for professional medical advice, diagnosis, or treatment. Always consult a healthcare provider for any medical decisions. The developer is not liable for any missed doses, alarm failures, or other issues resulting from using this software.

A clean, modern, and privacy-focused Android application designed to help you track your daily medication schedules and stay consistent with your doses.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Current Project Status](#current-project-status)
3. [Key Features](#key-features)
4. [Architecture & Tech Stack](#architecture--tech-stack)
5. [User Interface Design](#user-interface-design)

---

## Project Overview
MediTrack is a personal helper app designed to simplify tracking medication schedules. With support for flexible timing, custom dosage inputs, local databases, and system reminders, it offers a secure and hassle-free way to manage your health logs right from your device.

## Current Project Status
This project has been in active development for quite some time now. It has evolved through multiple iterations, transforming from a simple log utility into a fully immersive, Material 3 compliant application. We are continuously adding features and polishing components.

## Key Features
- **Daily Dashboard:** 
  - Groups today's doses clearly by time of day (Morning, Afternoon, Night).
  - Displays a visual circular progress indicator summarizing completed doses.
  - Features an intelligent "Next Up" indicator highlighting the next scheduled dose or warning about missed inputs.
- **Persistent Alarm Notifications:**
  - Integrates with Android's system `AlarmManager` to dispatch precise local reminder alerts.
  - Reschedules all active alarms automatically upon device reboot using system boot receivers.
- **Smart Expiry Tracking:**
  - Highlights validation milestones with visual indicators (Valid, Expiry Soon, Expired) based on medicine inventory.
- **Interactive Check-offs:**
  - One-tap dose logging that dynamically updates visual lists, strikes out completed items, and updates the dashboard.
- **History Logs:**
  - Clean tabbed interface separating Active and Expired medication history logs.

## Architecture & Tech Stack
- **Jetpack Compose:** 100% Kotlin-native UI declarations.
- **Material Design 3 (M3):** Fully immersive edge-to-edge screens with dynamic/fallback color matching, rounded cards, and floating pill-dock navigation.
- **Room Database:** Secure, lightweight, on-device SQLite storage.
- **Flow & Coroutines:** Asynchronous, reactive state management and database synchronization.
- **AlarmManager & Receivers:** System-level scheduling framework for local background notifications.

## User Interface Design
- **Immersive Full Screen:** Designed to draw layout elements directly under transparent status and navigation bars.
- **Floating Pill Dock:** Floating glassmorphic dock holding navigation controls and a centered FAB.
- **Custom Sectioning:** Distinct morning, afternoon, and night layouts with theme-aligned icons.
