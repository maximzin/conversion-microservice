package com.zinoviev.conversion_microservice.storage.service;

public class StorageServiceImpl implements StorageService {

    @Override
    public byte[] downloadFile(String filePath) {
        return new byte[0];
    }

    @Override
    public void uploadFile(String filePath, byte[] bytes) {
        System.out.println("UPLOAD");
    }
}
