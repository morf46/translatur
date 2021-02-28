package com.github.morf46.translatur.services

import com.github.morf46.translatur.MyBundle

class MyApplicationService {

    init {
        println(MyBundle.message("applicationService"))
    }

    public var translationKey: String = "moduleSetup"
}
