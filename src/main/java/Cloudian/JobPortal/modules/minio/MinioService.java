//document: https://lf-onap.atlassian.net/wiki/spaces/DW/pages/99057714/Spring+Boot+MinIO+Integration+Lab+and+Exercises

package Cloudian.JobPortal.modules.minio;

import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class MinioService {
    @Autowired
    MinioClient minioClient;
    @Value("${minio.url}")
    String minioUrl;
    @Value("${minio.bucket.name}")
    String bucketName;
    public String uploadFile(MultipartFile file) {
        try
        {
            String fileName = file.getOriginalFilename();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return fileName;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Upload file failed" , e);  //Xu li ngoai le thi su dung throw new, khong can quan tam nhung cai khac

        }
    }
    public String getFileUrl(String fileName)
    {
        return minioUrl + "/" + bucketName + "/" + fileName;
    }
    public void deleteFile(String fileName)
    {
        try
        {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build()
            );
        }
        catch (Exception e)
        {
            throw new RuntimeException("Delete file failed" , e);
        }
    }
    public List<String> listFile()
    {
        try
        {
            List<String> fileNames = new ArrayList<>();
            Iterable<Result<Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).build()
            );
            for (Result<Item> item : items) {
                fileNames.add(item.get().objectName());
            }
            return fileNames;
        }
        catch (Exception e)
        {
            throw new RuntimeException("List file failed" , e);
        }
    }
}
