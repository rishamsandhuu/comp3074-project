# Hunt Quest

Course project for **COMP-3074**  
**Group:** 20  

---

## Team Members

- **Gia Nagpal** – 101508691  
- **Nirja Arun Dabhi** – 101509539  
- **Danuja Shankar** – 101515077  
- **Rishamnoor Kaur** – 101508552  

---

# HuntQuest

HuntQuest is an Android field-game app inspired by classic scavenger hunts.  
Players visit real-world Points of Interest (POIs), see a task/mission to complete, and can:

- View the POI on a map  
- Get directions from their current location  
- Rate the POI  
- Share it with others (e.g. via email)

The app is standalone (no backend server required) and runs entirely on the device using local storage and Google APIs.

---

## 1. Overview

HuntQuest turns a simple city walk into an interactive scavenger hunt.  
Each Point of Interest (POI) represents a real-world location that includes a mission for the player to complete. From the app, players can:

- Browse a list of all POIs
- Open details for any POI
- See that POI on a map
- Get turn-by-turn directions
- Rate their experience
- Share the POI with friends

All data is stored locally on the device. Google Maps / Places APIs are used for map display, addresses, and navigation.

---

## 2. Features (Mapped to Project Requirements)

### ✅ Points of Interest Management (Req. 1, 6, 7)

Each POI stores:

- **Name** – the title/name of the point  
- **Address** – human-readable address (with Places API autocomplete)  
- **Task / Mission** – instructions for what the user should do or find at this location  
- **Tags** – extensible tags such as `easy`, `hard`, `photo`, `info`, etc.  
- **Rating** – 1–5 star rating given by the user  

Supported actions:

- **Add new POIs**  
- **Edit existing POIs**  
- **Delete POIs**  
- **View a list of all POIs**  
- **Open a POI details screen** when a point is selected from the list  

From the POI details screen, users can:

- View the POI on a **map**
- Get **directions** from their current location
- **Rate** the POI
- **Share** the POI (e.g. via email)

---

## 3. Technology Stack

- **Platform:** Android  
- **Language:** Kotlin / Java (depending on implementation)  
- **Maps & Places:** Google Maps SDK, Google Places API  
- **Storage:** Local on-device storage   

---

