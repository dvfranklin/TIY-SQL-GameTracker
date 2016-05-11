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


        // GET webroot
        Spark.get(
                "/",
                (request, response) -> {

                    // Hashmap as model
                    HashMap m = new HashMap();


                    // populate ArrayList of games, add it to model
                    ArrayList<Game> games = service.selectGames(connection);
                    m.put("games", games);

                    // show homepage
                    return new ModelAndView(m, "home.mustache");
                },
                new MustacheTemplateEngine()
        );


        // GET to redirect to create-game form
        Spark.get(
                "/create-game",
                (request, response) -> {


                        return new ModelAndView(null, "gameForm.mustache");
                },
                new MustacheTemplateEngine()
        );


        // POST to create game from user input
        Spark.post(
                "/create-game",
                (request, response) -> {


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







        // GET route to bring up edit-game form
        Spark.get(
                "/edit-game",
                (request, response) -> {

                    HashMap m = new HashMap();

                    // find the game user wanted to edit, pass it to the model
                    int editId = Integer.valueOf(request.queryParams("id"));
                    Game game = service.readGame(connection, editId);
                    m.put("game", game);


                    return new ModelAndView(m, "/editGame.mustache");
                },
                new MustacheTemplateEngine()
        );



        // POST route to edit game based on user input
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


        // POST route to delete a game
        Spark.post(
                "/delete-game",
                (request, response) -> {

                    // find the game user wanted to delete, pass it to the model
                    int deleteId = Integer.valueOf(request.queryParams("id"));
                    service.deleteGame(connection, deleteId);

                    response.redirect("/");
                    halt();

                    return "";
                }
        );

    }

}

