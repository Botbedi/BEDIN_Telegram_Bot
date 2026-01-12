import Bot.CALculator_Bot;
import Config.MyConfiguration;
import Database.Database;
import Model.Data;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

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
            System.err.println("Errore in main");
            e.printStackTrace();
        }
    }
}
