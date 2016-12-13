package com.cedarsoftware.ncubecli.commands

import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.stereotype.Component

@Component
class HelloWorldCommands implements CommandMarker {
    //TODO delete this before finalizing this project
   /* @CliAvailabilityIndicator( {"hw"})
    boolean isHelloThere(){
        return true
    }*/

    @CliCommand(value = "hw", help = "Says hello when you give your name")
    String hello(@CliOption(key = ["name"], mandatory = true, help = "Enter your name", specifiedDefaultValue = "weirdo") final String name){
        return "Hello "+name
    }
}