package com.moneycol.collections.server.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
//TODO: there should be several Dtos (command input, and outputs)
//TODO: Possible not working due to old java 8
//@RequiredArgsConstructor(onConstructor=@__(@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)))
public class CollectionDTO2 {
    private String id;
    private String name;
    private String description;
    private List<CollectionItemDTO> items;

    @JsonCreator
    public CollectionDTO2(@JsonProperty("id")  String id,
                          @JsonProperty("name")  String name,
                          @JsonProperty("description") String description,
                          @JsonProperty("items") List<CollectionItemDTO> items) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.items = items;
    }
}
