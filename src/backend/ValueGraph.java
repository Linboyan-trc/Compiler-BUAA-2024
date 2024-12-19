package backend;

import backend.Block.*;
import midend.MidCode.Value.Value;

import java.util.*;
import java.util.stream.Collectors;

public class ValueGraph {
    // 1. 这个冲突图对应的函数块
    private FuncBlock funcBlock;
    private HashMap<Value, HashSet<Value>> valueGraph = new HashMap<>();
    private HashSet<Value> unregisteredValues = new HashSet<>();

    public ValueGraph(FuncBlock funcBlock) {
        this.funcBlock = funcBlock;
    }

    public boolean isEmpty() {
        return valueGraph.isEmpty();
    }

    public HashSet<Value> getUnregisteredValues() {
        return unregisteredValues;
    }

    public void addValue(Value value) {
        if (!valueGraph.containsKey(value) && !unregisteredValues.contains(value)) {
            valueGraph.put(value, new HashSet<>());
        }
    }

    public void addEdge(Value a, Value b) {
        if (unregisteredValues.contains(a) || unregisteredValues.contains(b)) return;
        if (!valueGraph.containsKey(a)) {
            valueGraph.put(a, new HashSet<>());
        }
        if (!valueGraph.containsKey(b)) {
            valueGraph.put(b, new HashSet<>());
        }
        if (a.equals(b)) return;
        valueGraph.get(a).add(b);
        valueGraph.get(b).add(a);
        if (valueGraph.get(a).size() > 1000) {
            unregisteredValues.add(a);
            removeValue(a);
        }
        if (valueGraph.get(b).size() > 1000) {
            unregisteredValues.add(b);
            removeValue(b);
        }
    }

    public Value findValue(int size) {
        for (Value value : valueGraph.keySet()) {
            if (valueGraph.get(value).size() < size) {
                return value;
            }
        }
        return null;
    }

    public HashSet<Value> removeValue(Value value) {
        HashSet<Value> edges = valueGraph.remove(value);
        for (Value v : valueGraph.keySet()) {
            valueGraph.get(v).remove(value);
        }
        return edges;
    }

    public Value discardValue() {
        return valueGraph.keySet().stream()
                .min(Comparator.comparingDouble(a ->
                        Math.pow(2, funcBlock.getLevalValue(a)) / valueGraph.get(a).size()))
                .orElseThrow(() -> new NoSuchElementException("No elements in valueGraph"));
    }

    public void store(Value value, HashSet<Value> values) {
        valueGraph.put(value, values);
        for (Value v : values) {
            if (valueGraph.containsKey(v)) {
                valueGraph.get(v).add(value);
            }
        }
    }
}
