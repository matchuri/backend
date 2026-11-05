package matchuri.backend.infra.storage;

public interface ObjectStorageClient {

    void upload(UploadObjectCommand command);

    void delete(String objectKey);
}
