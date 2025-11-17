/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of Graph using edges list representation.
 *
 * <p>PS2 instructions: you MUST use the provided rep.
 */
public class ConcreteEdgesGraph<L> implements Graph<L> {

    private final Set<L> vertices = new HashSet<>();
    private final List<Edge<L>> edges = new ArrayList<>();

    // Abstraction function:
    //   Represents a weighted directed graph where:
    //   - vertices set contains all vertex labels in the graph
    //   - edges list contains all directed edges with positive weights
    //   The graph contains vertex v if and only if v is in vertices.
    //   There is an edge from source s to target t with weight w if and only if
    //   there exists an Edge object in edges with source=s, target=t, weight=w.
    //
    // Representation invariant:
    //   - vertices != null, edges != null
    //   - All elements in vertices are non-null
    //   - All edges in edges are valid (non-null source/target, positive weight)
    //   - For every edge in edges: source ∈ vertices and target ∈ vertices
    //   - No duplicate edges (same source and target) in edges list
    //   - All edge weights are positive integers
    //
    // Safety from rep exposure:
    //   - All fields are private and final
    //   - vertices and edges are mutable collections, but:
    //     - vertices() returns an unmodifiable view of vertices
    //     - sources() and targets() return new unmodifiable maps
    //     - Edge objects are immutable
    //   - No direct references to internal collections are exposed

    /**
     * Construct an empty ConcreteEdgesGraph.
     */
    public ConcreteEdgesGraph() {
        checkRep();
    }

    /**
     * Check the representation invariant.
     */
    private void checkRep() {
        assert vertices != null : "vertices should not be null";
        assert edges != null : "edges should not be null";

        // Check all vertices are non-null
        for (L vertex : vertices) {
            assert vertex != null : "vertex should not be null";
        }

        // Check all edges are valid
        for (Edge<L> edge : edges) {
            assert edge != null : "edge should not be null";
            assert vertices.contains(edge.getSource()) :
                    "edge source must be in vertices: " + edge.getSource();
            assert vertices.contains(edge.getTarget()) :
                    "edge target must be in vertices: " + edge.getTarget();
            assert edge.getWeight() > 0 : "edge weight must be positive: " + edge.getWeight();
        }

        // Check no duplicate edges (same source and target)
        Set<String> edgeKeys = new HashSet<>();
        for (Edge<L> edge : edges) {
            String key = edge.getSource() + "->" + edge.getTarget();
            assert !edgeKeys.contains(key) : "duplicate edge: " + key;
            edgeKeys.add(key);
        }
    }

    @Override
    public boolean add(L vertex) {
        if (vertex == null) {
            throw new NullPointerException("vertex cannot be null");
        }

        boolean added = vertices.add(vertex);
        checkRep();
        return added;
    }

    @Override
    public int set(L source, L target, int weight) {
        if (source == null || target == null) {
            throw new NullPointerException("source and target cannot be null");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("weight cannot be negative: " + weight);
        }

        // Ensure vertices exist
        vertices.add(source);
        vertices.add(target);

        // Find existing edge and remove it if exists
        Edge<L> existingEdge = null;
        for (Edge<L> edge : edges) {
            if (edge.getSource().equals(source) && edge.getTarget().equals(target)) {
                existingEdge = edge;
                break;
            }
        }

        int previousWeight = 0;
        if (existingEdge != null) {
            previousWeight = existingEdge.getWeight();
            edges.remove(existingEdge);
        }

        // Add new edge if weight > 0
        if (weight > 0) {
            edges.add(new Edge<L>(source, target, weight));
        }

        checkRep();
        return previousWeight;
    }

    @Override
    public boolean remove(L vertex) {
        if (vertex == null) {
            throw new NullPointerException("vertex cannot be null");
        }

        if (!vertices.contains(vertex)) {
            return false;
        }

        // Remove vertex
        vertices.remove(vertex);

        // Remove all edges involving this vertex
        edges.removeIf(edge ->
                edge.getSource().equals(vertex) || edge.getTarget().equals(vertex));

        checkRep();
        return true;
    }

    @Override
    public Set<L> vertices() {
        return Collections.unmodifiableSet(vertices);
    }

    @Override
    public Map<L, Integer> sources(L target) {
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        }

        Map<L, Integer> sourcesMap = new HashMap<>();
        for (Edge<L> edge : edges) {
            if (edge.getTarget().equals(target)) {
                sourcesMap.put(edge.getSource(), edge.getWeight());
            }
        }
        return Collections.unmodifiableMap(sourcesMap);
    }

    @Override
    public Map<L, Integer> targets(L source) {
        if (source == null) {
            throw new NullPointerException("source cannot be null");
        }

        Map<L, Integer> targetsMap = new HashMap<>();
        for (Edge<L> edge : edges) {
            if (edge.getSource().equals(source)) {
                targetsMap.put(edge.getTarget(), edge.getWeight());
            }
        }
        return Collections.unmodifiableMap(targetsMap);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConcreteEdgesGraph with ").append(vertices.size()).append(" vertices and ").append(edges.size()).append(" edges:\n");
        sb.append("Vertices: ").append(vertices).append("\n");
        sb.append("Edges:\n");
        for (Edge<L> edge : edges) {
            sb.append("  ").append(edge).append("\n");
        }
        return sb.toString();
    }
}

/**
 * Immutable directed edge in a weighted graph.
 *
 * This class represents a directed edge from source to target with a positive weight.
 * Edges are immutable and cannot be modified after creation.
 */
class Edge<L> {

    private final L source;
    private final L target;
    private final int weight;

    // Abstraction function:
    //   Represents a directed edge from source to target with weight
    //
    // Representation invariant:
    //   - source != null, target != null
    //   - weight > 0
    //
    // Safety from rep exposure:
    //   - All fields are private and final
    //   - All getters return immutable types or copies
    //   - No mutator methods

    /**
     * Create a new directed edge.
     *
     * @param source source vertex label, must not be null
     * @param target target vertex label, must not be null
     * @param weight edge weight, must be positive
     * @throws IllegalArgumentException if source or target is null, or weight is not positive
     */
    public Edge(L source, L target, int weight) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("source and target cannot be null");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be positive: " + weight);
        }

        this.source = source;
        this.target = target;
        this.weight = weight;

        checkRep();
    }

    /**
     * Check the representation invariant.
     */
    private void checkRep() {
        assert source != null : "source cannot be null";
        assert target != null : "target cannot be null";
        assert weight > 0 : "weight must be positive: " + weight;
    }

    /**
     * Get the source vertex of this edge.
     *
     * @return source vertex label
     */
    public L getSource() {
        return source;
    }

    /**
     * Get the target vertex of this edge.
     *
     * @return target vertex label
     */
    public L getTarget() {
        return target;
    }

    /**
     * Get the weight of this edge.
     *
     * @return edge weight
     */
    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " -> " + target + " (" + weight + ")";
    }

    // Note: We are not implementing equals() and hashCode() as per problem instructions
    // This means we cannot use Set<Edge> or Map<Edge, ...> effectively
}