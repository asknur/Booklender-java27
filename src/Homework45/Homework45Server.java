package Homework45;

import com.sun.net.httpserver.HttpExchange;
import lesson44.Lesson44Server;
import server.ContentType;
import server.ResponseCodes;
import server.RouteHandler;
import server.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Homework45Server extends Lesson44Server {
    private User currentUser = null;
    private Map<String, User> users = new HashMap<>();

    public Homework45Server(String host, int port) throws IOException {
        super(host, port);
        registerGet("/register", this::registerGet);
        registerPost("/register", this::registerPost);

        loginGet("/login", this::loginGet);
        loginPost("/login", this::loginPost);
        profileGet("/profile", this::profileGet);
    }


    private void registerGet(HttpExchange exchange) {
        Path path = makeFilePath("register.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }

    private void registerPost(HttpExchange exchange) {
        String raw = getBody(exchange);
        Map<String, String> parsed = Utils.parseUrlEncoded(raw, "&");
        String email = parsed.getOrDefault("email", "").trim();
        String fullName = parsed.getOrDefault("name", "").trim();
        String password = parsed.getOrDefault("password", "").trim();
        List<User> users = UserStorage.readUsers();

        boolean exists = users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        String response = "";
        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {

            response = """
                    <h2>Регистрация не удалась</h2>
                    <p>Заполните все поля</p>
                    <a href="/register">Назад</a>
                    """;
        } else if (exists) {
            response = """
                    <h2>Регистрация не удалась</h2>
                    <p>Пользователь уже существует</p>
                    <a href="/register">Попробовать снова</a>
                    """;
        } else {
            users.add(new User(fullName, email, password));
            UserStorage.writeUsers(users);
            redirect303(exchange, "/login");
        }

        try {
            sendByteData(exchange,
                    ResponseCodes.OK,
                    ContentType.TEXT_HTML,
                    response.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loginGet(HttpExchange exchange) {
        Path path = makeFilePath("login.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }

    private void loginPost(HttpExchange exchange) {
        Map<String, String> parsed = Utils.parseUrlEncoded(getBody(exchange), "&");
        String email = parsed.getOrDefault("email", "");
        String password = parsed.getOrDefault("password", "");

        List<User> users = UserStorage.readUsers();
        Optional<User> matched = users.stream().filter(u -> u.getEmail().equals(email) && u.getPassword().equals(password)).findFirst();

        if (matched.isPresent()) {
            currentUser = matched.get();
            redirect303(exchange, "/profile");
        } else {
            redirect303(exchange, "/login");
        }
    }

    private void profileGet(HttpExchange exchange) {
        Map<String, Object> model = new HashMap<>();
        if (currentUser != null) {
            model.put("email", currentUser.getEmail());
            model.put("name", currentUser.getName());
        } else {
            model.put("email", "анонимный@mail.com");
            model.put("name", "Некий пользователь");
        }
        renderTemplate (exchange, "profile.html", model);
    }


    public static String getContentType(HttpExchange exchange) {
        return exchange.getRequestHeaders()
                .getOrDefault("Content-Type", List.of(""))
                .get(0);
    }

    protected String getBody(HttpExchange exchange) {
        InputStream input = exchange.getRequestBody();
        Charset utf8 = StandardCharsets.UTF_8;
        InputStreamReader isr = new InputStreamReader(input, utf8);

        try (BufferedReader reader = new BufferedReader(isr)) {
            return reader.lines().collect(Collectors.joining(""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void redirect303(HttpExchange exchange, String path) {
        try {
            exchange.getResponseHeaders().add("Location", path);
            exchange.sendResponseHeaders(303, 0);
            exchange.getResponseBody().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
