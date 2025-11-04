# Alaska Senate Scraper

Small Spring Boot project that scrapes Alaska Senate member profiles using Playwright (Java) and exposes a simple REST endpoint.

Repository layout (relevant files)
- `src/main/java/com/kyc/scraper/utils/PlaywrightScraper.java` — scraping logic (uses Playwright)
- `src/main/java/com/kyc/scraper/persistance/models/Member.java` — model for a member
- `src/main/java/com/kyc/scraper/services/*` — service + controller that wrap the scraper
- `build.gradle` — Gradle build (uses Java 21 and Spring Boot)

What this does
- Visits https://akleg.gov/senate.php, collects member profile links, visits each profile, extracts name, party, district/position, phone, address and email.
- Returns the results via HTTP GET `/api/scrape` and writes `ak_senate_members.json` to the project working directory.

Prerequisites
- Java 21 (the project toolchain is set to Java 21 in `build.gradle`).
- Internet access for downloading dependencies and (on first run) Playwright browser binaries.
- Gradle wrapper is included — use `gradlew.bat` on Windows (project root).
- Optional: Node.js + npm if you prefer to install Playwright browsers using the Node Playwright CLI (see troubleshooting below).

Build
From the project root on Windows (using the included wrapper):

```cmd
gradlew.bat clean build
```

Run (development)
- Run the Spring Boot app with the Gradle wrapper:

```cmd
gradlew.bat bootRun
```

- Or build an executable jar and run it:

```cmd
gradlew.bat bootJar
java -jar build\libs\*.jar
```

API
- GET /api/scrape
  - Example (Windows PowerShell or cmd + curl installed):

```cmd
curl http://localhost:8080/api/scrape
```

- Response
  - Returns a JSON array of `Member` objects.
  - The scraper also writes `ak_senate_members.json` into the working directory where the app runs.

Playwright browser installation (important)
- Playwright Java requires browser binaries (Chromium, Firefox, WebKit) to be present. On first run, Playwright Java will normally attempt to download browsers when `Playwright.create()` is called (requires network access and write permission).

- If automatic download fails or you prefer to install manually, you can use the Node Playwright CLI (requires Node/npm) to install browsers:

```cmd
npm i -D playwright
npx playwright install chromium
```

- If you don't want Playwright to download browsers into the default location, set the `PLAYWRIGHT_BROWSERS_PATH` environment variable to a writable path before running the app.

Troubleshooting
- Playwright cannot download browsers:
  - Ensure the process can access the network and has permission to write into the user cache directory.
  - As an alternative, install Node (`npm`) and run `npx playwright install chromium`.

- Gradle/Java version errors:
  - Confirm you have Java 21 installed. The wrapper will use the project toolchain where possible.

- Permission to write `ak_senate_members.json`:
  - The app writes the JSON file to the current working directory. Run the JVM in a directory where the process has write permission.

- Want to watch the browser while debugging:
  - Edit `PlaywrightScraper.java` and change the launcher option from `setHeadless(true)` to `setHeadless(false)`.

Where to change scraping logic
- `PlaywrightScraper.java` contains the scraping logic. If the site layout changes, update selectors and extraction helpers there.

Fields in the JSON output
- Each `Member` contains:
  - `name`, `title`, `position` (district), `party`, `address`, `phone`, `email`, `url`