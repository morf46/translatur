package org.jetbrains.plugins.template.dialog

import com.github.morf46.translatur.services.MyApplicationService
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField


class ModuleKeyDialog : DialogWrapper(true) {

    var txt:JTextField = JTextField("")

    override fun createCenterPanel(): JComponent {
        val applicationService = service<MyApplicationService>()
        val dialogPanel = JPanel(BorderLayout())
        txt = JTextField(applicationService.translationKey)
        dialogPanel.add(txt,BorderLayout.CENTER)
        return dialogPanel
    }

    override fun doOKAction() {
        super.doOKAction()
        val applicationService = service<MyApplicationService>()
        applicationService.translationKey = txt.text
    }


    init {
        init()
        title = "Test DialogWrapper"
    }
}