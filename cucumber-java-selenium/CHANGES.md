# CHANGES.md — QA Framework Restructure

**Author:** Senior QA Automation Engineer  
**Date:** June 2025  
**Scope:** Full restructure of the Cucumber-Java-Selenium automation framework targeting https://the-internet.herokuapp.com

---

## 1. Framework Architecture — Layer Separation

### What Changed

The framework was reorganized from a flat, loosely structured codebase into four distinct packages under `io.cucumber`:

| Package | Purpose | Classes |
|---|---|---|
| `core` | WebDriver lifecycle, shared state, DI wiring | `Manager`, `Context`, `Hooks` |
| `glue` | Cucumber step definitions (Gherkin ↔ Java) | `NavigationSteps`, `HomeSteps`, `BasicAuthSteps`, `SortableDataTablesSteps` |
| `pages` | Page Object Model classes | `Page`, `HomePage`, `BasicAuthPage`, `SortableDataTablesPage` |
| `data` | Test data loading utilities | `TestDataLoader` |

### Rationale

The original structure mixed concerns — step definitions and page objects lived in the same conceptual space with generic names (`Home.java` appeared in both `glue/` and `pages/`). Separating into four layers achieves:

- **Single Responsibility:** Each package has one job. Core manages infrastructure, glue maps Gherkin to actions, pages encapsulate DOM interactions, and data handles test input loading.
- **Naming Clarity:** Classes are now descriptively named (`HomeSteps` vs `Home`, `HomePage` vs `Home`) so their role is immediately obvious from the filename.
- **Scalability:** Adding a new scenario means adding one step definition class in `glue/`, one page object in `pages/`, and one properties file in `testdata/` — no ambiguity about where things go.
- **Discoverability:** The `RunCucumberTest` runner uses `@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.cucumber")` to scan all sub-packages, so new packages are automatically discovered without configuration changes.

### Class Renames

| Old File | New File | Reason |
|---|---|---|
| `glue/Home.java` | `glue/HomeSteps.java` | Descriptive name; avoids collision with page object |
| `glue/Navigation.java` | `glue/NavigationSteps.java` | Consistent `*Steps` naming convention |
| `pages/Home.java` | `pages/HomePage.java` | Descriptive name; avoids collision with step definition |
| *(new)* | `glue/BasicAuthSteps.java` | New scenario step definitions |
| *(new)* | `glue/SortableDataTablesSteps.java` | New scenario step definitions |
| *(new)* | `pages/BasicAuthPage.java` | New page object |
| *(new)* | `pages/SortableDataTablesPage.java` | New page object |
| *(new)* | `data/TestDataLoader.java` | New utility class |

### Deleted Files

- `glue/Home.java` — replaced by `glue/HomeSteps.java`
- `glue/Navigation.java` — replaced by `glue/NavigationSteps.java`
- `pages/Home.java` — replaced by `pages/HomePage.java`

---

## 2. Thread.sleep Replacement with Explicit Waits

### What Changed

The base `Page` class previously used `Thread.sleep(5000)` in its `waitForPageLoad()` method. This was replaced with Selenium's `WebDriverWait` combined with `ExpectedConditions`.

**Before:**
```java
public void waitForPageLoad() {
    Thread.sleep(5000);
}
```

**After:**
```java
protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

public void waitForPageLoad() {
    new WebDriverWait(driver, DEFAULT_TIMEOUT)
        .until(ExpectedConditions.jsReturnsValue(
            "return document.readyState === 'complete' ? 'complete' : null"));
}
```

Additionally, every page object now uses `ExpectedConditions` before interacting with elements:

- `HomePage.getExampleLinkTexts()` → `ExpectedConditions.visibilityOfAllElements(exampleLinks)`
- `BasicAuthPage.getContentText()` → `ExpectedConditions.visibilityOf(contentText)`
- `SortableDataTablesPage.getTable1Data()` → `ExpectedConditions.visibilityOfAllElements(tableRows)`

### Rationale

`Thread.sleep` is a well-known anti-pattern in test automation:

1. **Wasted time:** A 5-second sleep always waits the full 5 seconds, even if the page loads in 200ms. Across three scenarios, that is 15 seconds of unnecessary waiting per run.
2. **Flakiness:** If the page takes longer than 5 seconds (slow network, heavy page), the test fails intermittently. There is no retry or polling.
3. **No condition checking:** `Thread.sleep` does not verify anything — it just pauses. The page could be in an error state and the test would proceed blindly after the sleep.

`WebDriverWait` with `ExpectedConditions` solves all three problems:

- **Polls efficiently:** Checks the condition every 500ms (default) and returns immediately when satisfied.
- **Configurable timeout:** The 10-second `DEFAULT_TIMEOUT` provides a generous upper bound while still failing fast on genuine issues.
- **Meaningful conditions:** `jsReturnsValue("return document.readyState === 'complete' ? 'complete' : null")` verifies the DOM is actually ready. `visibilityOfAllElements()` confirms elements are rendered and visible before interaction.
- **Clear failure messages:** When a wait times out, the `TimeoutException` message indicates exactly which condition was not met, making debugging straightforward.

The `PageFactory.initElements(new AppiumFieldDecorator(driver, DEFAULT_TIMEOUT), this)` call was retained in the `Page` constructor. The `AppiumFieldDecorator` provides its own implicit wait for `@FindBy`-annotated elements, and the explicit `WebDriverWait` calls in page methods provide an additional layer of synchronization for specific interactions.

---

## 3. Test Data Externalization

### What Changed

All test data was moved out of step definition Java code and into `.properties` files under `src/test/resources/testdata/`. A new `TestDataLoader` utility class in the `data` package provides a single method to load any properties file by name.

**Properties files created:**

| File | Scenario | Contents |
|---|---|---|
| `homepage-links.properties` | @TEST_TI_0001 | Base URL, link count (40), and all 40 expected link texts |
| `basic-auth.properties` | @TEST_TI_0002 | Username, password, base URL, auth path, expected congratulations text |
| `sortable-tables.properties` | @TEST_TI_0003 | Base URL, page path, row count (4), and all row data (lastName, firstName, email, due, webSite) |

**TestDataLoader usage:**
```java
Properties props = TestDataLoader.load("basic-auth.properties");
String username = props.getProperty("username");
```

### Rationale

Hardcoding test data inside step definitions creates several maintenance problems:

1. **Data changes require code changes:** If the homepage adds a 41st link, a developer must edit Java source, recompile, and redeploy. With properties files, it is a one-line text edit.
2. **Data is not visible at a glance:** Expected values buried in Java assertions are hard to review. Properties files provide a clear, scannable inventory of all test data.
3. **No separation of concerns:** Step definitions should define *behavior* (what to do), not *data* (what to expect). Mixing both makes the code harder to read and maintain.
4. **One file per scenario:** Each scenario's data lives in its own file, making it easy to find, update, and review independently. The naming convention (`homepage-links.properties`, `basic-auth.properties`, `sortable-tables.properties`) maps directly to the scenario under test.

The `TestDataLoader` class is deliberately simple — a single static method that loads from the `testdata/` classpath prefix. It throws an `IllegalArgumentException` with the filename in the message if the file is not found, providing immediate feedback during development. `IOException` during file reading is wrapped in a `RuntimeException` to avoid forcing callers to handle checked exceptions in test code.

### URL Externalization

URLs are also stored in properties files rather than hardcoded in step definitions:

- `base.url=https://the-internet.herokuapp.com` appears in all three properties files
- `auth.path=/basic_auth` in `basic-auth.properties`
- `page.path=/tables` in `sortable-tables.properties`

The `NavigationSteps.navigateToExamplePage()` method dynamically constructs URLs by loading the appropriate properties file based on the page name passed from the Gherkin step. This means adding a new page navigation requires only a new properties file — no Java code changes.

---

## 4. Test Scenarios

### Scenario 1: Homepage Link Verification (`@TEST_TI_0001`)

**Gherkin:**
```gherkin
@TEST_TI_0001
Scenario: Homepage has a list of links to Expected examples
  Given the page under test is 'https://the-internet.herokuapp.com'
  Then the homepage should display the expected list of example links
```

**Approach:**

- `NavigationSteps` opens the homepage URL.
- `HomeSteps.verifyHomepageLinks()` creates a `HomePage` page object, which uses `@FindBy(css = "#content ul li a")` to locate all example links in the content area.
- `HomePage.getExampleLinkTexts()` waits for all links to be visible via `ExpectedConditions.visibilityOfAllElements()`, then extracts the text of each link using Java streams.
- The step definition loads `homepage-links.properties` to get the expected 40 link texts, then asserts:
  1. The count matches (exactly 40 links).
  2. The content matches (both lists contain the same items, regardless of order).

**Design decision:** The order-independent comparison (`containsAll` in both directions) was chosen because the requirement is to verify the *presence* of all expected links, not their DOM ordering. If the site reorders links, the test should still pass as long as all 40 are present.

### Scenario 2: Basic Auth Verification (`@TEST_TI_0002`)

**Gherkin:**
```gherkin
@TEST_TI_0002
Scenario: Basic Auth allows validated access
  Given the page under test is 'https://the-internet.herokuapp.com'
  When the user navigates to Basic Auth with valid credentials
  Then the Basic Auth page should display the congratulations message
```

**Approach:**

- `NavigationSteps` opens the homepage.
- `BasicAuthSteps.navigateToBasicAuth()` loads credentials from `basic-auth.properties` and constructs a credential-embedded URL: `https://admin:admin@the-internet.herokuapp.com/basic_auth`.
- `BasicAuthPage` uses `@FindBy(css = "#content .example p")` to locate the congratulations paragraph, waiting for visibility before reading.
- The step definition asserts the page content contains the expected text ("Congratulations").

**Design decision:** The credential-embedded URL approach (`https://user:pass@host/path`) was chosen over alternatives like Selenium's `Alert` handling or browser-specific auth dialogs. This approach is:
- **Cross-browser compatible:** Works consistently across Chrome versions without relying on alert detection timing.
- **Simple and reliable:** No need to handle browser-native authentication dialogs, which behave differently across browsers and Selenium versions.
- **Externalized:** Credentials live in the properties file, not in Java code.

### Scenario 3: Sortable Data Tables Verification (`@TEST_TI_0003`)

**Gherkin:**
```gherkin
@TEST_TI_0003
Scenario: Sortable Data Tables - Example 1 displays the expected 4 results
  Given the page under test is 'https://the-internet.herokuapp.com'
  When the user navigates to the 'Sortable Data Tables' page
  Then table one should display the expected data
```

**Approach:**

- `NavigationSteps` opens the homepage, then `navigateToExamplePage("Sortable Data Tables")` dynamically loads `sortable-data-tables.properties` to construct the full URL (`https://the-internet.herokuapp.com/tables`).
- `SortableDataTablesPage` uses `@FindBy(css = "table#table1 tbody tr")` to locate all table body rows, waiting for visibility before parsing.
- `getTable1Data()` iterates each row, extracts the 5 cell values by index, and returns a `List<Map<String, String>>` with keys: `Last Name`, `First Name`, `Email`, `Due`, `Web Site`.
- The step definition loads `sortable-tables.properties`, asserts the row count is 4, then verifies each row's fields match the expected values.

**Design decision:** The table data is returned as a `List<Map<String, String>>` rather than a custom POJO. This keeps the page object generic — it does not need to know about the specific data model, and the step definition handles the comparison logic. The `LinkedHashMap` preserves column insertion order for debugging readability.

---

## 5. Dependency Injection Pattern

### What Was Preserved

The PicoContainer dependency injection pattern was retained without modification:

1. **Manager** is instantiated once per scenario by PicoContainer. It holds the `ChromeDriver` instance and a `HashMap<String, Object>` stash for cross-step data sharing.
2. **Context** is the base class for all step definitions and `Hooks`. It accepts a `Manager` via constructor injection and provides convenience methods (`getDriver()`, `getTestStash()`, `stash()`).
3. **All step definition classes** (`NavigationSteps`, `HomeSteps`, `BasicAuthSteps`, `SortableDataTablesSteps`) extend `Context` and receive `Manager` through their constructors.
4. **Hooks** extends `Context` and manages the ChromeDriver lifecycle (`@Before` creates the driver, `@After` quits it, `@AfterStep` captures screenshots).

This pattern ensures that all classes within a scenario share the same WebDriver instance and stash, while each scenario gets a fresh set of instances for isolation.

---

## 6. Page Object Design

### Element Location Strategy

All page objects use Selenium's `@FindBy` annotation with CSS selectors:

| Page Object | Selector | Element Type |
|---|---|---|
| `HomePage` | `#content ul li a` | `List<WebElement>` — all example links |
| `BasicAuthPage` | `#content .example p` | `WebElement` — congratulations paragraph |
| `SortableDataTablesPage` | `table#table1 tbody tr` | `List<WebElement>` — table body rows |

CSS selectors were chosen over XPath for readability and performance. The selectors target stable structural elements (IDs, semantic tags) rather than fragile attributes like class names that may change with styling updates.

### PageFactory with AppiumFieldDecorator

The `PageFactory.initElements(new AppiumFieldDecorator(driver, DEFAULT_TIMEOUT), this)` call in the base `Page` constructor was retained. The `AppiumFieldDecorator` provides:

- Automatic proxy creation for `@FindBy`-annotated fields
- Built-in implicit wait (matching `DEFAULT_TIMEOUT`) for element location
- Compatibility with both standard Selenium and Appium element types

This decorator was part of the original framework and remains appropriate for the Chrome-based test execution.

---

## 7. Step Definition Traceability

Each step definition class includes a Javadoc header linking it to the relevant feature file scenario:

```java
/**
 * Step definitions for Scenario 1: Homepage has a list of links to Expected examples.
 * @see theInternet.feature @TEST_TI_0001
 */
public class HomeSteps extends Context { ... }
```

This provides immediate traceability from code to Gherkin — a reviewer can see which scenario a step definition supports without searching the feature file. The `@see` tag references both the feature file name and the unique scenario tag.

---

## Summary of Files Changed

| Action | File Path |
|---|---|
| Created | `src/test/java/io/cucumber/data/TestDataLoader.java` |
| Created | `src/test/java/io/cucumber/pages/HomePage.java` |
| Created | `src/test/java/io/cucumber/pages/BasicAuthPage.java` |
| Created | `src/test/java/io/cucumber/pages/SortableDataTablesPage.java` |
| Created | `src/test/java/io/cucumber/glue/HomeSteps.java` |
| Created | `src/test/java/io/cucumber/glue/BasicAuthSteps.java` |
| Created | `src/test/java/io/cucumber/glue/SortableDataTablesSteps.java` |
| Created | `src/test/java/io/cucumber/glue/NavigationSteps.java` |
| Created | `src/test/resources/testdata/homepage-links.properties` |
| Created | `src/test/resources/testdata/basic-auth.properties` |
| Created | `src/test/resources/testdata/sortable-tables.properties` |
| Modified | `src/test/java/io/cucumber/pages/Page.java` |
| Modified | `src/test/resources/io/cucumber/features/theInternet.feature` |
| Deleted | `src/test/java/io/cucumber/glue/Home.java` |
| Deleted | `src/test/java/io/cucumber/glue/Navigation.java` |
| Deleted | `src/test/java/io/cucumber/pages/Home.java` |
