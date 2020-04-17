package com.lmw.fastbus.lib.utils;

import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class FastBusUtil {

    public static String split(List<String> list, String separator) {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            stringBuffer.append("\"").append(list.get(i)).append("\"");
            if (i != list.size() - 1) {
                stringBuffer.append(separator);
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 返回TypeMirror的类型
     *
     * @param typeMirror VariableElement
     * @return String
     */
    public static String parseVariableType(TypeMirror typeMirror) {
        TypeKind typeKind = typeMirror.getKind();
        switch (typeKind) {
            case BOOLEAN:
                return "java.lang.Boolean";
            case BYTE:
                return "java.lang.Byte";
            case SHORT:
                return "java.lang.Short";
            case INT:
                return "java.lang.Integer";
            case LONG:
                return "java.lang.Long";
            case CHAR:
                return "java.lang.Character";
            case FLOAT:
                return "java.lang.Float";
            case DOUBLE:
                return "java.lang.Double";
            default:
                if (typeMirror instanceof DeclaredType) {
                    return handleGenericTypeVariable(typeMirror);
                }
                return typeMirror.toString();
        }
    }

    /**
     * List<User> return java.util.List.class ,rather than java.util.List<User>.class
     *
     * @return String
     */
    public static String handleGenericTypeVariable(TypeMirror typeMirror) {
        DeclaredType declaredType = (DeclaredType) typeMirror;
        TypeElement typeElement = (TypeElement) declaredType.asElement();
        return typeElement.getQualifiedName().toString();
    }
}
