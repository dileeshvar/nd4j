package org.nd4j.linalg.api.ops.aggregates;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Pointer;
import org.nd4j.linalg.api.ops.aggregates.Aggregate;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for "batch of aggregates"
 *
 * @author raver119@gmail.com
 */
@Slf4j
public class Batch<T extends Aggregate> {
    // we
    @Getter @Setter private Pointer paramsSurface;
    @Getter private List<T> aggregates;
    @Getter private static final int batchLimit = 512;
    private T sample;
    @Getter private int numAggregates;


    public Batch(List<T> aggregates) {
        if (aggregates.size() > batchLimit)
            throw new RuntimeException("Number of aggregates is higher then 512 elements, multiple batches should be issued.");

        this.aggregates = aggregates;
        this.numAggregates = aggregates.size();

        // we fetch single sample for possible future use. not sure if will be used though
        this.sample = aggregates.get(0);
    }

    /**
     * This method tries to append aggregate to the current batch, if it has free room
     *
     * @param aggregate
     * @return
     */
    public boolean append(T aggregate) {
       if (!isFull()) {
           aggregates.add(aggregate);
           return true;
       } else return false;
    }

    /**
     * This method checks, if number of batched aggregates equals to maximum possible value
     *
     * @return
     */
    public boolean isFull() {
        return batchLimit == numAggregates;
    }

    /**
     * Helper method to create batch from list of aggregates, for cases when list of aggregates is higher then batchLimit
     *
     * @param list
     * @param <U>
     * @return
     */
    public static <U extends Aggregate> List<Batch<U>> getBatches(List<U> list) {
        List<List<U>> partitions =  Lists.partition(list, batchLimit);
        List<Batch<U>> split = new ArrayList<>();

        for (List<U> partition: partitions) {
            split.add(new Batch<U>(partition));
        }

        return split;
    }
}
