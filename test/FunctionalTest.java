import org.junit.Test;

import com.google.gson.JsonObject;

import models.Category;
import models.Recipe;

import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import play.twirl.api.Content;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.HashMap;
import java.util.Map;

/**
 * Clase que contiene los test funcionales del API de recetas de cocina
 */
public class FunctionalTest extends WithApplication {

    @Test
    public void testGetUsersWrongURL() {

        RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/users")
                .header("Accept", "application/json");
        Result r = Helpers.route(app, req);
        assertThat(r.status()).isEqualTo(409);
    }

    @Test
    public void testCategoryTemplate() {

        Category c = new Category("Carnes y aves");
        Content xml = views.xml._category.render(c);
        assertThat("application/xml").isEqualTo(xml.contentType());
        assertThat(xml.body()).contains("<name>");
    }

    @Test
    public void testDeleteNotExistingCategory() {

        RequestBuilder req = Helpers.fakeRequest()
                .method("DELETE")
                .uri("/category/999");
        Result r = Helpers.route(app, req);
        assertThat(r.status()).isEqualTo(403);//Solo puede eliminar una categoria el administrador
    }

    @Test
    public void testPostRecipeWithoutIngredientsAndApikey() {

        Map<String, String> data = new HashMap<String, String>();
        data.put("title", "Filete con patatas");
        data.put("steps", "Cortar la carne, meter en el horno");
        data.put("time", "30 min");
        data.put("difficulty", "INTERMEDIA");

        JsonObject category = new JsonObject();
        category.addProperty("id", 1);
        data.put("category", category.toString());

        RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe")
                .header("Content-Type", "application/json")
                .bodyForm(data);
        Result r = Helpers.route(app, req);
        assertThat(r.status()).isEqualTo(403);//La falta del Apikey tiene prioridad frente a los ingredientes
    }
    
    @Test
    public void testPostUserWithoutSurname() {
    	
        Map<String, String> data = new HashMap<String, String>();
        data.put("nick", "VaderRules61");
        data.put("name", "Darth Vader");
        data.put("city", "Galactic Empire");

        RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user")
                .header("Content-Type", "application/json")
                .bodyForm(data);
        Result r = Helpers.route(app, req);
        assertThat(r.status()).isEqualTo(409);
    }

    @Test
    public void testFindRecipeByName() {

        Recipe r = Recipe.findByName("Filete con cosas");
        assertThat(r).isNull();
    }


    @Test
    public void testNickMinLength() {
    	
        Map<String, String> data = new HashMap<String, String>();
        data.put("nick", "Nic");
        data.put("name", "Name");
        data.put("surname", "Surname");
        data.put("city", "City");

        RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user")
                .header("accept", "json")
                .bodyForm(data);
        Result r = Helpers.route(app, req);
        assertThat(r.status()).isEqualTo(409);
    }


    @Test
    public void testNickMaxLength() {
    	
        Map<String, String> data = new HashMap<String, String>();
        data.put("nick", "NickNickNickNick");
        data.put("name", "Name");
        data.put("surname", "Surname");
        data.put("city", "City");

        RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user")
                .header("accept", "json")
                .bodyForm(data);
        Result r = Helpers.route(app, req);
        assertThat(r.status()).isEqualTo(409);
    }

}
