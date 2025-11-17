/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package graph;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for instance methods of Graph.
 *
 * <p>PS2 instructions: you MUST NOT add constructors, fields, or non-@Test
 * methods to this class, or change the spec of {@link #emptyInstance()}.
 * Your tests MUST only obtain Graph instances by calling emptyInstance().
 * Your tests MUST NOT refer to specific concrete implementations.
 */
public abstract class GraphInstanceTest {

    // Testing strategy
    //
    // Partition for add(vertex):
    //   vertex: exists in graph, doesn't exist in graph
    //   graph: empty, non-empty
    //   return value: true (added), false (already exists)
    //
    // Partition for set(source, target, weight):
    //   weight: 0, >0
    //   edge: exists, doesn't exist
    //   vertices: both exist, source exists/target doesn't, target exists/source doesn't, neither exist
    //   return value: previous weight (if edge existed), 0 (if no previous edge)
    //
    // Partition for remove(vertex):
    //   vertex: exists in graph, doesn't exist in graph
    //   vertex: has incoming edges, has outgoing edges, has both, has none
    //   return value: true (removed), false (not found)
    //
    // Partition for vertices():
    //   graph: empty, non-empty
    //   return: unmodifiable set
    //
    // Partition for sources(target):
    //   target: exists in graph, doesn't exist in graph
    //   target: has incoming edges, has no incoming edges
    //   return: empty map, non-empty map
    //
    // Partition for targets(source):
    //   source: exists in graph, doesn't exist in graph
    //   source: has outgoing edges, has no outgoing edges
    //   return: empty map, non-empty map

    /**
     * Overridden by implementation-specific test classes.
     *
     * @return a new empty graph of the particular implementation being tested
     */
    public abstract Graph<String> emptyInstance();

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testInitialVerticesEmpty() {
        assertEquals("expected new graph to have no vertices",
                Collections.emptySet(), emptyInstance().vertices());
    }

    // Tests for add(vertex)

    @Test
    public void testAddVertexToEmptyGraph() {
        Graph<String> graph = emptyInstance();
        assertTrue("should be able to add vertex to empty graph", graph.add("A"));
        assertEquals("graph should contain the added vertex", Set.of("A"), graph.vertices());
    }

    @Test
    public void testAddDuplicateVertex() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        assertFalse("adding duplicate vertex should return false", graph.add("A"));
        assertEquals("graph should still contain only one vertex", Set.of("A"), graph.vertices());
    }

    @Test
    public void testAddMultipleVertices() {
        Graph<String> graph = emptyInstance();
        assertTrue(graph.add("A"));
        assertTrue(graph.add("B"));
        assertTrue(graph.add("C"));
        assertEquals("graph should contain all added vertices",
                Set.of("A", "B", "C"), graph.vertices());
    }

    @Test
    public void testAddNullVertex() {
        Graph<String> graph = emptyInstance();
        try {
            graph.add(null);
            fail("expected NullPointerException when adding null vertex");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // Tests for set(source, target, weight)

    @Test
    public void testSetNewEdgeWithNewVertices() {
        Graph<String> graph = emptyInstance();
        int result = graph.set("A", "B", 5);
        assertEquals("should return 0 for new edge", 0, result);
        assertEquals("graph should contain both vertices", Set.of("A", "B"), graph.vertices());
        assertEquals("should have correct edge weight", Integer.valueOf(5), graph.targets("A").get("B"));
    }

    @Test
    public void testSetNewEdgeWithExistingVertices() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        graph.add("B");
        int result = graph.set("A", "B", 3);
        assertEquals("should return 0 for new edge", 0, result);
        assertEquals("should have correct edge weight", Integer.valueOf(3), graph.targets("A").get("B"));
    }

    @Test
    public void testSetUpdateExistingEdge() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 2);
        int result = graph.set("A", "B", 4);
        assertEquals("should return previous weight", 2, result);
        assertEquals("should have updated edge weight", Integer.valueOf(4), graph.targets("A").get("B"));
    }

    @Test
    public void testSetRemoveEdgeWithZeroWeight() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 3);
        int result = graph.set("A", "B", 0);
        assertEquals("should return previous weight", 3, result);
        assertFalse("edge should be removed", graph.targets("A").containsKey("B"));
        assertTrue("vertices should still exist", graph.vertices().containsAll(Set.of("A", "B")));
    }

    @Test
    public void testSetRemoveNonExistentEdge() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        graph.add("B");
        int result = graph.set("A", "B", 0);
        assertEquals("should return 0 for non-existent edge", 0, result);
    }

    @Test
    public void testSetSelfLoop() {
        Graph<String> graph = emptyInstance();
        int result = graph.set("A", "A", 2);
        assertEquals("should return 0 for new self-loop", 0, result);
        assertEquals("should have self-loop edge", Integer.valueOf(2), graph.targets("A").get("A"));
        assertEquals("should have self-loop in sources", Integer.valueOf(2), graph.sources("A").get("A"));
    }

    @Test
    public void testSetMultipleEdgesFromSameSource() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 1);
        graph.set("A", "C", 2);
        graph.set("A", "D", 3);

        Map<String, Integer> expectedTargets = new HashMap<>();
        expectedTargets.put("B", 1);
        expectedTargets.put("C", 2);
        expectedTargets.put("D", 3);

        assertEquals("should have all edges from source A", expectedTargets, graph.targets("A"));
    }

    @Test
    public void testSetMultipleEdgesToSameTarget() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "Z", 1);
        graph.set("B", "Z", 2);
        graph.set("C", "Z", 3);

        Map<String, Integer> expectedSources = new HashMap<>();
        expectedSources.put("A", 1);
        expectedSources.put("B", 2);
        expectedSources.put("C", 3);

        assertEquals("should have all edges to target Z", expectedSources, graph.sources("Z"));
    }

    @Test
    public void testSetWithNegativeWeight() {
        Graph<String> graph = emptyInstance();
        try {
            graph.set("A", "B", -1);
            fail("expected IllegalArgumentException for negative weight");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Tests for remove(vertex)

    @Test
    public void testRemoveVertexFromEmptyGraph() {
        Graph<String> graph = emptyInstance();
        assertFalse("should return false when removing from empty graph", graph.remove("A"));
    }

    @Test
    public void testRemoveExistingVertex() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        assertTrue("should return true when removing existing vertex", graph.remove("A"));
        assertFalse("vertex should no longer be in graph", graph.vertices().contains("A"));
    }

    @Test
    public void testRemoveNonExistentVertex() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        assertFalse("should return false for non-existent vertex", graph.remove("B"));
        assertTrue("graph should still contain vertex A", graph.vertices().contains("A"));
    }

    @Test
    public void testRemoveVertexWithOutgoingEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 1);
        graph.set("A", "C", 2);

        assertTrue(graph.remove("A"));
        assertFalse("vertex A should be removed", graph.vertices().contains("A"));
        assertFalse("edge A->B should be removed", graph.targets("A").containsKey("B"));
        assertFalse("edge A->C should be removed", graph.targets("A").containsKey("C"));
        assertTrue("vertex B should still exist", graph.vertices().contains("B"));
        assertTrue("vertex C should still exist", graph.vertices().contains("C"));
    }

    @Test
    public void testRemoveVertexWithIncomingEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "C", 1);
        graph.set("B", "C", 2);

        assertTrue(graph.remove("C"));
        assertFalse("vertex C should be removed", graph.vertices().contains("C"));
        assertFalse("edge A->C should be removed", graph.targets("A").containsKey("C"));
        assertFalse("edge B->C should be removed", graph.targets("B").containsKey("C"));
        assertTrue("vertex A should still exist", graph.vertices().contains("A"));
        assertTrue("vertex B should still exist", graph.vertices().contains("B"));
    }

    @Test
    public void testRemoveVertexWithBothIncomingAndOutgoingEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 1);  // B has incoming from A
        graph.set("B", "C", 2);  // B has outgoing to C
        graph.set("D", "B", 3);  // B has incoming from D

        assertTrue(graph.remove("B"));
        assertFalse("vertex B should be removed", graph.vertices().contains("B"));
        assertFalse("edge A->B should be removed", graph.targets("A").containsKey("B"));
        assertFalse("edge B->C should be removed", graph.targets("B").containsKey("C"));
        assertFalse("edge D->B should be removed", graph.targets("D").containsKey("B"));

        // Verify other vertices and edges remain
        assertTrue("vertex A should still exist", graph.vertices().contains("A"));
        assertTrue("vertex C should still exist", graph.vertices().contains("C"));
        assertTrue("vertex D should still exist", graph.vertices().contains("D"));
    }

    // Tests for vertices()

    @Test
    public void testVerticesEmptyGraph() {
        Graph<String> graph = emptyInstance();
        assertEquals("empty graph should have empty vertices set",
                Collections.emptySet(), graph.vertices());
    }

    @Test
    public void testVerticesUnmodifiable() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        Set<String> vertices = graph.vertices();
        try {
            vertices.add("B");
            fail("expected UnsupportedOperationException when modifying vertices set");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testVerticesAfterMultipleOperations() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        graph.set("B", "C", 1);  // adds B and C
        graph.remove("A");

        assertEquals("vertices should reflect current state",
                Set.of("B", "C"), graph.vertices());
    }

    // Tests for sources(target)

    @Test
    public void testSourcesForVertexWithNoIncomingEdges() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        assertEquals("should return empty map for vertex with no incoming edges",
                Collections.emptyMap(), graph.sources("A"));
    }

    @Test
    public void testSourcesForNonExistentVertex() {
        Graph<String> graph = emptyInstance();
        assertEquals("should return empty map for non-existent vertex",
                Collections.emptyMap(), graph.sources("X"));
    }

    @Test
    public void testSourcesForVertexWithMultipleIncomingEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "Z", 1);
        graph.set("B", "Z", 2);
        graph.set("C", "Z", 3);

        Map<String, Integer> expected = new HashMap<>();
        expected.put("A", 1);
        expected.put("B", 2);
        expected.put("C", 3);

        assertEquals("should return all sources with correct weights",
                expected, graph.sources("Z"));
    }

    @Test
    public void testSourcesUnmodifiable() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 1);
        Map<String, Integer> sources = graph.sources("B");
        try {
            sources.put("C", 2);
            fail("expected UnsupportedOperationException when modifying sources map");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Tests for targets(source)

    @Test
    public void testTargetsForVertexWithNoOutgoingEdges() {
        Graph<String> graph = emptyInstance();
        graph.add("A");
        assertEquals("should return empty map for vertex with no outgoing edges",
                Collections.emptyMap(), graph.targets("A"));
    }

    @Test
    public void testTargetsForNonExistentVertex() {
        Graph<String> graph = emptyInstance();
        assertEquals("should return empty map for non-existent vertex",
                Collections.emptyMap(), graph.targets("X"));
    }

    @Test
    public void testTargetsForVertexWithMultipleOutgoingEdges() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "X", 1);
        graph.set("A", "Y", 2);
        graph.set("A", "Z", 3);

        Map<String, Integer> expected = new HashMap<>();
        expected.put("X", 1);
        expected.put("Y", 2);
        expected.put("Z", 3);

        assertEquals("should return all targets with correct weights",
                expected, graph.targets("A"));
    }

    @Test
    public void testTargetsUnmodifiable() {
        Graph<String> graph = emptyInstance();
        graph.set("A", "B", 1);
        Map<String, Integer> targets = graph.targets("A");
        try {
            targets.put("C", 2);
            fail("expected UnsupportedOperationException when modifying targets map");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // Integration tests - testing multiple operations together

    @Test
    public void testComplexScenario() {
        Graph<String> graph = emptyInstance();

        // Add some vertices
        graph.add("A");
        graph.add("B");
        graph.add("C");

        // Add edges
        assertEquals(0, graph.set("A", "B", 5));
        assertEquals(0, graph.set("B", "C", 3));
        assertEquals(0, graph.set("A", "C", 2));

        // Verify structure
        assertEquals(Set.of("A", "B", "C"), graph.vertices());

        Map<String, Integer> expectedATargets = new HashMap<>();
        expectedATargets.put("B", 5);
        expectedATargets.put("C", 2);
        assertEquals(expectedATargets, graph.targets("A"));

        Map<String, Integer> expectedCSources = new HashMap<>();
        expectedCSources.put("A", 2);
        expectedCSources.put("B", 3);
        assertEquals(expectedCSources, graph.sources("C"));

        // Update an edge
        assertEquals(5, graph.set("A", "B", 7));
        assertEquals(Integer.valueOf(7), graph.targets("A").get("B"));

        // Remove a vertex
        assertTrue(graph.remove("B"));
        assertEquals(Set.of("A", "C"), graph.vertices());
        assertFalse("edge A->B should be removed", graph.targets("A").containsKey("B"));
        assertFalse("edge B->C should be removed", graph.sources("C").containsKey("B"));
    }

    @Test
    public void testGraphWithMultiplePaths() {
        Graph<String> graph = emptyInstance();

        // Create a more complex graph
        graph.set("A", "B", 1);
        graph.set("B", "C", 1);
        graph.set("A", "C", 2);  // direct path A->C
        graph.set("C", "D", 1);
        graph.set("B", "D", 3);  // path B->D

        // Test various relationships
        assertEquals(Integer.valueOf(1), graph.targets("A").get("B"));
        assertEquals(Integer.valueOf(2), graph.targets("A").get("C"));
        assertEquals(Integer.valueOf(1), graph.targets("B").get("C"));
        assertEquals(Integer.valueOf(3), graph.targets("B").get("D"));
        assertEquals(Integer.valueOf(1), graph.targets("C").get("D"));

        assertEquals(Integer.valueOf(1), graph.sources("B").get("A"));

        // Vertex C has TWO sources: A (weight 2) and B (weight 1)
        assertEquals("Vertex C should have 2 sources", 2, graph.sources("C").size());
        assertEquals(Integer.valueOf(2), graph.sources("C").get("A"));
        assertEquals(Integer.valueOf(1), graph.sources("C").get("B"));

        assertEquals(Integer.valueOf(1), graph.sources("D").get("C"));
        assertEquals(Integer.valueOf(3), graph.sources("D").get("B"));
    }
}