package com.paias.air.n4j;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.YearMonth;

@ReadingConverter
public class YearMonthReadConverter
        implements Converter<Value, YearMonth> {

    @Override
    public YearMonth convert(Value source) {
        return YearMonth.parse(Values.ofString().apply(source));
    }
}