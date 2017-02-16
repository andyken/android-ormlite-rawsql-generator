package typegenerator;

/**
 * Created by fengyuexin on 17/2/6.
 */
public class PrimitiveTypeGenerator implements TypeGenerator{

    private final String typeName;

    public PrimitiveTypeGenerator(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String bindValue() {
        return "bind" + typeName;
    }

    @Override
    public String getValue(String object, String fieldName) {
        Character firstChar = fieldName.charAt(0);
        fieldName = String.valueOf(firstChar).toUpperCase() + fieldName.substring(1, fieldName.length());

        return object + ".get" + fieldName;
    }
}
