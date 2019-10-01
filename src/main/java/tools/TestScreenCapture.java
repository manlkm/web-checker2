package tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class TestScreenCapture {

	public static void main(String[] args) throws IOException {
		String webDriverPath = args[0]; //Users/manliu/temp/selenium_test/driver
		String destFile = args[1]; ///Users/manliu/temp/selenium_test
		
		System.out.println(webDriverPath);
		System.out.println(destFile);
		
		WebDriver driver;

		System.setProperty("webdriver.chrome.driver", webDriverPath);
		
		ChromeOptions chromeOptions = new ChromeOptions();
	    //chromeOptions.setHeadless(true);
	      
		driver = new ChromeDriver(chromeOptions);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		driver.get("http://www.homing.edu.hk/index/customIndex.aspx");

		File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
	     
	    BufferedImage  fullScreen = ImageIO.read(screenshot);
	    
		try {
			//ImageIO.write(screenshot.getImage(), "PNG", new File(destFile));
			//ImageIO.write(fullScreen, "png", screenshot);
		    FileUtils.copyFile(screenshot, new File(destFile));
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		driver.quit();

	}

}
