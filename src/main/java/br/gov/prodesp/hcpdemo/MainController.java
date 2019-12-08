package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.hcpModel.HCPDirectory;
import br.gov.prodesp.hcpdemo.hcpModel.HCPDirectoryMetadata;
import br.gov.prodesp.hcpdemo.hcpModel.HCPObject;
import com.google.common.collect.ObjectArrays;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

@Data
@RestController
public class MainController {
    private final MainService mainService;

    private static String[] pathFromElements(List<PathContainer.Element> elements) {
        return elements.stream()
                .filter(e -> e instanceof PathContainer.PathSegment)
                .skip(1)
                .map(PathContainer.Element::value)
                .toArray(String[]::new);
    }

    @GetMapping("download/**")
    public Mono<ResponseEntity<byte[]>> download(ServerHttpRequest request) {
        return mainService.getData(pathFromElements(request.getPath().elements()));
    }

    @GetMapping("metadata/**")
    public Mono<HCPObject> metadata(ServerHttpRequest request) {
        return mainService.getObjectMetadata(pathFromElements(request.getPath().elements()));
    }

    @GetMapping("metadata-annotation/**")
    public Mono<HCPObject> metadataWithAnnotation(ServerHttpRequest request) {
        return mainService.getObjectMetadataWithAnnotation(pathFromElements(request.getPath().elements()));
    }

    @GetMapping("metadata-annotation-url-name")
    public Mono<HCPObject> metadataWithAnnotationFQDN(@RequestParam("fqdn") String fqdn){
        return mainService.getObjectMetadataWithAnnotationFQDN(fqdn);
    }

    @GetMapping("directory/**")
    public Mono<HCPDirectory> directory(ServerHttpRequest request) {
        return mainService.getDirectory(pathFromElements(request.getPath().elements()));
    }

    @PostMapping("directory/**")
    public Mono<ResponseEntity<Void>> createDirectory(ServerHttpRequest request) {
        return mainService.createDirectory(pathFromElements(request.getPath().elements()));
    }

    @GetMapping("directory-metadata/**")
    public Mono<HCPDirectoryMetadata> directoryWithMetadata(ServerHttpRequest request) {
        String[] pathElements = pathFromElements(request.getPath().elements());
        return mainService.getDirectory(pathElements)
                .flatMapMany(r -> Flux.fromIterable(r.getEntries()).filter(i -> i.getType().equals("object")))
                .flatMap(i -> mainService.getObjectMetadata(ObjectArrays.concat(pathElements, i.getUrlName()))).collectList().map(r -> {
                    HCPDirectoryMetadata hcpDirectoryMetadata = new HCPDirectoryMetadata();
                    hcpDirectoryMetadata.setPath(String.join("/", pathElements));
                    hcpDirectoryMetadata.setObjects(r);
                    return hcpDirectoryMetadata;
                });
    }

    @PostMapping("upload/**")
    public Mono<ResponseEntity<Void>> upload(@RequestPart("file") Mono<FilePart> file, ServerHttpRequest request) {
        return mainService.postData(file, pathFromElements(request.getPath().elements()));
    }

    @PostMapping("upload-annotation/**")
    public Mono<ResponseEntity<Void>> uploadWithAnnotation(
            @RequestPart("file") Mono<FilePart> file,
            @RequestPart("annotation") Mono<FormFieldPart> annotation,
            @RequestPart("size") Mono<FormFieldPart> size,
            ServerHttpRequest request){
        return mainService.postDataWithAnnotation(file, annotation, size, pathFromElements(request.getPath().elements()));
    }

    @DeleteMapping("delete/**")
    public Mono<ResponseEntity<Void>> delete(ServerHttpRequest request) {
        return mainService.delete(pathFromElements(request.getPath().elements()));
    }
}
