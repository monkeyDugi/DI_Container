package org.example;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        // 클래스 로딩
        Class<Book> bookClass = Book.class;
        Constructor<Book> constructor = bookClass.getConstructor(String.class);
        // 인스턴스 생성
        Book book = constructor.newInstance("bBook");

        /* static 필드 값 가져오기 */
        Field a = bookClass.getDeclaredField("A");
        // static 하므로 인스턴스 타입이 없다. 인스턴스가 없어도 접근이 가능한 것이니까.
        System.out.println(a.get(null));
        // 필드 값 셋팅
        a.set(null, "AAAAA");
        System.out.println(a.get(null));

        /* 인스턴스 필드 값 가져오기 */
        Field b = bookClass.getDeclaredField("b");
        b.setAccessible(true); // private 필드 값에 접근 가능 하도록.
        System.out.println(b.get(book));
        b.set(book, "BBBB");
        System.out.println(b.get(book));
    }
}
