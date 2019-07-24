package com.lint.core;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.SourceCodeScanner;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiType;

import java.util.Collections;
import java.util.List;

import static com.android.SdkConstants.SUPPORT_LIB_ARTIFACT;

/**
 * @author 作者：codeman
 * @date 创建时间：2019/3/7
 * @Description 描述：HashMap检测
 * 存在问题：Java7以后支持的 HashMap<Integer, String> = new HashMap<>;
 *         目前是根据引用对象来获取泛型类型，是存在问题的
 *
 */
public class HashMapDetector extends Detector implements SourceCodeScanner {


    private static final String HASH_MAP = "java.util.HashMap";

    private static final String TYPE_INTEGER_WRAPPER = "java.lang.Integer";
    private static final String TYPE_BOOLEAN_WRAPPER = "java.lang.Boolean";
    private static final String TYPE_BYTE_WRAPPER = "java.lang.Byte";
    private static final String TYPE_LONG_WRAPPER = "java.lang.Long";

    public static final Issue USE_SPARSE_ARRAY = Issue.create(
            "UseSparseArrays",
            "HashMap can be replaced with SparseArray",
            "For maps where the keys are of type integer, it's typically more efficient to",
            Category.PERFORMANCE,
            4,
            Severity.WARNING,
            new Implementation(HashMapDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override
    public List<Class<? extends PsiElement>> getApplicablePsiTypes() {
        return Collections.singletonList(PsiNewExpression.class);
    }

    @Override
    public JavaElementVisitor createPsiVisitor(JavaContext context) {
        return new PerformanceVisitor(context);
    }

    private static class PerformanceVisitor extends JavaElementVisitor {

        private JavaContext context;
        private boolean checkMaps;

        public PerformanceVisitor(JavaContext context){
            this.context = context;
            checkMaps = context.isEnabled(USE_SPARSE_ARRAY);
        }

        @Override
        public void visitNewExpression(PsiNewExpression expression) {
            super.visitNewExpression(expression);
            String typeName;
            PsiJavaCodeReferenceElement classReference = expression.getClassReference();
            if (checkMaps){
                if (classReference != null){
                    typeName = classReference.getQualifiedName();
                    if (HASH_MAP.equals(typeName)){
                        checkHashMap(expression, classReference);
                    }
                }
            }
        }

        private void checkHashMap(PsiNewExpression expression, PsiJavaCodeReferenceElement classReference) {
            PsiType[] types = classReference.getTypeParameters();
            if (types.length == 2){
                PsiType first = types[0];
                String typeName = first.getCanonicalText();
                int minSdk = context.getMainProject().getMinSdk();
                if (TYPE_INTEGER_WRAPPER.equals(typeName) || TYPE_BYTE_WRAPPER.equals(typeName)){
                    String valueType = types[1].getCanonicalText();
                    if (TYPE_INTEGER_WRAPPER.equals(valueType)){
                        context.report(USE_SPARSE_ARRAY, expression, context.getLocation(expression),
                                "Use new `SparseIntArray(...)` instead for better performance");
                    }else if (TYPE_LONG_WRAPPER.equals(valueType) && minSdk > 18){
                        context.report(USE_SPARSE_ARRAY, expression, context.getLocation(expression),
                                "Use `new SparseBooleanArray(...)` instead for better performance");
                    }else if (valueType.equals(TYPE_BOOLEAN_WRAPPER)) {
                        context.report(USE_SPARSE_ARRAY, expression, context.getLocation(expression),
                                "Use `new SparseBooleanArray(...)` instead for better performance");
                    } else {
                        context.report(USE_SPARSE_ARRAY, expression, context.getLocation(expression),
                                String.format(
                                        "Use `new SparseArray<%1$s>(...)` instead for better performance",
                                        valueType.substring(valueType.lastIndexOf('.') + 1)));
                    }
                }else if (TYPE_LONG_WRAPPER.equals(typeName) && (minSdk >= 16 ||  Boolean.TRUE.equals(context.getMainProject().dependsOn(
                        SUPPORT_LIB_ARTIFACT)))){
                    boolean useBuiltin = minSdk >= 16;
                    String message = useBuiltin ?
                            "Use `new LongSparseArray(...)` instead for better performance" :
                            "Use `new android.support.v4.util.LongSparseArray(...)` instead for better performance";
                    context.report(USE_SPARSE_ARRAY, expression, context.getLocation(expression),
                            message);
                }
                return;
            }
        }
    }
}
