package com.cedarsoft.ncubecli.api

import groovy.transform.CompileStatic
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.expression.ParseException
import org.springframework.shell.Bootstrap
import org.springframework.shell.core.CommandResult
import org.springframework.shell.core.JLineShellComponent

@CompileStatic

class CommandTest {
    private static JLineShellComponent shell

    @BeforeClass
    static void startUp() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
        shell = bootstrap.getJLineShellComponent()
    }

    @AfterClass
    static void shutdown() {
        shell.stop()
    }

    static JLineShellComponent getShell() {
        return shell
    }
}