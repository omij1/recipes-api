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
    public void testGetCategoryWrongFormat() {

        RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/category/1")
                .header("Accept", "application/heml");
        Result r = Helpers.route(app, req);
        System.out.println(r.status());
        assertThat(r.status()).isEqualTo(415);
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
        assertThat(r.status()).isEqualTo(404);
    }

    @Test
    public void testPostRecipeWithoutIngredients() {

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
                .uri("/recipe?apiKey=20vzaBgEkhHqp8mfPjpTic2kAnkAWC")
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
        System.out.println(data.toString());
        assertThat(r.status()).isEqualTo(409);
    }


}
