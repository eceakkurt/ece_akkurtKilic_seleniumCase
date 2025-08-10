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

import core.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.Constants;
import utils.enums.ScrollAmount;
import utils.enums.ScrollDirection;

import java.time.Duration;

public class BasePage {

    // ---- Timing & wait configuration ----
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    protected static final Duration POLL_INTERVAL = Duration.ofMillis(250);

    protected WebDriverWait wait;

    public BasePage() {
        this.wait = new WebDriverWait(getDriver(), DEFAULT_TIMEOUT);
        // Make waits more resilient to DOM churn
        this.wait.pollingEvery(POLL_INTERVAL).ignoring(StaleElementReferenceException.class);
        PageFactory.initElements(getDriver(), this);
    }

    public WebDriver getDriver() {
        return DriverFactory.getDriver();
    }

    public String getPageTitle() {
        return getDriver().getTitle();
    }

    // ---- Generic waits ----
    public WebElement waitForElementVisible(String classText, String elementText) {
        return wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.xpath(generateXPath(classText, elementText))));
    }

    public WebElement waitForElementClickable(String classText, String elementText) {
        return wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath(generateXPath(classText, elementText))));
    }

    public WebElement waitForElementVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    // ---- Page helpers ----
    public void acceptCookies() {
        try {
            waitForElementClickable(Constants.A, Constants.ACCEPT).click();
        } catch (TimeoutException | NoSuchElementException ignored) {
            // Cookie banner not present — continue silently
        }
    }

    public boolean isCorrectPageOpened(String text) {
        return getDriver().getCurrentUrl().contains(text);
    }

    public boolean isElementVisible(String path, String text) {
        try {
            return waitForElementVisible(path, text).isDisplayed();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    public void scrollPage(ScrollDirection direction, ScrollAmount amount) {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        String script = (direction == ScrollDirection.VERTICAL)
                ? "window.scrollBy(0, " + amount.getValue() + ");"
                : "window.scrollBy(" + amount.getValue() + ", 0);";
        js.executeScript(script);
    }

    // ---- Safe interactions ----
    protected void safeClick(WebElement element) {
        if (element == null) return;
        try {
            wait.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(element))).click();
        } catch (RuntimeException first) {
            jsClick(element);
        }
    }

    private void jsClick(WebElement el) {
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", el);
    }

    protected void scrollIntoView(WebElement el) {
        try {
            ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        } catch (Exception ignored) {
        }
    }

    protected boolean isVisible(WebElement element) {
        try {
            return waitForElementVisible(element).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ---- Utilities ----
    public String generateXPath(String classText, String elementText) {
        return String.format("//%s[contains(text(),%s)]", classText, toXPathTextLiteral(elementText));
    }

    // Safely wraps arbitrary text as an XPath literal (handles quotes).
    private String toXPathTextLiteral(String text) {
        if (text == null) return "''"; // empty string literal
        if (!text.contains("'")) return "'" + text + "'";
        if (!text.contains("\"")) return '"' + text + '"';
        // Contains both single and double quotes: build concat('a',"'",'b',...)
        StringBuilder sb = new StringBuilder("concat(");
        String remaining = text;
        boolean first = true;
        while (!remaining.isEmpty()) {
            int i = remaining.indexOf('\'');
            String part = (i == -1) ? remaining : remaining.substring(0, i);
            if (!part.isEmpty()) {
                if (!first) sb.append(",");
                sb.append("'").append(part).append("'");
                first = false;
            }
            if (i != -1) {
                if (!first) sb.append(",");
                sb.append("\"'\""); // literal single-quote
                first = false;
                remaining = remaining.substring(i + 1);
            } else {
                remaining = "";
            }
        }
        sb.append(")");
        return sb.toString();
    }
}