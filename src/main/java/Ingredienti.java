public class Ingredienti {
    public String name;
    public int weight; // in grammi
    public int calories;
    public int protein;
    public int fat;
    public int carbs;
    public int fiber;
    public int sugar;

    // Costruttore
    public Ingredienti(String name, int weight, int calories, int protein,
                       int fat, int carbs, int fiber, int sugar) {
        this.name = name;
        this.weight = weight;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.fiber = fiber;
        this.sugar = sugar;
    }
}

