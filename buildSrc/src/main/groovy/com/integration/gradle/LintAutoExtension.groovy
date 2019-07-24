package com.integration.gradle

/**
 * @author 作者：codeman
 * @date 创建时间：2019/3/21
 * @Description 描述：
 */
class LintAutoExtension {
    def sourceJarPath = "buildSrc/config/lintjar.jar"
    def sourceXmlPath = "buildSrc/config/lint.xml"
    def goalJarDir = "~/.android/lint/"
    def goalXmlDir = "reports/"
    def goalXmlPath = "${goalXmlDir}lint.xml"
}
