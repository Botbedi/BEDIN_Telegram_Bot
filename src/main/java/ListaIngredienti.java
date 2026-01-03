import java.util.ArrayList;
import java.util.List;

public class ListaIngredienti {
    public List<Ingredienti> ingredients;
    public int totalCalories;
    public int totalProtein;
    public int totalFat;
    public int totalCarbs;
    public int totalFiber;
    public int totalSugar;

    public ListaIngredienti() {
        this.ingredients = new ArrayList<>();
    }

    public void addIngredient(Ingredienti ing) {
        ingredients.add(ing);
        totalCalories += ing.calories;
        totalProtein += ing.protein;
        totalFat += ing.fat;
        totalCarbs += ing.carbs;
        totalFiber += ing.fiber;
        totalSugar += ing.sugar;
    }
}

