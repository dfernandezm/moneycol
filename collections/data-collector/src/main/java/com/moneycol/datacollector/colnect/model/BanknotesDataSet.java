package com.moneycol.datacollector.colnect.model;

import com.moneycol.datacollector.colnect.pages.BanknoteData;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Data
@Getter
@Setter
public class BanknotesDataSet {
    private String country;
    private Integer pageNumber;
    private String language;
    private List<BanknoteData> banknotes;
}
