import com.google.gson.JsonElement;
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
import com.google.gson.JsonParser;


public class CALculator_Bot implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient telegramClient = new OkHttpTelegramClient(MyConfiguration.getInstance().getProperty("BOT_TOKEN"));

    public CALculator_Bot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }
    @Override
    public void consume(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {

                String text = update.getMessage().getText();


                // Split ingredienti
                String[] stringSplit = text.split(",");

                JsonArray ingr = new JsonArray();
                for (String s : stringSplit) {
                    ingr.add(s.trim());
                }

                JsonObject body = new JsonObject();
                body.add("ingr", ingr);


                String apiUrl = "https://api.edamam.com/api/nutrition-details"
                        + "?app_id=" + MyConfiguration.getInstance().getProperty("APP_ID")
                        + "&app_key=" + MyConfiguration.getInstance().getProperty("APP_KEY");
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);


                // Scrittura body JSON
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = body.toString().getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                BufferedReader in;

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                } else {
                    in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

                ListaIngredienti recipe = new ListaIngredienti();

                JsonArray ingredientsJson = jsonResponse.getAsJsonArray("ingredients");

                for (JsonElement elem : ingredientsJson) {
                    JsonObject ingObj = elem.getAsJsonObject();
                    JsonArray parsed = ingObj.getAsJsonArray("parsed");
                    if (parsed.size() > 0) {
                        JsonObject nut = parsed.get(0).getAsJsonObject().getAsJsonObject("nutrients");

                        Ingredienti ing = new Ingredienti(
                                ingObj.get("text").getAsString(),
                                parsed.get(0).getAsJsonObject().get("weight").getAsInt(),
                                nut.getAsJsonObject("ENERC_KCAL").get("quantity").getAsInt(),
                                nut.getAsJsonObject("PROCNT").get("quantity").getAsInt(),
                                nut.getAsJsonObject("FAT").get("quantity").getAsInt(),
                                nut.getAsJsonObject("CHOCDF").get("quantity").getAsInt(),
                                nut.getAsJsonObject("FIBTG").get("quantity").getAsInt(),
                                nut.getAsJsonObject("SUGAR").get("quantity").getAsInt()
                        );

                        recipe.addIngredient(ing);
                    }
                }
                System.out.println("Calorie totali: " + recipe.totalCalories);
                System.out.println("Proteine totali: " + recipe.totalProtein);
                System.out.println("Grassi totali: " + recipe.totalFat);
                System.out.println("Ingredienti:");
                for (Ingredienti i : recipe.ingredients) {
                    System.out.println("- " + i.name + ": " + i.calories + " kcal");
                }
                String responseText = "Calorie totali: " + (int)recipe.totalCalories + " kcal\n" +
                        "Proteine totali: " + (int)recipe.totalProtein + " g\n" +
                        "Grassi totali: " + (int)recipe.totalFat + " g\n" +
                        "Carboidrati totali: " + (int)recipe.totalCarbs + " g";

                SendMessage message = new SendMessage(update.getMessage().getChatId().toString(), responseText);

                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}