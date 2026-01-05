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
                /*

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
                }*/
                ListaIngredienti lista = new ListaIngredienti();
                Ingredienti riso = new Ingredienti(
                        "Riso",
                        195,    // peso in grammi
                        702,    // calorie
                        13,     // proteine
                        1,      // grassi
                        155,    // carboidrati
                        0,      // fibre
                        0       // zucchero
                );


                Ingredienti uova = new Ingredienti(
                        "Uova",
                        86,
                        123,
                        11,
                        8,
                        1,
                        0,
                        0
                );


                Ingredienti pettoDiPollo = new Ingredienti(
                        "Petto di pollo",
                        100,    // peso in grammi
                        120,    // calorie
                        22,     // proteine
                        2,      // grassi
                        0,      // carboidrati
                        0,      // fibre
                        0       // zucchero
                );


                lista.addIngredient(riso);
                lista.addIngredient(uova);
                lista.addIngredient(pettoDiPollo);
                System.out.println("Calorie totali: " + lista.calorieTotali);
                System.out.println("Carboidrati totali: " + lista.carboidratiTotali);
                System.out.println("Proteine totali: " + lista.proteineTotali);
                System.out.println("Grassi totali: " + lista.grassiTotali);
                System.out.println("Ingredienti:");
                for (Ingredienti i : lista.ingredienti) {
                    System.out.println("- " + i.nome + ": " + i.calorie + " kcal");
                }
                String responseText = "Calorie totali: " + lista.calorieTotali + " kcal\n" +
                        "Proteine totali: " + lista.proteineTotali + " g\n" +
                        "Grassi totali: " + lista.grassiTotali + " g\n" +
                        "Carboidrati totali: " + lista.carboidratiTotali + " g";

                SendMessage message = new SendMessage(update.getMessage().getChatId().toString(), responseText);

                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                salvaDB(text,update);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void salvaDB(String text, Update update) {
        if (text.startsWith("/macro")) {
            String[] p = text.split(" ");

            PreferenzeUtente pref = new PreferenzeUtente(
                    update.getMessage().getChatId(),
                    Integer.parseInt(p[1]),
                    Integer.parseInt(p[2]),
                    Integer.parseInt(p[3]),
                    Integer.parseInt(p[4])
            );

            try {
                Database.inserisciPreferenze(pref);
            } catch (Exception e) {
                System.err.println("Impossibile inserire le preferenze");
            }
            System.out.println("Preferenze salvate ✅");
            try {
                PreferenzeUtente pref2 = Database.leggi(update.getMessage().getChatId());
                System.out.println(pref2.chatId);
                System.out.println(pref2.calorie);
                System.out.println(pref2.carboidrati);
                System.out.println(pref2.proteine);
                System.out.println(pref2.grassi);
            } catch (Exception e) {
                System.err.println("Impossibile leggere le preferenze");
            }
        }
    }
}