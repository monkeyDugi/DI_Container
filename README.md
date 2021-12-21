# java 리플렉션

## 리플렉션이란?

---

> 클래스 타입을 알지 못해도, 해당 클래스의 필드, 메서드 등 클래스의 모든 정보에 접근할 수 있도록 도와주는 자바 API이다.

### 구체적인 클래스 타입을 알지 못한다?

이 의미는 스프링의 DI 컨테이너에서 @Service, @Repository를 생각하면 된다.
해당 애노테이션을 붙이면 new를 해주지 않아도 알아서 주입이 된다. 이걸 가능하게 하는 것이 리플렉션이다.

**그럼 클래스 타입을 알지 못한다는 것은 무슨 상관일까?**
이 부분은 해당 글이 마지막 **DI 프레임워크 만들기**에서 더상세하게 설명 하겠지만, 간략히 설명 하겠다.
코드로 예를 들어보자.

아래는 두 개의 클래스가 있다. 두 클래스를 만든다는 것을 소스 상으로는 누구나 보면 알 수 있다.
@Autowired가 붙어있는 필드에는 자동으로 FirstService 타입의 인스턴스를 주입 해주어야 하지만 클래스 타입이 항상 같은 것도 아니고
어떻게 결정될지 모르기 때문에 컴퓨터 입장에서는 어떤 타입의 인스턴스를 주입 해주어야 하는지 도무지 알 길이 없다.
그래서 Controller 클래스에서 @Autowired가 붙은 필드를 찾아 해당 타입에 맞는 인스턴스를 생성해서 주입 해주는 것이다.
여기서 핵심은 **@Autowired가 붙은 필드를 찾는 것**이다. 이 의미는 결국 클래스 정보를 읽어 무언가를 처리 하겠다는 것이다.
상세한 내용은 **DI 프레임워크 만들기**에서 살펴보자.

```java
public class Contorller {
    @Autowired
    private FirstService firstService;

    @Autowired
    private SecondService secondService;
}
```



## 리플렉션 API : 클래스 정보 조회

---

- package java.lang.Class를 사용
- 모든 클래스 정보에 접근을 할 수 있다. 값 까지도 접근 할 수 있다.

### 간단한 사용 예

클래스 정보에 접근 하는 방법은 3가지로 나뉜다.  
클래스 정보로 접근할 때(Book.class) 클래스 로딩이 된다.

1. 클래스.class
2. 인스턴스.getClass()
3. FQNC(풀패키지)

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

### DI를 사용할 클래스 생성

BookService는 @InjectDugi에 BookRepository를 DI 받고싶은 클래스이다.

```java
public class BookRepository {
}

public class BookService {

    @InjectDugi
    private BookRepository bookRepository;

    public BookRepository getBookRepository() {
        return bookRepository;
    }
}
```

### 애노테이션 생성

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectDugi {
}
```

### DI 컨테이너 클래스 생성

```java
package org.DI;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ContainerService {

    public static <T> T getObject(Class<T> classType) {
        T instance = createInstance(classType); // 인스턴스 생성
        Arrays.stream(classType.getDeclaredFields()) // 필드 루프
                .forEach(field -> {
                    if (field.getAnnotation(InjectDugi.class) != null) { // 필드에 InjectDugi 어노테이션이 붙은 것 찾기
                        Object fieldInstance = createInstance(field.getType());
                        field.setAccessible(true); // private 접근 허용
                        try {
                            field.set(instance, fieldInstance); // instance 클래스에 해당 field에 fieldInstance 셋팅
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException();
                        }
                    }
                });
        return instance;
    }

    // 인스턴스 생성
    private static <T> T createInstance(Class<T> classType) {
        try {
            return classType.getConstructor(null).newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException();
        }
    }
}
```

주석을 달아 놓긴 했지만 조금 더 상세하게 알아 보자.

```java
private static <T> T createInstance(Class<T> classType)
```

- 리플렉션 API인 Class<T> 타입으로 파리미터를 받는다.
- 기본 생성자를 이용해 생성하기 때문에 **getConstructor(null) 에 null을 넘긴 후 인스턴스를 생성한다.

**이제 public static <T> T getObject(Class<T> classType) 메서드를 알아보자.**

BookService를 주입한다는 가정으로 classType은 BookService.class 이다.

```java
T instance = createInstance(classType); // 인스턴스 생성
```

- BookService instance = new BookService가 된다. createInstance()에서 생성을 했기 때문이다.

```java
Arrays.stream(classType.getDeclaredFields()).forEach(field -> { // 필드 루프
```

- classType은 BookService이기 때문에 해당 클래스에서 모든 필드를 찾기 위해 반복한다.

```java
if (field.getAnnotation(InjectDugi.class) != null) { // 필드에 InjectDugi 어노테이션이 붙은 것 찾기
```

- 여기 부터 중요하다. 찾은 필드가 **@InjectDugi**가 붙어 있다면 getAnnotation()의 반환 값은 null이 아니므로 충족한다.
  BookService에는 BookRepository라는 필드에 @InjectDugi가 붙어있으므로 이때 조건에 충족 된다.

```java
Object fieldInstance = createInstance(field.getType());
field.setAccessible(true); // private 접근 허용
```

- 조건이 만족한 필드인 BookRepository.class를 createInstance() 메서드를 이용해 인스턴스를 생성한다.
- 그리고 그 필드가 private이어도 접근 가능하도록 설정한다.

```java
field.set(instance, fieldInstance); // instance 클래스에 해당 field에 fieldInstance 셋팅
```

- 처음 대상이었던 BookService에서 가져온 field(private BookRepository bookRepository)에 생성된 BookRepository 인스턴스를 셋팅한다.

### DI 프레임 워크 테스트 코드

```java
package org.DI;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ContainerServiceTest {

    @Test
    public void getObject_BookRepository() {
        Class<BookRepository> bookRepositoryClass = BookRepository.class;
        BookRepository bookRepository = ContainerService.getObject(bookRepositoryClass);
        assertNotNull(bookRepository);
    }

    @Test
    public void getObject_BookService() {
        Class<BookService> bookServiceClass = BookService.class;
        BookService bookService = ContainerService.getObject(bookServiceClass);
        assertNotNull(bookService);
        assertNotNull(bookService.getBookRepository());
    }
}
```

### 외부 프로젝트에서 DI 프레임 워크 사용 해보기

진짜 우리가 스프링 프레임 워크를 사용하듯이 사용 해보겠다.
**maven install을 해서 생긴 jar를 의존성 추가만 해주면 사용할 수 있다.**

```java
  <dependencies>
    <dependency>
      <groupId>org.example</groupId>
      <artifactId>refactoringexample</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
```

- maven 의존성 주입

```java
package org.example;

import org.DI.InjectDugi;

public class ShopService {

    @InjectDugi
    private ShopRepository shopRepository;

    public void buy() {
        System.out.println("ShopService.buy");
        shopRepository.save();
    }
}
```

```java
package org.example;

public class ShopRepository {

    public void save() {
        System.out.println("ShopRepository.save");
    }
}
```

```java
public class App {
    public static void main( String[] args ) {
        ShopService object = ContainerService.getObject(ShopService.class);
        object.buy();
    }
}
```

- 스프링과 같지는 않지만 만든 DI프레임 워크를 이렇게 다른 프로젝트에서 사용할 수 있다.