/*
 * Copyright (c) 2025 Ece Akkurt Kılıç
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.Constants;
import utils.enums.ScrollAmount;
import utils.enums.ScrollDirection;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class QAJobsPage extends BasePage {

    private static final String QA = "Quality Assurance";
    private static final String CITY = "Istanbul, Turkiye";
    private static final Duration POLL_INTERVAL = Duration.ofMillis(250);
    private static final Duration STABLE_WINDOW = Duration.ofSeconds(1);

    @FindBy(xpath = "//a[contains(.,'See all QA jobs')]")
    private WebElement seeAllQaJobs;

    @FindBy(id = "select2-filter-by-location-container")
    private WebElement locDropdown;

    private final By locationOptionBy = By.xpath("//li[contains(@id, 'select2-filter-by-location-result-') and contains(normalize-space(.),'" +
            CITY.replace("'", "\\'") + "')]");

    // Fixed the accidentally truncated alphabet in the translate() mapping
    private final By viewRoleAny = By.linkText("View Role");

    private final By jobCards = By.cssSelector("[data-team-item], .position-list-item, .job-card, [data-position]");

    public boolean isOpened() {
        return isElementVisible(Constants.SPAN, QA);
    }

    public void clickSeeAllQAJobs() {
        String beforeUrl = getDriver().getCurrentUrl();
        safeClick(seeAllQaJobs);

        // Wait for navigation or content to be ready
        wait.until(driver -> {
            // 1) Prefer URL change when navigation happens in the same tab
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.equals(beforeUrl)) {
                return true;
            }
            // 2) Otherwise, ensure the DOM is ready and job cards are present
            Object ready = ((JavascriptExecutor) driver).executeScript("return document.readyState");
            boolean domReady = "complete".equals(ready) || "interactive".equals(ready);
            boolean hasCards = !driver.findElements(jobCards).isEmpty();
            return domReady && hasCards;
        });
        // Additional delay for dropdown data to load
        delayFor(4000);
    }

    public void filterLocation() {
        delayFor(1000);
        safeClick(locDropdown);
        delayFor(1000);
        safeClick(getDriver().findElement(locationOptionBy));
        scrollPage(ScrollDirection.VERTICAL, ScrollAmount.MEDIUM);
    }

    public boolean isQAJobListCardsExists() {
        List<WebElement> cards = waitForQaJobCards();
        return !cards.isEmpty();
    }

    public void clickViewRole() {
        List<WebElement> cards = waitForQaJobCards();
        if (cards.isEmpty()) {
            throw new AssertionError("No jobs found after filtering.");
        }

        // Prefer the first visible/interactive hit inside the first visible card;
        // if none found, search across all cards as fallback
        WebElement target = cards.stream()
                .filter(WebElement::isDisplayed)
                .map(card -> card.findElements(viewRoleAny))
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream().filter(WebElement::isDisplayed).findFirst().orElse(list.get(0)))
                .findFirst()
                .orElseGet(() -> {
                    List<WebElement> hits = getDriver().findElements(viewRoleAny);
                    if (hits.isEmpty()) throw new AssertionError("'View role' link/button not found in any job card.");
                    return hits.get(0);
                });

        scrollIntoView(target);
        safeClick(target);
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        switchToNewTabIfOpened();
    }

    private void switchToNewTabIfOpened() {
        String currentHandle = getDriver().getWindowHandle();
        Set<String> allHandles = getDriver().getWindowHandles();
        if (allHandles.size() > 1) {
            for (String handle : allHandles) {
                if (!handle.equals(currentHandle)) {
                    getDriver().switchTo().window(handle);
                    break;
                }
            }
        }
    }

    /**
     * Waits until job cards are visible and their count remains unchanged for a short, stable window.
     * Additionally, checks that all titles/texts include the QA keyword.
     */
    private List<WebElement> waitForQaJobCards() {
        wait.pollingEvery(POLL_INTERVAL).ignoring(StaleElementReferenceException.class);

        final long stableNanos = STABLE_WINDOW.toNanos();
        final String qaKey = QA.toLowerCase(Locale.ROOT);

        final AtomicLong lastChangeNs = new AtomicLong(System.nanoTime());
        final AtomicInteger lastCount = new AtomicInteger(-1);

        return wait.until(driver -> {
            List<WebElement> cards = driver.findElements(jobCards);
            if (cards.isEmpty() || cards.stream().noneMatch(WebElement::isDisplayed)) return null;

            int count = cards.size();
            long now = System.nanoTime();

            if (count != lastCount.getAndSet(count)) {
                lastChangeNs.set(now);
                return null; // count changed → not stable yet
            }
            if ((now - lastChangeNs.get()) < stableNanos) return null; // wait until stable window passes

            boolean allQa = cards.stream()
                    .map(el -> {
                        try {
                            return el.getText();
                        } catch (StaleElementReferenceException e) {
                            return "";
                        }
                    })
                    .filter(t -> t != null && !t.isBlank())
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .allMatch(s -> s.contains(qaKey));

            return allQa ? cards : null;
        });
    }

    private void delayFor(long millis) {
        new Actions(getDriver()).pause(Duration.ofMillis(millis)).perform();
    }
}
