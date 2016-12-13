package com.cedarsoftware.ncubecli.api

import com.cedarsoftware.ncube.ApplicationID
import com.cedarsoftware.ncube.NCubeInfoDto
import com.cedarsoftware.ncube.NCubeManager
import com.cedarsoftware.ncube.ReleaseStatus
import com.cedarsoftware.util.io.JsonObject
import com.cedarsoftware.util.io.JsonReader
import com.cedarsoftware.util.io.JsonWriter
import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.http.*
import org.apache.http.client.CookieStore
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair

import java.util.*

@CompileStatic

class NCubeEditorApi {
    // TODO need to config this

    private final String requestURL="https://nce-sb.td.afg/n-cube-editor/"
    private final String commandPrefix = "cmd/ncubeController/"

    // TODO: needs to go...  put in a spring config or properties
    // proxy host and request url

    // TODO accept string,  break up into pieces
    private final HttpHost proxyHost = new HttpHost("squid.td.afg",3128,"http") // "http://squid.td.afg:3128"

    private Log log = LogFactory.getLog(NCubeEditorApi.class)
    private CloseableHttpClient client       // TODO: not using HttpURLconnection
    private CookieStore cookies
    private final Map<String, Object> falseType

    CloseableHttpClient getClient() {
        return client
    }

    HttpURLConnection junk
    CookieStore getCookies() {
        return cookies
    }

    NCubeEditorApi(){
        client = createClient()
        falseType = new HashMap<>()
        falseType.put(JsonWriter.TYPE, false)
    }
    /**
     * Creates the client object with the proxy and cookie store for later use.
     *
     * @return A {@link CloseableHttpClient} with the GAIG proxy
     */
    private CloseableHttpClient createClient(){
        cookies = new BasicCookieStore()

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setProxy(proxyHost)
                .setDefaultCookieStore(cookies)
                .build()

        return httpClient
    }

    /**
     * Build the URI for a given URL/REST call with the given JSON values
     * @param url the URL to encode
     * @param json the values to encode into the URL as a json= parameter
     * @return the URL Encoded URI
     */
    private URI buildUriWithJsonArray(String url, String json){
        URIBuilder urib
        URI uri = null
        try {
            urib = new URIBuilder(url)
            if(json != null){
                urib.addParameter("json",json)
            }
            uri = urib.build()
        } catch (URISyntaxException e) {
            log.error(e)
        }

        return uri
    }

    /**
     * Perform an {@link HttpGet} call to the given {@link URI}
     * @param uri the URI to connect to
     * @return the data Object returned or Boolean of the status if the call was successful or not
     */
    private NCEResponse doGet(URI uri){
        NCEResponse response = null

        if(uri != null){
            HttpGet get = new HttpGet(uri)
            response = performHttpCall(get)
        }

        return response
    }

    /**
     * Perform an {@link HttpPost} call to the given {@link URI}
     * @param uri the URI to connect to
     * @return the data Object returned or Boolean of the status if the call was successful or not
     */
    private NCEResponse doPost(URI uri, String json){
        NCEResponse response = null

        if(uri != null){
            HttpPost post = new HttpPost(uri)
            post.setEntity(new StringEntity(json,ContentType.create("application/json", "UTF-8")))
            response =  performHttpCall(post)
        }

        return response
    }

    /**
     * Actually perform the HTTP Request passed in
     * @param request an {@link HttpGet} or {@link HttpPost} to perform
     * @return the data Object returned or Boolean of the status if the call was successful or not
     */
    private NCEResponse performHttpCall(HttpRequestBase request){
        HttpResponse response       // TODO: should we be using HttpServletResponse instead
        NCEResponse resp = null

        try {
            response = client.execute(request)

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                resp = new NCEResponse((JsonObject) JsonReader.jsonToJava(response.getEntity().getContent(), null))
            } else {
                // UrlUtilities.readErrorResponse(conn)    // TODO: has to take UrlConnection
                // TODO:  response.sendError(resCode, conn.responseMessage)
                log.error("Response had an error "+ response.getStatusLine())
                return null
            }

        } catch (IOException e) {
            log.error(e)
        }

        return resp
    }
    /**
     * Creates a JSON from an {@link com.cedarsoftware.ncube.ApplicationID} from the given information.
     * <p>
     * Example JSON: {"app":"UD", "branch":"something", "version":"1.1.1"}
     *
     * @param app the application name
     * @param branch the branch to use
     * @param version the version of the application to add
     * @return a JSONObject containing the given information
     */
    static private ApplicationID createNCubeApplicationIdObject(String app, String branch, String version, String tenant){
        ApplicationID id
        String[] versions = version.split("-")
        version = versions[0]

        if(versions.length ==2 && versions[1].contains("SNAPSHOT")){
            id = new ApplicationID(tenant,app, version, ReleaseStatus.SNAPSHOT.name(), branch)
        } else {
            id = new ApplicationID(tenant,app, version, ReleaseStatus.RELEASE.name(), branch)
        }

        return id
    }

    /**
     * Some calls need to remove "SNAPSHOT" from the version before making the REST call, this does that
     * @param version the actual version
     * @return the version without -SNAPSHOT if it is in the version number
     */
    static String removeReleaseTypeFromVersion(String version){
        if(version.contains("-")){
            version = version.split("-")[0]
        }
        return version
    }


    /**
     * Increments the minor version of the given release number String
     * @param version the entire version to increment
     * @return the version given with the minor version incremented and the release number reset to 0
     */
    static String incrementVersion(String version, VersionType versionType){
        String[] versionNumbers
        String incrementedVersion
        Integer changedVersion

        incrementedVersion = removeReleaseTypeFromVersion(version)
        versionNumbers = incrementedVersion.split("\\.")

        if(versionNumbers.length == 3){
            //make sure it is at least in the format of x.x.x
            switch (versionType){
                case VersionType.MAJOR:
                    changedVersion = Integer.parseInt(versionNumbers[0])
                    changedVersion ++
                    incrementedVersion = changedVersion+".0.0"
                    break
                case VersionType.MINOR:
                    changedVersion = Integer.parseInt(versionNumbers[1])
                    changedVersion ++
                    incrementedVersion = versionNumbers[0]+"."+changedVersion+".0"
                    break
                case VersionType.PATCH:
                    changedVersion = Integer.parseInt(versionNumbers[2])
                    changedVersion ++
                    incrementedVersion = versionNumbers[0]+"."+versionNumbers[1]+"."+changedVersion
                    break
                default:
                    throw new RuntimeException("Something went horribly wrong! Parameters for incrementVersion are version: "+version+" versionType: "+versionType.name())
            }
        }else{
            throw new RuntimeException("Version number give is incorrect format, should be X.YY.ZZ but is "+version + "\n If this is no longer true please update this code.")
        }

        return incrementedVersion
    }

    /**
     * Authenticate to the NCube Editor
     * <p>
     * Performs a POST to the NCE URL to authenticate a session with the given username and password.  Cookies for this session are stored in the cookies object.
     * @param user The user to authenticate as
     * @param pass The password to use for authentication
     */
    void authenticate(String user, String pass){
        HttpPost post = new HttpPost(requestURL)

        try {
            HttpResponse resp = client.execute(post)

            // TODO: should we add error checking suggested by John?

            if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY && resp.getHeaders("Location").length >0){
                //if the response is a redirect (302) and there is a redirect location provided
                Header redirectLocationHeader = resp.getHeaders("Location")[0] //get the first Location header
                //get exact URL to authenticate to
                String authUrl = redirectLocationHeader.getValue()

                List<NameValuePair> formparams = new ArrayList<NameValuePair>()
                formparams.add(new BasicNameValuePair("USER", user))
                formparams.add(new BasicNameValuePair("PASSWORD", pass))

                UrlEncodedFormEntity credentials = new UrlEncodedFormEntity(formparams, Consts.UTF_8)
                HttpPost authPost = new HttpPost(authUrl)
                authPost.setEntity(credentials)

                // Gets rid of invalid cookie header due to expired date
                RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build()
                RequestConfig localConfig  = RequestConfig.copy(globalConfig).setCookieSpec(CookieSpecs.STANDARD).build()
                authPost.setConfig(localConfig)

                HttpResponse authResp = client.execute(authPost)

                //the authentication should return back to
                if(authResp.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY){
                    log.error("Error authenticating to " + requestURL)
                }
            }

        } catch (IOException e) {
            log.error(e)
        }
    }

    /**
     * Call Get Versions from the NCube Controller for the given application
     *
     * @param app the application to retrieve the latest version for
     * @return the latest version of an application ex. "1.6.1-SNAPSHOT"
     */
    String getLatestVersion(String app){
        String getUrl = requestURL+commandPrefix+"getVersions"
        String version = null
        Object[] sendJson = new Object[1]
        sendJson[0] = app

        URI uri = buildUriWithJsonArray(getUrl, JsonWriter.objectToJson(sendJson, falseType))

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doGet(uri)
        if(resp.getStatus() && resp.getData() != null && resp.getData() instanceof Object[]){
            version = (String)((Object[]) resp.getData())[0]
        }

        return version
    }

    Object[] getVersions(String app){
        String getUrl = requestURL+commandPrefix+"getVersions"
        Object[] versions = null
        Object[] sendJson = new Object[1]
        sendJson[0] = app

        URI uri = buildUriWithJsonArray(getUrl, JsonWriter.objectToJson(sendJson, falseType))

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doGet(uri)
        if(resp.getStatus() && resp.getData() != null && resp.getData() instanceof Object[]){
            versions = (Object[])  resp.getData()
        }
        return versions
    }

    /**
     * Get the count of all of the cubes in an app/version by performing a call to getSearchCount
     * @param app
     * @param version
     * @return
     */
    Integer getCubeCount(String app, String version){
        String getUrl = requestURL+commandPrefix+"getSearchCount"
        Object[] sendJson = new Object[1]
        sendJson[0] = createNCubeApplicationIdObject(app,ApplicationID.HEAD, version, ApplicationID.DEFAULT_TENANT)
        Integer count = -1

        URI uri = buildUriWithJsonArray(getUrl, JsonWriter.objectToJson(sendJson, falseType))

        System.out.println(uri)

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doGet(uri)
        if(resp.getStatus() && resp.getData() != null){
            //response object is {"data":{"@type":"int","value":21},"status":true}
            count = (Integer)resp.getData()
        }

        return count
    }

    /**
     * Call copyBranch for a given application and version from 1 branch to another
     *
     * @param app application to use
     * @param branchFrom the branch to copy from
     * @param branchTo the branch to copy to
     * @param versionFrom the version to copy
     * @return true or false if the call was successful
     */
    Boolean copyBranch(String app, String branchFrom, String branchTo, String versionFrom, String versionTo){
        String postUrl = requestURL+commandPrefix+"copyBranch"
        Boolean success = false
        Object[] sendJson = new Object[2]
        sendJson[0] = createNCubeApplicationIdObject(app,branchFrom, versionFrom, ApplicationID.DEFAULT_TENANT) //json
        sendJson[1] = createNCubeApplicationIdObject(app,branchTo, versionTo, ApplicationID.DEFAULT_TENANT) //json

        //URL encode the information
        String json = JsonWriter.objectToJson(sendJson, falseType)
        URI uri = buildUriWithJsonArray(postUrl, json)
        log.debug("Delete URI: "+uri.toString())

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doGet(uri)
        if(resp.getData() == null){
            success = resp.getStatus()
            log.info("Copied branch ("+json.toString()+")? "+success)
        }

        return success
    }

    /**
     * Call deleteBranch for a given application, version, and branch
     *
     * @param app application to use
     * @param branch the branch to delete from
     * @param version the version of this branch to delete
     * @return true or false if the call was successful
     */
    Boolean deleteBranch(String app, String branch, String version){
        String postUrl = requestURL+commandPrefix+"deleteBranch"
        Boolean success = false
        Object[] sendJson = new Object[1]
        sendJson[0] = createNCubeApplicationIdObject(app, branch, version, ApplicationID.DEFAULT_TENANT) //json

        //URL encode the information
        URI uri = buildUriWithJsonArray(postUrl, null)
        log.debug("Delete URI: "+uri.toString())
        log.info("Deleting Branch: "+sendJson.toString())

        // TODO: should we add error checking suggested by John?

        String json = JsonWriter.objectToJson(sendJson, falseType)
        NCEResponse resp = doPost(uri, json)
        if(resp.getStatus() && resp.getData() == null){
            success = resp.getStatus()
            log.info("Deleted branch ("+json.toString()+")? "+success)
        }else{
            log.error("Deleting branch failed! Response: "+resp)
        }

        return success
    }

    /**
     * Call isAppLocked for the given application
     * @param app the application to check
     * @return true or false if the app is locked or not
     */
    Boolean isAppLocked(String app){
        String postUrl = requestURL+commandPrefix+"isAppLocked"
        Boolean success = false
        Object[] sendJson = new Object[1]
        sendJson[0] = createNCubeApplicationIdObject(app, ApplicationID.HEAD, ApplicationID.DEFAULT_VERSION, ApplicationID.DEFAULT_TENANT)

        URI uri = buildUriWithJsonArray(postUrl, JsonWriter.objectToJson(sendJson, falseType))
        log.debug("isAppLocked URI: "+uri.toString())

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doGet(uri)
        if(resp.getStatus() && resp.getData() != null && resp.getData() instanceof Boolean){
            success = (Boolean)resp.getData()
        }

        return success
    }

    /**
     * Call lockApp for the given application
     * @param app the application to lock
     * @param shouldLock true or false to lock or unlock the application
     * @return true or false if the application is locked
     */
    Boolean lockApp(String app, String version, boolean shouldLock){
        String postUrl = requestURL+commandPrefix+"lockApp"
        Boolean success = false
        Object[] sendJson = new Object[2]
        sendJson[0] = createNCubeApplicationIdObject(app, ApplicationID.HEAD, ApplicationID.DEFAULT_VERSION, ApplicationID.DEFAULT_TENANT)
        sendJson[1] = shouldLock

        URI uri = buildUriWithJsonArray(postUrl, null)
        log.debug("lockApp URI: "+uri.toString())

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doPost(uri, JsonWriter.objectToJson(sendJson, falseType))
        if(resp.getData() == null){
            success = resp.getStatus()
        }

        return success
    }

    /**
     * Get a {@link List} of all branches in an application
     * @param app the application to get branches from
     * @param version the version to use
     * @return a List of String of branch names
     */
    List<Object> getBranches(String app, String version){
        String postUrl = requestURL+commandPrefix+"getBranches"
        List<Object> branches = null
        Object[] sendJson = new Object[1]
        sendJson[0] = createNCubeApplicationIdObject(app, ApplicationID.HEAD, version, ApplicationID.DEFAULT_TENANT)

        URI uri = buildUriWithJsonArray(postUrl, null)
        log.debug("getBranches URI: "+uri.toString())

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doPost(uri, JsonWriter.objectToJson(sendJson, falseType))
        if(resp.getStatus() && resp.getData() != null && resp.getData() instanceof Object[]){
            branches = new ArrayList<>()
            branches.addAll(Arrays.asList((Object[]) resp.getData()))
        }

        return branches
    }

    Integer getBranchCount(String app, String version){
        List<Object> branches = getBranches(app, version)
        Integer count = branches.size()
        log.debug("Branch Count - "+ count)

        return count
    }

    /**
     * Move a given branch to a new version.
     * <p>
     * HEAD cannot be moved in this way and the application must be locked by the user doing the move before this is called.
     *
     * @param app
     * @param version
     * @param branch
     * @param newVersion
     * @return true or false if the move was completed or not
     */
    Boolean moveBranch(String app, String version, String branch, String newVersion){
        String postUrl = requestURL+commandPrefix+"moveBranch"
        Boolean success = false
        Object[] sendJson = new Object[2]
        sendJson[0] = createNCubeApplicationIdObject(app, branch, version, ApplicationID.DEFAULT_TENANT) //json
        sendJson[1] = newVersion

        URI uri = buildUriWithJsonArray(postUrl, null)
        String json = JsonWriter.objectToJson(sendJson, falseType)
        log.debug("moveBranch URI: "+uri.toString())
        log.info("Moving branch: "+json)

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doPost(uri, json)
        if(resp.getStatus() && resp.getData() == null){
            success = resp.getStatus()
            log.info("Branch moved? "+success)
        }else{
            log.error("Issue moving Response was"+ resp )
        }

        return success
    }


    /**
     * Release all cubes for a given application and move it to a new SNAPSHOT version.
     *
     * @param app
     * @param version
     * @param newVersion
     * @return true or false if the release completed without errors
     */
    Boolean releaseVersion(String app, String version, String newVersion){
        String postUrl = requestURL+commandPrefix+"releaseVersion"
        Boolean success = false
        Object[] sendJson = new Object[2]
        sendJson[0] = createNCubeApplicationIdObject(app, ApplicationID.HEAD, version, ApplicationID.DEFAULT_TENANT) //json
        sendJson[1] = newVersion

        URI uri = buildUriWithJsonArray(postUrl, null)
        String json = JsonWriter.objectToJson(sendJson, falseType)
        log.debug("Release URI: "+uri.toString())
        log.info("Releasing application: "+json)

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doPost(uri,json)
        if(resp.getStatus() && resp.getData() == null){
            success = resp.getStatus()
            log.info("Application released? "+success)
        }else{
            log.error("Issue releasing cubes - Response was"+ resp )
        }

        return success
    }

    /**
     * Returns a JSON string of the getJson response including all cells
     *
     * @param app
     * @param version
     * @param branch
     * @param cubeName
     * @return
     */
    String getJson(String app, String version, String branch, String cubeName){
        String postUrl = requestURL+commandPrefix+"getJson"
        String jsonResponse = null
        Object[] sendJson = new Object[3]
        sendJson[0] = createNCubeApplicationIdObject(app, ApplicationID.HEAD, version, ApplicationID.DEFAULT_TENANT)
        sendJson[1] = cubeName
        Map options = new HashMap()
        options.put("mode", "")
        sendJson[2] = options

        URI uri = buildUriWithJsonArray(postUrl, null)
        //log.debug("getJson URI: "+uri.toString())

        NCEResponse resp
        try {
            resp = doPost(uri, JsonWriter.objectToJson(sendJson, falseType))
        } catch (Exception e) {
            log.error("\t\t " + cubeName + "*** getJson.doPost *** : " + e)
            throw e
        }

        // TODO: should we add error checking suggested by John?

        if(resp.getStatus() && resp.getData() != null && resp.getData() instanceof String){
            jsonResponse = (String)resp.getData()
            //log.info("Json Response: "+jsonResponse)
        } else {
            log.error("Issue calling getJson - Response was"+ resp )
        }

        return jsonResponse
    }

    /**
     * Searches for n-cubes matching a pattern.  Returns a Dictionary containing names of all n-cubes.
     * To be used alongside of getJson (for now) to retrieve individual n-cubes.
     *
     * @param app
     * @param version
     * @param branch
     * @param cubeName
     * @return
     */
    List<NCubeInfoDto> search(String app, String version, String branch, String cubeSearchPattern) {
        String postUrl = requestURL+commandPrefix+"search"
        List<NCubeInfoDto> nCubeDtoList = new ArrayList()

        Object[] sendJson = new Object[4]

        sendJson[0] = createNCubeApplicationIdObject(app, ApplicationID.HEAD, version, ApplicationID.DEFAULT_TENANT)
        sendJson[1] = ""   // SEARCH BY CUBE NAME    "rpm.class.Coverage.*"
        sendJson[2] = ""   // SEARCH BY CUBE CONTENTS

        Map options = new HashMap()
        options.put(NCubeManager.SEARCH_ACTIVE_RECORDS_ONLY, true)
        sendJson[3] = options

        URI uri = buildUriWithJsonArray(postUrl, null)
        // log.info("getJson URI: "+uri)
        NCEResponse resp = doPost(uri, JsonWriter.objectToJson(sendJson, falseType))

        // TODO: should we add error checking suggested by John?

        if(resp.getStatus() && resp.getData() != null && resp.getData() != null){
            Object[] myResponse = (Object[]) resp.getData()
            for (int i=0; i < myResponse.length; i++) {
                nCubeDtoList.add((NCubeInfoDto) myResponse[i])
            }
        }else{
            log.error("Issue calling getJson - Response was"+ resp )
        }
        return nCubeDtoList
    }


    /**
     * Updates the value of a specific axes column in the given application and cube
     *
     * @param app
     * @param version
     * @param branch
     * @param cubeName
     * @param axesInfo
     * @param newColumnValue
     * @return true or false if the cell was updated
     */
    Boolean updateCellAt(String app, String version, String branch, String cubeName, Map<String, String> axesInfo, String newColumnValue){
        String postUrl = requestURL+commandPrefix+"updateCellAt"
        Boolean success = false
        Object[] sendJson = new Object[4]
        Map<String, String> newValue = new HashMap<>()
        newValue.put("value", newColumnValue)
        sendJson[0] = createNCubeApplicationIdObject(app, branch, version, ApplicationID.DEFAULT_TENANT)
        sendJson[1] = cubeName
        sendJson[2] = axesInfo
        sendJson[3] = newValue

        URI uri = buildUriWithJsonArray(postUrl, null)
        String json = JsonWriter.objectToJson(sendJson, falseType)
        log.debug("updateCellAt URI: "+uri.toString())
        log.info("Updating Cell with: "+json)

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doPost(uri, json)
        if(resp.getStatus() && resp.getData() != null && resp.getData() instanceof Boolean){
            success = (Boolean) resp.getData()
            log.info("Cell updated? "+success)
        }else{
            log.error("Issue Updating Cell - Response was"+ resp )
        }

        return success
    }

    HashMap<String, List<NCubeInfoDto>> commitCube(String app, String version, String branch, String cubeName){
        String postUrl = requestURL+commandPrefix+"commitCube"
        LinkedHashMap<String, List<NCubeInfoDto>> commitReturn = null
        Object[] sendJson = new Object[2]

        sendJson[0] = createNCubeApplicationIdObject(app, branch, version, ApplicationID.DEFAULT_TENANT)
        sendJson[1] = cubeName

        URI uri = buildUriWithJsonArray(postUrl, null)
        String json = JsonWriter.objectToJson(sendJson, falseType)
        log.debug("commitCube URI: "+uri.toString())
        log.info("Committing cube: "+json)

        // TODO: should we add error checking suggested by John?

        NCEResponse resp = doPost(uri, json)
        if(resp.getStatus() && resp.getData() != null && resp.getData() instanceof HashMap){
            commitReturn = (LinkedHashMap) resp.getData()
            log.info("Cubes committed: "+commitReturn)
        }else{
            log.error("Issue Updating Cell - Response was"+ resp )
        }

        return commitReturn
    }
}