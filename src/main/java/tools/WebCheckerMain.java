/**
 * 
 */
package tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

/**
 * @author manliu
 *
 */
public class WebCheckerMain {
	static Logger logger = LogManager.getRootLogger();
	static SimpleDateFormat sdFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		String driverBinPath = args[0];
		String outputDir = args[1];
		String configDir = args[2];
		
		
		WebDriver driver = null;
		List<File> siteConfigs = null;
		try {
			driver = SeleniumUtil.getPhantomJSDriver(driverBinPath);
			logger.debug("Getting site configs");
			
			siteConfigs = resolveJsonConfig(configDir);
			
			logger.debug("Getting site configs completed");
			
			if(siteConfigs != null) {
				for(File siteConfig : siteConfigs) {
					logger.debug("Processing " + siteConfig.getName());
					
					JSONParser parser = new JSONParser();
					Object obj = parser.parse(new FileReader(siteConfig));
					JSONObject jsonObj = (JSONObject) obj;
			        String siteDesc = (String)jsonObj.get("desc");
			        String siteUrl = (String)jsonObj.get("url");
			        String coordinateStr = (String)jsonObj.get("coordinate");
			        
			        logger.debug("URL found for " + siteDesc + ": " + siteUrl);
			        
			        File prevScreenCap = findPrevScreenShot(siteConfig.getName().replace(".json", ""), outputDir);
			        
			        File currScreenCap = null;
			        try {
			        	
			        	currScreenCap = takeScreenShot(driver, siteConfig.getName(), siteUrl, outputDir, coordinateStr);
			        	
			        }catch(Exception e) {
			        	e.printStackTrace();
			        }
			        
			        if(prevScreenCap != null) {
			        	logger.debug("Compare " + currScreenCap.getName() + " vs " + prevScreenCap.getName());
			        	
			        	//Comparison starts
			        	
			        	String resultImg = outputDir+File.separatorChar+"compare-"+siteConfig.getName().replace(".json", "")+".diff";
						boolean imgDiff = compareWithBaseImage(prevScreenCap, currScreenCap, resultImg); 
						logger.debug("Differences found for " + siteConfig.getName().replace(".json", "") + ": " + imgDiff);
						
						if(imgDiff) {
							
							File attachment = new File(resultImg + ".png");
							Email email = EmailBuilder.startingBlank()
								    .withSubject("[WebChecker] " + siteDesc + "("+siteConfig.getName().replace(".json", "")+") changed")
								    .withPlainText("Pls find the diff. in the attachment. Check the change: " + siteUrl)
								    .withAttachment(attachment.getName(), readFileToByteArray(attachment), "img/jpeg")
								    .buildEmail();

							MailerBuilder.buildMailer().sendMail(email);
						}
			        	
						File resultFileToBeRemoved = new File(resultImg + ".png");
						logger.debug("Remove " + resultFileToBeRemoved.getAbsolutePath());
						resultFileToBeRemoved.delete();
			        	//Comparison ends
			        	
			        	logger.debug("remove previous screen cap for " + siteConfig.getName());
			        	prevScreenCap.delete();
			        	
			        }
			        else {
			        	logger.debug("No need to compare for " + siteConfig.getName());
			        }
			        
			        
			        logger.debug("======================================");
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.close();
			
			logger.debug("Job completed");
		}
		
	}
	
	private static byte[] readFileToByteArray(File file){
        FileInputStream fis = null;
        // Creating a byte array using the length of the file
        // file.length returns long which is cast to int
        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();        
            
        }catch(IOException ioExp){
            ioExp.printStackTrace();
        }
        return bArray;
    }


	public static boolean compareWithBaseImage(File baseImage, File compareImage, String resultOfComparison)
			throws IOException {
		BufferedImage bImage = ImageIO.read(baseImage);
		BufferedImage cImage = ImageIO.read(compareImage);
		int height = bImage.getHeight();
		int width = bImage.getWidth();
		BufferedImage rImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		boolean imgDiff = false;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				try {
					int pixelC = cImage.getRGB(x, y);
					int pixelB = bImage.getRGB(x, y);
					if (pixelB == pixelC) {
						rImage.setRGB(x, y, bImage.getRGB(x, y));
					} else {
						imgDiff = true;
						int a = 0xff | bImage.getRGB(x, y) >> 24, r = 0xff & bImage.getRGB(x, y) >> 16,
								g = 0x00 & bImage.getRGB(x, y) >> 8, b = 0x00 & bImage.getRGB(x, y);

						int modifiedRGB = a << 24 | r << 16 | g << 8 | b;
						rImage.setRGB(x, y, modifiedRGB);
					}
				} catch (Exception e) {
					// handled hieght or width mismatch
					rImage.setRGB(x, y, 0x80ff0000);
				}
			}
		}
		
		if(imgDiff) {
			String filePath = baseImage.toPath().toString();
			String fileExtenstion = filePath.substring(filePath.lastIndexOf('.'), filePath.length());
			if (fileExtenstion.toUpperCase().contains("PNG")) {
				createPngImage(rImage, resultOfComparison + fileExtenstion);
			} else {
				createJpgImage(rImage, resultOfComparison + fileExtenstion);
			}
		}
		
		return imgDiff;
	}
	
	private static void createPngImage(BufferedImage image, String fileName) throws IOException {
		ImageIO.write(image, "png", new File(fileName));
	}

	private static void createJpgImage(BufferedImage image, String fileName) throws IOException {
		ImageIO.write(image, "jpg", new File(fileName));
	}
	
	private static File findPrevScreenShot(String siteId, String outputDir) throws Exception{
		File outputDirFileObj = new File(outputDir);
		for(File file : outputDirFileObj.listFiles()) {
			if(file.isFile() && file.getName().startsWith(siteId) && file.getName().endsWith(".png")) {
				return file;
			}
		}
		
		return null;
	}
	
	private static File takeScreenShot(WebDriver driver, String siteId, String siteUrl, String outputDir, String coordinateStr) throws Exception{
		logger.debug("visiting " + siteUrl);
		
		try {
			driver.get(siteUrl);
			
			Thread.sleep(10000);
			
			Date now = new Date();
			String filename = siteId.replace(".json", "") + "." + sdFormat.format(now)+".png";
	        
	        if(!new File(outputDir).exists()) {
	        	new File(outputDir).mkdirs();
	        }
	        
	        Path screenshotPath = Paths.get(outputDir, filename);
	        
	        logger.debug("Taking screenshot for " + siteId);
	        
	        File srcFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
	        
	        logger.debug("srcFile: " + srcFile.getAbsolutePath() + " - " + srcFile.exists() + " - " + srcFile.length() + " - " + srcFile.canRead());
	        
	        //File tmpFile = new File(outputDir+File.separator+srcFile.getName());
	        //FileUtils.copyFile(srcFile, tmpFile);
	        
	        //logger.debug("tmpFile: " + tmpFile.getAbsolutePath() + " - " + tmpFile.exists() + " - " + tmpFile.length() + " - " + tmpFile.canRead());
	        
	        BufferedImage fullImg = ImageIO.read(srcFile);
	        //BufferedImage fullImg = ImageIO.read(tmpFile);
	
	        logger.debug("fullImg width: " + fullImg.getWidth());
	        
	        logger.debug("Cropping with " + coordinateStr);
	        
		    BufferedImage newScreenshot= fullImg.getSubimage(Integer.parseInt(coordinateStr.split(",")[0]), Integer.parseInt(coordinateStr.split(",")[1]),
		    		Integer.parseInt(coordinateStr.split(",")[2]), Integer.parseInt(coordinateStr.split(",")[3]));
		    
		    ImageIO.write(newScreenshot, "png", srcFile);
	
	     
	        FileUtils.copyFile(srcFile, screenshotPath.toFile());
	        
	        return screenshotPath.toFile();
	        
		}catch (Exception e) {
			throw e;
		}
	}
	
	private static List<File> resolveJsonConfig(String configDir) throws Exception{
		List<File> configFiles = new ArrayList<File>();
		
		Files.walk(Paths.get(configDir))
		 .filter(p -> p.getFileName().toString().startsWith("site-"))
	     .filter(p -> p.getFileName().toString().endsWith(".json"))
	     .forEach(p -> {
	        try {
	        	configFiles.add(p.toFile());
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    });
		
		return configFiles;
	}
}
