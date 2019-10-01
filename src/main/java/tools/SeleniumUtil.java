package tools;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumUtil {
	private static final long DEFAULT_TIMEOUT_SEC = 20;
	public static void findAndWaitAndInput(WebDriver driver, String xpath, String inputVal, String desc) {
		System.out.print("waiting for " + desc);
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT_SEC);
        By by = By.xpath(xpath);
        WebElement ele = wait.until(ExpectedConditions.elementToBeClickable(by));
        ele.sendKeys(inputVal);
        System.out.println(" -> inputed " + desc);
	}
	
	public static void findAndWaitAndClick(WebDriver driver, String xpath, String desc) {
		System.out.print("waiting for " + desc);
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT_SEC);
        By by = By.xpath(xpath);
        WebElement ele = wait.until(ExpectedConditions.elementToBeClickable(by));
        ele.click();
        System.out.println(" -> clicked " + desc);
	}
	
	public static List<WebElement> findAndWaitAndReturnElements(WebDriver driver, String xpath, String desc) {
		System.out.print("waiting for " + desc);
		WebDriverWait wait = new WebDriverWait(driver, DEFAULT_TIMEOUT_SEC);
        By by = By.xpath(xpath);
        wait.until(ExpectedConditions.elementToBeClickable(by));
        
        System.out.println(" -> returning " + desc);
        
        return driver.findElements(by);
	}
	
//	public static void takeScreenShot(WebDriver driver, String id) throws IOException {
//		String screenshotsDir = "/Users/manliu/eclipse-workspace/general_ws/isas-system/output/";
//        String filename = id+".png";
//        
//        if(!new File(screenshotsDir).exists()) {
//        	new File(screenshotsDir).mkdirs();
//        }
//        
//        Path screenshotPath = Paths.get(screenshotsDir, filename);
//        File srcFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
//        FileUtils.copyFile(srcFile, screenshotPath.toFile());
//	}
	
	public static PhantomJSDriver getPhantomJSDriver(String phantomBinPath){
		/**
		 * When running in Linux environment, set below in phantomjs process
		 * (e.g. vi /usr/bin/phantomjs):
		 * 
		 * 	export QT_QPA_PLATFORM=offscreen
		 *	export QT_QPA_FONTDIR=/usr/share/fonts
		 */
        DesiredCapabilities dcaps = new DesiredCapabilities();
        dcaps.setCapability("acceptSslCerts", true);
        dcaps.setCapability("takesScreenshot", true);
        dcaps.setCapability("cssSelectorsEnabled", true);
        dcaps.setJavascriptEnabled(true);
        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomBinPath);

        PhantomJSDriver driver = new PhantomJSDriver(dcaps);
        driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        
        return  driver;
    }
}
