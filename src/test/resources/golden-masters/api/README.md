# API Golden Masters

These files contain reference API responses captured from the Spring Boot app.

## Captured from Spring Boot (empty database)

- `family-tree-initial.json` — GET /api/family-tree/initial
- `family-tree-expand-root.json` — GET /api/family-tree/expand?personId=1&...
- `family-tree-expand-with-siblings.json` — GET /api/family-tree/expand?personId=1&includeSiblings=true&...
- `individuals-root.json` — GET /api/individuals/root

## Expected response structure

### FamilyTreeResponse (Spring)
```json
{
  "individuals": [
    {
      "id": 1,
      "givenName": "John",
      "surname": "Doe",
      "sex": "M",
      "birthDate": "11 FEB 1991",
      "birthPlace": "City, State",
      "deathDate": null,
      "deathPlace": null,
      "relationships": {
        "childFamilyIds": [1],
        "spouseFamilyIds": [2],
        "hasUnloadedAncestors": false,
        "hasUnloadedDescendants": true,
        "hasUnloadedSiblings": false
      }
    }
  ],
  "families": [
    {
      "familyId": 1,
      "parentIds": [1, 2],
      "childIds": [3, 4]
    }
  ]
}
```

### FamilyTreeResponse (Ktor)
```json
{
  "individuals": [
    {
      "id": 1,
      "gedcomId": "@I1@",
      "givenName": "John",
      "surname": "Doe",
      "sex": "M",
      "isTreeRoot": true,
      "metadata": {
        "childFamilyIds": [1],
        "spouseFamilyIds": [2],
        "hasUnloadedAncestors": false,
        "hasUnloadedDescendants": true,
        "hasUnloadedSiblings": false
      }
    }
  ],
  "families": [
    {
      "familyId": 1,
      "parentIds": [1, 2],
      "childIds": [3, 4]
    }
  ]
}
```

### Key differences between Spring and Ktor responses

1. **Individual fields**: Spring uses `IndividualResponse` with `birthDate`, `birthPlace`, `deathDate`, `deathPlace`, `relationships`. Ktor uses `IndividualTreeNode` with `gedcomId`, `isTreeRoot`, `metadata`.
2. **Metadata vs relationships**: Same data, different field names (`relationships` vs `metadata`).
3. The frontend D3 tree app needs to handle both formats or the Ktor format needs to match Spring's exactly.
