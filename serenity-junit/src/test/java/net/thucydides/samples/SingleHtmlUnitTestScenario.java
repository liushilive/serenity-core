package net.thucydides.samples;

import net.thucydides.core.annotations.*;
import net.thucydides.core.pages.*;
import net.thucydides.junit.runners.*;
import org.junit.*;
import org.junit.runner.*;
import org.openqa.selenium.*;

@RunWith(ThucydidesRunner.class)
@UserStoryCode("US01")
public class SingleHtmlUnitTestScenario {
    
    @Managed(driver = "htmlunit")
    public WebDriver webdriver;

    @ManagedPages(defaultUrl = "classpath:static-site/index.html")
    public Pages pages;
    
    @Steps
    public SampleScenarioSteps steps;
        
    @Test
    @TestsRequirement("SOME_BUSINESS_RULE")
    public void happy_day_scenario() {
        steps.stepThatUsesABrowser();
        steps.stepThatSucceeds();
        steps.stepThatIsIgnored();
        steps.anotherStepThatSucceeds();
        steps.stepThatFails();
        steps.stepThatShouldBeSkipped();
    }
}