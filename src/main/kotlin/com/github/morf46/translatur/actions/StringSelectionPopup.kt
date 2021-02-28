package com.github.morf46.translatur.actions

import com.intellij.codeInsight.template.impl.SurroundWithTemplateHandler.createActionGroup
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiFile
import java.util.HashSet
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.ui.awt.RelativePoint

class StringSelectionPopup: AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
        val psiFile: PsiFile = event.getRequiredData(CommonDataKeys.PSI_FILE)
        val group = DefaultActionGroup()

        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            null,
            group,
            SimpleDataContext.getProjectContext(null),
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            false
        )
    }
}