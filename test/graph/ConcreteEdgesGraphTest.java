/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

/**
 * Tests for ConcreteEdgesGraph.
 *
 * This class runs the GraphInstanceTest tests against ConcreteEdgesGraph, as
 * well as tests for that particular implementation.
 *
 * Tests against the Graph spec should be in GraphInstanceTest.
 */
public class ConcreteEdgesGraphTest extends GraphInstanceTest {

    /*
     * Provide a ConcreteEdgesGraph for tests in GraphInstanceTest.
     */
    @Override public Graph<String> emptyInstance() {
        return new ConcreteEdgesGraph();
    }

    /*
     * Testing ConcreteEdgesGraph...
     */

    // Testing strategy for ConcreteEdgesGraph.toString()
    //   - empty graph
    //   - graph with vertices but no edges
    //   - graph with vertices and edges
    //   - verify format includes vertices count, edges count, vertices list, and edges list

    @Test
    public void testToStringEmptyGraph() {
        Graph<String> graph = emptyInstance();
        String result = graph.toString();
        assertTrue("should mention vertices count", result.contains("0 vertices"));
        assertTrue("should mention edges count", result.contains("0 edges"));
        assertTrue("should list vertices", result.contains("Vertices: []"));
    }

    @Test
    public void testToStringWithVerticesOnly() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        graph.add("B");
        String result = graph.toString();
        assertTrue("should contain vertex A", result.contains("A"));
        assertTrue("should contain vertex B", result.contains("B"));
        assertTrue("should mention 0 edges", result.contains("0 edges"));
    }

    @Test
    public void testToStringWithEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 3);
        graph.set("B", "C", 5);
        String result = graph.toString();
        assertTrue("should contain edge A->B", result.contains("A -> B (3)"));
        assertTrue("should contain edge B->C", result.contains("B -> C (5)"));
        assertTrue("should mention 2 edges", result.contains("2 edges"));
    }

    /*
     * Testing Edge...
     */

    // Testing strategy for Edge
    //   - constructor with valid parameters
    //   - constructor with null source/target
    //   - constructor with non-positive weight
    //   - getSource(), getTarget(), getWeight()
    //   - toString() format

    @Test
    public void testEdgeConstructorValid() {
        Edge edge = new Edge("A", "B", 5);
        assertEquals("source should be A", "A", edge.getSource());
        assertEquals("target should be B", "B", edge.getTarget());
        assertEquals("weight should be 5", 5, edge.getWeight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeConstructorNullSource() {
        new Edge(null, "B", 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeConstructorNullTarget() {
        new Edge("A", null, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeConstructorZeroWeight() {
        new Edge("A", "B", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeConstructorNegativeWeight() {
        new Edge("A", "B", -1);
    }

    @Test
    public void testEdgeToString() {
        Edge edge = new Edge("Source", "Target", 42);
        String result = edge.toString();
        assertTrue("should contain source", result.contains("Source"));
        assertTrue("should contain target", result.contains("Target"));
        assertTrue("should contain weight", result.contains("42"));
        assertEquals("should have correct format", "Source -> Target (42)", result);
    }

    // Test that edges are properly used in graph operations
    @Test
    public void testGraphUsesEdgesCorrectly() {
        ConcreteEdgesGraph graph = new ConcreteEdgesGraph();
        graph.set("X", "Y", 10);
        graph.set("Y", "Z", 20);

        // Test targets
        Map<String, Integer> targets = graph.targets("X");
        assertEquals("should have one target", 1, targets.size());
        assertEquals("should have correct weight", Integer.valueOf(10), targets.get("Y"));

        // Test sources
        Map<String, Integer> sources = graph.sources("Z");
        assertEquals("should have one source", 1, sources.size());
        assertEquals("should have correct weight", Integer.valueOf(20), sources.get("Y"));
    }
}