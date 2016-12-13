package com.cedarsoftware.ncubecli.commands

import com.cedarsoftware.ncube.NCubeInfoDto
import com.cedarsoftware.util.io.JsonReader
import com.cedarsoftware.util.io.JsonWriter
import com.cedarsoftware.api.NCubeEditorApi
import com.gaic.bue.devops.api.UpdatePomApi
import com.cedarsoftware.api.VersionType
import org.apache.http.cookie.Cookie
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.stereotype.Component
import org.w3c.dom.Document
import org.xml.sax.SAXException

import javax.xml.parsers.ParserConfigurationException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Component
class NCECommands implements CommandMarker {
    private final Logger log = LogManager.getLogger(NCECommands.class)
    private NCubeEditorApi nce
    private final String SANDBOX = "sandbox"
    private final String HEAD = "HEAD"
    private final String SYS_VERSIONS = "sys.versions"
    private final String SYS_BOOTSTRAP_INFO = "sys.bootstrap.info"
    private final String _000_VERSION = "0.0.0-SNAPSHOT"

    NCECommands(){
        nce = new NCubeEditorApi()
    }

    private void addCookieFromString(String cookieStr){
        Cookie cookie =  (BasicClientCookie)JsonReader.jsonToJava(cookieStr)
        nce.getCookies().addCookie(cookie)
    }

    @CliCommand(value = "nce-auth", help = "Authenticates to NCE with the given user information.")
    String authenticate(
            @CliOption(key = [ "user", "u" ], mandatory = true, help = "Enter your username") final String user,
            @CliOption(key = [ "pass", "p" ], mandatory = true, help = "Enter your password") final String pass){

        nce.authenticate(user, pass)

        String output = JsonWriter.objectToJson(
                nce.getCookies().getCookies().stream().filter {
                    c -> c.getName().equals("SMSESSION")
                }.findFirst().get())
        System.out.println(output)
        return output
    }

    @CliCommand(value = "nce-getappversion", help = "Returns the application version from NCE of the provided application.")
    String getVersion(
            @CliOption(key = [ "cookie", "c" ], mandatory = true, help = "Enter your username") final String cookieStr,
            @CliOption(key = [ "app" ], mandatory = true, help = "Enter the application name") final String appName){

        addCookieFromString(cookieStr)
        String version = nce.getLatestVersion(appName)
        log.info("Version number: "+version)

        System.out.println(version)
        return version
    }

    @CliCommand(value = "nce-copybranch", help = "Copies a branch from the given version of the provided application.")
    Boolean copyBranch(
            @CliOption(key = [ "cookie", "c" ], mandatory = true, help = "Enter your username") final String cookieStr,
            @CliOption(key = [ "app" ], mandatory = true, help = "Enter the application name") final String appName,
            @CliOption(key = [ "version", "v" ], mandatory = true, help="Enter the version of the application") final String version,
            @CliOption(key = [ "branch-from", "brf" ], mandatory = true, help = "Enter the name of the branch being copied from") final String branchFrom,
            @CliOption(key = [ "branch-to", "brt" ], mandatory = true, help = "Enter the name of the branch being copied to") final String branchTo){

        addCookieFromString(cookieStr)
        Boolean isLocked = nce.isAppLocked(appName)
        if(isLocked){
            //don't do anything if it is already locked
            String error = "ERROR: Application is locked - Cannot copy branch."
            System.out.println(error)
            throw new RuntimeException(error)
        }

        Boolean success = nce.copyBranch(appName,branchFrom,branchTo,version, version)

        System.out.println(success)
        return success
    }

    @CliCommand(value = "nce-deletebranch", help = "Deletes a branch from the given version of the provided application.")
    Boolean deleteBranch(
            @CliOption(key = [ "cookie", "c" ], mandatory = true, help = "Enter your username") final String cookieStr,
            @CliOption(key = [ "app" ], mandatory = true, help = "Enter the application name") final String appName,
            @CliOption(key = [ "version", "v" ], mandatory = true, help="Enter the version of the application") final String version,
            @CliOption(key = [ "branch", "br" ], mandatory = true, help = "Enter the name of the branch being copied from") final String branch){

        addCookieFromString(cookieStr)
        Boolean isLocked = nce.isAppLocked(appName)
        if(isLocked){
            //don't do anything if it is already locked
            String error = "ERROR: Application is locked - Cannot Delete Branch"
            System.out.println(error)
            throw new RuntimeException(error)
        }

        Boolean success = nce.deleteBranch(appName,branch,version)

        System.out.println(success)
        return success
    }

    @CliCommand(value = "nce-deleteandcopybranch", help = "Copies a branch from the given version of the provided application after deleting any existing copy first.")
    Boolean deleteAndCopyBranch(
            @CliOption(key = ["cookie", "c"], mandatory = true, help = "Enter your username") final String cookieStr,
            @CliOption(key = ["app"], mandatory = true, help = "Enter the application name") final String appName,
            @CliOption(key = ["version", "v"], mandatory = true, help="Enter the version of the application") final String version,
            @CliOption(key = ["branch-from", "brf"], mandatory = true, help = "Enter the name of the branch being copied from") final String branchFrom,
            @CliOption(key = ["branch-to", "brt"], mandatory = true, help = "Enter the name of the branch being copied to") final String branchTo){

        addCookieFromString(cookieStr)
        Boolean isLocked = nce.isAppLocked(appName)
        if(isLocked){
            //don't do anything if it is already locked
            String error = "ERROR: Application is locked - Cannot Delete or Copy the Branch"
            System.out.println(error)
            throw new RuntimeException(error)
        }

        Boolean success = nce.deleteBranch(appName, branchTo, version)

        if (success){
            log.info("Delete was successful, copying branch ...")
            success = nce.copyBranch(appName,branchFrom,branchTo,version,version)
        }

        if(success){
            log.info("Copy was successful!")
        }else{
            log.warn("Delete/Copy of branch failed!")
        }

        System.out.println(success)
        return success
    }

    /**
     * Test it: $> java -jar crt-0.0.9-SNAPSHOT-jar-with-dependencies.jar update-poms --minor --pom-file ..\\src\\test\\resources\\poms\\ci-pom-before.xml
     * @param paths
     * @param major
     * @param minor
     * @param patch
     * @return
     */
    @CliCommand(value = "update-poms", help = "Performs a minor upgrade to pom versions with respect to properties and dependencies")
    Boolean updatePoms(
            @CliOption(key = ["pom-file"], mandatory = true, help = "Paths to the pom filenames, comma separated") final String[] paths,
            @CliOption(key = ["major-release", "major"], mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help="Specify if this is a major release") final Boolean major,
            @CliOption(key = ["minor-release", "minor"], mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help="Specify if this is a minor release") final Boolean minor,
            @CliOption(key = ["patch-release", "patch"], mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help="Specify if this is a patch release") final Boolean patch) {
        boolean success = true

        VersionType versionType = VersionType.MINOR
        if(major){
            versionType = VersionType.MAJOR
        }else if (minor){
            versionType = VersionType.MINOR
        }else if(patch){
            versionType = VersionType.PATCH
        }

        List<Document> pomFiles = new ArrayList<>()
        Arrays.asList(paths).stream().each { fileName ->
            try {
                Document pomFile = UpdatePomApi.loadXml(fileName)
                pomFiles.add(pomFile)
            } catch (ParserConfigurationException e) {
                System.out.println("ERROR: Unable to parse the XML file. " + e.getMessage())
            } catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage())
            } catch (SAXException e) {
                System.out.println("ERROR: " + e.getMessage())
            }
        }
        success = paths.length == pomFiles.size()
        if(success){
            UpdatePomApi.updatePoms(pomFiles, versionType)
            pomFiles.each { Document xmlDoc ->
                UpdatePomApi.saveXml(xmlDoc)
            }
        }
        return success
    }

    @CliCommand(value = "nce-releaseversion", help = "Release a given version of an NCE application")
    Boolean releaseVersion(
            @CliOption(key = ["cookie", "c"], mandatory = true, help = "Enter your username") final String cookieStr,
            @CliOption(key = ["app"], mandatory = true, help = "Enter the application name") final String appName,
            @CliOption(key = ["version", "v"], mandatory = true, help="Enter the version of the application") final String version,
            @CliOption(key = ["major-release", "major"], mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help="Specify if this is a major release") final Boolean major,
            @CliOption(key = ["minor-release", "minor"], mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help="Specify if this is a minor release") final Boolean minor,
            @CliOption(key = ["patch-release", "patch"], mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help="Specify if this is a patch release") final Boolean patch){

        VersionType versionType = VersionType.MINOR
        if(major){
            versionType = VersionType.MAJOR
        }else if (minor){
            versionType = VersionType.MINOR
        }else if(patch){
            versionType = VersionType.PATCH
        }

        final String newVersion = nce.incrementVersion(version, versionType)
        addCookieFromString(cookieStr)
        Boolean isLocked = nce.isAppLocked(appName)
        Boolean success = false
        String versionAfter, errorMessage
        Integer afterCount, beforeBranchCount, afterBranchCount

        if(isLocked){
            //don't do anything if it is already locked
            errorMessage = "ERROR: Application is locked! Cannot start release process."
            System.out.println(errorMessage)
            throw new RuntimeException(errorMessage)
        }

        Integer beforeCount = nce.getCubeCount(appName, version)
        isLocked = nce.lockApp(appName, version, true)
        if(isLocked){
            //get all of the branches
            List<Object> branches = nce.getBranches(appName, version)
            final AtomicInteger moving = new AtomicInteger(0)
            final int count = branches.size() - 1 //Subtract 1 for HEAD
            beforeBranchCount = count +1
            //call moveBranch for each branch (except HEAD)
            branches.stream()
                .filter{ br -> !"HEAD".equals(br) }
                .each{ br ->
                    log.info("Moving branch "+ br +" Number: "+ moving.incrementAndGet() + " of "+ count)
                    nce.moveBranch(appName, version, (String)br, newVersion)
                }

            //release the version
            success = nce.releaseVersion(appName,version, newVersion)

            if(success){
                versionAfter = nce.getLatestVersion(appName)
                success = nce.copyBranch(appName, HEAD, HEAD, nce.removeReleaseTypeFromVersion(version), versionAfter)
                log.info("Copied HEAD from just released version. Did it work? "+success)
                if(!success){
                    errorMessage = "ERROR: Error Copying HEAD from released version to new SNAPSHOT."
                    System.out.println(errorMessage)
                    throw new RuntimeException(errorMessage)
                }

                isLocked = nce.isAppLocked(appName)
                //is locked will be false here if the app is not locked
                if(isLocked){
                    nce.lockApp(appName, newVersion, false)
                    isLocked = nce.isAppLocked(appName)
                }
                success = !isLocked
            }else{
                errorMessage = "ERROR: Release Version failed!"
                System.out.println(errorMessage)
                throw new RuntimeException(errorMessage)
            }

            if(success){
                afterCount = nce.getCubeCount(appName, versionAfter)
                afterBranchCount = nce.getBranchCount(appName, versionAfter)

                if(!beforeCount.equals(afterCount)){
                    errorMessage = "ERROR: Cube Counts do not match! Before- "+ beforeCount +" After- "+afterCount
                    System.out.println(errorMessage)
                    throw new RuntimeException(errorMessage)
                }else{
                    log.info("Verifying release - cube counts match ...")
                }

                if(!beforeBranchCount.equals(afterBranchCount)){
                    errorMessage = "ERROR: Branch Counts do not match! Before- "+ beforeBranchCount +" After- "+afterBranchCount
                    System.out.println(errorMessage)
                    throw new RuntimeException(errorMessage)
                }else{
                    log.info("Release successful, cube counts and branch counts match.  Updating sys.versions and sys.bootstrap.info cubes now ...")
                }

                //update version in sys.versions and commit after releasing
                Map<String, String> axesInfo = new HashMap<>(2)
                axesInfo.put("feature", "resources")
                axesInfo.put("env", "PROD")
                String msg = updateAndCommitCube(appName, versionAfter, SANDBOX, SYS_VERSIONS, axesInfo, newVersion)
                if (msg.contains("ERROR")){
                    System.out.println(msg)
                    throw new RuntimeException(msg)
                }

                //update version in 0.0.0. sys.bootstrap.info and commit before releasing
                axesInfo = new HashMap<>(1)
                axesInfo.put("param", "version")
                msg = updateAndCommitCube(appName, _000_VERSION, SANDBOX, SYS_BOOTSTRAP_INFO, axesInfo, newVersion)
                if (msg.contains("ERROR")){
                    System.out.println(msg)
                    throw new RuntimeException(msg)
                }

                axesInfo = new HashMap<>(2)
                axesInfo.put("env", "TESTING")
                axesInfo.put("param", "version")
                msg = updateAndCommitCube(appName, _000_VERSION, SANDBOX, SYS_BOOTSTRAP_INFO, axesInfo, newVersion)
                if (msg.contains("ERROR")){
                    System.out.println(msg)
                    throw new RuntimeException(msg)
                }

                axesInfo = new HashMap<>(2)
                axesInfo.put("env", "SAND")
                axesInfo.put("param", "version")
                msg = updateAndCommitCube(appName, _000_VERSION, SANDBOX, SYS_BOOTSTRAP_INFO, axesInfo, newVersion)
                if (msg.contains("ERROR")){
                    System.out.println(msg)
                    throw new RuntimeException(msg)
                }

                axesInfo = new HashMap<>(2)
                axesInfo.put("env", "DEV")
                axesInfo.put("param", "version")
                msg = updateAndCommitCube(appName, _000_VERSION, SANDBOX, SYS_BOOTSTRAP_INFO, axesInfo, newVersion)
                if (msg.contains("ERROR")){
                    System.out.println(msg)
                    throw new RuntimeException(msg)
                }

                success = "passed".equals(msg)
            }
        }

        System.out.println(success)

        return success
    }

    @CliCommand(value = "nce-updatesysbootinfo", help = "Updates the sys.bootstrap.info cube version in the provided application and environment")
    Boolean updateSysBootstrapInfo(
            @CliOption(key = ["cookie", "c"], mandatory = true, help = "Enter your username") final String cookieStr,
            @CliOption(key = ["app"], mandatory = true, help = "Enter the application name") final String appName,
            @CliOption(key = ["environment", "env"], mandatory = true, help = "Enter the application name") final String envName,
            @CliOption(key = ["version", "v"], mandatory = true, help="Enter the new version to update the cube with") final String version){

        addCookieFromString(cookieStr)
        Map<String, String> axesInfo = new HashMap<>(2)
        axesInfo.put("env", envName)
        axesInfo.put("param", "version")
        String msg = updateAndCommitCube(appName, _000_VERSION, SANDBOX, SYS_BOOTSTRAP_INFO, axesInfo, version)
        if (msg.contains("ERROR")){
            System.out.println(msg)
            throw new RuntimeException(msg)
        }

        System.out.println(true)
        return true
    }

    @CliCommand(value = "nce-updatecube", help = "Updates the given cube ")
    Boolean updateCube(
            @CliOption(key = ["cookie", "c"], mandatory = true, help = "Enter your username") final String cookieStr,
            @CliOption(key = ["app"], mandatory = true, help = "Enter the application name") final String appName,
            @CliOption(key = ["axes-info", "a"], mandatory = true, help = "Enter axes info in comma separated name value pairs.\nE.g. param=versions, axesName=columnName") final String[] axes,
            @CliOption(key = ["version", "v"], mandatory = true, help="Enter the version of the application to update") final String version,
            @CliOption(key = ["branch", "b"], mandatory = true, help="Enter the branch of the application to update") final String branch,
            @CliOption(key = ["cube"], mandatory = true, help="Enter the cube name to update") final String cube,
            @CliOption(key = ["value"], mandatory = true, help="Enter the new value to use") final String value) {

        addCookieFromString(cookieStr)
        final Map<String, String> axesInfo = new HashMap<>(axes.length)
        Arrays.stream(axes).each{ ax ->
            String[] kv = ax.split("=")
            axesInfo.put(kv[0], kv[1])
        }

        String msg = updateAndCommitCube(appName, version, branch, cube, axesInfo, value)
        if (msg.contains("ERROR")){
            System.out.println(msg)
            throw new RuntimeException(msg)
        }

        System.out.println(true)
        return true
    }

    private String updateAndCommitCube(String app, String version, String branch, String cubeName, Map<String, String> axesInfo, String updates){
        HashMap<String, List<NCubeInfoDto>> committedCubes
        String returnMsg = "passed"

        boolean success = nce.updateCellAt(app,version,branch,cubeName, axesInfo, updates)
        if (success){
            committedCubes = nce.commitCube(app,version,branch,cubeName)
            if(committedCubes != null && committedCubes.size() >0){
                List<NCubeInfoDto> updatedCubes = committedCubes.get("updates")
                success = (updatedCubes.stream().filter{ cube -> cubeName.equals(cube.name) }.count() == 1)
                if(!success){
                    returnMsg = String.format("ERROR: Could not commit update to %s %s %s '%s': committedCubes- '%s'",version, branch, cubeName, axesInfo, committedCubes)
                }
            }else{
                returnMsg = String.format("ERROR: No cubes Committed! %s %s %s '%s': committedCubes- '%s'",version, branch, cubeName, axesInfo, committedCubes)
            }
        }else{
            returnMsg = String.format("ERROR: Could not update %s %s %s '%s'",version, branch, cubeName, axesInfo )
        }

        return returnMsg
    }
}