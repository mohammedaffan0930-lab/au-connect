package edu.anurag.auconnect.config;

import edu.anurag.auconnect.dto.Dtos.Option;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Fixed lists of departments and complaint categories. */
public final class Catalog {
    private Catalog() {}

    public static final List<Option> DEPARTMENTS = List.of(
            new Option("ALL", "All Departments"),
            new Option("CSE", "Computer Science & Engineering"),
            new Option("AIML", "CSE (AI & Machine Learning)"),
            new Option("DS", "CSE (Data Science)"),
            new Option("IT", "Information Technology"),
            new Option("ECE", "Electronics & Communication"),
            new Option("EEE", "Electrical & Electronics"),
            new Option("MECH", "Mechanical Engineering"),
            new Option("CIVIL", "Civil Engineering"),
            new Option("PHARMACY", "Pharmacy"),
            new Option("MBA", "Management Studies"));

    public static final List<Option> COMPLAINT_CATEGORIES = List.of(
            new Option("ACADEMIC", "Academics"),
            new Option("HOSTEL", "Hostel"),
            new Option("TRANSPORT", "Transport"),
            new Option("CANTEEN", "Canteen & Food"),
            new Option("INFRASTRUCTURE", "Infrastructure"),
            new Option("SAFETY", "Safety & Security"),
            new Option("OTHER", "Other"));

    private static final Set<String> DEPARTMENT_CODES =
            DEPARTMENTS.stream().map(Option::code).collect(Collectors.toSet());
    private static final Set<String> CATEGORY_CODES =
            COMPLAINT_CATEGORIES.stream().map(Option::code).collect(Collectors.toSet());

    public static boolean isDepartment(String code) {
        return code != null && DEPARTMENT_CODES.contains(code);
    }

    public static boolean isCategory(String code) {
        return code != null && CATEGORY_CODES.contains(code);
    }
}
