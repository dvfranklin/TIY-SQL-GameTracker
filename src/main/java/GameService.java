import java.sql.*;
import java.util.ArrayList;

public class GameService {

    public void deleteGame(Connection connection, int id) throws SQLException {
        // create a prepared statement to delete the game that has the provided id.
        PreparedStatement prepStmt = connection.prepareStatement("DELETE FROM game WHERE id = ?");


        // set the parameter for the id to the provided id.
        prepStmt.setInt(1, id);

        // execute the statement
        prepStmt.execute();
    }

    public void updateGame(Connection connection, Game game) throws SQLException {
        // create a prepared statement to update the game record in the DB for the provided Game instance
        PreparedStatement prepStmt = connection.prepareStatement("UPDATE game SET name = ?, genre = ?, platform = ?, releaseYear = ? WHERE id = ?");


        // set the give properties in the update statement (name, genre, platform, year, and id)
        // there should be give lines of code
        prepStmt.setString(1, game.name);
        prepStmt.setString(2, game.genre);
        prepStmt.setString(3, game.platform);
        prepStmt.setInt(4, game.releaseYear);
        prepStmt.setInt(5, game.id);

        // execute the statement.
        prepStmt.execute();

    }

    public Game readGame(Connection connection, int id) throws SQLException {
        // create a prepared statement to select the game matching the provided ID
        PreparedStatement prepStmt = connection.prepareStatement("SELECT * FROM game WHERE id = ?");


        // set the parameter for the game id
        prepStmt.setInt(1, id);

        // execute the query and set this into a ResultSet variable
        ResultSet results = prepStmt.executeQuery();

        // read the first line of the result set using the next() method on ResultSet
        results.next();

        // create a new game. Pass the give fields you read from the database into the constructor
        Game game = new Game(results.getString("name"), results.getString("genre"), results.getString("platform"), results.getInt("releaseYear"), results.getInt("id"));

        // return the game
        return game;
    }

    public ArrayList<Game> selectGames(Connection connection) throws SQLException {
        // create an arraylist to hold all the games in our database
        ArrayList<Game> games = new ArrayList<>();

        // create a new statement
        Statement statement = connection.createStatement();

        // use the statement to execute a query to select all rows from the game table
        ResultSet results = statement.executeQuery("SELECT * FROM game");


        // iterate over the result set while we have records to read.
        while(results.next()){
            // create a new instance of game using the data in the query.
            Game game = new Game(results.getString("name"), results.getString("genre"), results.getString("platform"), results.getInt("releaseYear"), results.getInt("id"));

            // add the game to the games arraylist
            games.add(game);
        }

        // return the arraylist of games
        return games;
    }

    public void insertGame(Connection connection, Game game) throws SQLException {
        // create a prepared statement to insert a new game into the game table
        PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO game VALUES (NULL, ?, ?, ?, ?)");


        // set the four fields (not ID!) for the prepared statement
        // you should have four lines of code here
        prepStmt.setString(1, game.name);
        prepStmt.setString(2, game.genre);
        prepStmt.setString(3, game.platform);
        prepStmt.setInt(4, game.releaseYear);

        // execute the statement
        prepStmt.executeUpdate();

        ResultSet results = prepStmt.getGeneratedKeys();
        results.next();
        game.setId(results.getInt(1));

    }

    public void initializeDatabase(Connection connection) throws SQLException {
        // Create a new SQL statement
        Statement statement = connection.createStatement();

        // execute a statement to create the game table if it doesn't exist already.
        // the id field should be an IDENTITY
        statement.execute("CREATE TABLE IF NOT EXISTS game (id IDENTITY, name VARCHAR, genre VARCHAR, platform VARCHAR, releaseYear INT)");

    }

}
