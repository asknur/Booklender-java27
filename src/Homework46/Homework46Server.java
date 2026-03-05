package Homework46;

import Homework44.BookHandler;
import Homework44.Employee;
import Homework45.Homework45Server;
import com.sun.net.httpserver.HttpExchange;
import server.Utils;

import java.io.IOException;
import java.util.*;

public class Homework46Server extends Homework45Server {

    public Homework46Server(String host, int port) throws IOException {
        super(host, port);
        registerGet("/cookie", this::cookieHandler);
        registerGet("/logout", this::logoutHandler);
        registerPost("/take", this::takeHandler);
        registerPost("/return", this::returnHandler);
    }

    private void cookieHandler(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();

        String name = "Global visit";
        data.put("times", UUID.randomUUID().toString());

        Cookie sessionCookie = Cookie.make("userID", "123");
        setCookie(exchange, sessionCookie);

        Cookie c1 = Cookie.make("user%Id", "321");
        setCookie(exchange, c1);
        Cookie c2 = Cookie.make("user-mail", "example@mail.com");
        setCookie(exchange, c2);
        Cookie c3 = Cookie.make("restricted()<>@,;:\\\"/[]?={}", "()<>@,;:\\\"/[]?={}");
        setCookie(exchange, c3);

        String cookieRaw = getCookies(exchange);
        Map<String, String> cookies = Cookie.parse(cookieRaw);

        String visitedValue = cookies.getOrDefault(name, "0");
        int times = Integer.parseInt(visitedValue) + 1;
        Cookie visitedCookie = new Cookie(name, times);
        setCookie(exchange, visitedCookie);
        data.put(name, times);

        data.put("cookies", cookies);
        renderTemplate(exchange, "cookie.ftlh", data);
    }

    private Employee getCurrentUser(HttpExchange exchange) {
        String cookieRaw = getCookies(exchange);
        Map<String, String> cookies = Cookie.parse(cookieRaw);
        String sessionId = cookies.get("sessionId");
        return sessions.get(sessionId);
    }

    private void takeHandler(HttpExchange exchange) {
        try {
            Employee user = getCurrentUser(exchange);
            if (user == null) {
                redirect303(exchange, "/login");
                return;
            }
            Map<String, String> params = Utils.parseUrlEncoded(getBody(exchange), "&");
            String bookId = params.get("id");
            if (bookId != null) {
                BookHandler.takeBook(bookId, user.getId());
            }
            redirect303(exchange, "/books");
        } catch (Exception e) {
            e.printStackTrace();
            redirect303(exchange, "/books");
        }
    }

    private void returnHandler(HttpExchange exchange) {
        try {
            Employee user = getCurrentUser(exchange);
            if (user == null) {
                redirect303(exchange, "/login");
                return;
            }
            Map<String, String> params = Utils.parseUrlEncoded(getBody(exchange), "&");
            String bookId = params.get("id");
            if (bookId != null) {
                BookHandler.returnBook(bookId, user.getId());
            }
            redirect303(exchange, "/books");
        } catch (Exception e) {
            e.printStackTrace();
            redirect303(exchange, "/books");
        }
    }

    private void logoutHandler(HttpExchange exchange) {
        String cookieRaw = getCookies(exchange);
        Map<String, String> cookies = Cookie.parse(cookieRaw);
        String sessionId = cookies.get("sessionId");

        if (sessionId != null){
            sessions.remove(sessionId);
        }

        Cookie expired = new Cookie<>("sessionId", "");
        expired.setMaxAge(0);
        expired.setHttpOnly(true);
        setCookie(exchange, expired);
        redirect303(exchange, "/login");
    }
}
