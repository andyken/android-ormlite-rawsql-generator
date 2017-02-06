package typegenerater;

/**
 * Created by fengyuexin on 17/2/6.
 */
public interface TypeGenerator {

    String bindValue();

    String getValue(String object, String fieldName);
}
