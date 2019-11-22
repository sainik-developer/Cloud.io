package com.cloudio.rest.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseModel {

    private Object data;
    private String message;
    private int code;
    private static ObjectMapper om = new ObjectMapper();

    public enum ResponseCode{
        SUCCESS(1000), BAD_REQUEST(1001), FORBIDDEN(1002), SERVER_ERROR(1003);
        int responseCode;
        ResponseCode(int responseCode){
            this.responseCode = responseCode;
        }

        public int getResponseCode() {
            return responseCode;
        }
    }
    public ResponseModel() {
    }

    public ResponseModel(Object data, String message, ResponseCode rCode) {
        this.data = data;
        this.message = message;
        this.code = rCode.getResponseCode();
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String toJSON(){
        try {
           return om.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
