package com.cedarsoftware.ncubecli

import groovy.transform.CompileStatic
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.shell.plugin.support.DefaultPromptProvider
import org.springframework.stereotype.Component

@CompileStatic

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class Prompt extends DefaultPromptProvider {
    @Override
    String getPrompt(){
        return "n-cube-cli> "
    }
    @Override
    String getProviderName() {
        return "GAIG"
    }    // TODO: what to put?
}
