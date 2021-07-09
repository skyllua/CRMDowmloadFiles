package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.List;

import static java.nio.charset.StandardCharsets.*;

public class Runner {
    static final String link = "https://crm.ukc.loc/crm";
    static ChromeDriver driver;
    static List<String> files = new ArrayList<>();
    static List<String> requests = new ArrayList<>();
    static String downloadFolder = "C:/Users/Starzhynskiy/Downloads/";
    static File dataFile;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        if (!readProperties()) {
            while (true) {
                System.out.print("Input Downloads Folder path: ");
                String line = reader.readLine();
                if (new File(line).exists()) {
                    downloadFolder = line;
                    if (!(downloadFolder.charAt(downloadFolder.length() - 1) == '/' || downloadFolder.charAt(downloadFolder.length() - 1) == '\\')) {
                        downloadFolder += "/";
                    }
                    break;
                }
                System.out.println("Path doesn't exist!");
            }

            while (true) {
                System.out.print("Input path to REQUEST'S *.txt file: ");
                String line = reader.readLine();
                if (new File(line).exists()) {
                    dataFile = new File(line);
                    readDataFile();
                    break;
                }
                System.out.println("File doesn't exist!");
            }
        } else {
            System.out.println("Downloads Folder path: " + downloadFolder);
            System.out.println("REQUEST'S *.txt file: " + dataFile.getAbsolutePath());
            if (!(downloadFolder.charAt(downloadFolder.length() - 1) == '/' || downloadFolder.charAt(downloadFolder.length() - 1) == '\\')) {
                downloadFolder += "/";
            }
            readDataFile();
        }

        System.out.println("Count Requests: " + requests.size());
        sleep(1000);

        long start = System.currentTimeMillis();

        System.setProperty("webdriver.chrome.driver", "chromedriver/chromedriver.exe");
        driver = new ChromeDriver();

        setSettings();
        login();

        int count = 0;
        for (String request : requests) {
            inputReqNum(request);
            System.out.print(".Input ");
            downloadFiles();
            System.out.print(".Downloaded ");
            deleteOldFiles();
            System.out.print(".Deleted ");
            uploadFiles();
            System.out.print(".Uploaded ");
            deleteDownloadedFiles();
            System.out.print(".Deleted ");
            saveRequest();
            System.out.print(".Saved ");
            closeReq();
            System.out.println("\nFile replacement in " + request + " completed! " + (requests.size() - ++count) + " requests left!");
        }

        long takesTime = System.currentTimeMillis() - start;
        System.out.println("\n\n\n\n==========================================\n\n");
        System.out.println("Task completed! It takes " + (takesTime/60000) + " minutes!");


        driver.quit();
    }

    private static boolean readProperties() {
        FileInputStream fis;
        Properties property = new Properties();

        try {
            fis = new FileInputStream("config.properties");
            property.load(fis);

            downloadFolder = property.getProperty("downpath");
            String file = property.getProperty("datafile");
            dataFile = new File(file);
            if (new File(downloadFolder).exists() && dataFile.exists()) return true;
            else {
                System.out.println("ERROR: Path " + downloadFolder + " or " + dataFile.getAbsolutePath() + " isn't exist!");
            }
        } catch (IOException e) {
            System.err.println("ERROR: config.properties isn't exist!");
        }

        return false;
    }

    private static void readDataFile() throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(dataFile), "UTF8"));

        String line;

        while ((line = reader.readLine()) != null) {
            requests.add(line);
        }
    }

    private static void closeReq() {
        driver.executeScript("document.getElementsByClassName('icon-close hidden-tabs-close')[1].click()");
    }

    private static void deleteOldFiles() {
        for (WebElement file : driver.findElementsByXPath("//div[@class='input-file']").get(1).findElements(By.className("kb-file-wrapper"))) {
            driver.executeScript("document.getElementsByClassName('input-file')[1].getElementsByClassName('kb-file-wrapper')[0].getElementsByTagName('i')[0].click();");

            //press button YES
//            waitOnWebElement(By.className("modal-module__modalContent___24PZt"));
            while (true) {
                try {
                    sleep(500);
                    driver.findElementByClassName("modal-module__modalContent___24PZt").findElement(By.cssSelector(".btn.btn-primary")).click();
                    break;
                } catch (Exception e) {
                }
            }
            sleep(100);
        }
    }

    private static void inputReqNum(String req) {
        waitOnWebElement(By.className("searchPage-module__searchInput___1vL6Y"));

        driver.findElementByClassName("searchPage-module__searchInput___1vL6Y").sendKeys(Keys.CONTROL + "a");
        driver.findElementByClassName("searchPage-module__searchInput___1vL6Y").sendKeys(Keys.DELETE);
        sleep(100);
        driver.findElementByClassName("searchPage-module__searchInput___1vL6Y").sendKeys(req);
        sleep(300);
        driver.findElementByClassName("searchPage-module__searchInput___1vL6Y").sendKeys(Keys.RETURN);
        sleep(500);

        waitOnWebElement(By.xpath("//div[@title='" + req + "']"));
        sleep(1000);

        driver.findElementByXPath("//div[@title='" + req + "']").click();
    }

    private static void saveRequest() {
        driver.findElementByClassName("btn-withDropdown").findElement(By.className("_btn")).click();
    }

    private static void deleteDownloadedFiles() {
        for (String file : files) {
            File realFile = new File(downloadFolder + file);
            while (realFile.exists()) {
                realFile.delete();
                sleep(100);
            }
        }
    }

    private static void uploadFiles() {
        for (String file : files) {
            driver.findElementsByXPath("//input[@type='file']").get(1).sendKeys(downloadFolder + file);
        }

        while (driver.findElementsByXPath("//div[@class='input-file']").get(1).findElements(By.className("kb-file-wrapper")).size() != (files.size()));
    }

    private static void downloadFiles() {
        while (true) {
            try {
                driver.findElementsByXPath("//div[@class='input-file']").get(1).findElements(By.className("kb-file-wrapper"));
                break;
            } catch (Exception e) {
            }
        }
        sleep(100);

        files.clear();
        for (WebElement file : driver.findElementsByXPath("//div[@class='input-file']").get(1).findElements(By.className("kb-file-wrapper"))) {
            files.add(file.findElement(By.tagName("a")).getText().replaceAll("~", "_"));
            while (true) {
                try {
                    file.findElement(By.tagName("a")).click();
                    break;
                } catch (Exception e) {
                }
            }
        }

        while (true) {
            int countExistFiles = 0;
            for (String file : files) {
                if (new File(downloadFolder + file).exists()) countExistFiles++;
            }
            if (countExistFiles == files.size()) break;
        }
        sleep(100);
    }

    private static void login() {
        waitOnWebElement(By.xpath("//input[@class='login-page__form-input']"));

        for (WebElement element : driver.findElementsByXPath("//input[@class='login-page__form-input']")) {
            switch (element.getAttribute("placeholder")) {
                case "Домен...": element.sendKeys("UKC");
                    break;
                case "Ім’я користувача...": element.sendKeys("admin");
                    break;
                case "Пароль...": element.sendKeys("<thtptym14");
                    break;
            }
        }

        waitOnWebElement(By.xpath("//input[@class='login-page__form-submit']"));

        driver.findElementByXPath("//input[@class='login-page__form-submit']").click();
        driver.findElementByXPath("//button[@class='icon-menu-cross add-tab-button']").click();
        driver.findElementByXPath("//span[@class='new-tab-item-text']").click();
    }

    private static void waitOnWebElement(By by) {
        while (true) {
            try {
                driver.findElement(by);
                sleep(200);
                break;
            } catch (Exception e) {
            }
        }
    }

    private static void setSettings() {
        driver.manage().window().maximize();
        driver.get(link);
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
