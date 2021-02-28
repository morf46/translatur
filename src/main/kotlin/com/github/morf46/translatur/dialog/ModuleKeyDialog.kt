package org.jetbrains.plugins.template.dialog

import com.intellij.openapi.components.service
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JComponent
import com.intellij.openapi.ui.DialogWrapper
import org.jetbrains.plugins.template.services.MyApplicationService
import javax.swing.JTextField


class ModuleKeyDialog : DialogWrapper(true) {

    var txt:JTextField = JTextField("")

    override fun createCenterPanel(): JComponent? {
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