package hu.beni.amusementpark.exception;

import java.util.Optional;

public class ExceptionUtil {
    
    public static void exceptionIfNull(Object o, String message){
        Optional.ofNullable(o).orElseThrow(() -> new AmusementParkException(message));
    }
    
    public static void exceptionIfNotNull(Object o, String message) {
    	if(o != null) {
    		throw new AmusementParkException(message);
    	}
    }
    
    public static <T> void exceptionIfEquals(T t1, T t2, String message) {
        Optional.ofNullable(t1).filter(t -> !t.equals(t2)).orElseThrow(() -> new AmusementParkException(message));
    }
    
    public static <T> void exceptionIfNotEquals(T t1, T t2, String message) {
    	Optional.ofNullable(t1).filter(t -> t.equals(t2)).orElseThrow(() -> new AmusementParkException(message));
    }

    public static void exceptionIfFirstLessThanSecond(long first, long second, String message) {
        if (first < second) {
            throw new AmusementParkException(message);
        }
    }

    public static void exceptionIfNotZero(long value, String message) {
        if (value != 0) {
            throw new AmusementParkException(message);
        }
    }

    public static void exceptionIfPrimitivesEquals(long l1, long l2, String message) {
        if (l1 == l2) {
            throw new AmusementParkException(message);
        }
    }
    
    private ExceptionUtil() {
    	super();
    }

}