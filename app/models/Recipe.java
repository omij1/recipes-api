package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.PagedList;
import play.data.validation.Constraints.Required;

/**
 * Clase modelo que representa la tabla Recipe donde se almacenan las recetas del API.
 *
 * @author MIMO
 */

@Entity
public class Recipe extends Model {

    /**
     * Permite hacer búsquedas de recetas
     */
    public static final Finder<Long, Recipe> find = new Finder<>(Recipe.class);

    /**
     * Identificador de la receta
     */
    @Id
    Long recipeId;

    /**
     * Nombre de la receta
     */
    @Required
    String title;

    /**
     * Lista de ingredientes de una receta
     */
    @Required
    @JsonManagedReference
    @ManyToMany(cascade = CascadeType.ALL)
    public List<Ingredient> ingredients = new ArrayList<Ingredient>();

    /**
     * Pasos para elaborar la receta
     */
    @Required
    String steps;

    /**
     * Unidad de tiempo necesario para elaborar la receta. Pueden ser minutos u horas
     */
    @Required
    String time;

    /**
     * Valor de dificultad de la receta
     */
    @Enumerated(EnumType.STRING)
    @Required
    public Difficulty difficulty;

    /**
     * Categoría de la receta
     */
    @Required
    @JsonManagedReference
    @ManyToOne
    public Category category;

    /**
     * Autor de la receta
     */
    @ManyToOne
    public User user;


    /**
     * Constructor de la clase Recipe
     *
     * @param title       Nombre de la receta
     * @param ingredients Ingredientes de la receta
     * @param steps       Pasos para elaborar la receta
     * @param time        Unidad de tiempo para elaborar la receta
     * @param difficulty  Dificultad de la receta
     * @param category    Categoría de la receta
     */
    public Recipe(@Required String title, @Required List<Ingredient> ingredients, @Required String steps, @Required String time, Difficulty difficulty,
                  Category category) {

        super();
        this.title = title;
        this.ingredients = ingredients;
        this.steps = steps;
        this.time = time;
        this.difficulty = difficulty;
        this.category = category;
    }

    /**
     * Método que busca una receta basándose en su identificcador
     *
     * @param id Identificador de la receta
     * @return Un objeto con la receta
     */
    public static Recipe findById(Long id) {

        return find.byId(id);
    }

    /**
     * Método que comprueba si una receta ya existe
     *
     * @param title Nombre de la receta
     * @return Un objeto con la receta
     */
    public static Recipe findByName(String title) {

        return find.query().where().isNotNull("title").eq("title", title).findOne();
    }

    /**
     * Método que muestra las recetas existentes de forma paginada
     *
     * @param page Número de página que se desea ver
     * @return Devuelve una lista con las recetas
     */
    public static PagedList<Recipe> findPage(Integer page) {

        return find.query().setMaxRows(10).setFirstRow(10 * page).findPagedList();
    }

    /**
     * Método que comprueba si la categoría de la receta introducida existe
     *
     * @return Devuelve true si la categoría existe y false en caso contrario
     */
    public boolean checkCategory() {

        Category c = Category.findByCategoryId(this.category.getCategoryId());
        if (c != null) {
            this.category = c;
            return true;
        }
        return false;
    }

    /**
     * Método que comprueba si una receta ya existe y la crea en caso de que sea posible
     *
     * @return Devuelve false si la receta ya existe y true si se creó correctamente
     */
    public boolean checkRecipe() {

        if (Recipe.findByName(this.title.toUpperCase()) == null) {
            this.title = this.title.toUpperCase();
            this.checkIngredients(this.ingredients);
            Ebean.beginTransaction();
            this.save();
            Ebean.commitTransaction();
            Ebean.endTransaction();
            return true;
        }
        return false;
    }

    /**
     * Método que comprueba si los ingredientes de la receta ya existen en la base de datos para evitar la creación de tuplas repetidas con la
     * misma información. Además transforma los ingredientes en minúsculas.
     *
     * @param i Los ingredientes de la receta
     */
    public void checkIngredients(List<Ingredient> i) {

        Ingredient ing;
        for (int j = 0; j < i.size(); j++) {
            i.get(j).setIngredientName(i.get(j).getIngredientName().toLowerCase());
            i.get(j).setUnits(i.get(j).getUnits().toLowerCase());
            ing = Ingredient.findIngredientByNameAndUnit(i.get(j).getIngredientName(), i.get(j).getUnits());
            if (ing != null) {
                i.set(j, ing);
            }
        }

    }

    /**
     * Método que actualiza los ingredientes de una receta. Hay cuatro escenarios posibles: se incorpora un ingrediente/es, se
     * elimina un ingrediente/es, se modifica un ingrediente/es y la unión de los casos anteriores.
     *
     * @param i Lista con los ingredientes de la receta que se pretende actualizar
     */
    public void updateRecipeIngredients(List<Ingredient> i) {

        Ingredient ing;
        this.ingredients.clear();
        for (int j = 0; j < i.size(); j++) {
            i.get(j).setIngredientName(i.get(j).getIngredientName().toLowerCase());
            i.get(j).setUnits(i.get(j).getUnits().toLowerCase());
            ing = Ingredient.findIngredientByNameAndUnit(i.get(j).getIngredientName(), i.get(j).getUnits());
            if (ing == null) {
                ing = new Ingredient(i.get(j).getIngredientName(), i.get(j).getUnits());
            }
            this.ingredients.add(ing);
        }

    }

    /**
     * Getter de recipeId
     *
     * @return Identificador de la receta
     */
    public Long getRecipeId() {
        return recipeId;
    }

    /**
     * Setter de recipeId
     *
     * @param recipeId El identificador de la receta
     */
    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    /**
     * Getter de title
     *
     * @return El título de la receta
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter de title
     *
     * @param title Título de la receta
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter de ingredients
     *
     * @return los ingredientes de la receta
     */
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     * Setter de ingredients
     *
     * @param ingredients Ingredientes de la receta
     */
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Getter de steps
     *
     * @return Los pasos para elaborar la receta
     */
    public String getSteps() {
        return steps;
    }

    /**
     * Setter de steps
     *
     * @param steps Pasos para elaborar la receta
     */
    public void setSteps(String steps) {
        this.steps = steps;
    }

    /**
     * Getter de time
     *
     * @return El tiempo necesario para elaborar una receta
     */
    public String getTime() {
        return time;
    }

    /**
     * Setter de time
     *
     * @param time El tiempo necesario para elaborar una receta
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Getter de difficulty
     *
     * @return Valor de dificultad para elaborar la receta
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Setter de difficulty
     *
     * @param difficulty Dificultad de la receta
     */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Getter de category
     *
     * @return Categoría de la receta
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Setter de category
     *
     * @param category Categoría de la receta
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
