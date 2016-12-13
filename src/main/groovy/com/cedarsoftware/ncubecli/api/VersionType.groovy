package com.cedarsoftware.ncubecli.api

import groovy.transform.CompileStatic

/* Created by dben and ihiggins */

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