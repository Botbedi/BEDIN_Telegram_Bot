package Model;

import org.jetbrains.annotations.Nullable;

public class Ingredienti {
    public String name;
    public int calorie;
    public int proteine;
    public int grassi;
    public int carboidrati;

    public Ingredienti(int calorie, int proteine,
                       int grassi, int carboidrati, String name ) {
        this.name = name;
        this.calorie = calorie;
        this.proteine = proteine;
        this.grassi = grassi;
        this.carboidrati = carboidrati;
    }
    @Override
    public String toString() {
        return "Valori nutrizionali " + name + ":" +
                "\nCalorie: " + calorie +
                "\nCarboidrati: " + carboidrati +
                "\nProteine: " + proteine +
                "\nGrassi: " + grassi ;
    }
}