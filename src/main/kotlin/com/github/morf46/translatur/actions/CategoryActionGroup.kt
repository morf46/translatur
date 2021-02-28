package org.jetbrains.plugins.template.actions

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