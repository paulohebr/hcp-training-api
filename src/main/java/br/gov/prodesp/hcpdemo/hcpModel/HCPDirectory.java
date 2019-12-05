package br.gov.prodesp.hcpdemo.hcpModel;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@JacksonXmlRootElement(localName = "directory")
public class HCPDirectory {
    @JacksonXmlProperty(localName = "entry")
    @JacksonXmlElementWrapper(useWrapping = false)
    @NotNull
    private List<HCPEntry> entries;

    @JacksonXmlProperty(isAttribute = true)
    private String path;

    public static HCPDirectory error(String path) {
        HCPDirectory hcpDirectory = new HCPDirectory();
        ArrayList<HCPEntry> entries = new ArrayList<>();
        HCPEntry entry = new HCPEntry();
        entry.setUrlName("error");
        entries.add(entry);
        hcpDirectory.setEntries(entries);
        hcpDirectory.setPath(path);
        return hcpDirectory;
    }
}
