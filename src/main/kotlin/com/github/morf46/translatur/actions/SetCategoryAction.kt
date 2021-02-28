package com.github.morf46.translatur.actions

import com.github.morf46.translatur.services.MyApplicationService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.util.NlsActions
import org.jetbrains.annotations.Nullable

class SetCategoryAction(text: @Nullable @NlsActions.ActionText String?) : AnAction(text) {


    override fun actionPerformed(e: AnActionEvent) {
        val text = e.presentation.text
        val applicationService = service<MyApplicationService>()
        applicationService.translationKey = text
    }
}