package com.cedarsoft.ncubecli.api

import com.cedarsoftware.ncubecli.api.NCubeEditorApi
import com.cedarsoftware.ncubecli.api.VersionType
import com.cedarsoftware.ncube.NCubeInfoDto
import org.apache.http.cookie.Cookie
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.junit.*

/* Created by dben and ihiggins */

class TestNCubeEditorApi {
    NCubeEditorApi nce
    private final Logger log = LogManager.getLogger(TestNCubeEditorApi.class)

    final private String TEST_USER     = "XXXXXXXXX"
    final private String TEST_PASSWORD = "XXXXXXXXX"

    final private String test_app         = "Devops.Test"
    final private String test_branch      = "unit.test"
    final private String test_branch_to   = "unit.test.dev"
    final private VersionType versionType = VersionType.MINOR

    @Before
    void setup(){
        nce = new NCubeEditorApi()
        nce.authenticate(TEST_USER, TEST_PASSWORD)
    }

    @After
    void teardown() throws IOException {
        nce.getClient().close()
    }

    @Test
    @Ignore
    void testAuthentication(){
        List<Cookie> cookies = nce.getCookies().getCookies()

        Assert.assertTrue(cookies.size() >0)
        Assert.assertTrue(
            cookies.stream()
                .filter{ cookie -> cookie.getName().contains("SMSESSION") }
                .findFirst()
                .get().getDomain().equals("td.afg")
        )
    }

    @Test
    @Ignore
    void testGetVersions(){
        String version = nce.getLatestVersion(test_app)
        Assert.assertNotNull(version)
    }

    @Test
    @Ignore
    void testCopyAndDeleteBranch(){
        String version = nce.getLatestVersion(test_app)
        Assert.assertNotNull(version)

        Boolean success = nce.copyBranch(test_app,test_branch, test_branch_to, version, version)
        Assert.assertTrue("Copy should have been successful.",success)

        success = nce.deleteBranch(test_app,test_branch_to, version)
        Assert.assertTrue("Delete should have been successful.",success)
    }

    @Test
    @Ignore
    void testLockApp(){
        String version = nce.getLatestVersion(test_app)
        Assert.assertNotNull(version)

        Boolean isLocked = nce.isAppLocked(test_app)
        Assert.assertFalse("App Should not be locked",isLocked)

        isLocked = nce.lockApp(test_app, version, true)
        Assert.assertTrue("App Should be locked",isLocked)

        isLocked = nce.isAppLocked(test_app)
        Assert.assertTrue("isAppLocked Should return true",isLocked)

        isLocked = nce.lockApp(test_app, version, false)
        Assert.assertTrue("App Should no longer be locked",isLocked)
    }

    @Test
    @Ignore
    void testGetAndMoveBranches(){
        /*
            Copy and move a branch to a new version, delete the branch at the end.
         */
        String version = nce.getLatestVersion(test_app)
        Assert.assertNotNull(version)

        //reset the branch before this runs, and after
        Boolean success = nce.copyBranch(test_app,test_branch, test_branch_to, version, version)
        Assert.assertTrue("Copy should have been successful.",success)

        Boolean isLocked = nce.isAppLocked(test_app)
        Assert.assertFalse("App Should not be locked",isLocked)

        isLocked = nce.lockApp(test_app, version, true)
        Assert.assertTrue("App Should be locked",isLocked)

        List<Object> branches = nce.getBranches(test_app, version)
        Assert.assertNotNull("App Should contain a HEAD branch",
            branches.stream().filter{ br -> "HEAD".equalsIgnoreCase((String)br)}.findFirst() )

        //only move the test branch
        branches.stream().filter{ br ->
            test_branch_to.equalsIgnoreCase((String) br).each{ br2 ->
                Boolean didWork = nce.moveBranch(test_app, version, br2.toString(), nce.incrementVersion(version, versionType))
                Assert.assertTrue("Move should have been successful.", didWork)
            }
        }

        isLocked = nce.lockApp(test_app, version, false)
        Assert.assertTrue("App Should no longer be locked",isLocked)
        isLocked = nce.isAppLocked(test_app)
        Assert.assertFalse("isAppLocked Should return false",isLocked)

        //delete branch
        success = nce.deleteBranch(test_app,test_branch_to, version)
        Assert.assertTrue("Delete should have been successful.",success)
    }

    @Test
    @Ignore
    void testReleaseCubes() throws InterruptedException {
        String version = nce.getLatestVersion(test_app)
        String newVersion = nce.incrementVersion(version, versionType)
        Assert.assertNotNull(version)

        Boolean isLocked = nce.isAppLocked(test_app)
        Assert.assertFalse("App Should not be locked",isLocked)

        isLocked = nce.lockApp(test_app, version, true)
        Assert.assertTrue("App Should be locked",isLocked)

        List<Object> branches = nce.getBranches(test_app, version)
        Assert.assertNotNull("App Should contain a HEAD branch",
            branches.stream().filter{ br -> "HEAD".equalsIgnoreCase((String)br) }.findFirst())

        //move all branches except HEAD
        branches.stream().filter {
            br -> !"HEAD".equals(br).each {
                br2 ->
                    Boolean didWork = nce.moveBranch(test_app, version, br2.toString(), newVersion)
                    Assert.assertTrue("Move should have been successful.", didWork)
            }
        }

        Boolean success = nce.releaseVersion(test_app,version, newVersion)
        Assert.assertTrue("Release should have been successful.",success)

        isLocked = nce.isAppLocked(test_app)
        Assert.assertTrue("isAppLocked Should return true",isLocked)
        nce.lockApp(test_app, newVersion, false)
        isLocked = nce.isAppLocked(test_app)
        Assert.assertFalse("isAppLocked Should return false",isLocked)
    }

    @Test
    @Ignore
    void testCubeCount(){
        String version = nce.getLatestVersion(test_app)
        Assert.assertNotNull(version)

        Integer count = nce.getCubeCount(test_app,version)
        Assert.assertTrue("There should be more than 0 cubes",count >0)
    }

    @Test
    @Ignore
    void testGetJson(){
        String version = nce.getLatestVersion(test_app)
        Assert.assertNotNull(version)

        String success = nce.getJson(test_app,"0.0.0-SNAPSHOT", "sandbox", "sys.bootstrap.info")
        Assert.assertTrue("getJson should have been successful.",success != null)
    }

    @Test
    @Ignore
    void testUpdateCellAndCommit(){
        String version = nce.getLatestVersion(test_app)
        Assert.assertNotNull(version)
        String cubeName = "sys.versions"
        Map<String, String> axes = new HashMap<>(2)
        axes.put("feature", "resources")
        axes.put("env", "PROD")

        Boolean success = nce.updateCellAt(test_app,version, test_branch, cubeName,axes, nce.incrementVersion(version, versionType))
        Assert.assertTrue("updateCellAt should have been successful.",success)

        HashMap<String, List<NCubeInfoDto>> things = nce.commitCube(test_app, version, test_branch, cubeName)
        List<NCubeInfoDto> updates = things.get("updates")
        Assert.assertTrue("commitCube should have been successful.",updates.size()>0)
        Assert.assertTrue("commitCube should have updated the proper cube name.",updates.get(0).name.equals(cubeName))
    }

    @Test
    @Ignore
    void testGetBranchCount(){
        String version = nce.getLatestVersion(test_app)
        Assert.assertNotNull(version)

        Integer count = nce.getBranchCount(test_app, version)
        Assert.assertNotNull("App Should contain a branch",count)
        Assert.assertTrue("App Should contain branches",count > 0)
    }

    @Test
    @Ignore
    void testCopy2() {
        String version = nce.getLatestVersion(test_app)
        Assert.assertNotNull(version)

        Boolean success = nce.copyBranch(test_app, "HEAD", "HEAD", "1.87.0-RELEASE", version)
        Assert.assertTrue("Copy should have been successful.", success)
    }
}