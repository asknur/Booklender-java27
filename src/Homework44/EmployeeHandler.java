package Homework44;

import Homework45.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeHandler {
    private static List<Employee> employees = UserStorage.readUsers();

    public static Employee findEmployeeById(String id) {
        for (Employee emp : employees) {
            if (emp.getId().equals(id)) {
                return emp;
            }
        }
        return null;
    }

    public static Map<String, Object> handleEmployee(String id) {
        Employee emp = findEmployeeById(id);
        if (emp == null) return null;

        List<Book> currentBooks = BookHandler.getAllBooks().stream()
                .filter(b -> emp.getCurrentBooks().contains(b.getId()))
                .toList();
        List<Book> historyBooks = BookHandler.getAllBooks().stream()
                .filter(b -> emp.getHistoryBooks().contains(b.getId()))
                .toList();

        Map<String, Object> model = new HashMap<>();
        model.put("employee", emp);
        model.put("currentBooks", currentBooks);
        model.put("historyBooks", historyBooks);

        return model;
    }
}
