import java.util.ArrayList;
import java.util.List;

public class ListaIngredienti {
    public List<Ingredienti> ingredienti;
    public int calorieTotali;
    public int proteineTotali;
    public int grassiTotali;
    public int carboidratiTotali;
    public int fibreTotali;
    public int zuccheroTotale;

    public ListaIngredienti() {
        this.ingredienti = new ArrayList<>();
    }

    public void addIngredient(Ingredienti ing) {
        ingredienti.add(ing);
        calorieTotali += ing.calorie;
        proteineTotali += ing.proteine;
        grassiTotali += ing.grassi;
        carboidratiTotali += ing.carboidrati;
        fibreTotali += ing.fibre;
        zuccheroTotale += ing.zucchero;
    }
}

