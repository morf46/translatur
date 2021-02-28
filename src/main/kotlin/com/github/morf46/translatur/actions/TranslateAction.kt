package org.jetbrains.plugins.template.actions


import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.file.PsiDirectoryFactory
import org.apache.commons.io.FileUtils
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.template.dialog.ModuleKeyDialog
import org.jetbrains.plugins.template.services.MyApplicationService
import java.io.File


import com.intellij.psi.PsiFile
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.random.Random


class TranslateAction : AnAction() {
    override fun update(event: AnActionEvent) {
        val currentProject: @Nullable Project? = event.project;
        val editor: @Nullable Editor? = event.getData(CommonDataKeys.EDITOR);

        val bShow = currentProject != null
                && editor != null
                && editor.selectionModel.hasSelection();

        event.presentation.isEnabledAndVisible = bShow;

    }

    override fun actionPerformed(event: AnActionEvent) {
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
        val psiFile: PsiFile = event.getRequiredData(CommonDataKeys.PSI_FILE)
        val document: Document = editor.document
        val applicationService = service<MyApplicationService>()

        val primaryCaret: Caret = editor.caretModel.primaryCaret
        val start: Int = primaryCaret.selectionStart
        val end: Int = primaryCaret.selectionEnd
        var translationKey = "tranlation_key_" + Random.nextInt(1000, 9999)

        val selectionString: String = document.getText(TextRange(start, end));
        val preparedSelection: String = removeQuotes(selectionString)

        val sqlFile: @Nullable VirtualFile = getOrCreateFile(project)

        val translate: Translate? = TranslateOptions.newBuilder().setCredentials(
            ServiceAccountCredentials.fromStream(
                this.javaClass.getResourceAsStream("/auth/gcloud-morf.json")
            )
        ).build().service

        val translation: Translation = translate!!.translate(
            preparedSelection,
            Translate.TranslateOption.sourceLanguage("de"),
            Translate.TranslateOption.targetLanguage("en")
        );

        val preparedString: String = translation.translatedText.replace("&quot;", "\"");
        var returnString = ""

        if (psiFile.language.isKindOf(HTMLLanguage.INSTANCE)) {
            returnString =
                String.format("{{\"%s\"}} | i18n : \"%s\"", translationKey, applicationService.translationKey)
        } else {
            returnString = String.format("translations.%s", translationKey)
        }

        WriteCommandAction.runWriteCommandAction(
            project
        ) { document.replaceString(start, end, returnString) }


        if (sqlFile != null) {
            writeToSqlFile(sqlFile, applicationService.translationKey, translationKey, preparedSelection, preparedString)
        }

        primaryCaret.removeSelection()
    }


    private fun writeToSqlFile(
        sqlFile: @Nullable VirtualFile,
        category: String,
        keyName: String,
        original: String,
        translated: String
    ) {
        val file = File(sqlFile.path)
        var content: String = ""

        try {
            content = FileUtils.readFileToString(file, Charsets.UTF_8)
        }catch (exception: FileNotFoundException){

        }

        val baseString: String =
            "INSERT INTO `trbo_masterdata`.`translations` (`category`, `phrase`, `translation_de`, `translation_en`)\n" +
                    "VALUES \n"
        if (content.trim() == "") {
            content = baseString
        }
        if (content.trim().endsWith(";")) {
            content = content.replace(";", ",")
        }

        content += String.format("  ('%s', '%s', '%s', '%s');\n", category, keyName, original, translated)

        FileUtils.write(file, content, Charsets.UTF_8)
    }

    private fun getOrCreateFile(project: Project): @Nullable VirtualFile {
        val findFileByIoFile: @Nullable VirtualFile? =
            LocalFileSystem.getInstance().findFileByIoFile(File(project.basePath + "/TRBO-xxxx.sql"))

        if (findFileByIoFile != null) {
            return findFileByIoFile
        }

        val createFileFromText =
            PsiFileFactory.getInstance(project).createFileFromText("TRBO-xxxx.sql", PlainTextLanguage.INSTANCE, "");
        val guessProjectDir = project.guessProjectDir()

        PsiDirectoryFactory.getInstance(project).createDirectory(guessProjectDir!!).add(createFileFromText)

        val file = File(project.basePath + "/TRBO-xxxx.sql")
        try {
            file.createNewFile()
        }catch (exception : IOException){

        }
        val _findFileByIoFile: @Nullable VirtualFile? =
            LocalFileSystem.getInstance().findFileByIoFile(File(project.basePath + "/TRBO-xxxx.sql"))

        return _findFileByIoFile!!
    }

    private fun removeQuotes(selectionString: String): String {
        var newString: String = selectionString.trim()
        if (newString.startsWith("\"")) {
            newString = newString.substring(1, newString.length);
        }
        if (newString.endsWith("\"")) {
            newString = newString.substring(0, newString.length - 1);
        }
        return newString
    }
}