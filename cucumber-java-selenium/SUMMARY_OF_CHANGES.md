Summary of Changes
---

This document covers what I changed in the framework and why — from how I reorganized the project structure, to how I implemented the three test scenarios, and what I'd improve next if the framework needed to scale.

# 1. Framework Structure Improvements

## 1.1 Reorganized the folder structure

The original project had everything kind of mixed together — `Home.java` existed in both `glue/` and `pages/`, and there was no clear separation between test data, step definitions, and page interactions.

I split things into four packages:

- `core/` — Manager, Context, and Hooks. Added `getTestData()` and `getBaseUrl()` helpers to Context, and config loading to Hooks.
- `glue/` — step definitions only. Renamed to `*Steps` so it's obvious what they are.
- `pages/` — page objects only. Renamed to `*Page` to avoid name collisions.
- `data/` — a `TestDataLoader` utility for reading properties files.

The idea is that if you need to add a new scenario, you know exactly where each piece goes. One step def in `glue/`, one page object in `pages/`, one data file in `testdata/`.

### Files renamed/replaced

- `glue/Home.java` → `glue/HomeSteps.java`
- `glue/Navigation.java` → `glue/NavigationSteps.java`
- `pages/Home.java` → `pages/HomePage.java`

### New files

- `glue/BasicAuthSteps.java`, `glue/SortableDataTablesSteps.java`
- `pages/BasicAuthPage.java`, `pages/SortableDataTablesPage.java`
- `data/TestDataLoader.java`
- `config.properties`

## 1.2 Shared config.properties

I created a `config.properties` file at the root of `src/test/resources/` for environment-level settings:

```properties
base.url=https://the-internet.herokuapp.com
testdata.path=testdata/
```

This gets loaded once in the `@Before` hook and stashed so every step definition can access it. The base URL and testdata path are no longer hardcoded anywhere in Java code or duplicated across scenario files.

To switch environments (staging, prod, etc.), you change one file. Step definitions use `getBaseUrl()` and the testdata path flows through `TestDataLoader` automatically.

The feature file also benefits — instead of hardcoding the URL in the Gherkin, it now just says `Given the page under test is loaded` and the URL comes from config.

## 1.3 Automatic test data loading

Test data lives in `.properties` files under `src/test/resources/testdata/`, one per scenario, named after the scenario tag:

- `TEST_TI_0001.properties` — the 40 expected link names
- `TEST_TI_0002.properties` — credentials, auth path, expected text
- `TEST_TI_0003.properties` — page path and the 4 expected table rows

The loading is fully automatic:

1. `Hooks.@Before` grabs the scenario tag (e.g. `@TEST_TI_0001`) and stashes it
2. Any step definition calls `getTestData()` (inherited from Context)
3. Context reads the tag from the stash, builds the filename, loads it via `TestDataLoader`, and caches it
4. Subsequent calls in the same scenario return the cached properties

So step definitions just call `getTestData()` — no filenames, no paths, no manual loading. The right data shows up based on which scenario is running.

I originally named the files by feature (like `homepage-links.properties`) but ran into a bug where the dynamic filename generation didn't match the actual filename. Naming by scenario tag removes that ambiguity — if you see `@TEST_TI_0004` in the feature file, the data is in `TEST_TI_0004.properties`.

## 1.4 Got rid of Thread.sleep

The base `Page` class had a `Thread.sleep(5000)` in `waitForPageLoad()`. That's 5 seconds of dead waiting every time a page object is created, even if the page loaded instantly.

Replaced it with `WebDriverWait` + `ExpectedConditions`. Now it polls for `document.readyState === 'complete'` and returns as soon as the page is ready. Same idea in the page objects — each one waits for its specific elements to be visible before interacting with them.

This makes the tests faster and less flaky. If a page is slow, the wait handles it gracefully instead of just hoping 5 seconds is enough.

## 1.5 What I kept as-is

- **PicoContainer DI** — the Manager/Context pattern works well. All new step defs extend Context and get the shared driver through constructor injection.
- **PageFactory with AppiumFieldDecorator** — already set up in the base Page class, handles `@FindBy` element initialization with built-in waits.
- **JUnit 5 runner** — the `RunCucumberTest` suite class and its configuration stayed the same.
- **CSS selectors** — used everywhere for locators. They're readable and faster than XPath for the simple selectors we need here.

---

# 2. Added Features

## 2.1 The Test Scenarios

### Scenario 1: Homepage links (@TEST_TI_0001)

Opens the homepage, waits for it to fully display, grabs all the link texts from `#content ul li a`, and checks them against the expected 40 from the properties file. The comparison checks that all expected links are present — the site may have extra links and that's fine.

### Scenario 2: Basic Auth (@TEST_TI_0002)

Navigates to Basic Auth using credentials embedded in the URL (`https://admin:admin@...`). This avoids dealing with browser auth dialogs, which are unreliable across different Chrome versions and Selenium can't natively interact with them. Then checks that "Congratulations" appears on the page.

### Scenario 3: Sortable Data Tables (@TEST_TI_0003)

Goes to the tables page, parses all rows from `table#table1` into a list of maps (Last Name, First Name, Email, Due, Web Site), and compares each row against the expected data from the properties file.

## 2.2 Element highlighting

I added `highlight()` and `highlightAll()` helpers in the base `Page` class that draw a red border around elements using JavaScript. Each page object calls this before reading element values, so the screenshot captured by `@AfterStep` shows exactly which elements the test was interacting with. Handy for visual debugging — you can open the HTML report and see a red outline around the links, the auth message, or the table rows being verified.

The highlight is wrapped in a try/catch so it never fails the test if something goes wrong (e.g. stale element).

## 2.3 Screenshot naming convention

The `@AfterStep` hook takes a screenshot after every step and attaches it to the Cucumber HTML report. I updated the naming from the generic "A Nice Screenshot." to a standardized format:

```
{SCENARIO_TAG}_Step_{STEP_NUMBER}
```

For example: `TEST_TI_0001_Step_01`, `TEST_TI_0001_Step_02`, `TEST_TI_0002_Step_01`, etc.

A step counter in `Hooks` increments with each `@BeforeStep` and resets per scenario (since PicoContainer creates a fresh Hooks instance). This makes it easy to identify which screenshot belongs to which step when reviewing the HTML report.

## 2.4 Traceability and logging

Each step definition has a Javadoc comment at the top linking it to its scenario tag (e.g., `@TEST_TI_0001`). Makes it easy to trace from code back to the feature file.

Every step also logs what it's doing — URLs being navigated to, expected vs actual values, row-by-row verification results. If a test fails, the logs tell you exactly where and why without needing to debug.

---

# 3. Files summary

| What | File |
|---|---|
| Created | `config.properties` (shared environment config) |
| Created | `data/TestDataLoader.java` |
| Created | `pages/HomePage.java`, `BasicAuthPage.java`, `SortableDataTablesPage.java` |
| Created | `glue/HomeSteps.java`, `BasicAuthSteps.java`, `SortableDataTablesSteps.java`, `NavigationSteps.java` |
| Created | `testdata/TEST_TI_0001.properties`, `TEST_TI_0002.properties`, `TEST_TI_0003.properties` |
| Modified | `core/Context.java` (added `getTestData()` and `getBaseUrl()` helpers) |
| Modified | `core/Hooks.java` (added config loading, scenario tag stashing, screenshot naming) |
| Modified | `pages/Page.java` (Thread.sleep → WebDriverWait, added highlight helpers) |
| Modified | `features/theInternet.feature` (uncommented and updated scenarios) |
| Deleted | `glue/Home.java`, `glue/Navigation.java`, `pages/Home.java` |

---

# 4. Future Recommendations

- A shared `Constants` class — right now each constant lives close to where it's used (e.g. `DEFAULT_TIMEOUT` in `Page.java`). With only 3 scenarios that's fine, but if we start sharing values across many unrelated classes, pulling them into one place would reduce duplication.
- More config options — browser choice, timeout values, headless mode toggle. All could go in `config.properties`.
- Proper logging with SLF4J + Log4j2 — replace `System.out.println` with leveled logging (INFO, DEBUG, WARN, ERROR) for better control over output verbosity and log file output for CI/CD.
- Parallel test execution — the current PicoContainer setup creates isolated state per scenario, so it should be parallelizable with some Maven Surefire config.
- Conventional Commits standard for git messages — using `type(scope): description` format (e.g. `feat(glue): implement BasicAuthSteps`) for consistent, parseable commit history.
