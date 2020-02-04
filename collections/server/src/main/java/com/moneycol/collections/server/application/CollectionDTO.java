package com.moneycol.collections.server.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//TODO: there should be several Dtos (command input, and outputs)
//TODO: Possible not working due to old java 8
//@RequiredArgsConstructor(onConstructor=@__(@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)))
public class CollectionDTO {
    private String id;
    private  String name;
    private  String description;
    private  String collectorId;

    @JsonCreator
    public CollectionDTO(@JsonProperty("id")  String id,
                         @JsonProperty("name")  String name,
                         @JsonProperty("description") String description,
                         @JsonProperty("collectorId") String collectorId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.collectorId = collectorId;
    }
}
