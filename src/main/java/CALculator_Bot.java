import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.io.*;
import java.net.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class CALculator_Bot implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient telegramClient = new OkHttpTelegramClient(MyConfiguration.getInstance().getProperty("BOT_TOKEN"));

    public CALculator_Bot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }
    @Override
    public void consume(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String s = update.getMessage().getText();
                String[] stringSplit = s.split(",");
                JsonArray ingr = new JsonArray();
                for(String s1 : stringSplit) {
                    ingr.add(s1);
                }
                System.out.println(ingr);
                String apiUrl = "https://api.edamam.com/api/nutrition-data?app_id=" + MyConfiguration.getInstance().getProperty("APP_ID")
                        + "&app_key=" + MyConfiguration.getInstance().getProperty("APP_KEY");
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                int responseCode = conn.getResponseCode();
                BufferedReader in;
                if (responseCode == 200) {
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                } else {
                    in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                }
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line.trim());
                }
                in.close();
                System.out.println(response.toString());
            /*SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(update.getMessage().getChatId())
                    .text("down")
                    .build();
            try {
                telegramClient.execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
             */
            }
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}