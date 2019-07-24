package com.lint.core;



import com.android.tools.lint.client.api.JavaEvaluator;
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
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;

import org.jetbrains.uast.UCallExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/****
 * 1 Scanner
 * Detector.JavaPsiScanner接口，要分析Java源文件的lint检测器实现该接口
 * 它公开了底层的抽象语法树，并提供了解析符号等核心业务
 * 核心流程：由get**方法指定了感兴趣的AST节点类型、方法名称、构造函数类型、引用名称类型列表、android资源引用、限定超类等点
 *          然后visit**回调具体对应的感兴趣的点
 *
 * 2 Detector
 * Detector抽象类：检测器能够找到特定问题（或一组相关问题）。每种问题类型都唯一标识为Issue
 * 定义了支持的各个类型的文件：（内部类实现归类设计思想）
 * 清单文件
 * 资源文件按资源类型按字母顺序排列（因此，在“值”之前检查“布局”，在“值-en”之前但在“值”之后检查“values-de”，依此类推。
 * Java class文件
 * Java类
 * Gradle文件
 * 通用文件
 * Proguard文件
 * 属性文件
 *
 * 3 context.report(ISSUE, call, context.getLocation(call), message);
 *   a 参数一 警告实体
 *   b 参数二 当前节点
 *   c 参数三 当前的位置信息，便于在报告中显示定位
 *   d 参数四 为警告添加解释
 *
 * 4 Issue
 *   由Detector发现并报告android 程序代码可能存在的bug
 *   Issue create(String id, String briefDescription,
                 String explanation, Category category,
                 int priority, Severity severity,
                 Implementation implementation) {}
     a id  唯一值，应该能简短描述当前问题。利用Java注解或者XML属性进行屏蔽时，使用的就是这个id。
     b briefDescription 简短的总结，通常5-6个字符，描述问题而不是修复措施
     c explanation 完整的问题解释和修复建议
     d category 问题类别
     e priority  优先级。1-10的数字，10为最重要/最严重。
     f severity  严重级别：Fatal, Error, Warning, Informational, Ignore
     g Implementation 为Issue和Detector提供映射关系，Detector就是当前Detector。声明扫描检测的范围Scope，Scope用来描述Detector
                      需要分析时需要考虑的文件集，包括：Resource文件或目录、Java文件、Class文件。

    5 Category概述
     a 系统已有12种类别
     b 支持自定义Category类别，Category.create("命名规范", 101)
       对于自定义lint规则特别实用

    6 IssueRegistry
      a 提供检测的issue列表
      b 在build.gradle 需要配置      jar {
                                        manifest {
                                            attributes("Lint-Registry": "com.lint.core.MyIssueRegistry")
                                        }
                                    }

 */
public class LogDetector extends Detector implements Detector.UastScanner {

    private static final String LOG_CLS = "android.util.Log";
    private static final String SYSTEM_PRINTLN_CLS = "System.out.println";
    private static final String SYSTEM_PRINT_CLS = "System.out.print";

    public static final Issue ISSUE = Issue.create(
            "LogUsage",
            "避免调用android.util.Log/System.out.println----",
            "请使用统一工具类LogUtils----",
            Category.SECURITY, 5, Severity.ERROR,
            new Implementation(LogDetector.class, Scope.JAVA_FILE_SCOPE));
    /***
     * 返回此检测器感兴趣的方法名称列表
     * @return
     */
    @Override
    public List<String> getApplicableMethodNames() {
        return Arrays.asList("v", "d", "i", "w", "e", "wtf", "print", "println" );
    }


    /***
     * 返回访问者返回的AST节点类型 Detector.createJavaVisitor(JavaContext)应该访问
     * @return
     */
    @Override
    public List<Class<? extends PsiElement>> getApplicablePsiTypes() {
        return Collections.singletonList(PsiMethodCallExpression.class);
    }

    /***
     * 为找到的任何匹配getApplicableMethodNames()返回的名称，执行调用
     * @param context
     * @param node
     * @param method
     */
    @Override
    public void visitMethod(JavaContext context, UCallExpression node, PsiMethod method) {
        super.visitMethod(context, node, method);
        /****
         * 1 context.getEvaluator()
         *   可以快速检查给定方法是否是给定类的子类的成员，或者方法是否具有某组参数等。
         *   它还可以轻松检查给定方法是否为私有，抽象或静电，等等
         */
        JavaEvaluator evaluator = context.getEvaluator();
        String message = null;
        if (evaluator.isMemberInClass(method, LOG_CLS)) {
            message = "请使用统一工具类LogUtils，避免使用" + LOG_CLS;
        }else if (evaluator.isMemberInClass(method, SYSTEM_PRINT_CLS)){
            message = "请使用统一工具类LogUtils，避免使用" + SYSTEM_PRINT_CLS;
        }else if (evaluator.isMemberInClass(method, SYSTEM_PRINTLN_CLS)){
            message = "请使用统一工具类LogUtils，避免使用" + SYSTEM_PRINTLN_CLS;
        }
        if (message != null){
            context.report(ISSUE, node, context.getLocation(node), message);
        }
    }

    //    /***
//     * 创建解析树访问者
//     * 所有的Detector.JavaPsiScanner探测器都必须提供访问者，除非appliesToResourceRefs()返回true，或者getApplicableMethodNames()返回非null
//     * @param context
//     * @return
//     */
//    @Override
//    public JavaElementVisitor createPsiVisitor(JavaContext context) {
//        return new JavaElementVisitor() {
//            @Override
//            public void visitMethod(PsiMethod method) {
//                super.visitMethod(method);
//                if (method.getText().startsWith(SYSTEM_PRINTLN_CLS)){
//                    context.report(ISSUE, method, context.getLocation(method), "请使用统一工具类LogUtils，避免使用" + SYSTEM_PRINTLN_CLS);
//                    return;
//                }
//            }
//        };
//    }

    @Override
    public boolean appliesToResourceRefs() {
        return super.appliesToResourceRefs();
    }
}