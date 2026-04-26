import * as dagre from 'dagre';
import { Individual } from '../types/individual';
import { FamilyUnit } from '../types/family-tree';
import { PersonNode } from '../types/node';
import { NODE_WIDTH, NODE_HEIGHT } from './person-node';

export interface LayoutConfig {
  rankdir?: 'TB' | 'BT' | 'LR' | 'RL'; // Top to Bottom, Bottom to Top, Left to Right, Right to Left
  nodesep?: number; // Horizontal spacing between nodes
  ranksep?: number; // Vertical spacing between generations
  marginx?: number;
  marginy?: number;
}

const DEFAULT_CONFIG: LayoutConfig = {
  rankdir: 'TB',
  nodesep: 150, // Increased from 100: horizontal spacing between nodes in same generation
  ranksep: 180, // Increased from 150: vertical spacing between generations to accommodate taller nodes
  marginx: 50,
  marginy: 50,
};

/**
 * Calculate positions for individuals using dagre graph layout algorithm.
 *
 * @param individuals List of individuals to position
 * @param families Family units containing parent-child relationships
 * @param config Optional layout configuration
 * @returns Array of PersonNodes with calculated x,y positions
 */
export function calculateLayout(
  individuals: Individual[],
  families: FamilyUnit[],
  config: LayoutConfig = {}
): PersonNode[] {
  const mergedConfig = { ...DEFAULT_CONFIG, ...config };

  // Create a new directed graph
  const g = new dagre.graphlib.Graph();

  // Configure the graph layout
  g.setGraph({
    rankdir: mergedConfig.rankdir,
    nodesep: mergedConfig.nodesep,
    ranksep: mergedConfig.ranksep,
    marginx: mergedConfig.marginx,
    marginy: mergedConfig.marginy,
  });

  // Default to assigning a new object as a label for each new edge.
  g.setDefaultEdgeLabel(() => ({}));

  // Add nodes to the graph
  individuals.forEach((individual) => {
    g.setNode(individual.id.toString(), {
      width: NODE_WIDTH,
      height: NODE_HEIGHT,
      label: `${individual.givenName || ''} ${individual.surname || ''}`.trim(),
    });
  });

  // Convert family units to edges for layout purposes
  // For dagre, we still need parent -> child edges to determine generational layers
  const edges: Array<{ from: number; to: number }> = [];
  families.forEach((family) => {
    // Add parent→child edges for families with children
    family.parentIds.forEach((parentId) => {
      family.childIds.forEach((childId) => {
        g.setEdge(parentId.toString(), childId.toString());
        edges.push({ from: parentId, to: childId });
      });
    });

    // For childless marriages, create a horizontal constraint to keep spouses together
    // This ensures spouses without children still appear next to each other
    if (family.childIds.length === 0 && family.parentIds.length === 2) {
      const [parent1, parent2] = family.parentIds;
      // Set a very short edge weight to keep them close horizontally
      g.setEdge(parent1.toString(), parent2.toString(), { minlen: 0, weight: 100 });
      console.log(
        `Adding spouse constraint for childless family ${family.familyId}: ${parent1} ↔ ${parent2}`
      );
    }
  });

  console.log('Dagre layout edges (parent→child):', edges);

  // Calculate the layout
  dagre.layout(g);

  // Extract positions and create PersonNodes
  const personNodes: PersonNode[] = individuals.map((individual) => {
    const nodeId = individual.id.toString();
    const node = g.node(nodeId);

    if (!node) {
      console.warn(`No layout position found for individual ${nodeId}, using default position`);
      return {
        id: nodeId,
        individual,
        x: 0,
        y: 0,
      };
    }

    return {
      id: nodeId,
      individual,
      x: node.x,
      y: node.y,
    };
  });

  // Post-process: Add extra spacing between different family groups
  // This helps visually distinguish siblings from cousins
  addFamilyGroupSpacing(personNodes, families);

  return personNodes;
}

/**
 * Add extra horizontal spacing between different family groups at the same generation.
 * This helps visually distinguish siblings from cousins.
 *
 * Algorithm:
 * 1. Group children by their parent family
 * 2. For each generation (y-level), sort individuals by x-position
 * 3. When we encounter a switch from one family to another, add extra spacing
 * 4. Re-center parents over their children after spacing is applied
 */
function addFamilyGroupSpacing(personNodes: PersonNode[], families: FamilyUnit[]): void {
  const FAMILY_GAP = 200; // Extra horizontal space between different family groups (increased from 150)

  // Build a map of child -> parent family
  const childToFamilyMap = new Map<number, number>();
  families.forEach((family) => {
    family.childIds.forEach((childId) => {
      childToFamilyMap.set(childId, family.familyId);
    });
  });

  // Create a lookup map for quick node access
  const nodeMap = new Map<number, PersonNode>();
  personNodes.forEach((node) => {
    nodeMap.set(node.individual.id, node);
  });

  // Group nodes by generation (y-coordinate)
  const generations = new Map<number, PersonNode[]>();
  personNodes.forEach((node) => {
    const roundedY = Math.round(node.y); // Group by rounded Y to handle floating point
    if (!generations.has(roundedY)) {
      generations.set(roundedY, []);
    }
    generations.get(roundedY)!.push(node);
  });

  // Process each generation
  generations.forEach((nodesInGeneration) => {
    // Sort by x-position
    nodesInGeneration.sort((a, b) => a.x - b.x);

    // Track cumulative offset as we add spacing
    let cumulativeOffset = 0;
    let previousFamilyId: number | undefined = undefined;

    nodesInGeneration.forEach((node) => {
      const currentFamilyId = childToFamilyMap.get(node.individual.id);

      // If this person is a child and belongs to a different family than the previous person
      if (
        currentFamilyId !== undefined &&
        previousFamilyId !== undefined &&
        currentFamilyId !== previousFamilyId
      ) {
        // Add extra spacing between family groups
        cumulativeOffset += FAMILY_GAP;
      }

      // Apply the cumulative offset to this node
      node.x += cumulativeOffset;

      // Update previous family for next iteration
      if (currentFamilyId !== undefined) {
        previousFamilyId = currentFamilyId;
      }
    });
  });

  // After spacing children, re-center parents over their children
  families.forEach((family) => {
    if (family.childIds.length === 0) return; // Skip childless families

    // Get all children nodes for this family
    const childNodes = family.childIds
      .map((childId) => nodeMap.get(childId))
      .filter((node) => node !== undefined);

    if (childNodes.length === 0) return;

    // Calculate the center x-position of all children
    const childXPositions = childNodes.map((node) => node.x);
    const minChildX = Math.min(...childXPositions);
    const maxChildX = Math.max(...childXPositions);
    const childrenCenterX = (minChildX + maxChildX) / 2;

    // Get parent nodes
    const parentNodes = family.parentIds
      .map((parentId) => nodeMap.get(parentId))
      .filter((node) => node !== undefined);

    if (parentNodes.length === 0) return;

    // Calculate current center of parents
    const parentXPositions = parentNodes.map((node) => node.x);
    const currentParentCenterX =
      parentXPositions.reduce((sum, x) => sum + x, 0) / parentNodes.length;

    // Calculate offset needed to center parents over children
    const offset = childrenCenterX - currentParentCenterX;

    // Apply offset to all parents
    parentNodes.forEach((parentNode) => {
      parentNode.x += offset;
    });
  });
}
