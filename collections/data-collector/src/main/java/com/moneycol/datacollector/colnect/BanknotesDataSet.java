package com.moneycol.datacollector.colnect;

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
    private List<BanknoteData> banknotes;
}
