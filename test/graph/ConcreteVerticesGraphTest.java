/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

/**
 * Tests for ConcreteVerticesGraph.
 *
 * This class runs the GraphInstanceTest tests against ConcreteVerticesGraph, as
 * well as tests for that particular implementation.
 *
 * Tests against the Graph spec should be in GraphInstanceTest.
 */
public class ConcreteVerticesGraphTest extends GraphInstanceTest {

    /*
     * Provide a ConcreteVerticesGraph for tests in GraphInstanceTest.
     */
    @Override public Graph<String> emptyInstance() {
        return new ConcreteVerticesGraph();
    }

    /*
     * Testing ConcreteVerticesGraph...
     */

    // Testing strategy for ConcreteVerticesGraph.toString()
    //   - empty graph
    //   - graph with vertices but no edges
    //   - graph with vertices and edges
    //   - verify format includes vertices count and each vertex's representation

    @Test
    public void testToStringEmptyGraph() {
        Graph<String> graph = emptyInstance();
        String result = graph.toString();
        assertTrue("should mention vertices count", result.contains("0 vertices"));
    }

    @Test
    public void testToStringWithVerticesOnly() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        graph.add("B");
        String result = graph.toString();
        assertTrue("should contain vertex A", result.contains("Vertex 'A'"));
        assertTrue("should contain vertex B", result.contains("Vertex 'B'"));
        assertTrue("should mention no outgoing edges", result.contains("no outgoing edges"));
    }

    @Test
    public void testToStringWithEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 3);
        graph.set("A", "C", 5);
        String result = graph.toString();
        assertTrue("should contain edge A->B", result.contains("B(3)"));
        assertTrue("should contain edge A->C", result.contains("C(5)"));
    }

    /*
     * Testing Vertex...
     */

    // Testing strategy for Vertex
    //   - constructor with valid and invalid parameters
    //   - setEdge with positive weight, zero weight, negative weight
    //   - getLabel(), getTargets(), getEdgeWeight()
    //   - removeEdgeTo()
    //   - toString() format

    @Test
    public void testVertexConstructorValid() {
        Vertex vertex = new Vertex("TestVertex");
        assertEquals("label should be TestVertex", "TestVertex", vertex.getLabel());
        assertTrue("targets should be empty", vertex.getTargets().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVertexConstructorNullLabel() {
        new Vertex(null);
    }

    @Test
    public void testVertexSetEdgeNew() {
        Vertex vertex = new Vertex("A");
        int previousWeight = vertex.setEdge("B", 5);
        assertEquals("should return 0 for new edge", 0, previousWeight);
        assertEquals("should have correct weight", Integer.valueOf(5), vertex.getEdgeWeight("B"));
        assertEquals("should have one target", 1, vertex.getTargets().size());
    }

    @Test
    public void testVertexSetEdgeUpdate() {
        Vertex vertex = new Vertex("A");
        vertex.setEdge("B", 3);
        int previousWeight = vertex.setEdge("B", 7);
        assertEquals("should return previous weight", 3, previousWeight);
        assertEquals("should have updated weight", Integer.valueOf(7), vertex.getEdgeWeight("B"));
    }

    @Test
    public void testVertexSetEdgeRemove() {
        Vertex vertex = new Vertex("A");
        vertex.setEdge("B", 4);
        int previousWeight = vertex.setEdge("B", 0);
        assertEquals("should return previous weight", 4, previousWeight);
        assertNull("edge should be removed", vertex.getEdgeWeight("B"));
        assertTrue("targets should be empty", vertex.getTargets().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVertexSetEdgeNegativeWeight() {
        Vertex vertex = new Vertex("A");
        vertex.setEdge("B", -1);
    }

    @Test
    public void testVertexRemoveEdgeTo() {
        Vertex vertex = new Vertex("A");
        vertex.setEdge("B", 2);
        vertex.setEdge("C", 3);

        assertTrue("should remove existing edge", vertex.removeEdgeTo("B"));
        assertFalse("should not remove non-existent edge", vertex.removeEdgeTo("X"));
        assertNull("edge to B should be gone", vertex.getEdgeWeight("B"));
        assertEquals("edge to C should remain", Integer.valueOf(3), vertex.getEdgeWeight("C"));
    }

    @Test
    public void testVertexToString() {
        Vertex vertex = new Vertex("Test");
        vertex.setEdge("X", 10);
        vertex.setEdge("Y", 20);

        String result = vertex.toString();
        assertTrue("should contain vertex label", result.contains("Test"));
        assertTrue("should contain edge to X", result.contains("X(10)"));
        assertTrue("should contain edge to Y", result.contains("Y(20)"));
    }

    @Test
    public void testVertexGetTargetsReturnsCopy() {
        Vertex vertex = new Vertex("A");
        vertex.setEdge("B", 1);

        Map<String, Integer> targets = vertex.getTargets();
        targets.put("C", 2); // Modify the returned map

        // Original vertex should not be affected
        assertEquals("original should still have one target", 1, vertex.getTargets().size());
        assertNull("C should not be in original", vertex.getEdgeWeight("C"));
    }

    // Test integration between Vertex and ConcreteVerticesGraph
    @Test
    public void testGraphVertexIntegration() {
        ConcreteVerticesGraph graph = new ConcreteVerticesGraph();
        graph.set("P", "Q", 15);
        graph.set("Q", "R", 25);

        // Test targets
        Map<String, Integer> targets = graph.targets("P");
        assertEquals("should have one target", 1, targets.size());
        assertEquals("should have correct weight", Integer.valueOf(15), targets.get("Q"));

        // Test sources
        Map<String, Integer> sources = graph.sources("R");
        assertEquals("should have one source", 1, sources.size());
        assertEquals("should have correct weight", Integer.valueOf(25), sources.get("Q"));
    }
}