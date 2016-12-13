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
        StringBuffer sb = new StringBuffer()
        sb.append("                                         ▄▄                                      ▄▄▄▄         ██   " + OsUtils.LINE_SEPARATOR)
        sb.append("                                         ██                                      ▀▀██         ▀▀   " + OsUtils.LINE_SEPARATOR)
        sb.append(" ██▄████▄             ▄█████▄  ██    ██  ██▄███▄    ▄████▄              ▄█████▄    ██       ████   " + OsUtils.LINE_SEPARATOR)
        sb.append(" ██▀   ██            ██▀    ▀  ██    ██  ██▀  ▀██  ██▄▄▄▄██            ██▀    ▀    ██         ██   " + OsUtils.LINE_SEPARATOR)
        sb.append(" ██    ██   █████    ██        ██    ██  ██    ██  ██▀▀▀▀▀▀   █████    ██          ██         ██   " + OsUtils.LINE_SEPARATOR)
        sb.append(" ██    ██            ▀██▄▄▄▄█  ██▄▄▄███  ███▄▄██▀  ▀██▄▄▄▄█            ▀██▄▄▄▄█    ██▄▄▄   ▄▄▄██▄▄▄" + OsUtils.LINE_SEPARATOR)
        sb.append(" ▀▀    ▀▀              ▀▀▀▀▀    ▀▀▀▀ ▀▀  ▀▀ ▀▀▀      ▀▀▀▀▀               ▀▀▀▀▀      ▀▀▀▀   ▀▀▀▀▀▀▀▀" + OsUtils.LINE_SEPARATOR)

        return sb.toString()
    }

    @Override
    String getWelcomeMessage() {
        return "Type 'help' and hit ENTER for a list of commands."
    }
}