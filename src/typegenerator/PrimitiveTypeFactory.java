package typegenerator;

import com.intellij.psi.PsiType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengyuexin on 17/2/6.
 */
public class PrimitiveTypeFactory implements TypeFactory {

    private final Map<String, TypeGenerator> generatorMethodsForTypes = new HashMap<String, TypeGenerator>();

    public PrimitiveTypeFactory() {
        initPrimitives();
    }

    private void initPrimitives(){
        generatorMethodsForTypes.put("char", new PrimitiveTypeGenerator("String"));
        generatorMethodsForTypes.put("byte", new PrimitiveTypeGenerator("String"));
        generatorMethodsForTypes.put("java.lang.String", new PrimitiveTypeGenerator("String"));
        generatorMethodsForTypes.put("double", new PrimitiveTypeGenerator("Double"));
        generatorMethodsForTypes.put("float", new PrimitiveTypeGenerator("Double"));
        generatorMethodsForTypes.put("int", new PrimitiveTypeGenerator("Long"));
        generatorMethodsForTypes.put("long", new PrimitiveTypeGenerator("Long"));
    }

    @Override
    public TypeGenerator getGenerator(PsiType psiType) {
        return generatorMethodsForTypes.get(psiType.getCanonicalText());
    }
}
