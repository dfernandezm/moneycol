package com.moneycol.indexer.worker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor // for Jackson
@AllArgsConstructor
public class BanknotesDataSet {
    private String country;
    private Integer pageNumber;
    private String language;
    private String filename;

    @Builder.Default
    private List<BanknoteData> banknotes = new ArrayList<>();
}