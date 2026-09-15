package com.paias.air.model.n4j.projection;

import java.time.YearMonth;

public record CountryRoutesInMonthCount(
    YearMonth month,
    long count
){}