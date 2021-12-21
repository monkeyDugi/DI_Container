package org.DI;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ContainerService {

    public static <T> T getObject(Class<T> classType) {
        T instance = createInstance(classType); // 인스턴스 생성
        Arrays.stream(classType.getDeclaredFields()).forEach(field -> { // 필드 루프
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
