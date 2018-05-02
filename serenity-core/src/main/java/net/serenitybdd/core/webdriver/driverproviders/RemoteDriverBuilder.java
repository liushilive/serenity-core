package net.serenitybdd.core.webdriver.driverproviders;

import com.google.common.base.*;
import io.appium.java_client.android.*;
import io.appium.java_client.ios.*;
import net.serenitybdd.core.buildinfo.*;
import net.serenitybdd.core.di.*;
import net.serenitybdd.core.exceptions.*;
import net.thucydides.core.steps.*;
import net.thucydides.core.util.*;
import net.thucydides.core.webdriver.appium.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;

import java.io.*;
import java.net.*;
import java.util.*;

abstract class RemoteDriverBuilder {

    private final DriverCapabilityRecord driverProperties;
    protected final EnvironmentVariables environmentVariables;

    RemoteDriverBuilder(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
        this.driverProperties = WebDriverInjectors.getInjector().getInstance(DriverCapabilityRecord.class);
    }

    abstract WebDriver buildWithOptions(String options) throws MalformedURLException;

    WebDriver newRemoteDriver(URL remoteUrl, Capabilities remoteCapabilities, String options) {
        if (StepEventBus.getEventBus().webdriverCallsAreSuspended()) {
            return RemoteWebdriverStub.from(environmentVariables);
        }

        try {
            ensureHostIsAvailableAt(remoteUrl);

            RemoteWebDriver driver;

            switch (AppiumConfiguration.from(environmentVariables).definedTargetPlatform()) {

                case IOS:
                    Capabilities iosCapabilities = AppiumConfiguration.from(environmentVariables).getCapabilities(options);
                    driver = new IOSDriver<>(remoteUrl, iosCapabilities.merge(remoteCapabilities));
                    break;

                case ANDROID:
                    Capabilities androidCapabilities = AppiumConfiguration.from(environmentVariables).getCapabilities(options);
                    driver = new AndroidDriver<>(remoteUrl, androidCapabilities.merge(remoteCapabilities));
                    break;

                case NONE:
                default:
                    driver = new RemoteWebDriver(remoteUrl, remoteCapabilities);
                    break;
            }

            driverProperties.registerCapabilities(remoteCapabilities.getBrowserName(),  CapabilitiesToPropertiesConverter.capabilitiesToProperties(driver.getCapabilities()));
            return driver;

        } catch (UnreachableBrowserException unreachableBrowser) {
            String errorMessage = unreachableBrowserErrorMessage(unreachableBrowser);
            throw new SerenityManagedException(errorMessage, unreachableBrowser);
        } catch (UnknownHostException unknownHost) {
            throw new SerenityManagedException(unknownHost.getMessage(), unknownHost);
        }
    }

    private void ensureHostIsAvailableAt(URL remoteUrl) throws UnknownHostException {
        if (!hostIsAvailableAt(remoteUrl)) {
            theRemoteServerIsUnavailable(remoteUrl.getHost() + " could not be reached");
        }
    }

    private boolean hostIsAvailableAt(URL remoteUrl) {
        try {
            URLConnection urlConnection = remoteUrl.openConnection();
            urlConnection.connect();
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    private void theRemoteServerIsUnavailable(String message) throws UnknownHostException {
        throw new UnknownHostException(message);
    }

    private String unreachableBrowserErrorMessage(Exception unreachableBrowser) {
        List<String> errorLines = Splitter.onPattern("\n").splitToList(unreachableBrowser.getLocalizedMessage());
        Throwable cause = unreachableBrowser.getCause();
        String errorCause = ((cause == null) ? "" :
                System.lineSeparator() + cause.getClass().getSimpleName() + " - " + cause.getLocalizedMessage());
        return errorLines.get(0) + errorCause;
    }


}
