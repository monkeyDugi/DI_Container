# java 리플렉션
- 클래스 로딩이 된 후 메모리에 올라갔을 때 클래스 정보에 접근할 수 있다.  
  즉, 런타임에 조작을 하는 것.
- **대표적인 활용 예로는 스프링의 DI가 있다.**
  
## 주의 사항
잘못된 사용을 할 시 주의 사항
- 지나친 사용은 성능 이슈 가능성이 있다.  
  -> 이미 인스턴스가 있을 때 해당 인스턴스를 바로 사용해도 되는데 굳이 리플렉션을 이용해  
     getDeclaredField()와 같이 접근해서 사용하기 때문이다.
- 컴파일 타임에 확인되지 않고 런타임 시에만 발생하는 문제를 만들 가능성이 있다.  
  -> 런타임 시에 조작을 하는 것이기 때문.
- 접근 지시자를 무시할 수 있다.  

## 리플렉션 API : 클래스 정보 조회

---
- package java.lang.Class를 사용
- 모든 클래스 정보에 접근을 할 수 있다. 값 까지도 접근 할 수 있다.
### 간단한 사용 예
클래스 정보에 접근 하는 방법은 3가지로 나뉜다.  
클래스 정보로 접근할 때(Book.class) 클래스 로딩이 된다.
1. 클래스.class
2. 인스턴스.getClass()
3. FQNC(풀패키지) - 풀 패키지 경로 문자열
```java
// 리플렉션이 제공하는 API를 사용해서 클래스 정보에 접근하기.

// 타입으로 클래스 정보로 접근
Class<Book> bookClass = Book.class;

// 인스턴스(힙)로 클래스 정보 접근
Book book = new Book();
Class<? extends Book> aClass = book.getClass();

// 문자열로만 클래 정보 접근(forName : ClassNotFoundException)
Class<?> aClass1 = Class.forName("org.example.Book");

/*
 * 필드 정보 가져오기
 * 생성자 정보 가져오기
 */
System.out.println("필드");
Arrays.stream(bookClass.getDeclaredFields()).forEach(System.out::println);
System.out.println("생성자");
Arrays.stream(bookClass.getConstructors()).forEach(System.out::println);

/* 출력  결과
필드
private static java.lang.String org.example.Book.B
private static final java.lang.String org.example.Book.C
private java.lang.String org.example.Book.a
public java.lang.String org.example.Book.d
protected java.lang.String org.example.Book.e
생성자
public org.example.Book()
public org.example.Book(java.lang.String,java.lang.String,java.lang.String)
 */
```

## 리플렉션 API : 클래스 정보 수정

---
- 리플렉션 API를 이용해서 클래스 정보를 변경할 수 있는 예제를 살펴 보자.
```java
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
```

## DI 프레임워크 만들기

---
리플렉션 API를 이용해서 DI 프레임워크를 만들어 본다.

### 요구사항
- @Inject라는 애노테이션으로 필드 주입을 해주는 컨테이너 서비스 만들기
```java
public class  BookService {
    
    @Inject
    BookRepository bookRepository
}
```

- ContainerService.java  
  -> classType에 해당하는 타입의 객체를 만들어 준다.  
  -> 단, 해당 객체의 필드 중에 @Inject가 있다면 해당 필드도 같이 만들어 제공한다.
```java
pubilc static <T> T getObject(Class<T> classType)
```
