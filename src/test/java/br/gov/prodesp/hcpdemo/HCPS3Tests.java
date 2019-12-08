package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.model.MyObject;
import com.amazonaws.services.s3.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

@SpringBootTest(classes = HcpDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HCPS3Tests {
    private S3Service s3Service;
    @Autowired
    public void setS3Service(final S3Service s3Service){
        this.s3Service = s3Service;
    }

    @Test
    @Order(7)
    void S3Ingest(){
        s3Service.initBucket();
        for (MyObject myObject : TestHelper.getSample()) {
            String filename = TestHelper.getFilename(myObject);
            byte[] bytes = TestHelper.generateImage(myObject);
            ObjectMetadata metadata = myObject.toS3Metadata();
            metadata.setContentLength(bytes.length);
            PutObjectResult putObjectResult = s3Service.putObject(metadata, filename, bytes);
            Assertions.assertNotNull(putObjectResult);
            Assertions.assertFalse(StringUtils.isEmpty(putObjectResult.getETag()));
        }
    }


    @Test
    @Order(8)
    void S3DeleteAll(){
        ObjectListing objectListing = s3Service.listObjects();
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            String key = objectSummary.getKey();
            s3Service.deleteObject(key);
            AmazonS3Exception amazonS3Exception = Assertions.assertThrows(AmazonS3Exception.class, () -> s3Service.getObjectMetadata(key));
            Assertions.assertEquals("404 Not Found", amazonS3Exception.getErrorCode());
        }
    }
}
