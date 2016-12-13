TODO
* Change GAIG references
  * supplying POM info
  * return provider name from function
  * replace DevopsBanner/Prompt classes
  * different CLI prompts for each app?
  * factor out updatePom method into subclass for CubeReleaseTool
* Maven Version of n-cube-client 1.0.0, CubeReleaseTool new major  1.0.0
DONE  namespace path
DONE  static compile
DONE  rename getVersions to getLatestVersion, add getVersions
* if we sort on numeric ...  PATCHES take most recently created.  (highest version number).    sort numerical.  patch # only single digit
* all tests passing
* test CLI w/ some examples
* type of connection / draining errors (per John), BUT:
        HttpClient resource deallocation: When an instance CloseableHttpClient is no longer needed and is about to go out of scope the connection manager associated with it must be shut down by calling the CloseableHttpClient#close() method.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            //do something
        } finally {
            httpclient.close();
        }