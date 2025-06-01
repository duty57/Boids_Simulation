# Boids Simulation
An interactive boids simulation written in Java with OpenGL and Swing for the graphical user interface.


https://github.com/user-attachments/assets/c4713a73-2aa4-47e5-8e7d-f424470cdb98

## Table of Contents

1. [Project Overview](#project-overview)  
2. [Features](#features)
3. [Getting Started](#getting-started)
   - [Clone & Build](#clone--build)  
   - [Configuration (OpenWeather API Key)](#configuration-openweather-api-key)  
   - [Running the Simulation](#running-the-simulation)  
4. [Usage](#usage)  
   - [Simulation Tab](#simulation-tab)  
   - [Search Tab](#search-tab)  
   - [Mouse Interaction](#mouse-interaction)
5. [Technologies Used](#technologies)

## Project Overview
The simulation involves a flock of autonomous “boid” agents that move within a two-dimensional space. Each boid continuously adjusts its velocity based on three core behaviors—alignment (matching the average heading of nearby boids), cohesion (steering toward the center of mass of its neighborhood), and separation (avoiding overcrowding). Boids also contend with environmental forces: wind exerts a directional bias on their movement (making it easier to travel with the wind and harder against it), and lighting conditions—determined by sun position and cloudiness—affect their visual appearance via a fragment shader (cloudy skies dim specular highlights, while bright sun enhances them). You can further interact with the flock using the mouse: left‐clicking creates an attractive force that pulls nearby boids toward the cursor, while right‐clicking produces a repulsive force that scatters boids away. All boid updates (position, velocity, and shading) run in real time on the JOGL rendering thread, with separate threads handling wind‐vector animation and weather updates.

A Swing‐based GUI overlays the OpenGL canvas, providing sliders for tuning boid parameters (such as max speed, alignment force, cohesion force, separation force, vision range, and drag) alongside weather controls (temperature, wind speed, wind direction, cloudiness, and sun position). Users can enter a city name in the “Search” tab to fetch live weather data from the OpenWeatherAPI, which then populates the corresponding sliders. When ready, a configuration—consisting of all boid parameters and current weather values—can be saved to an embedded SQLite database, displayed in a scrollable list with timestamps and key details, and later loaded or deleted at will.

## Features
- **Flocking / Boids Behavior**  
  - Adjustable parameters for alignment, cohesion, separation, max speed, vision range, and drag force.  
- **Weather-Driven Simulation**  
  - Fetch live data from OpenWeatherAPI by entering a city name.  
  - Wind direction alters the movement direction for boids (simulates gusts).  
  - Sun position & cloudiness feed into a GLSL fragment shader to produce realistic specular lighting and cloud‐driven dimming.  
- **Mouse Interaction (Pull / Push Boids)**  
  - Left‐click near boids to attract them toward the cursor.  
  - Right‐click to repel boids away from the cursor.  
  - Enables real‐time, interactive influence on the flock.  
- **Configurable UI (Swing)**  
  - Two tabs:  
    1. **Simulation** (sliders + save button)  
       - Sliders for:  
         - Max Speed  
         - Alignment Force  
         - Cohesion Force  
         - Separation Force  
         - Vision Range  
         - Drag Force  
         - Temperature (fetched or manual)  
         - Wind Speed (fetched or manual)  
         - Cloudiness (fetched or manual)  
         - Sun Position (fetched or manual)  
         - Wind Direction (fetched or manual)  
    2. **Search** (enter city name to fetch weather + manage saved configurations)  
       - Text field to input a city name and fetch live weather from OpenWeatherAPI  
       - Scrollable list showing saved configuration profiles 
       - Buttons to **Import** or **Delete** selected profiles from the SQLite database  

- **Persistence (SQLite)**  
  - **Save** (from the Simulation tab’s “Save” button) writes a new profile into the database, including:  
    - Timestamp  
    - Boid parameter values (max speed, alignment, cohesion, separation, vision range, drag)  
    - Weather values (temperature, wind speed, wind direction, cloudiness, sun position)  
  - **Import** (in the Search tab) loads a profile from the scrollable list into the sliders on the Simulation tab.  
  - **Delete** (in the Search tab) removes the selected profile from the database. 
- **Multithreaded Weather Visualization**  
  - Wind is displayed (animated vector) in a separate thread, parallel to the boids’ simulation.  
- **Gradle‐Powered Build**  
  - All JOGL, SQLite, and third‐party dependencies are managed via Gradle.
 
## Getting Started
### Clone & Build

1. **Clone the repository**  
   ```bash
   git clone https://github.com/duty57/Boids_Simulation.git
   cd Boids_Simulation
2. **Verify the Graddle Wrapper**
   ```bash
   ./gradlew clean
   ./gradlew build![Simulation_Tab](https://github.com/user-attachments/assets/11c80a12-be84-4ee8-bfca-f1fd38a63794)

### Configuration (OpenWeather API Key)
1. **Obtain Your API Key**
  - Register for a free OpenWeatherAPI account at OpenWeather.
  - Copy your unique API key (a long alphanumeric string).
2. **Create a .env File**
  - In the src/main/resources/ directory, create a file named .env (no prefix) with the following content:
  ```bash
WEATHER_API_KEY=YOUR_OPENWEATHER_API_KEY_HERE
```
  - Replace YOUR_OPENWEATHER_API_KEY_HERE with your actual key.
### Running the Simulation
```bash
./gradlew run
```
## Usage
### Simulation Tab
![Simulation_Tab](https://github.com/user-attachments/assets/8f169563-17b1-4347-b2b5-32725a09a9af)
#### Simulation Parameters
  - Max Speed: Top absolute speed a boid can travel.
  - Alignment Force: Tendency to match neighbor velocities.
  - Cohesion Force: Tendency to move toward the average position of nearby boids.
  - Separation Force: Tendency to steer away from too-close boids.
  - Vision Range: Distance within which a boid “sees” neighbors.
  - Drag Force: Global drag applied each frame (slows down boids over time).
  - Temperature: Can be fetched from OpenWeather or set manually. Impacts shading (warmer = slightly brighter light).
  - Wind Speed: Fetched or manual. Affects boid acceleration magnitude along the wind direction.
  - Cloudiness: Fetched or manual (0–100%). Feeds into fragment shader to dim specular highlights (more clouds = duller reflections).
  - Sun Position: Azimuth angle (0–180°). Used in the fragment shader for specular lighting direction.
  - Wind Direction: Azimuth angle (0–180°). Boids find it “easier” to move with the wind direction vector.\
#### Save Button
https://github.com/user-attachments/assets/4223117b-8f7d-44bd-bf4e-34c4a804f210
  - The current simulation parameters + weather values are stored as a new profile in the SQLite database, along with a timestamp.

### Search Tab
#### Fetch Weather
https://github.com/user-attachments/assets/d80c8504-1cc3-47ab-b050-9b185ee50564
  - Enter the name of a city (e.g., London, San Francisco, São Paulo) and press Enter or click “Search”.
  - The app reads WEATHER_API_KEY from src/main/resources/.env and calls the OpenWeatherAPI to fetch:
    - Temperature (°C)
    - Wind Speed (m/s)
    - Wind Direction (degrees)
    - Cloudiness (%)
    - Sun Position (calculated from sunrise/sunset times)
  - Sliders on the Simulation tab automatically update to match the fetched values.
  - If the city is not found, an error dialog will appear.

#### Manage Configurations
https://github.com/user-attachments/assets/4dc4da80-5f46-4cac-9ac0-c0a39f7dd611
  - A scrollable list of saved configuration profiles appears below the search field.
  - Each entry shows:
    - Date of save
    - Key parameter values (e.g., “Vision=0.05m, Temperature=10℃, Speed=0.25m/s”)
  - Import: Click a saved profile and then the Load button (or double-click) to apply that configuration to the sliders.
  - Delete: Select one or more profiles and click Delete to remove them from the SQLite database.
### Mouse Interaction
https://github.com/user-attachments/assets/11c49cf9-58d2-4d59-91df-6b58225d6af7
  - Left-Click: Attract nearby boids toward the mouse cursor.
  - Right-Click: Repel boids away from the cursor.
  - These interactions update in real time and can be combined with slider changes to create complex flocking patterns.

## Technologies Used
  - Java 8+
  - JOGL (Java OpenGL) 2.3.1
  - Swing (javax.swing) for the GUI
  - SQLite JDBC Driver
  - Gradle (build automation and dependency management)
  - OpenWeatherAPI for live weather data (configured via src/main/resources/.env)
  - GLSL (Vertex & Fragment shaders) for boid rendering, lighting, and shading
