package Homework45;

import Homework44.Employee;
import Homework46.Cookie;
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
    private Employee currentUser;
    protected Map<String, Employee> sessions = new HashMap<>();

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
        String name = parsed.getOrDefault("name", "").trim();
        String password = parsed.getOrDefault("password", "").trim();
        List<Employee> users = UserStorage.readUsers();

        Map<String, Object> model = new HashMap<>();
        boolean exists = users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            model.put("error", "Регистрация не удалась. Заполните все поля");
            renderTemplate(exchange, "register.html", model);
        } else if (exists) {
            model.put("error", "Такой пользователь уже существует");
            renderTemplate(exchange, "register.html", model);
        } else {
            users.add(new Employee(name, email, password));
            UserStorage.writeUsers(users);
            redirect303(exchange, "/login");
        }
    }

    private void loginGet(HttpExchange exchange) {
        Path path = makeFilePath("login.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }

    private void loginPost(HttpExchange exchange) {
        String raw = getBody(exchange);
        Map<String, String> parsed = Utils.parseUrlEncoded(raw, "&");
        String email = parsed.getOrDefault("email", "").trim();
        String password = parsed.getOrDefault("password", "").trim();

        List<Employee> users = UserStorage.readUsers();
        Optional<Employee> matched = users.stream().filter(u -> email.equals(u.getEmail()) && password.equals(u.getPassword())).findFirst();

        if (matched.isPresent()) {
            currentUser = matched.get();
            String sessionId = UUID.randomUUID().toString();
            sessions.put(sessionId, currentUser);
            Cookie cookie = new Cookie("sessionId", sessionId);
            cookie.setMaxAge(600);
            cookie.setHttpOnly(true);
            setCookie(exchange, cookie);
            redirect303(exchange, "/profile");
        } else {
            Cookie cookie = new Cookie("sessionId", "");
            cookie.setMaxAge(0);
            setCookie(exchange, cookie);
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
