package org.jetbrains.plugins.template.actions

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import org.jetbrains.plugins.template.services.MyApplicationService

class CategoryActionGroup : ActionGroup() {
    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val applicationService = service<MyApplicationService>()
        return arrayOf(
            SetCategoryAction("moduleSetup"),
            SetCategoryAction("campaignList"),
            SetCategoryAction("global"),
            SetCategoryAction("moduleList")
        )
    }
}