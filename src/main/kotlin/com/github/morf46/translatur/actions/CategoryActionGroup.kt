package com.github.morf46.translatur.actions

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CategoryActionGroup : ActionGroup() {
    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return arrayOf(
            SetCategoryAction("moduleSetup"),
            SetCategoryAction("campaignList"),
            SetCategoryAction("global"),
            SetCategoryAction("moduleList")
        )
    }
}