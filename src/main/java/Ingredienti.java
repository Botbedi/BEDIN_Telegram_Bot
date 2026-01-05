public class Ingredienti {
    public String nome;
    public int peso; // in grammi
    public int calorie;
    public int proteine;
    public int grassi;
    public int carboidrati;
    public int fibre;
    public int zucchero;

    // Costruttore
    public Ingredienti(String nome, int peso, int calorie, int proteine,
                       int grassi, int carboidrati, int fibre, int zucchero) {
        this.nome = nome;
        this.peso = peso;
        this.calorie = calorie;
        this.proteine = proteine;
        this.grassi = grassi;
        this.carboidrati = carboidrati;
        this.fibre = fibre;
        this.zucchero = zucchero;
    }
}

