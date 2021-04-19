/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.r2dbc.spi.test;

import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class MockResult implements Result {

    private final Mono<RowMetadata> rowMetadata;

    private final Flux<Row> rows;

    private final Flux<Integer> rowsUpdated;

    private MockResult(Mono<RowMetadata> rowMetadata, Flux<Row> rows, Flux<Integer> rowsUpdated) {
        this.rowMetadata = Assert.requireNonNull(rowMetadata, "rowMetadata must not be null");
        this.rows = Assert.requireNonNull(rows, "rows must not be null");
        this.rowsUpdated = Assert.requireNonNull(rowsUpdated, "rowsUpdated must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MockResult empty() {
        return builder().build();
    }

    @Override
    public Flux<Integer> getRowsUpdated() {
        return this.rowsUpdated;
    }

    @Override
    public <T> Flux<T> map(BiFunction<Row, RowMetadata, ? extends T> mappingFunction) {
        Assert.requireNonNull(mappingFunction, "f must not be null");

        return this.rows
            .zipWith(this.rowMetadata.repeat())
            .map((tuple) -> {
                Row row = tuple.getT1();
                RowMetadata rowMetadata = tuple.getT2();

                return mappingFunction.apply(row, rowMetadata);
            });
    }

    @Override
    public String toString() {
        return "MockResult{" +
            "rowMetadata=" + this.rowMetadata +
            ", rows=" + this.rows +
            ", rowsUpdated=" + this.rowsUpdated +
            '}';
    }

    // TODO: TO BE DONE AFTER API STABILITY
    @Override
    public Result filter(Predicate<Segment> filter) {
        return null;
    }

    @Override
    public <T> Publisher<T> flatMap(Function<Segment, ? extends Publisher<T>> mappingFunction) {
        return null;
    }

    public static final class Builder {

        private final List<Row> rows = new ArrayList<>();

        private final List<Integer> rowsUpdated = new ArrayList<>();

        private RowMetadata rowMetadata;

        private Builder() {
        }

        public MockResult build() {
            return new MockResult(Mono.justOrEmpty(this.rowMetadata), Flux.fromIterable(this.rows), Flux.fromIterable(this.rowsUpdated));
        }

        public Builder row(Row... rows) {
            Assert.requireNonNull(rows, "rows must not be null");

            Stream.of(rows)
                .peek(row -> Assert.requireNonNull(row, "row must not be null"))
                .forEach(this.rows::add);

            return this;
        }

        public Builder rowMetadata(RowMetadata rowMetadata) {
            this.rowMetadata = Assert.requireNonNull(rowMetadata, "rowMetadata must not be null");
            return this;
        }

        public Builder rowsUpdated(Integer rowsUpdated) {
            Assert.requireNonNull(rowsUpdated, "rowsUpdated must not be null");

            this.rowsUpdated.add(rowsUpdated);
            return this;
        }

        @Override
        public String toString() {
            return "Builder{" +
                "rowMetadata=" + this.rowMetadata +
                ", rows=" + this.rows +
                ", rowsUpdated=" + this.rowsUpdated +
                '}';
        }

    }

}
