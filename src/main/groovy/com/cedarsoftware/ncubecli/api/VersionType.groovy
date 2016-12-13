package com.cedarsoftware.ncubecli.api

import groovy.transform.CompileStatic

@CompileStatic

enum VersionType {
    MAJOR,
    MINOR,
    PATCH

    static VersionType find(String version){
        switch (version.toUpperCase()) {
            case "MAJOR":
                return MAJOR
            case "MINOR":
                return MINOR
            case "PATCH":
                return PATCH
        }
        return null
    }
}