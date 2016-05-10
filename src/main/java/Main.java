import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static spark.Spark.halt;

public class Main {
    public static void main(String[] args) throws SQLException{

        // set up server & connection, create GameService model, initialize DB
        Server server = Server.createTcpServer("-baseDir", "./data").start();
        Connection connection = DriverManager.getConnection("jdbc:h2:" + server.getURL() + "/main");
        GameService service = new GameService();
        service.initializeDatabase(connection);


        // fill in the spark route definition
        // set the route to "/"
        // add the lambda

        Spark.get(
                "/",
                (request, response) -> {

                    // create a hashmap for the model
                    HashMap m = new HashMap();


                    // use your selectGames method to select all of the games from the database
                    ArrayList<Game> games = service.selectGames(connection);


                    // put the games arraylist into the model
                    m.put("games", games);

                    // show the home page template
                    return new ModelAndView(m, "home.mustache");
                },
                new MustacheTemplateEngine()
        );


        // create a "get" spark route for the create-game endpoint
        // set the endpoint
        // add your lambda
        Spark.get(
                "/create-game",
                (request, response) -> {


                        return new ModelAndView(null, "gameForm.mustache");
                },
                new MustacheTemplateEngine()
        );


        // create a post route for create-game
        // set the endpoint path
        // add your lambda
        Spark.post(
                "/create-game",
                (request, response) -> {

                    // use a try/catch block when creating the game. This is used to catch validation
                    // errors on the game year.
                    try {
                        // create game and add to user's collection
                        String name = request.queryParams("name");
                        String genre = request.queryParams("genre");
                        String platform = request.queryParams("platform");
                        int releaseYear = Integer.valueOf(request.queryParams("releaseYear"));

                        Game game = new Game(name, genre, platform, releaseYear);

                        service.insertGame(connection, game);
                        response.redirect("/");
                        halt();

                    } catch (NumberFormatException e) {

                        HashMap m = new HashMap();
                        m.put("error", "There was a problem with your release year.");
                        m.put("name", request.queryParams("name"));
                        m.put("genre", request.queryParams("genre"));
                        m.put("platform", request.queryParams("platform"));
                        m.put("releaseYear", request.queryParams("releaseYear"));



                        return new ModelAndView(m, "gameForm.mustache");
                        }
                    return null;
                },
                new MustacheTemplateEngine()
        );







        // create a get route for the edit-game page

        // set the endpoint route

        // add your lambda
        Spark.get(
                "/edit-game",
                (request, response) -> {

                    HashMap m = new HashMap();
                    int editId = Integer.valueOf(request.queryParams("id"));
                    Game game = service.readGame(connection, editId);

                    m.put("game", game);


                    return new ModelAndView(m, "/editGame.mustache");
                },
                new MustacheTemplateEngine()
        );



        // create a spark post endpoint for edit-game
        Spark.post(
                "/edit-game",
                (request, response) -> {

                    try {
                        String name = request.queryParams("name");
                        String genre = request.queryParams("genre");
                        String platform = request.queryParams("platform");
                        int releaseYear = Integer.valueOf(request.queryParams("releaseYear"));
                        int id = Integer.valueOf(request.queryParams("id"));
                        Game game = new Game(name, genre, platform, releaseYear, id);

                        service.updateGame(connection, game);

                        response.redirect("/");
                        halt();

                    } catch (NumberFormatException e){
                        HashMap m = new HashMap();
                        m.put("error", "There was a problem with your release year.");
                        m.put("name", request.queryParams("name"));
                        m.put("genre", request.queryParams("genre"));
                        m.put("platform", request.queryParams("platform"));
                        m.put("releaseYear", request.queryParams("releaseYear"));

                        return new ModelAndView(m, "editGame.mustache");
                    }

                    return null;

                },
                new MustacheTemplateEngine()
        );


        Spark.post(
                "/delete-game",
                (request, response) -> {
                    int deleteId = Integer.valueOf(request.queryParams("id"));

                    service.deleteGame(connection, deleteId);

                    response.redirect("/");
                    halt();

                    return "";
                }
        );

    }

}

