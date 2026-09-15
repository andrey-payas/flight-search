package com.paias.air.n4j;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.YearMonth;

@WritingConverter
public class YearMonthWriteConverter
        implements Converter<YearMonth, Value> {

    @Override
    public Value convert(YearMonth source) {
        return Values.value(source.toString());
    }
}