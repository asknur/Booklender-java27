package Homework46;

import Homework44.BookHandler;
import Homework45.Homework45Server;
import Homework45.User;
import Homework45.UserStorage;
import com.sun.net.httpserver.HttpExchange;
import server.Utils;

import java.io.IOException;
import java.util.*;

public class Homework46Server extends Homework45Server {
    BookHandler bookHandler;

    public Homework46Server(String host, int port) throws IOException {
        super(host, port);
        registerGet("/cookie", this::cookieHandler);
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

}
