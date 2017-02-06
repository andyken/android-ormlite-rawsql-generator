package typegenerater;

import com.intellij.psi.PsiType;

/**
 * Created by fengyuexin on 17/2/6.
 */
public interface TypeFactory {
    public TypeGenerator getGenerator(PsiType psiType);
}
