package Database;


import Enumerators.MacroStatus;
import Model.Data;
import Model.InformazioniUtente;
import Model.Ingredienti;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;

public class Database {
    private static Database database;
    private Connection connection;
    private Database() throws Exception {
        String url = "jdbc:sqlite:database.db";
        connection = DriverManager.getConnection(url);
    }


    public static Database getInstance() throws Exception {
        if(database==null){
            database = new Database();
                String sql = """
                    CREATE TABLE IF NOT EXISTS preferenzeUtente (
                        chat_id INTEGER PRIMARYKEY,
                        calorie INT,
                        proteine INT,
                        carboidrati INT,
                        grassi INT
                    );
                """;
                Connection conn = database.getConn();
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                stmt.close();
            }
        return database;
    }


    public Connection getConn() throws Exception{
        return connection;
    }


    public static void insertInformazioniUtente(@NotNull InformazioniUtente p){
        String sql = """
            INSERT OR REPLACE INTO preferenzeUtente
            (chat_id, calorie, proteine, carboidrati, grassi, macro_status)
            VALUES (?, ?, ?, ?, ?,?)
        """;
        try {
            PreparedStatement ps = Database.getInstance()
                    .getConn()
                    .prepareStatement(sql);

            ps.setLong(1, p.chatId);
            ps.setInt(2, p.calorie);
            ps.setInt(3, p.proteine);
            ps.setInt(4, p.carboidrati);
            ps.setInt(5, p.grassi);
            ps.setString(6, MacroStatus.NONE.name());
            ps.executeUpdate();
            ps.close();
        }catch (Exception e){
            System.err.println("Errore in insertInformazioniUtente");
        }
    }
    public static void insertFoodDB(Ingredienti ing,String name){
        String sql = "INSERT INTO foodDB (calorie, proteine, carboidrati, grassi, name) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = Database.getInstance().getConn().prepareStatement(sql);
            ps.setInt(1, ing.calorie);
            ps.setInt(2, ing.proteine);
            ps.setInt(3, ing.carboidrati);
            ps.setInt(4, ing.grassi);
            ps.setString(5, name);
            ps.executeUpdate();
            ps.close();
        }catch (Exception e){
            System.err.println("Errore in insertFoodDB " + e.getMessage());
        }
    }
    public static Ingredienti readRow(String table,Long chatID){
        String sql = String.format("SELECT * FROM %s where chat_id = ?",table);
        try{
            PreparedStatement ps = Database.getInstance().getConn().prepareStatement(sql);
            ps.setLong(1, chatID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return new Ingredienti(
                        rs.getInt("calorie"),
                        rs.getInt("proteine"),
                        rs.getInt("grassi"),
                        rs.getInt("carboidrati"),
                        ""
                );
            }
        }catch(Exception e){
            System.err.println("Errore in readRow" + e.getMessage());
            return null;
        }
        return null;
    }


    public static Ingredienti readRow(String table,String cibo){
        String sql = String.format("SELECT * FROM %s where name = ?",table);
        try{
            PreparedStatement ps = Database.getInstance().getConn().prepareStatement(sql);
            ps.setString(1, cibo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return new Ingredienti(
                        rs.getInt("calorie"),
                        rs.getInt("proteine"),
                        rs.getInt("grassi"),
                        rs.getInt("carboidrati"),
                        cibo
                );
            }
        }catch(Exception e){
            System.err.println("Errore in readRow" + e.getMessage());
            return null;
        }
        return null;
    }
    public static Ingredienti readRowFoodDB(String name){
        String sql = String.format("SELECT * FROM foodDB where name = ?");
        try{
            PreparedStatement ps = Database.getInstance().getConn().prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return new Ingredienti(
                        rs.getInt("calorie"),
                        rs.getInt("proteine"),
                        rs.getInt("grassi"),
                        rs.getInt("carboidrati"),
                        name
                );
            }
        }catch(Exception e){
            System.err.println("Errore in readRow" + e.getMessage());
            return null;
        }
        return null;
    }
    public static boolean readRowData(String Data,Long chatID){
        String sql = "SELECT data FROM dailyConsume  where chat_id = ?";
        try{
            PreparedStatement ps = Database.getInstance().getConn().prepareStatement(sql);
            ps.setLong(1, chatID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                if(rs.getString("data").equals(Data)){
                    return true;
                }else{
                    return false;
                }
            }
        }catch(Exception e){
            System.err.println("Errore in readRow" + e.getMessage());
            return false;
        }
        return false;
    }
    public static void updateRowDailyConsume(Long chatId,String cibi, int peso){
        if(!(Database.readRowData(Data.oggi(),chatId))){
            resetDailyConsume(chatId);
        }
        sameDay(chatId,cibi,peso);
    }
    public static void resetDailyConsume(Long chatId){
        Database.updateDailyConsume(0,0,0,0,chatId);
    }
    private static void updateDailyConsume(int calorie,int proteine,int grassi,int carboidrati,Long chatId){
        String sql = "UPDATE dailyConsume SET calorie = ?, proteine = ?, carboidrati = ?, grassi = ?, data = ?  WHERE chat_id = ?;";
        try {
            Connection conn = Database.getInstance().getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, calorie);
            ps.setInt(2, proteine);
            ps.setInt(3, carboidrati);
            ps.setInt(4, grassi);
            ps.setString(5,Data.oggi());
            ps.setLong(6, chatId);
            ps.executeUpdate();
        }catch (Exception e){
            System.err.println("Errore in UpdateRowDailyConsume " + e.getMessage());
        }
    }
    private static void sameDay(Long chatId,String cibo,int peso){
        Ingredienti ingredienti = readRowFoodDB(cibo);
        int calorie = ingredienti.calorie*(peso/100);
        int proteine = ingredienti.proteine*(peso/100);
        int carboidrati = ingredienti.carboidrati*(peso/100);
        int grassi = ingredienti.grassi*(peso/100);
        Ingredienti macroDailyConsume = Database.readRow("DailyConsume",chatId);
        updateDailyConsume(
                calorie+=macroDailyConsume.calorie,
                proteine+=macroDailyConsume.proteine,
                grassi+=macroDailyConsume.grassi,
                carboidrati+=macroDailyConsume.carboidrati,
                chatId);
    }
    public static void deleteFood(Long chatId,String cibo,int peso){
        Ingredienti ingredienti = readRowFoodDB(cibo);
        int calorie = ingredienti.calorie*(peso/100);
        int proteine = ingredienti.proteine*(peso/100);
        int carboidrati = ingredienti.carboidrati*(peso/100);
        int grassi = ingredienti.grassi*(peso/100);
        Ingredienti macroDailyConsume = Database.readRow("DailyConsume",chatId);
        updateDailyConsume(
                macroDailyConsume.calorie-=calorie,
                macroDailyConsume.carboidrati-=carboidrati,
                macroDailyConsume.proteine-=proteine,
                macroDailyConsume.grassi-=grassi,
                chatId);
    }

    public static void updateRow(Long chatId,String row,String table,int macro){
        String sql = String.format("UPDATE %s SET %s = ? WHERE chat_id = ?;", table, row);
        try {
            Connection conn = Database.getInstance().getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, macro);
            ps.setLong(2, chatId);
            ps.executeUpdate();
        }catch (Exception e){
            System.err.println("Errore in UpdateRow");
        }
    }

    public static void updateRow(Long chatId, String row, String table, String status){
        String sql = String.format("UPDATE %s SET %s = ? WHERE chat_id = ? ",table,row);
        try { Connection conn = Database.getInstance().getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setLong(2, chatId);
            ps.executeUpdate();
        }catch(Exception e) {
            System.err.println("Errore in updateRow");
        }
    }

    @Nullable
    public static String checkStatus(long chatId, String row, String table){
        String sql = String.format("SELECT %s FROM %s WHERE chat_id = ?", row, table);

        try {
            Connection conn = Database.getInstance().getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, chatId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(row);
            }
        }catch (Exception e){
            System.err.println("Errore in checkStatus");
        }
        return null;
    }

    public static void createRow(long chatId, String row, String table, String status){
        String sql = String.format("INSERT INTO %s (%s, chat_id) VALUES (?, ?)",table,row);
        try {
            Connection conn = Database.getInstance().getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            if(status!=null){
                ps.setString(1, status);
            }
            ps.setLong(2, chatId);

            int rows = ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Errore in createRow " + e.getMessage());
        }
    }


    public static void createRowDailyConsume(long chatId, String table, String data){
        String sql = String.format("INSERT INTO %s (chat_id,data) VALUES (?,?)",table);
        try {
            Connection conn = Database.getInstance().getConn();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, chatId);
            ps.setString(2, data);
            ps.execute();
        } catch (Exception e) {
            System.err.println("Errore in createRowDailiConsume" + e.getMessage());
        }
    }
}