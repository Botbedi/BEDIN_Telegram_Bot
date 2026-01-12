package Bot;
import API.API;
import Config.MyConfiguration;
import Database.Database;
import Enumerators.MacroStatus;
import Enumerators.UserStatus;
import Model.Data;
import Model.InformazioniUtente;
import Model.Ingredienti;
import Model.ListaIngredienti;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
                if(text.startsWith("/")) {
                    handleCommand(update,text);
                }else {
                    handleInput(update);
                }
            }
        }catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void handleInput(Update update) {
        try{
            switch (UserStatus.valueOf(Database.checkStatus(update.getMessage().getChatId(),"status","status"))) {
                case WAITING_MACRO -> handleSingleMacro(update);
                case WAITING_FOOD -> handleWaitingFood(update);
                case WAITING_DELETE -> handleWaitingDelete(update);
                case NONE -> handleMessage(update);
            }
        }catch (Exception e){
            System.err.println("Errore in handleInput " +  e.getMessage());
        }
    }

    private void handleWaitingDelete(Update update) {
        String[][] input = formatazzioneInput(update);
        for (int i=0;i<input[0].length;i++) {
            System.out.println(input[0][i]);
            Ingredienti ing = Database.readRowFoodDB(input[0][i]);
            if(ing!=null){
                Database.deleteFood(update.getMessage().getChatId(),input[0][i],conversioneInt(input[1][i]));
            }
        }
        Database.updateRow(update.getMessage().getChatId(),"status","status",UserStatus.NONE.name());
        risposta(update,buildRisposta(update));
    }
    private void handleWaitingFood(Update update) {
        if (Database.readRowFoodDB(update.getMessage().getText())==null){
            Database.insertFoodDB(API.getMacro(update.getMessage().getText()),update.getMessage().getText());
        }
        risposta(update,buildRispostaFood(update));
        Database.updateRow(update.getMessage().getChatId(),"status","status", UserStatus.NONE.name());
    }

    private void handleMessage(Update update) {
        String[][] string = formatazzioneInput(update);
        for(int i=0;i<string[0].length;i++){
            if (Database.readRow("foodDB", string[0][i]) == null) {
                handleMessageAPI(string[0][i]);
            }
            handleMessageDB(string[0][i],update,string[1][i]);
        }
        risposta(update,buildRisposta(update));
    }
    private String[][] formatazzioneInput(Update update) {
        String input = update.getMessage().getText();
        List<String> cibi = new ArrayList<>();
        List<String> quantita = new ArrayList<>();
        Pattern p = Pattern.compile("(\\d+\\s*(?:g)?)\\s*(?:di\\s+)?(.+?)\\s*(?:,|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(input);
        while (m.find()) {
            quantita.add(m.group(1).trim());
            cibi.add(m.group(2).trim());
        }
        String[] cibiArr = cibi.toArray(new String[0]);
        String[] quantitaArr = quantita.toArray(new String[0]);
        String[][] result = new String[2][];
        result[0] = cibiArr;
        result[1] = quantitaArr;
        return result;
    }
    private void handleMessageDB(String cibo,Update update, String quantita) {
        Database.updateRowDailyConsume(update.getMessage().getChatId(),cibo,conversioneInt(quantita));
    }
    private String buildRisposta(Update update) {
        Ingredienti ing = Database.readRow("dailyConsume",update.getMessage().getChatId());
        Ingredienti ingPreferenzeUtente = Database.readRow("preferenzeUtente",update.getMessage().getChatId());
        try {
            if (MacroStatus.valueOf(Database.checkStatus(update.getMessage().getChatId(), "macro_status", "preferenzeUtente"))==MacroStatus.SAVED) {
                return "Valori nutrizionali di oggi:\n" +
                        "Calorie: " + ing.calorie + "/" + ingPreferenzeUtente.calorie + "kcal\n" +
                        "Carboidrati: " + ing.carboidrati + "/" + ingPreferenzeUtente.carboidrati+ "g\n" +
                        "Grassi: " + ing.grassi + "/" + ingPreferenzeUtente.grassi + "g\n"+
                        "Proteine: " + ing.proteine + "/" + ingPreferenzeUtente.proteine + "g";
            }else{
                return "Valori nutrizionali:\n" +
                        "Calorie: " + ing.calorie + "kcal\n" +
                        "Carboidrati: " + ing.carboidrati + "g\n" +
                        "Grassi: " + ing.grassi + "g\n"+
                        "Proteine: " + ing.proteine + "g";
            }
        }catch (Exception e){
            System.err.println("Errore in buildRisposta " +  e.getMessage());
            return "";
        }
    }

    private void handleMessageAPI(String cibo){
        Ingredienti ing = API.getMacro(cibo);
        Database.insertFoodDB(ing,cibo);
    }

    private void handleCommand(Update update, String text) {
        switch(text.split(" ")[0]) {
            case "/start" -> start(update);
            case "/help" -> help(update);
            case "/macro" -> macro(update);
            case "/deletemacro" -> deleteMacro(update);
            case "/food" -> searchFood(update);
            case "/delete" -> delete(update);
            case "/reset" -> reset(update);
            default -> risposta(update,"Comando non riconosciuto");
        }
    }
    private void help(Update update) {
        String help ="/help - Mosta questo messaggio \n" +
                "/macro - Imposta gli obbiettivi giornalieri che vuoi raggiungere \n" +
                "/deletemacro - Elimina i tuoi obbiettivi nutrizionali\n" +
                "/food - Mostra i valori di un cibo per 100g\n" +
                "/delete - Elimina i macro dei cibi inseriti nei macro giornalieri \n" +
                "/reset - Rimuove tutti i macro del giorno\n\n  " +
                "Se si vuole iniziare a calcolare i macro di un giorno scrivere: \n" +
                "\"*peso* *cibo*, *peso* *cibo*\" (il peso inserito in grammi)";
        risposta(update,help);
    }

    private void reset(Update update) {
        Database.resetDailyConsume(update.getMessage().getChatId());
        risposta(update,"Rimuossi tutti i macro");
    }
    private void delete(Update update){
        Database.updateRow(update.getMessage().getChatId(),"status","status", UserStatus.WAITING_DELETE.name());
        risposta(update,"Inserire i cibi da eliminare");
    }

    private void searchFood(Update update) {
        Database.updateRow(update.getMessage().getChatId(),"status","status", UserStatus.WAITING_FOOD.name());
        risposta(update,"Di che cibo vorresti sapere i macro");
    }
    private String buildRispostaFood(Update update) {
        Ingredienti ing = Database.readRowFoodDB(update.getMessage().getText());
        if(ing!=null){
            return "Valori nutrizionali per 100g:\n" +
                    "Calorie: " + ing.calorie + "kcal\n" +
                    "Carboidrati: " + ing.carboidrati + "g\n" +
                    "Grassi: " + ing.grassi + "g\n"+
                    "Proteine: " + ing.proteine + "g";
        }
        return "Errore";
    }

    private void deleteMacro(Update update) {
        Database.insertInformazioniUtente(new InformazioniUtente(update.getMessage().getChatId(), 0,0,0,0));
        Database.updateRow(update.getMessage().getChatId(),"macro_status", "preferenzeUtente",MacroStatus.NONE.name());
        Database.updateRow(update.getMessage().getChatId(),"status", "status",MacroStatus.NONE.name());
    }

    private void risposta(Update update, String responseText) {
        try {
            telegramClient.execute(new SendMessage(update.getMessage().getChatId().toString(), responseText));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void start(Update update){
        try {
            help(update);
            Database.createRowDailyConsume(update.getMessage().getChatId(),"dailyConsume", Data.oggi());
            Database.createRow(update.getMessage().getChatId(),"status","status",UserStatus.NONE.name());
            Database.createRow(update.getMessage().getChatId(),"macro_status","preferenzeUtente",MacroStatus.NONE.name());
        } catch (Exception e) {
            System.out.println("Errore nell'aggiornare lo status ❌");
        }
    }

    private void macro (Update update){
        Database.updateRow(update.getMessage().getChatId(), "status", "status", UserStatus.WAITING_MACRO.name());
        Database.updateRow(update.getMessage().getChatId(), "macro_status", "preferenzeUtente", MacroStatus.calorie.name());
        risposta(update, "Inserire le " + MacroStatus.calorie.name());
    }
    private void handleSingleMacro (Update update){
    try {
        switch (MacroStatus.valueOf(Database.checkStatus(update.getMessage().getChatId(), "macro_status", "preferenzeUtente"))) {
            case calorie -> updateCaloriePreferenzeUtente(update);
            case carboidrati -> updateCarboidratiPreferenzeUtente(update);
            case grassi -> updateGrassiPreferenzeUtente(update);
            case proteine -> updateProteinePreferenzeUtente(update);
        }
    } catch (Exception e) {
        System.err.println("Errore nell'inserire i macro");
        risposta(update, "Errore nel inserire i macro");
    }
}
    private void updateCaloriePreferenzeUtente (Update update){
    Database.updateRow(update.getMessage().getChatId(), MacroStatus.calorie.name(), "preferenzeUtente", Integer.parseInt(update.getMessage().getText()));
    risposta(update, "Inserire i " + MacroStatus.carboidrati.name());
    Database.updateRow(update.getMessage().getChatId(), "macro_status", "preferenzeUtente", MacroStatus.carboidrati.name());
}
    private void updateCarboidratiPreferenzeUtente (Update update){
    Database.updateRow(update.getMessage().getChatId(), MacroStatus.carboidrati.name(), "preferenzeUtente", Integer.parseInt(update.getMessage().getText()));
    risposta(update, "Inserire i " + MacroStatus.grassi.name());
    Database.updateRow(update.getMessage().getChatId(), "macro_status", "preferenzeUtente", MacroStatus.grassi.name());
}
    private void updateGrassiPreferenzeUtente (Update update){
    Database.updateRow(update.getMessage().getChatId(), MacroStatus.grassi.name(), "preferenzeUtente", Integer.parseInt(update.getMessage().getText()));
    risposta(update, "Inserire i " + MacroStatus.proteine.name());
    Database.updateRow(update.getMessage().getChatId(), "macro_status", "preferenzeUtente", MacroStatus.proteine.name());
}
    private void updateProteinePreferenzeUtente (Update update){
    Database.updateRow(update.getMessage().getChatId(), MacroStatus.proteine.name(), "preferenzeUtente", Integer.parseInt(update.getMessage().getText()));
    Database.updateRow(update.getMessage().getChatId(), "macro_status", "preferenzeUtente", MacroStatus.SAVED.name());
    Database.updateRow(update.getMessage().getChatId(),"status","status", UserStatus.NONE.name());
}
    private int conversioneInt(String string){
        try{
            return Integer.parseInt(string.replaceAll("g$", ""));
        }catch (Exception e){
            System.err.println("Errore in conversione int");
            return -1;
        }
    }
}