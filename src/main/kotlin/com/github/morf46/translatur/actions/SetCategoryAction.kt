package org.jetbrains.plugins.template.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.util.NlsActions
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.template.dialog.ModuleKeyDialog
import org.jetbrains.plugins.template.services.MyApplicationService

class SetCategoryAction(text: @Nullable @NlsActions.ActionText String?) : AnAction(text) {


    override fun actionPerformed(e: AnActionEvent) {
        val text = e.presentation.text
        val applicationService = service<MyApplicationService>()
        applicationService.translationKey = text
    }
}