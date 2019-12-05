package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.hcpModel.HCPDirectory;
import br.gov.prodesp.hcpdemo.hcpModel.HCPDirectoryMetadata;
import br.gov.prodesp.hcpdemo.hcpModel.HCPObject;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Data
@RestController
public class MainController {
    private final MainService mainService;

    @GetMapping("download/{urlName}")
    public Mono<ResponseEntity<byte[]>> download(@PathVariable String urlName) {
        return mainService.getData(urlName);
    }

    @GetMapping("metadata/{id}")
    public Mono<HCPObject> metadata(@PathVariable String id) {
        return mainService.getObjectMetadata(id);
    }

    @GetMapping("metadata-annotation/{urlName}")
    public Mono<HCPObject> metadataWithAnnotation(@PathVariable String urlName) {
        return mainService.getObjectMetadataWithAnnotation(urlName);
    }

    @GetMapping("directory/{urlName}")
    public Mono<HCPDirectory> directory(@PathVariable String urlName) {
        return mainService.getDirectory(urlName);
    }

    @GetMapping("directory-metadata/{urlName}")
    public Mono<HCPDirectoryMetadata> directoryWithMetadata(@PathVariable String urlName) {
        return mainService.getDirectory(urlName)
                .flatMapMany(r -> Flux.fromIterable(r.getEntries()).filter(i -> i.getType().equals("object")))
                .flatMap(i -> mainService.getObjectMetadata(i.getUrlName())).collectList().map(r -> {
                    HCPDirectoryMetadata hcpDirectoryMetadata = new HCPDirectoryMetadata();
                    hcpDirectoryMetadata.setPath(urlName);
                    hcpDirectoryMetadata.setObjects(r);
                    return hcpDirectoryMetadata;
                });
    }

    @PostMapping("upload")
    public Mono<ResponseEntity<Void>> upload(@RequestPart("file") Mono<FilePart> file) {
        return mainService.postData(file);
    }

    @PostMapping("upload-annotation")
    public Mono<ResponseEntity<Void>> uploadWithAnnotation(
            @RequestPart("file") Mono<FilePart> file,
            @RequestPart("annotation") Mono<FormFieldPart> annotation,
            @RequestPart("size") Mono<FormFieldPart> size){
        return mainService.postDataWithAnnotation(file, annotation, size);
    }

    @DeleteMapping("delete/{urlName}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String urlName) {
        return mainService.delete(urlName);
    }
}
