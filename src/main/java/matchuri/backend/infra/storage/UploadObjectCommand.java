package matchuri.backend.infra.storage;

public record UploadObjectCommand(
        String objectKey,
        String contentType,
        byte[] content
) {
}
