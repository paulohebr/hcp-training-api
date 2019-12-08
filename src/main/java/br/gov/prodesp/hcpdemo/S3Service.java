package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.config.HCPConfig;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.List;

@Service
public class S3Service {
    private final AmazonS3Client hs3Client;
    private final HCPConfig hcpConfig;

    public S3Service(HCPConfig hcpConfig) {
        this.hcpConfig = hcpConfig;
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setMaxConnections(200);
        clientConfiguration.setProtocol(Protocol.HTTPS);
        System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");
        String accessKey = BaseEncoding.base64().encode(hcpConfig.getUsername().getBytes());
        //noinspection UnstableApiUsage,deprecation
        String secretKey = Hashing.md5().hashString(hcpConfig.getPassword(), Charset.defaultCharset()).toString();
        hs3Client = new AmazonS3Client(
                new BasicAWSCredentials(accessKey,
                        secretKey), clientConfiguration);
        hs3Client.setEndpoint(hcpConfig.getTenantHostname());
    }

    void initBucket() {
        boolean bucketExists = hs3Client.doesBucketExist(hcpConfig.getBucketName());
        if (!bucketExists) {
            hs3Client.createBucket(hcpConfig.getBucketName());
        }
        AccessControlList bucketAcl = hs3Client.getBucketAcl(hcpConfig.getBucketName());
        List<Grant> grants = bucketAcl.getGrantsAsList();
        boolean granted = false;
        for (Grant grant : grants) {
            String identifier = grant.getGrantee().getIdentifier();
            Permission permission = grant.getPermission();
            if (identifier.equals(hcpConfig.getUserId()) && permission.equals(Permission.FullControl)) {
                granted = true;
            }
        }
        if (!granted) {
            bucketAcl.grantPermission(new CanonicalGrantee(hcpConfig.getUserId()), Permission.FullControl);
            hs3Client.setBucketAcl(hcpConfig.getBucketName(), bucketAcl);
        }
    }

    public S3Object getObject(String key) {
        return hs3Client.getObject(hcpConfig.getBucketName(), key);
    }

    public ObjectMetadata getObjectMetadata(String key) {
        return hs3Client.getObjectMetadata(hcpConfig.getBucketName(), key);
    }

    public ObjectListing listObjects() {
        return hs3Client.listObjects(hcpConfig.getBucketName());
    }

    public void deleteObject(String key) {
        hs3Client.deleteObject(hcpConfig.getBucketName(), key);
    }

    public PutObjectResult putObject(ObjectMetadata metadata, String key, byte[] bytes) {
        return hs3Client.putObject(new PutObjectRequest(hcpConfig.getBucketName(), key, new ByteArrayInputStream(bytes), metadata));
    }

}
