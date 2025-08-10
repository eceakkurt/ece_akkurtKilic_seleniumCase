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

package core;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import utils.Constants;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class DriverFactory {
    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();

    private DriverFactory() {}

    public static void init(String baseURL, String browser, String firefoxBinary) {
        String br = browser == null ? Constants.CHROME : browser.trim();
        switch (br) {
            case Constants.FIREFOX:
                TL_DRIVER.set(createFirefoxDriver(firefoxBinary));
                break;
            case Constants.CHROME:
            default:
                WebDriverManager.chromedriver().setup();
                TL_DRIVER.set(new ChromeDriver(getChromeOptions()));
                break;
        }

        WebDriver driver = getDriver();
        // Prefer explicit waits (implicit 0) to avoid mixing
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        try {
            driver.manage().window().maximize();
        } catch (Exception ignored) {}

        if (baseURL != null && !baseURL.isEmpty()) {
            driver.navigate().to(baseURL);
        }
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu", "--no-sandbox", "--window-size=1920,1080");
        options.addArguments("--disable-notifications");
        // Headless toggle via -Dheadless=true
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("profile.default_content_setting_values.geolocation", 2);
        options.setExperimentalOption("prefs", prefs);
        options.setAcceptInsecureCerts(true);
        return options;
    }

    private static WebDriver createFirefoxDriver(String firefoxBinary) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("network.stricttransportsecurity.preloadlist", false);
        options.addPreference("security.cert_pinning.enforcement_level", 0);
        options.addPreference("dom.security.https_only_mode", false);
        options.addPreference("services.settings.server", "");
        options.setAcceptInsecureCerts(true);
        if (firefoxBinary != null && !firefoxBinary.isEmpty()) {
            options.setBinary(firefoxBinary);
        }
        return new FirefoxDriver(options);
    }

    public static WebDriver getDriver() {
        WebDriver driver = TL_DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("Driver is not initialized. Call DriverFactory.init() first.");
        }
        return driver;
    }

    public static void quit() {
        WebDriver driver = TL_DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                TL_DRIVER.remove();
            }
        }
    }
}
