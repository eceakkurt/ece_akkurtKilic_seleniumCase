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

package tests;

import core.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.FileHandler;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class TestBase {

    @BeforeClass
    @Parameters({"baseURL", "browser", "firefoxBinary"})
    public void setup(String baseURL, @Optional("chrome") String browser, @Optional("") String firefoxBinary) {
        DriverFactory.init(baseURL, browser, firefoxBinary);
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        System.out.println("Test closed with result: " + result.getStatus());
        if (ITestResult.FAILURE == result.getStatus()) {
            Path path = takeScreenshot(result.getName());
            if (path != null) {
                System.out.println("Screenshot saved to: " + path.toAbsolutePath());
            }
        }
    }

    @AfterClass
    public void closeBrowser() {
        DriverFactory.quit();
    }

    public Path takeScreenshot(String testName) {
        try {
            Path dir = Paths.get("src", "test", "resources", "screenshots");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path target = dir.resolve(testName + "_" + timestamp + ".png");
            File screenshot = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
            FileHandler.copy(screenshot, target.toFile());
            return target;
        } catch (IOException ex) {
            System.out.println("Screenshot error: " + ex.getMessage());
            return null;
        }
    }

    public WebDriver getDriver() {
        return DriverFactory.getDriver();
    }
}
