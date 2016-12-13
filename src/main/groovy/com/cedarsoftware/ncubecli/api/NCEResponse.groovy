package com.cedarsoftware.ncubecli.api

import com.cedarsoftware.util.io.JsonObject
import groovy.transform.CompileStatic

/* Created by dben and ihiggins */

@CompileStatic

class NCEResponse {
    private Object data
    private Boolean status

    NCEResponse(Object data, Boolean status){
        this.data = data
        this.status = status
    }

    NCEResponse(JsonObject json){
        this.data = json.get("data")
        this.status = (Boolean)json.get("status")
    }

    Object getData() {
        return data
    }

    void setData(Object data) {
        this.data = data
    }

    Boolean getStatus() {
        return status
    }

    void setStatus(Boolean status) {
        this.status = status
    }

    @Override
    String toString() {
        return "NCEResponse{" +
                "data=" + data +
                ", status=" + status +
                '}'
    }
}