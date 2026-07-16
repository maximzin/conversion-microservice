package com.zinoviev.conversion_microservice.storage.service;

public interface StorageService {

    byte[] downloadFile(String fileKey);

    void uploadFile(String fileKey, byte[] fileBytes, String contentType);
}
