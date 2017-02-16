import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import typegenerator.PrimitiveTypeFactory;
import typegenerator.TypeFactory;
import typegenerator.TypeGenerator;

import java.util.List;

/**
 * Created by fengyuexin on 17/2/5.
 */
public class CodeGenerator {

    private PsiClass psiClass;
    private List<PsiField> fields;
    private TypeFactory typeFactory;
    private static final int FIELD_RETURN_NUM = 5;

    public CodeGenerator(PsiClass psiClass, List<PsiField> fields) {
        this.psiClass = psiClass;
        this.fields = fields;
        this.typeFactory = new PrimitiveTypeFactory();
    }

    public void generate(){
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiMethod rawInsertMethod = elementFactory.createMethodFromText(generateRawInsert(fields, psiClass), psiClass);
        PsiMethod rawUpdateMethod = elementFactory.createMethodFromText(generateRawUpdate(fields, psiClass), psiClass);
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(psiClass.getProject());
        styleManager.shortenClassReferences(psiClass.addBefore(rawInsertMethod, psiClass.getLastChild()));
        styleManager.shortenClassReferences(psiClass.addBefore(rawUpdateMethod, psiClass.getLastChild()));
    }

    private String generateRawInsert(List<PsiField> fields, PsiClass psiClass) {
        String entityClassName = psiClass.getQualifiedName();
        String tableName = "tableName";
        PsiField tableNameField = null;
        String entityObjectName = "entity";
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

        StringBuilder sb = new StringBuilder("public long rawInsertWithRawSql(" + entityClassName + " " +
                entityObjectName + ", android.database.sqlite.SQLiteDatabase db) {\n");
        sb.append("android.database.sqlite.SQLiteStatement statement = null;\n")
                .append("long rowId = 0;\n")
                .append("try {\n")
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
        sb.append(")\");\n");

        sb.append("statement.clearBindings();\n");

        for (int i = 0; i < fieldLength; i++) {
            TypeGenerator typeGenerator = typeFactory.getGenerator(fields.get(i).getType());
            if (typeGenerator != null) {
                //bind index start from one
                sb.append("statement.").append(typeGenerator.bindValue()).append("(")
                        .append(i + 1).append(", ").append(typeGenerator.getValue(entityObjectName, fields.get(i).getName()))
                        .append("());\n");
            }
        }

        sb.append("rowId = statement.executeInsert();\n");
        sb.append("} catch (Exception e) {\n")
                .append("e.printStackTrace();\n")
                .append("} finally {\n")
                .append("if (statement != null) {\n")
                .append("statement.close();\n")
                .append("}\n")
                .append("}\n")
                .append("return rowId;\n");
        sb.append("}");
        return sb.toString();
    }

    private String generateRawUpdate(List<PsiField> fields, PsiClass psiClass) {
        String entityClassName = psiClass.getQualifiedName();
        String tableName = "tableName";
        PsiField tableNameField = null;
        String entityObjectName = "entity";
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
        StringBuilder sb = new StringBuilder("public long rawUpdateWithRawSql(" + entityClassName + " " +
                entityObjectName + ", android.database.sqlite.SQLiteDatabase db) {\n");
        sb.append("android.database.sqlite.SQLiteStatement statement = null;")
                .append("long rowId = 0;")
                .append("try {")
                .append("statement = db.compileStatement(\"update ")
                .append(tableName)
                .append(" set\"+\n");

        int fieldLength = fields.size();
        PsiField indexField = null;
        for (int i = 0; i < fieldLength; i++) {
            PsiField field = fields.get(i);
            if (field.getText().indexOf("generatedId") > 0) {
                indexField = field;
                continue;
            }
            sb.append("\" ").append(field.getName()).append(" = ?,\" + \n");
        }
        sb.append("\" where ");
        if (indexField != null) {
            sb.append(indexField.getName()).append(" = ?\");");
        } else {
            sb.append("id = ?\");\n");
        }
        sb.append("statement.clearBindings();\n");

        TypeGenerator typeGenerator = null;
        for (int i = 0; i < fieldLength; i++) {
            PsiField field = fields.get(i);
            if (field == indexField) {
                continue;
            }
            typeGenerator = typeFactory.getGenerator(field.getType());
            if (typeGenerator != null) {
                //bind index start from one
                sb.append("statement.").append(typeGenerator.bindValue()).append("(")
                        .append(i + 1).append(", ").append(typeGenerator.getValue(entityObjectName, field.getName()))
                        .append("());\n");
            }
        }
        if (indexField != null) {
            typeGenerator = typeFactory.getGenerator(indexField.getType());
            if (typeGenerator != null) {
                sb.append("statement.").append(typeGenerator.bindValue()).append("(")
                        .append(fieldLength).append(", ").append(typeGenerator.getValue(entityObjectName, indexField.getName()))
                        .append("());\n");
            }
        }
        sb.append("rowId = statement.executeInsert();\n");
        sb.append("} catch (Exception e) {\n")
                .append("e.printStackTrace();\n")
                .append("} finally {\n")
                .append("if (statement != null) {\n")
                .append("statement.close();\n")
                .append("}\n")
                .append("}\n")
                .append("return rowId;\n");
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
