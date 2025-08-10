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

import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CareersPage;
import pages.HomePage;
import pages.LeverApplicationFormPage;
import pages.QAJobsPage;
import utils.Constants;

public class CareerTests extends TestBase {

    @Test(
            description = "Verify that a user can navigate from the Home page to the Careers page, " +
                    "open the QA jobs listing, filter by location, and view the details of the first QA role."
    )
    public void shouldNavigateToFirstQARoleDetailsFromHomePage() {
        HomePage homePage = new HomePage();
        Assert.assertTrue(homePage.isOpened(), Constants.ERROR_HOME_PAGE_NOT_OPENED);

        homePage.openCareersFromCompanyMenu();
        CareersPage careers = new CareersPage();
        Assert.assertTrue(careers.isOpened(), Constants.ERROR_CAREER_PAGE_NOT_OPENED);

        careers.openQACategory();

        QAJobsPage qaJobsPage = new QAJobsPage();
        Assert.assertTrue(qaJobsPage.isOpened(), Constants.ERROR_JOB_PAGE_NOT_OPENED);

        qaJobsPage.clickSeeAllQAJobs();
        qaJobsPage.filterLocation();

        Assert.assertTrue(qaJobsPage.isQAJobListCardsExists(), Constants.ERROR_NO_QA_JOBS_FOUND);

        qaJobsPage.clickViewRole();

        LeverApplicationFormPage leverApplicationFormPage = new LeverApplicationFormPage();
        Assert.assertTrue(leverApplicationFormPage.isOpened(), Constants.ERROR_LEVER_APP_PAGE_NOT_OPENED);
    }
}
