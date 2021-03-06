# n-cube-cli
Command line interface and API for n-cube.

# Setup

1. **Maven**
   If you're importing this project into an existing IntelliJ project,
   Drag the pom.xml file into the Maven Projects window to get Maven to load dependencies.
   Run Maven 'clean install' tasks.

2. **Configure to connect to your own n-cube environment**
   Modify the requestURL bean to your env in the src/main/resources/META-INF.spring/spring-shell-plugin.xml file:
    ```xml
        <bean id="requestURL" class="java.lang.String">
            <constructor-arg value="https://nce-sb.yourinternaldomain/n-cube-editor/" />
        </bean>
    ```

# Using this
There are several ways to use this command line tool:

**Launch an n-cube Interactive Session**

If you've a number of commands to execute, use this approach.

```shell      
$ ./nce-cli
                                            ▄▄                                      ▄▄▄▄         ██
                                            ██                                      ▀▀██         ▀▀
    ██▄████▄             ▄█████▄  ██    ██  ██▄███▄    ▄████▄              ▄█████▄    ██       ████
    ██▀   ██            ██▀    ▀  ██    ██  ██▀  ▀██  ██▄▄▄▄██            ██▀    ▀    ██         ██
    ██    ██   █████    ██        ██    ██  ██    ██  ██▀▀▀▀▀▀   █████    ██          ██         ██
    ██    ██            ▀██▄▄▄▄█  ██▄▄▄███  ███▄▄██▀  ▀██▄▄▄▄█            ▀██▄▄▄▄█    ██▄▄▄   ▄▄▄██▄▄▄
    ▀▀    ▀▀              ▀▀▀▀▀    ▀▀▀▀ ▀▀  ▀▀ ▀▀▀      ▀▀▀▀▀               ▀▀▀▀▀      ▀▀▀▀   ▀▀▀▀▀▀▀▀

   Type 'help' and hit ENTER for a list of commands.
   n-cube-cli>

```

**Script individual commands**

Format:    `java -jar target/n-cube-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar [command] [parameters]`

An example (that is currently broken) can be found in:
``` ./nce-cli-examples your_nce_user your_nce_password ```
