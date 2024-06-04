package api;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rebecca Zhang
 * Created on 2024-06-02
 */
public class SkierThreadLocal<T> extends ThreadLocal<T> {

    private static final Logger LOGGER = Logger.getLogger(SkierThreadLocal.class.getName());

    @Override
    protected T initialValue() {
        return (T) new ApiClient();
    }

    @Override
    public void remove() {
        // System.out.println("remove ThreadLocal");
        T value = get();
        if (value instanceof AutoCloseable) {
            try {
                ((AutoCloseable) value).close(); // 调用 AutoCloseable 类型对象的 close() 方法
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error removing ThreadLocal: " + e.getMessage(), e);
            }
        }
        super.remove();
    }

}
