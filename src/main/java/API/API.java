package API;

import Config.MyConfiguration;
import Model.Ingredienti;
import Model.ListaIngredienti;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class API {
    public static Ingredienti getMacro(String alimento){
        try {

            String urlStr = String.format(
                    "https://api.edamam.com/api/food-database/v2/parser" +
                            "?app_id=%s&app_key=%s&ingr=%s",
                    MyConfiguration.getInstance().getProperty("APP_ID"),
                    MyConfiguration.getInstance().getProperty("APP_KEY"),
                    URLEncoder.encode(alimento, "UTF-8")
            );

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            reader.close();
            conn.disconnect();

            // 🔹 PARSING CON GSON
            JsonObject root = JsonParser.parseString(json.toString()).getAsJsonObject();
            JsonArray parsed = root.getAsJsonArray("parsed");
            JsonObject food;

            if (parsed != null && parsed.size() > 0) {
                food = parsed.get(0).getAsJsonObject().getAsJsonObject("food");
            } else {
                // usa hints se parsed è vuoto
                JsonArray hints = root.getAsJsonArray("hints");
                if (hints == null || hints.size() == 0) return null;
                food = hints.get(0).getAsJsonObject().getAsJsonObject("food");
            }

            String name = food.get("label").getAsString();
            JsonObject n = food.getAsJsonObject("nutrients");

            int calorie = n.has("ENERC_KCAL") ? n.get("ENERC_KCAL").getAsInt() : 0;
            int proteine = n.has("PROCNT") ? n.get("PROCNT").getAsInt() : 0;
            int grassi = n.has("FAT") ? n.get("FAT").getAsInt() : 0;
            int carboidrati = n.has("CHOCDF") ? n.get("CHOCDF").getAsInt() : 0;

            return new Ingredienti(
                    calorie,
                    proteine,
                    grassi,
                    carboidrati,
                    name
            );
        }catch (Exception e){
            System.out.println("Errore in getMacro " + e.getMessage());
            return null;
        }
    }

}
