package com.github.morf46.translatur.actions


import com.github.morf46.translatur.services.MyApplicationService
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.hint.HintManagerImpl.getHintPosition
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.ui.awt.RelativePoint
import org.apache.commons.io.FileUtils
import org.jetbrains.annotations.Nullable
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.swing.event.ListSelectionListener
import kotlin.random.Random


class TranslateAction : AnAction() {
    override fun update(event: AnActionEvent) {
        val currentProject: @Nullable Project? = event.project
        val editor: @Nullable Editor? = event.getData(CommonDataKeys.EDITOR)

        val bShow = currentProject != null
                && editor != null
                && editor.selectionModel.hasSelection()

        event.presentation.isEnabledAndVisible = bShow

    }

    override fun actionPerformed(event: AnActionEvent) {
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
        val psiFile: PsiFile = event.getRequiredData(CommonDataKeys.PSI_FILE)
        val document: Document = editor.document
        val popupLocation = JBPopupFactory.getInstance().guessBestPopupLocation(event.dataContext);
        val applicationService = service<MyApplicationService>()


        val primaryCaret: Caret = editor.caretModel.primaryCaret
        val start: Int = primaryCaret.selectionStart
        val end: Int = primaryCaret.selectionEnd
        var translationKey = "translation_key_" + Random.nextInt(1000, 9999)

        val selectionString: String = document.getText(TextRange(start, end))
        val preparedSelection: String = removeQuotes(selectionString)

        val sqlFile: @Nullable VirtualFile = getOrCreateFile(project,"TRBO-xxxx.sql")
        val csvFile: @Nullable VirtualFile = getOrCreateFile(project,"TRBO-xxxx.csv")
//        val translate: Translate? = TranslateOptions.newBuilder().setCredentials(
//            ServiceAccountCredentials.fromStream(
//                this.javaClass.getResourceAsStream("/auth/gcloud-morf.json")
//            )
//        ).build().service
//
//        val translation: Translation = translate!!.translate(
//            preparedSelection,
//            Translate.TranslateOption.sourceLanguage("de"),
//            Translate.TranslateOption.targetLanguage("en")
//        )

      //  val preparedString: String = translation.translatedText.replace("&quot;", "\"")
        val preparedString: String = preparedSelection.replace("&quot;", "\"")
        val newKey = makeTranslationKey(preparedString)
        translationKey = if (newKey.length > 3)  newKey else translationKey

        val returnString: String = if (psiFile.language.isKindOf(HTMLLanguage.INSTANCE)) {
            String.format("{{\"%s\" | i18n : \"%s\"}}", translationKey, applicationService.translationKey)
        } else {
            String.format("translations.%s", translationKey)
        }

       // showStringSelections(popupLocation)

        WriteCommandAction.runWriteCommandAction(
            project
        ) { document.replaceString(start, end, returnString) }


        writeToSqlFile(sqlFile, applicationService.translationKey, translationKey, preparedSelection, preparedString)
        writeToCsvFile(csvFile, applicationService.translationKey, translationKey, preparedSelection, preparedString)

        primaryCaret.removeSelection()
    }


    private fun writeToSqlFile(
        sqlFile: @Nullable VirtualFile,
        category: String,
        keyName: String,
        original: String,
        translated: String,
    ) {
        val file = File(sqlFile.path)
        var content = ""

        try {
            content = FileUtils.readFileToString(file, Charsets.UTF_8)
        } catch (exception: FileNotFoundException) {

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

    private fun writeToCsvFile(
        sqlFile: @Nullable VirtualFile,
        category: String,
        keyName: String,
        original: String,
        translated: String,
    ) {
        val file = File(sqlFile.path)
        var content = ""

        try {
            content = FileUtils.readFileToString(file, Charsets.UTF_8)
        } catch (exception: FileNotFoundException) {

        }

        val baseString: String =
            "category, phrase, translation_de, translation_en\n";
        if (content.trim() == "") {
            content = baseString
        }

        content += String.format("%s, %s, %s, %s\n", category, keyName, original, translated)

        FileUtils.write(file, content, Charsets.UTF_8)
    }

    private fun getOrCreateFile(project: Project,fileName:String): @Nullable VirtualFile {
        val findFileByIoFile: @Nullable VirtualFile? =
            LocalFileSystem.getInstance().findFileByIoFile(File(project.basePath + "/" + fileName))

        if (findFileByIoFile != null) {
            return findFileByIoFile
        }

        val createFileFromText =
            PsiFileFactory.getInstance(project).createFileFromText(fileName, PlainTextLanguage.INSTANCE, "")
        val guessProjectDir = project.guessProjectDir()

        PsiDirectoryFactory.getInstance(project).createDirectory(guessProjectDir!!).add(createFileFromText)

        val file = File(project.basePath + "/" + fileName)
        try {
            file.createNewFile()
        } catch (exception: IOException) {

        }
        val _findFileByIoFile: @Nullable VirtualFile? =
            LocalFileSystem.getInstance().findFileByIoFile(File(project.basePath + "/"+fileName))

        return _findFileByIoFile!!
    }

    private fun removeQuotes(selectionString: String): String {
        var newString: String = selectionString.trim()
        if (newString.startsWith("\"")) {
            newString = newString.substring(1, newString.length)
        }
        if (newString.endsWith("\"")) {
            newString = newString.substring(0, newString.length - 1)
        }
        return newString
    }

    private fun makeTranslationKey(translatedText: String): String {
        val txt = translatedText.trim()
        val list = txt.split(" ")
        return list.joinToString(
            "_",
            "",
            "",
            4,
            "",
            transform = { it.toLowerCase() }
        )
    }

    private fun showStringSelections(location:RelativePoint){
     /*   val group = DefaultActionGroup()
        val popup  = JBPopupFactory.getInstance().createActionGroupPopup(
            null,
            group,
            SimpleDataContext.getProjectContext(null),
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            false
        )
        /*val popup  = JBPopupFactory.getInstance().createListPopup(
            ListPopupStep<String>("asd"),
            5
        )*/
popup.addListSelectionListener(ListSelectionListener {

})
        popup.show(location)*/
    }
}