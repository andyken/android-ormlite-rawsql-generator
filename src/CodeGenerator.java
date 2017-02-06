import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import typegenerater.PrimitiveTypeFactory;
import typegenerater.TypeFactory;
import typegenerater.TypeGenerator;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by fengyuexin on 17/2/5.
 */
public class CodeGenerator {

    private PsiClass psiClass;
    private List<PsiField> fields;
//    private Class entityClass;
    private TypeFactory typeFactory;
    private static final int FIELD_RETURN_NUM = 5;

    public CodeGenerator(PsiClass psiClass, List<PsiField> fields) {
        this.psiClass = psiClass;
        this.fields = fields;
//        this.entityClass = entityClass;
        this.typeFactory = new PrimitiveTypeFactory();
    }

    public void generate(){
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiMethod rawInsertMethod = elementFactory.createMethodFromText(generateRawInsert(fields, psiClass), psiClass);
//        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(psiClass.getProject());
        psiClass.addBefore(rawInsertMethod, psiClass.getLastChild());
    }

    private String generateRawInsert(List<PsiField> fields, PsiClass psiClass) {
        Class entityClass = psiClass.getClass();
        String entityClassName = psiClass.getQualifiedName();
        String tableName = "tableName";
        PsiField tableNameField = null;
        try {
            tableNameField = psiClass.findFieldByName("TABLE_NAME", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (tableNameField != null) {
            String tableNameVar = tableNameField.getText();
            int startIndex = tableNameVar.indexOf("\"");
            int endIndex = tableNameVar.lastIndexOf("\"");
            tableName = tableNameVar.substring(startIndex + 1, endIndex);
        }
        String entityObjectName = getEntityObjectName(entityClassName);
        StringBuilder sb = new StringBuilder("public long rawInsertWithRawSql(" + entityClassName + " " +
                entityObjectName + ", SQLiteDatabase db) {");
        sb.append("SQLiteStatement statement = null;")
                .append("long rowId = 0;")
                .append("try {")
                .append("statement = db.compileStatement(\"insert into ")
                .append(tableName)
                .append(" (");

        int fieldLength = fields.size();
        for (int i = 0; i < fieldLength - 1; i++) {
            sb.append(fields.get(i).getName()).append(", ");
            if (i % FIELD_RETURN_NUM == 0) {
                sb.append("\" +\n\"");
            }
        }
        sb.append(fields.get(fieldLength - 1).getName());
        sb.append(") \" +\n\"VALUES (?");
        for (int i = 1; i < fieldLength; i++) {
            sb.append(", ?");
        }
        sb.append(")\");");

        sb.append("statement.clearBindings();");

        for (int i = 0; i < fieldLength; i++) {
            TypeGenerator typeGenerator = typeFactory.getGenerator(fields.get(i).getType());
            //bind index start from one
            sb.append("statement.").append(typeGenerator.bindValue()).append("(")
                    .append(i + 1).append(", ").append(typeGenerator.getValue(entityObjectName, fields.get(i).getName()))
                    .append("());");
        }

        sb.append("rowId = statement.executeInsert();");
        sb.append("} catch (Exception e) {")
                .append("e.printStackTrace();")
                .append("} finally {")
                .append("if (statement != null) {")
                .append("statement.close();")
                .append("}")
                .append("}")
                .append("return rowId;");
        sb.append("}");
        return sb.toString();
    }

    private String getEntityObjectName(String entityClassName) {
        if (entityClassName == null || entityClassName.length() == 0) {
            return "";
        }
        Character firstChar = entityClassName.charAt(0);
        return String.valueOf(firstChar).toLowerCase() + entityClassName.substring(1, entityClassName.length());
    }
}
