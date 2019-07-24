package com.integration.gradle

import com.android.build.api.dsl.model.BuildType
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.dsl.LintOptions
import com.android.build.gradle.tasks.LintGlobalTask
import org.apache.commons.io.IOUtils
import org.gradle.api.DomainObjectCollection
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.tasks.TaskState

/**
 * @author 作者：codeman
 * @date 创建时间：2019/3/8
 * @Description 描述：
 * a 统一管理lint.xml和lintOptions，已实现
 * b 并且自动添加自定义lint依赖，已实现
 *
 * 适配gradle3.0以及以上
 * gradle提供了 lintChecks project(":lintrules")引入自定义lint
 */
class LintAutoPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.add("lintAutoInfo", LintAutoExtension)
        applyTask(project, getAndroidVariants(project))
    }

    private DomainObjectCollection<BuildType> getAndroidVariants(Project project) {
        if (project.getPlugins().hasPlugin(AppPlugin)) {
            return project.getPlugins().getPlugin(AppPlugin).extension.getBuildTypes()
        }

        if (project.getPlugins().hasPlugin(LibraryPlugin)) {
            return project.getPlugins().getPlugin(LibraryPlugin).extension.getBuildTypes()
        }

        //throw new GradleException("目前只支持 android和android-library插件配置过的")
    }

    private void applyTask(Project project, DomainObjectCollection<BuildType> variants) {

        println "1--------自定义lint插件开始 ----- $project"

        if (variants == null) {
            return
        }

        def lintTaskExists = false

        // --- 统一 自动添加lint检测依（使用lintChecks方式） ---
        addLintRely(project)

        println "3-1--------自定义lint插件开始 ----- ${project.tasks}"
        variants.all { variant ->
            //获取Lint task
            def variantName = variant.name.capitalize()
            println "3-2--------自定义lint插件开始 ----- $variantName"
            if (variantName != "Debug") {
                LintGlobalTask lintTask = project.tasks.getByName("lint") as LintGlobalTask
                println "3-3-------- 获取每个model对应的lint任务 ${lintTask.name}"
                File lintFile = project.file("${project.lintAutoInfo.goalXmlPath}")
                File lintOldFile = null

                lintTask.doFirst {
                    //b --- 统一管理 lintOptions ----
                    println("4 ------ 统一管理 lintOptions")
                    manageLintOptions(lintFile, lintTask, project)

                    //c --- 统一lint.xml ---
                    println("5 ------ 统一管理 lint.xml")
                    def isGoalFileExist
                    //c1 判断如果项目中已有lint.xml，则改名为lintOld.xml
                    if (lintFile.exists()) {
                        println("5-1 ------ lintFile = ${lintFile.getAbsolutePath()}")
                        lintOldFile = project.file("${project.lintAutoInfo.goalXmlDir}lintOld.xml")
                        lintFile.renameTo(lintOldFile)
                        //c2 将plugin内置的lint.xml文件和项目下面的lint.xml进行复制合并操作
                        isGoalFileExist = copyLintXml(project, lintOldFile)
                    } else {
                        //c3 说明目标文件不存在，直接把文件转移到model指定目录
                        println("5-2 ------ lint.xml不存在，直接把文件转移到model指定目录")
                        isGoalFileExist = copyToGoalDir(project, "${project.lintAutoInfo.sourceXmlPath}", "${project.lintAutoInfo.goalXmlDir}")
                    }

                    //c3 完成合并后，将lintOld.xml文件名改为lint.xml
                    if (!isGoalFileExist) {
                        if (lintOldFile != null) {
                            lintOldFile.renameTo(lintFile)
                        }
                        throw new GradleException("lint.xml不存在")
                    }
                }

                //c4 任务执行结束后删除lint.xml
                project.gradle.taskGraph.afterTask { task, TaskState state ->
                    if (task == lintTask) {
                        lintFile.delete()
                        if (lintOldFile != null) {
                            lintOldFile.renameTo(lintFile)
                        }
                    }
                }

                //d --- 在终端执行命令gradlew lintConfigTask  ---
                if (!lintTaskExists) {
                    lintTaskExists = true
                    project.task("lintConfigTask").dependsOn lintTask
                }
            }
        }
    }

    private void manageLintOptions(File lintFile, LintGlobalTask lintTask, Project project) {
        /***
         * b1 lint会把project下的lint.xml和lintConfig指定的lint.xml进行合并，
         * 为了确保只执行插件中的规则，采取此策略
         */
        //b1 统一 lintOptions 开始
        def newLintOptions = new LintOptions()
        newLintOptions.warningsAsErrors = false
        newLintOptions.lintConfig = lintFile
        newLintOptions.abortOnError = false
        newLintOptions.htmlReport = true
        newLintOptions.htmlOutput = project.file("${project.lintAutoInfo.goalXmlDir}lint-report.html")
        newLintOptions.xmlReport = true
        newLintOptions.xmlOutput = project.file("${project.lintAutoInfo.goalXmlDir}lint-report.xml")
        //b2 替换默认lint规则为自定义
        lintTask.lintOptions = newLintOptions
    }

    /***
     * 自动添加lint依赖
     * @param project
     */
    private void addLintRely(Project project) {
        def lintDependencies = ":lintjar"
        project.dependencies {
            //如果是android application和library
            if (project.getPlugins().hasPlugin(AppPlugin)
                    || project.getPlugins().hasPlugin(LibraryPlugin)) {
                println("2-1 ---- lintDependencies = $lintDependencies")
                lintChecks project.project("$lintDependencies")
            }
        }
    }

    private boolean copyLintXml(Project project, File oldFile) {
        println("5-1-0 ------ oldFile = ${oldFile.getAbsolutePath()}")
        if (oldFile == null) {
            return false
        }

        File goalFile = project.file("${project.lintAutoInfo.goalXmlDir}lint.xml")
        println("5-1-1 ------ goalFile = ${goalFile.getAbsolutePath()}")
        if (!goalFile.parentFile.exists()) {
            goalFile.parentFile.mkdirs()
        }
        if (!goalFile.exists()) {
            goalFile.createNewFile()
        }
        println("5-1-2 ------ goalFile.parentFile = ${goalFile.getAbsolutePath()}")
        File sourceFile = project.getParent().file("${project.lintAutoInfo.sourceXmlPath}")
        println("5-1-3 ------ sourceFile = ${sourceFile.getAbsolutePath()}")

        InputStream sourceLintStream = new FileInputStream(sourceFile.getAbsolutePath())
        InputStream oldStream = new FileInputStream(oldFile)
        OutputStream outputStream = new FileOutputStream(goalFile)

        int retroPluginVersion = getRetrolPluginVersion(project)
        println("5-1-4 ------ retroPluginVersione = $retroPluginVersion")
        if (retroPluginVersion >= 180) {
            //加入屏蔽try with resource检测，1.8.0引入该功能
            //InputStream retrolLintStream = this.class.getResourceAsStream("/config/retro_lint.xml")
            File innerLintFile = project.file("${project.lintAutoInfo.sourceXmlPath}")
            println("5-1-5 ------ innerLintFile = ${innerLintFile.getAbsolutePath()}")
            XMLMergeUtil.merge(oldFile.getPath(), innerLintFile.getPath())
        } else {
            println("5-1-6 ------ lintStream = $sourceLintStream")
            println("5-1-7 ------ oldStream = $oldStream")
            println("5-1-8 ------ outputStream = $outputStream")
            try {
                IOUtils.copy(sourceLintStream, outputStream)
                IOUtils.copy(oldStream, outputStream)
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                IOUtils.closeQuietly(outputStream)
                IOUtils.closeQuietly(sourceLintStream)
                IOUtils.closeQuietly(oldStream)
            }
        }

        return oldFile.exists()
    }

    /***
     * 获取使用的 RetroLambda Plugin版本
     * @param project
     * @return
     */
    int getRetrolPluginVersion(Project project) {
        DefaultExternalModuleDependency retroLambdaPlugin = findClassPathDependencyVersion(project,
                'me.tatarka', 'gradle-retrolambda') as DefaultExternalModuleDependency
        if (retroLambdaPlugin == null) {
            retroLambdaPlugin == findClassPathDependencyVersion(project.getRootProject(),
                    'me.tatarka', 'gradle-retrolambda') as DefaultExternalModuleDependency
        }
        if (retroLambdaPlugin == null) {
            return -1
        }

        return retroLambdaPlugin.version.split("-")[0].replaceAll("\\.", "").toInteger();
    }

    DefaultExternalModuleDependency findClassPathDependencyVersion(Project project, group, attributeId) {
        return project.buildscript.configurations.classpath.dependencies.find {
            it.group != null && it.group.equals(group) && it.name.equals(attributeId)
        }
    }

    /***
     * copy 文件到指定目录
     * @param project
     * @param sourcePath
     * @param localDir
     */
    private boolean copyToGoalDir(Project project, String sourcePath, String localDir) {
        File sourceFile = project.getParent().file(sourcePath)
        File goalFile = project.file(localDir)
        if (!goalFile.exists()) {
            goalFile.mkdirs()
        }
        if (!sourceFile.exists()) {
            throw new GradleException("$sourcePath 不存在")
        }
        project.copy {
            from sourceFile.getAbsolutePath()
            into localDir
        }

        return true
    }

}
