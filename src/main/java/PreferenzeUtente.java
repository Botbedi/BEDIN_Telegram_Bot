public class PreferenzeUtente {
    public long chatId;
    public int calorie;
    public int proteine;
    public int carboidrati;
    public int grassi;

    public PreferenzeUtente(long chatId, int calorie, int proteine, int carboidrati, int grassi) {
        this.chatId = chatId;
        this.calorie = calorie;
        this.proteine = proteine;
        this.carboidrati = carboidrati;
        this.grassi = grassi;
    }
}
