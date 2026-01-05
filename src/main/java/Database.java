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
                        chat_id INTEGER PRIMARY KEY,
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
    public static void inserisciPreferenze(PreferenzeUtente p) throws Exception{
        String sql = """
            INSERT OR REPLACE INTO preferenzeUtente
            (chat_id, calorie, proteine, carboidrati, grassi)
            VALUES (?, ?, ?, ?, ?)
        """;
        PreparedStatement ps = Database.getInstance()
                .getConn()
                .prepareStatement(sql);

        ps.setLong(1, p.chatId);
        ps.setInt(2, p.calorie);
        ps.setInt(3, p.proteine);
        ps.setInt(4, p.carboidrati);
        ps.setInt(5, p.grassi);
        ps.executeUpdate();
        ps.close();
    }
    public static PreferenzeUtente leggi(long chatId) throws Exception {
        String sql = "SELECT * FROM preferenzeUtente WHERE chat_id = ?";

        PreparedStatement ps = Database.getInstance()
                .getConn()
                .prepareStatement(sql);
        ps.setLong(1, chatId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new PreferenzeUtente(
                    chatId,
                    rs.getInt("calorie"),
                    rs.getInt("proteine"),
                    rs.getInt("carboidrati"),
                    rs.getInt("grassi")
            );
        }

        return null;
    }
}

