package org.example;

public class Book {

    public static String A = "A";
    private String b = "b";

    public Book() {
    }

    public Book(String b) {
        this.b = b;
    }

    public String getB() {
        return b;
    }

    public void c() {
        System.out.println("C");
    }

    public int sum(int left, int right) {
        return left + right;
    }
}
