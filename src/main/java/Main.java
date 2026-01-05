import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.io.File;

public class Main {
    public static void main(String[] args){
            try {
                Database db = Database.getInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        String botToken = MyConfiguration.getInstance().getProperty("BOT_TOKEN");
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new CALculator_Bot(botToken));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
