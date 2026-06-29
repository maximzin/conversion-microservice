package com.zinoviev.conversion_microservice.storage.service;

public interface StorageService {

    byte[] downloadFile(String filePath);

    void uploadFile(String filePath, byte[] bytes);
}
