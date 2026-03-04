package Homework45;

import Homework44.Employee;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class UserStorage {
    private static final Path PATH = Paths.get("data/employees.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static List<Employee> readUsers() {
        try {
            if (Files.notExists(PATH)) return new ArrayList<>();
            String json = Files.readString(PATH);
            Employee[] arr = GSON.fromJson(json, Employee[].class);
            return new ArrayList<>(Arrays.asList(arr));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void writeUsers(List<Employee> employees) {
        try {
            String json = GSON.toJson(employees);
            Files.write(PATH, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}