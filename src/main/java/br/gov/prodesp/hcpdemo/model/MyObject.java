package br.gov.prodesp.hcpdemo.model;

import br.gov.prodesp.hcpdemo.hcpModel.query.request.expression.HCPExpressionHelper;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class MyObject {
    public static final String ANNOTATION = "my-object";


    private String owner;


    private Long id;


    private ZonedDateTime signDate;

    public ObjectMetadata toS3Metadata(){
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("owner", this.owner);
        metadata.addUserMetadata("id", this.id.toString());
        metadata.addUserMetadata("signDate", HCPExpressionHelper.formatTemporalAccessor(this.signDate));
        return metadata;
    }

    public static MyObject fromS3Metadata(ObjectMetadata metadata){
        MyObject myObject = new MyObject();
        myObject.owner = metadata.getUserMetaDataOf("owner");
        myObject.id = Long.valueOf(metadata.getUserMetaDataOf("id"));
        myObject.signDate = ZonedDateTime.parse(metadata.getUserMetaDataOf("signDate"), HCPExpressionHelper.dateTimeFormatter);
        return myObject;
    }
}
