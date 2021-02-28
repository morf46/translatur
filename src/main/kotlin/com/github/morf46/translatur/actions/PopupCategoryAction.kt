package com.github.morf46.translatur.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.template.dialog.ModuleKeyDialog

class PopupCategoryAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val moduleKeyDialog = ModuleKeyDialog()
        if (moduleKeyDialog.showAndGet()) {

        }
    }

}