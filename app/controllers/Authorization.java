package controllers;

import models.User;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

import java.util.Optional;

public class Authorization extends Security.Authenticator {

    Messages messages = Http.Context.current().messages();

    @Override
    public String getUsername(Http.Context context) {
        Optional<String> auth = context.request().header("Authorization");
        if (auth.isPresent()) {
            String apiKey = auth.get();
            if (apiKey == null) {
                return null;
            }
            User user = User.findByApiKey(apiKey);
            if (user == null) {
                return null;
            }
            context.args.put("loggedUser", user);
            return user.getNick();
        }
        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return Results.forbidden(messages.at("apiKey.null"));
    }

}
