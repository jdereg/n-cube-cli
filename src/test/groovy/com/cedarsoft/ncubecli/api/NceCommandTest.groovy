package com.cedarsoft.ncubecli.api

import groovy.transform.CompileStatic
import org.junit.*
import org.springframework.expression.ParseException
import org.springframework.shell.Bootstrap
import org.springframework.shell.core.CommandResult
import org.springframework.shell.core.JLineShellComponent

/* Created by dben and ihiggins */

@CompileStatic

class NceCommandTest {
    private static JLineShellComponent shell
    final private String TEST_USER     = "XXXXXXXXXX"
    final private String TEST_PASSWORD = "XXXXXXXXXX"

    final private String test_app       = "Devops.Test"
    final private String test_branch    = "unit.test"
    final private String test_branch_to = "unit.test.dev"

    @Before
    void startUp() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
        shell = bootstrap.getJLineShellComponent()
    }

    @After
    void shutdown() {
        shell.stop()
    }

    static JLineShellComponent getShell() {
        return shell
    }

    @Test
    @Ignore
    void nceAuthTest() throws ParseException {
        //Execute command
        CommandResult cr = getShell().executeCommand(String.format("nce-auth --u %s --p %s", TEST_USER, TEST_PASSWORD))

        System.out.println(cr.getResult())
        Assert.assertTrue("Command should be successful", cr.isSuccess())

    }

    @Test
    @Ignore
    void nceGetAppVersionTest() throws ParseException {
        //Execute command
        CommandResult cr = getShell().executeCommand(String.format("nce-auth --u %s --p %s", TEST_USER, TEST_PASSWORD))

        System.out.println(cr.getResult())
        Assert.assertTrue("Command should be successful", cr.isSuccess())

        cr = getShell().executeCommand(String.format("nce-getappversion --c %s --app %s", cr.getResult(), test_app))
        System.out.println(cr.getResult())
        Assert.assertTrue("Command should be successful", cr.isSuccess())
    }

    @Test
    @Ignore
    void nceCopyAndDeleteBranchTest() throws ParseException {
        //Execute command
        CommandResult cr = getShell().executeCommand(String.format("nce-auth --u %s --p %s", TEST_USER, TEST_PASSWORD))
        System.out.println(cr.getResult())
        String cookie = (String)cr.getResult()
        Assert.assertTrue("Command should be successful", cr.isSuccess())

        cr = getShell().executeCommand(String.format("nce-getappversion --cookie %s --app %s", cookie, test_app))
        String version = (String)cr.getResult()
        Assert.assertTrue("Command should be successful", cr.isSuccess())

        cr = getShell().executeCommand(String.format("nce-deleteandcopybranch --c %s --app %s --version %s --branch-from %s --branch-to %s",
                cookie, test_app, version, test_branch, test_branch_to))
        System.out.println(cr.getResult())
        Assert.assertTrue("Command should be successful", cr.isSuccess())

        //delete the branch copied to so that NCE is reset back to the way it was
        cr = getShell().executeCommand(String.format("nce-deletebranch --c %s --app %s --version %s --branch %s",
                cookie, test_app, version, test_branch_to))
        System.out.println(cr.getResult())
        Assert.assertTrue("Command should be successful", cr.isSuccess())
    }

    @Test
    @Ignore
    void testNCEReleaseVersion() throws ParseException {
        //Execute command
        CommandResult cr = getShell().executeCommand(String.format("nce-auth --u %s --p %s", TEST_USER, TEST_PASSWORD))
        System.out.println(cr.getResult())
        String cookie = (String)cr.getResult()
        Assert.assertTrue("auth should be successful", cr.isSuccess())

        cr = getShell().executeCommand(String.format("nce-getappversion --cookie %s --app %s", cookie, test_app))
        String version = (String)cr.getResult()
        Assert.assertTrue("getappversion should be successful", cr.isSuccess())

        cr = getShell().executeCommand(String.format("nce-releaseversion --c %s --app %s --v %s --minor", cookie, test_app, version))
        System.out.println(cr.getResult())
        Assert.assertTrue("Release should be successful", cr.isSuccess())

        cr = getShell().executeCommand(String.format("nce-getappversion --cookie %s --app %s", cookie, test_app))
        String newVersion = (String)cr.getResult()
        Assert.assertTrue("getappversion should be successful", cr.isSuccess())
        Assert.assertNotSame("Versions should be different", version, newVersion)

    }

    @Test
    @Ignore
    void testNCEUpdateSysBootstrapInfo() throws ParseException {
        //Execute command
        CommandResult cr = getShell().executeCommand(String.format("nce-auth --u %s --p %s", TEST_USER, TEST_PASSWORD))
        System.out.println(cr.getResult())
        String cookie = (String)cr.getResult()
        Assert.assertTrue("auth should be successful", cr.isSuccess())

        cr = getShell().executeCommand(String.format("nce-getappversion --cookie %s --app %s", cookie, test_app))
        String version = (String)cr.getResult()
        Assert.assertTrue("getappversion should be successful", cr.isSuccess())

        cr = getShell().executeCommand(String.format("nce-updatesysbootinfo --c %s --app %s --v %s --environment %s", cookie, test_app, version, "CERT"))
        System.out.println(cr.getResult())
        Assert.assertTrue("Update should be successful", cr.isSuccess())
        Assert.assertTrue("Update result should be true, check log for error", (Boolean)cr.getResult())

        cr = getShell().executeCommand(String.format("nce-updatesysbootinfo --c %s --app %s --v %s --environment %s", cookie, test_app, version.replace("SNAPSHOT", "TEST"), "CERT"))
        System.out.println(cr.getResult())
        Assert.assertTrue("Update should be successful", cr.isSuccess())
        Assert.assertTrue("Update result should be true, check log for error", (Boolean)cr.getResult())
    }

    @Test
    @Ignore
    void testNCEUpdateCube() throws ParseException {
        //Execute command
        CommandResult cr = getShell().executeCommand(String.format("nce-auth --u %s --p %s", TEST_USER, TEST_PASSWORD))
        System.out.println(cr.getResult())
        String cookie = (String)cr.getResult()
        Assert.assertTrue("auth should be successful", cr.isSuccess())

        cr = getShell().executeCommand(String.format("nce-getappversion --cookie %s --app %s", cookie, test_app))
        String version = (String)cr.getResult()
        Assert.assertTrue("getappversion should be successful", cr.isSuccess())

        cr = getShell().executeCommand(String.format("nce-updatecube --c %s --app %s --v %s --branch %s --axes-info %s --cube %s --value %s"
                , cookie, test_app, "0.0.0-SNAPSHOT", test_branch, "env=CERT,param=version", "sys.bootstrap.info", version))
        System.out.println(cr.getResult())
        Assert.assertTrue("Update should be successful", cr.isSuccess())
        Assert.assertTrue("Update result should be true, check log for error", (Boolean)cr.getResult())

        cr = getShell().executeCommand(String.format("nce-updatecube --c %s --app %s --v %s --branch %s --axes-info %s --cube %s --value %s"
                , cookie, test_app, "0.0.0-SNAPSHOT", test_branch, "env=CERT,param=version", "sys.bootstrap.info", version.replace("SNAPSHOT", "TEST")))
        System.out.println(cr.getResult())
        Assert.assertTrue("Update should be successful", cr.isSuccess())
        Assert.assertTrue("Update result should be true, check log for error", (Boolean)cr.getResult())
    }
}
