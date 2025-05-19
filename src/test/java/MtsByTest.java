import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class MtsByTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private WebDriverWait longWait;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.manage().window().setSize(new Dimension(1920, 1080));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testOnlineTopUpBlock() {
        driver.get("https://www.mts.by/404");
        driver.manage().addCookie(new Cookie("BITRIX_SM_COOKIES_AGREEMENT", "yes", ".mts.by", "/", null));
        driver.get("https://www.mts.by/");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.pay")));
        WebElement blockTitle = driver.findElement(By.cssSelector("section.pay h2"));
        String actualTitle = blockTitle.getText().replaceAll("\\s+", " ").trim();
        Assertions.assertEquals(
            "Онлайн пополнение без комиссии",
            actualTitle,
            "Заголовок блока не совпадает с ожидаемым"
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", blockTitle);
        List<WebElement> paymentLogos = driver.findElements(By.xpath(
                "//section[contains(@class,'pay')]//img[contains(@src,'visa') or contains(@src,'mastercard') or contains(@src,'belkart') or contains(@src,'webpay') or contains(@src,'bepaid')]"));
        Assertions.assertFalse(paymentLogos.isEmpty(), "Логотипы платёжных систем не найдены. Проверь XPATH и наличие картинок на странице.");
        WebElement moreLink = driver.findElement(By.xpath(
            "//section[contains(@class,'pay')]//a[contains(text(),'Подробнее о сервисе')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", moreLink);
        moreLink.click();
        wait.until(d -> d.getCurrentUrl().equals("https://www.mts.by/help/poryadok-oplaty-i-bezopasnost-internet-platezhey/"));
        Assertions.assertEquals(
            "https://www.mts.by/help/poryadok-oplaty-i-bezopasnost-internet-platezhey/",
            driver.getCurrentUrl(),
            "Открыт неверный url после перехода на 'Подробнее о сервисе'"
        );
    }

    @Test
    public void testContinueButton() {
        driver.get("https://www.mts.by/404");
        driver.manage().addCookie(new Cookie("BITRIX_SM_COOKIES_AGREEMENT", "yes", ".mts.by", "/", null));
        driver.get("https://www.mts.by/");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
            webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.pay")));
        WebElement phoneInput = driver.findElement(By.xpath(
                "//form[@id='pay-connection']//input[@id='connection-phone']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", phoneInput);
        phoneInput.clear();
        phoneInput.sendKeys("297777777");
        WebElement sumInput = driver.findElement(By.xpath(
                "//form[@id='pay-connection']//input[@id='connection-sum']"));
        sumInput.clear();
        sumInput.sendKeys("100");
        WebElement emailInput = driver.findElement(By.xpath(
                "//form[@id='pay-connection']//input[@id='connection-email']"));
        emailInput.clear();
        emailInput.sendKeys("test@test.com");
        WebElement continueBtn = driver.findElement(By.xpath(
                "//form[@id='pay-connection']//button[contains(text(),'Продолжить')]"
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", continueBtn);
        continueBtn.click();
        WebElement paymentIframe = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("iframe.bepaid-iframe")));
        driver.switchTo().frame(paymentIframe);
        WebElement payDescription = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".pay-description__text")));
        boolean textAppeared = wait.until(driver ->
            payDescription.getText() != null && payDescription.getText().contains("Оплата: Услуги связи Номер:375297777777")
        );
        Assertions.assertTrue(
            textAppeared,
            "Модальное окно оплаты не появилось или текст не совпадает"
        );
        driver.switchTo().defaultContent();
        driver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("section.pay")));
    }
} 
