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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.Constants;

import java.time.Duration;

public class HomePage extends BasePage {

    private static final String CAREERS_URL = "https://useinsider.com/careers/";
    private static final Duration HOVER_PAUSE = Duration.ofMillis(200);
    private static final Duration SUBMENU_PAUSE = Duration.ofMillis(150);

    @FindBy(xpath = "//a[@id='navbarDropdownMenuLink' and normalize-space()='Company']")
    private WebElement navbarCompany;

    @FindBy(xpath = "//a[not(@aria-hidden='true') and contains(@href, '/careers')]")
    private WebElement menuCareers;

    public HomePage() {
        acceptCookies();
    }

    public boolean isOpened() {
        String actualTitle = getPageTitle();
        return actualTitle.contains(Constants.NAME);
    }

    public void openCareersFromCompanyMenu() {
        Actions actions = new Actions(getDriver());

        WebElement company = waitForElementVisible(navbarCompany);
        scrollIntoView(company);
        actions.moveToElement(company).pause(HOVER_PAUSE).perform();
        safeClick(company);

        WebElement careers = waitForElementVisible(menuCareers);
        actions.moveToElement(careers).pause(SUBMENU_PAUSE).perform();
        safeClick(careers);

        // wait until URL contains /careers
        wait.until(ExpectedConditions.urlContains("/careers"));
    }
}
