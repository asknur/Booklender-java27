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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Homework45Server extends Lesson44Server {

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

    private void registerPost(String route, RouteHandler handler) {
        getRoutes().put("POST " + route, handler);
    }

    private void loginGet(HttpExchange exchange) {
        Path path = makeFilePath("login.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }

    private void loginPost(String route, RouteHandler handler) {
        getRoutes().put("POST " + route, handler);
    }

    private void profileGet(HttpExchange exchange) {
        Path path = makeFilePath("profile.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }





    private void loginPost(HttpExchange exchange) {
        String cType = getContentType(exchange);
        String raw = getBody(exchange);
        Map<String, String> parsed = Utils.parseUrlEncoded(raw, "&");

        String fmt = "<p>Необработанные данные: <b>%s</b></p>"
                + "<p>Content-type: <b>%s</b></p>"
                + "<p>После обработки: <b>%s</b></p>";
        String data = String.format(fmt, raw, cType, parsed);
        try {
            sendByteData(exchange, ResponseCodes.OK,
                    ContentType.TEXT_HTML, data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerPost(HttpExchange exchange) {
        String cType = getContentType(exchange);
        String raw = getBody(exchange);
        Map<String, String> parsed = Utils.parseUrlEncoded(raw, "&");

        String fmt = "<p>Необработанные данные: <b>%s</b></p>"
                + "<p>Content-type: <b>%s</b></p>"
                + "<p>После обработки: <b>%s</b></p>";
        String data = String.format(fmt, raw, cType, parsed);
        try {
            sendByteData(exchange, ResponseCodes.OK,
                    ContentType.TEXT_HTML, data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
