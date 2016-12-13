package com.cedarsoftware.ncubecli

import groovy.transform.CompileStatic
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.shell.plugin.support.DefaultBannerProvider
import org.springframework.shell.support.util.OsUtils
import org.springframework.stereotype.Component

@CompileStatic

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class Banner extends DefaultBannerProvider {
    @Override
    String getBanner() {
        // GAIG DevOps
        StringBuffer sb = new StringBuffer()
        sb.append("   ___     _     ___    ___     ___                 ___              " + OsUtils.LINE_SEPARATOR)
        sb.append("  / __|   /_\\   |_ _|  / __|   |   \\   ___  __ __  / _ \\   _ __   ___" + OsUtils.LINE_SEPARATOR)
        sb.append(" | (_ |  / _ \\   | |  | (_ |   | |) | / -_) \\ V / | (_) | | '_ \\ (_-<" + OsUtils.LINE_SEPARATOR)
        sb.append("  \\___| /_/ \\_\\ |___|  \\___|   |___/  \\___|  \\_/   \\___/  | .__/ /__/" + OsUtils.LINE_SEPARATOR)
        sb.append("                                                          |_|        " + OsUtils.LINE_SEPARATOR)

        return sb.toString()
    }

    @Override
    String getWelcomeMessage() {
        return "Welcome to our utility! Type 'help' and hit ENTER for a list of commands."
    }
}
