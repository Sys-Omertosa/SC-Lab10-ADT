/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of Graph using vertices list representation.
 *
 * <p>PS2 instructions: you MUST use the provided rep.
 */
public class ConcreteVerticesGraph<L> implements Graph<L> {

    private final List<Vertex<L>> vertices = new ArrayList<>();

    // Abstraction function:
    //   Represents a weighted directed graph where:
    //   - The vertices list contains Vertex objects representing all vertices
    //   - Each Vertex object maintains its outgoing edges (targets and weights)
    //   The graph contains vertex v if and only if there exists a Vertex in vertices with label v.
    //   There is an edge from source s to target t with weight w if and only if
    //   the Vertex with label s has an outgoing edge to t with weight w.
    //
    // Representation invariant:
    //   - vertices != null
    //   - All Vertex objects in vertices are non-null and valid
    //   - No duplicate vertex labels in vertices list
    //   - All edge weights in all vertices are positive
    //   - For every edge s->t with weight w in any vertex, there exists a vertex with label t
    //
    // Safety from rep exposure:
    //   - vertices field is private and final
    //   - vertices() returns an unmodifiable set of vertex labels (not Vertex objects)
    //   - sources() and targets() return new unmodifiable maps
    //   - Vertex objects are mutable but not directly exposed to clients

    /**
     * Construct an empty ConcreteVerticesGraph.
     */
    public ConcreteVerticesGraph() {
        checkRep();
    }

    /**
     * Check the representation invariant.
     */
    private void checkRep() {
        assert vertices != null : "vertices should not be null";

        Set<L> vertexLabels = vertices.stream()
                .map(Vertex::getLabel)
                .collect(Collectors.toSet());

        // Check no duplicate vertex labels
        assert vertexLabels.size() == vertices.size() : "duplicate vertex labels found";

        // Check all vertices are valid and their edges point to existing vertices
        for (Vertex<L> vertex : vertices) {
            assert vertex != null : "vertex should not be null";
            vertex.checkRep();

            // Check all targets of this vertex exist in the graph
            for (L target : vertex.getTargets().keySet()) {
                assert vertexLabels.contains(target) :
                        "target vertex does not exist: " + target;
            }
        }
    }

    @Override
    public boolean add(L vertex) {
        if (vertex == null) {
            throw new NullPointerException("vertex cannot be null");
        }

        // Check if vertex already exists
        for (Vertex<L> v : vertices) {
            if (v.getLabel().equals(vertex)) {
                return false;
            }
        }

        // Add new vertex
        vertices.add(new Vertex<L>(vertex));
        checkRep();
        return true;
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
        add(source);
        add(target);

        // Find source vertex and set the edge
        Vertex<L> sourceVertex = findVertex(source);
        assert sourceVertex != null : "source vertex should exist";

        int previousWeight = sourceVertex.setEdge(target, weight);

        checkRep();
        return previousWeight;
    }

    @Override
    public boolean remove(L vertex) {
        if (vertex == null) {
            throw new NullPointerException("vertex cannot be null");
        }

        // Find and remove the vertex
        Vertex<L> vertexToRemove = findVertex(vertex);
        if (vertexToRemove == null) {
            return false;
        }

        vertices.remove(vertexToRemove);

        // Remove all edges pointing to this vertex from other vertices
        for (Vertex<L> v : vertices) {
            v.removeEdgeTo(vertex);
        }

        checkRep();
        return true;
    }

    @Override
    public Set<L> vertices() {
        return Collections.unmodifiableSet(vertices.stream()
                .map(Vertex::getLabel)
                .collect(Collectors.toSet()));
    }

    @Override
    public Map<L, Integer> sources(L target) {
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        }

        Map<L, Integer> sourcesMap = new HashMap<>();
        for (Vertex<L> vertex : vertices) {
            Integer weight = vertex.getEdgeWeight(target);
            if (weight != null) {
                sourcesMap.put(vertex.getLabel(), weight);
            }
        }
        return Collections.unmodifiableMap(sourcesMap);
    }

    @Override
    public Map<L, Integer> targets(L source) {
        if (source == null) {
            throw new NullPointerException("source cannot be null");
        }

        Vertex<L> sourceVertex = findVertex(source);
        if (sourceVertex == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(new HashMap<>(sourceVertex.getTargets()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConcreteVerticesGraph with ").append(vertices.size()).append(" vertices:\n");
        for (Vertex<L> vertex : vertices) {
            sb.append("  ").append(vertex).append("\n");
        }
        return sb.toString();
    }

    /**
     * Find a vertex by label.
     *
     * @param label vertex label to find
     * @return the Vertex object with the given label, or null if not found
     */
    private Vertex<L> findVertex(L label) {
        for (Vertex<L> vertex : vertices) {
            if (vertex.getLabel().equals(label)) {
                return vertex;
            }
        }
        return null;
    }
}

/**
 * Mutable vertex in a weighted directed graph.
 *
 * This class represents a vertex that maintains its outgoing edges.
 * The vertex is mutable - edges can be added, updated, or removed.
 */
class Vertex<L> {

    private final L label;
    private final Map<L, Integer> targets; // target vertex -> weight

    // Abstraction function:
    //   Represents a vertex with label and outgoing edges
    //   The targets map represents edges from this vertex to other vertices
    //
    // Representation invariant:
    //   - label != null
    //   - targets != null
    //   - All weights in targets are positive integers
    //   - All target labels are non-null
    //
    // Safety from rep exposure:
    //   - label is final and immutable
    //   - targets is mutable but:
    //     - getTargets() returns a copy of the map
    //     - No direct reference to internal targets map is exposed

    /**
     * Create a new vertex with the given label.
     *
     * @param label vertex label, must not be null
     * @throws IllegalArgumentException if label is null
     */
    public Vertex(L label) {
        if (label == null) {
            throw new IllegalArgumentException("label cannot be null");
        }

        this.label = label;
        this.targets = new HashMap<>();

        checkRep();
    }

    /**
     * Check the representation invariant.
     */
    public void checkRep() {
        assert label != null : "label cannot be null";
        assert targets != null : "targets cannot be null";

        for (Map.Entry<L, Integer> entry : targets.entrySet()) {
            assert entry.getKey() != null : "target vertex cannot be null";
            assert entry.getValue() > 0 : "edge weight must be positive: " + entry.getValue();
        }
    }

    /**
     * Get the label of this vertex.
     *
     * @return vertex label
     */
    public L getLabel() {
        return label;
    }

    /**
     * Get a copy of the outgoing edges from this vertex.
     *
     * @return map from target vertex labels to edge weights
     */
    public Map<L, Integer> getTargets() {
        return new HashMap<>(targets);
    }

    /**
     * Set or update an outgoing edge from this vertex.
     *
     * @param target target vertex label
     * @param weight edge weight (0 to remove, positive to add/update)
     * @return previous weight of the edge, or 0 if no previous edge
     */
    public int setEdge(L target, int weight) {
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        }

        Integer previousWeight = targets.get(target);

        if (weight > 0) {
            targets.put(target, weight);
        } else if (weight == 0) {
            targets.remove(target);
        } else {
            throw new IllegalArgumentException("weight cannot be negative: " + weight);
        }

        checkRep();
        return previousWeight != null ? previousWeight : 0;
    }

    /**
     * Remove an outgoing edge to the specified target.
     *
     * @param target target vertex label
     * @return true if an edge was removed, false if no such edge existed
     */
    public boolean removeEdgeTo(L target) {
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        }

        boolean removed = targets.remove(target) != null;
        checkRep();
        return removed;
    }

    /**
     * Get the weight of the edge to the specified target.
     *
     * @param target target vertex label
     * @return edge weight, or null if no edge exists
     */
    public Integer getEdgeWeight(L target) {
        if (target == null) {
            throw new NullPointerException("target cannot be null");
        }

        return targets.get(target);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vertex '").append(label).append("'");
        if (targets.isEmpty()) {
            sb.append(" (no outgoing edges)");
        } else {
            sb.append(" -> ");
            boolean first = true;
            for (Map.Entry<L, Integer> entry : targets.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("(").append(entry.getValue()).append(")");
                first = false;
            }
        }
        return sb.toString();
    }
}