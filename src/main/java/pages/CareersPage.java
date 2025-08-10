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

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CareersPage extends BasePage {
    private static final String QA_CATEGORY_URL = "https://useinsider.com/careers/quality-assurance/";

    @FindBy(id = "career-our-location")
    private WebElement blockLocations;

    //@FindBy(css = "section[id*='team' i], div[id*='team' i], section[class*='team' i], div[class*='team' i]")
    @FindBy(id = "career-find-our-calling")
    private WebElement blockTeams;

    @FindBy(xpath = "//section[contains(@class,'elementor-section')][.//h2[text()='Life at Insider']]")
    private WebElement blockLifeAtInsider;

    public boolean isOpened() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOf(blockLocations),
                    ExpectedConditions.visibilityOf(blockTeams),
                    ExpectedConditions.visibilityOf(blockLifeAtInsider)
            ));
            return true;
        } catch (TimeoutException e) {
            return isVisible(blockLocations) || isVisible(blockTeams) || isVisible(blockLifeAtInsider);
        }
    }

    public void openQACategory() {
        getDriver().navigate().to(QA_CATEGORY_URL);
        wait.until(ExpectedConditions.urlContains("/careers/quality-assurance"));
    }
}
